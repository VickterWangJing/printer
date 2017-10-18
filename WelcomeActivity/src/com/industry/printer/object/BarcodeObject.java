package com.industry.printer.object;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.industry.printer.FileFormat.QRReader;
import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.data.BinCreater;
import com.industry.printer.data.BinFileMaker;
import com.industry.printer.data.BinFromBitmap;
import com.industry.printer.object.BaseObject;

import com.industry.printer.BinInfo;
import com.industry.printer.R;																																																																								
					
import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BarcodeObject extends BaseObject {

	public String mFormat;
	public int mCode;
	public boolean mShow;
	/*内容来源 是否U盤*/
	public boolean mSource;
	
	public Bitmap mBinmap;
	
	private Map<String, Integer> code_format;
	private Map<Integer, String> format_code;
	
	public BarcodeObject(Context context, float x) {
		super(context, BaseObject.OBJECT_TYPE_BARCODE, x);
		// TODO Auto-generated constructor stub
		mShow = true;
		mCode = 3;
		mFormat="CODE_128";
		setContent("123456789");
		mWidth=0;
		
	}

	public void setCode(String code)
	{
		mId = BaseObject.OBJECT_TYPE_BARCODE;
		if ("EAN8".equals(code)) {
			mCode = 0;
		} else if ("EAN13".equals(code)) {
			mCode = 1;
		} else if ("EAN128".equals(code)) {
			mCode = 2;
		} else if ("CODE_128".equals(code)) {
			mCode = 3;
		} else if ("CODE_39".equals(code)) {
			mCode = 5;
		} else if ("ITF_14".equals(code)) {
			mCode = 6;
		} else if ("QR".equals(code)) {
			mCode = 0;
			mId = BaseObject.OBJECT_TYPE_QR;
		} else {
			return;
		}
		mFormat = code;
		isNeedRedraw = true;
	}
	
	public void setCode(int code)
	{
		if (code == 0) {
			mCode = 0;
			mFormat = "EAN8";
		} else if (code == 1) {
			mCode = 1;
			mFormat = "EAN13";
		} else if (code == 2) {
			mCode = 2;
			mFormat = "EAN128";
		} else if (code == 3) {
			mCode = 3;
			mFormat = "CODE_128";
		} else if (code == 5) {
			mCode = 5;
			mFormat = "CODE_39";
		} else if (code == 6) {
			mCode = 6;
			mFormat = "ITF_14";
		}
		mId = BaseObject.OBJECT_TYPE_BARCODE;
		isNeedRedraw = true;
	}
	
	public String getCode()
	{
		return mFormat;
	}
	
	public boolean isQRCode() {
		return "QR".equals(mFormat);
	}
	
	public void setShow(boolean show)
	{
		mShow = show;
	}
	public boolean getShow()
	{
		return mShow;
	}
	
	@Override
	public void setContent(String content)
	{
		mContent=content;
		isNeedRedraw = true;
	}
	
	private static final String CODE = "utf-8"; 
	
	public Bitmap getScaledBitmap(Context context) {
		if (!isNeedRedraw) {
			return mBitmap; 			
		}
		
		isNeedRedraw = false;
		BitMatrix matrix=null;
		int margin = 0;
		try {
			MultiFormatWriter writer = new MultiFormatWriter();
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();  
            hints.put(EncodeHintType.CHARACTER_SET, CODE);
            // hints.put(EncodeHintType.MARGIN, margin);
            BarcodeFormat format = getBarcodeFormat(mFormat);
            
			if(!is2D())
			{
				if (mWidth <= 0) {
					mWidth = mContent.length() * 70;
				}

	            /* 条形码的宽度设置:每个数字占70pix列  */
				if ("EAN13".equals(mFormat)) {
					matrix = writer.encode(checkSum(),
					        format, (int)mWidth, (int)(mHeight - 30), null);
	            
				} else if ("EAN8".equals(mFormat)) {
					matrix = writer.encode(checkLen(),
					        format, (int)mWidth, (int)(mHeight - 30), null);
	            
				} else {
					matrix = writer.encode(mContent,
					        format, (int)mWidth, (int)(mHeight - 30), null);
				}
	            
			} else {
				if (mWidth <= 0) {
					mWidth = mHeight;
				}
				Debug.d(TAG, "--->content = " + mContent);
				matrix = writer.encode(mContent,
						format, (int)mWidth, (int)mHeight, hints);
				matrix = deleteWhite(matrix);
			}
			int tl[] = matrix.getTopLeftOnBit();
			int width = matrix.getWidth();
			int height = matrix.getHeight();
			Debug.d(TAG, "mWidth="+mWidth+", width="+width + "   height=" + height);
			setWidth(width);
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					if (matrix.get(x, y)) 
					{
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			/* 条码/二维码的四个边缘空出20像素作为白边 */
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			
			mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			if (is2D()) {
				mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mHeight, (int)mHeight, false);
				setWidth(mHeight);
			}
			// mBinmap = Bitmap.createBitmap(mBitmap);
			/*if content need to show, draw it*/
			if(mShow && !is2D())
			{
				// 用於生成bin的bitmap
				Bitmap bmp = Bitmap.createBitmap(width, height+15, Config.ARGB_8888);
				Bitmap code = createCodeBitmapFromDraw(mContent, width-tl[0]*2, 15);
				Debug.d(TAG, "===>code width=" + code.getWidth());
				//BinCreater.saveBitmap(code, "barcode.png");
				Canvas can = new Canvas(bmp);
				can.drawBitmap(mBitmap, 0, 0, mPaint);
				can.drawBitmap(code, tl[0], height, mPaint);
				BinFromBitmap.recyleBitmap(mBitmap);
				BinFromBitmap.recyleBitmap(code);
				mBitmap = bmp;
			}
			
			return mBitmap;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Bitmap getPrintBitmap(int totalW, int totalH, int w, int h, int y) {
		BitMatrix matrix=null;
		MultiFormatWriter writer = new MultiFormatWriter();
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();  
        hints.put(EncodeHintType.CHARACTER_SET, CODE);
        try {
			matrix = writer.encode(mContent,
					BarcodeFormat.QR_CODE, w, w, hints);
			matrix = deleteWhite(matrix);
        } catch (Exception e) {
        	return null;
        }
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		for (int y1 = 0; y1 < height; y1++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (matrix.get(x, y1)) 
				{
					pixels[y1 * width + x] = 0xff000000;
				} else {
					pixels[y1 * width + x] = 0xffffffff;
				}
			}
		}
		Bitmap bg = Bitmap.createBitmap(totalW, totalH, Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, y, mPaint);
		return bg;
	}
	
	public int getDotcount() {
		Bitmap bmp = getScaledBitmap(mContext);
		BinFileMaker maker = new BinFileMaker(mContext);
		int dots = maker.extract(bmp);
		return dots;
	}

	protected Bitmap createCodeBitmapFromTextView(String contents,int width,int height, boolean isBin) {
		float div = (float) (4.0/mTask.getHeads());
		Debug.d(TAG, "===>width=" + width);
		width = (int) (width/div);
		TextView tv=new TextView(mContext);
	    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(15);
        tv.setHeight(height);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);  
        tv.setTextColor(Color.BLACK);
        tv.measure(  
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),  
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));  
        tv.layout(0, 0, tv.getMeasuredWidth(),  
        		tv.getMeasuredHeight());
  
        tv.buildDrawingCache();  
        Bitmap bitmapCode=tv.getDrawingCache();
        Debug.d(TAG, "===>width=" + width + ", bmp width=" + bitmapCode.getWidth());
        return isBin?Bitmap.createScaledBitmap(bitmapCode, (int) (bitmapCode.getWidth()*div), bitmapCode.getHeight(), true) : bitmapCode;
	}
	
	protected Bitmap createCodeBitmapFromDraw(String content, int width, int height) {
		Paint paint = new Paint(); 
		
		paint.setTextSize(10);
		paint.setTextScaleX(2);
		paint.setColor(Color.BLACK);
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		//每个字符占的宽度
		int perPix = width/content.length();
		//字符本身的宽度
		float numWid = paint.measureText("0");
		int left = (int) ((perPix - numWid)/2);
		for (int i = 0; i < content.length(); i++) {
			String n = content.substring(i, i+1);
			canvas.drawText(n, i*perPix + left, 10, paint);
		}
		return bitmap;
	}
	
	public int getBestWidth()
	{
		int width=0;
		BitMatrix matrix=null;
		try{
			MultiFormatWriter writer = new MultiFormatWriter();
			BarcodeFormat format = getBarcodeFormat(mFormat);
			if(is2D())
			{
				Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();  
	            hints.put(EncodeHintType.CHARACTER_SET, CODE);
	            
				matrix = writer.encode(mContent,
					                format, (int)mWidth, (int)mHeight, null);
			} else {
				matrix = writer.encode(mContent,
		                format, (int)mWidth, (int)mHeight);
			}
			width = matrix.getWidth();
			int height = matrix.getHeight();
			Debug.d(TAG, "mWidth="+mWidth+", width="+width);
		}
		catch(Exception e)
		{
			Debug.d(TAG, "exception:"+e.getMessage());
		}
		return width;
	}
	
	private boolean is2D() {
		if (mFormat.equalsIgnoreCase("QR")
				|| mFormat.equalsIgnoreCase("DATA_MATRIX")
				|| mFormat.equalsIgnoreCase("AZTEC")
				|| mFormat.equalsIgnoreCase("PDF_417")) {
			return true;
		}
		return false;
	}
	
	private BarcodeFormat getBarcodeFormat(String format) {
		int i;
		if ("CODE_128".equals(format)) {
			return BarcodeFormat.CODE_128;
		} else if ("CODE_39".equals(format)) {
			return BarcodeFormat.CODE_39;
		} else if ("CODE_93".equals(format)) {
			return BarcodeFormat.CODE_93;
		} else if ("CODABAR".equals(format)) {
			return BarcodeFormat.CODABAR;
		} else if ("EAN8".equals(format)) {
			return BarcodeFormat.EAN_8;
		} else if ("EAN13".equals(format)) {
			return BarcodeFormat.EAN_13;
		} else if ("UPC_E".equals(format)) {
			return BarcodeFormat.UPC_E;
		} else if ("UPC_A".equals(format)) {
			return BarcodeFormat.UPC_A;
		} else if ("ITF".equals(format)) {
			return BarcodeFormat.ITF;
		} else if ("RSS14".equals(format)) {
			return BarcodeFormat.RSS_14;
		} else if ("RSS_EXPANDED".equals(format)) {
			return BarcodeFormat.RSS_EXPANDED;
		} else if ("QR".equals(format)) {
			return BarcodeFormat.QR_CODE;
		} else if ("DATA_MATRIX".equals(format)) {
			return BarcodeFormat.DATA_MATRIX;
		} else if ("AZTEC".equals(format)) {
			return BarcodeFormat.AZTEC;
		} else if ("PDF_417".equals(format)) {
			return BarcodeFormat.PDF_417;
		} else {
			return BarcodeFormat.CODE_128;
		}
		
	}
	
	
	private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }
	
	/**
	 * 計算EAN13的校驗和
	 * 奇数位和：6 + 0 + 2 + 4 + 6 + 8 = 26
	 * 偶数位和：9 + 1 + 3 + 5 + 7 + 9 = 34
	 * 将奇数位和与偶数位和的三倍相加：26 + 34 * 3 = 128
	 * 取结果的个位数：128的个位数为8
	 * 用10减去这个个位数：10 - 8 = 2
	 * @return
	 */
	private String checkSum() {
		String code = "";
		int odd = 0, even = 0;
		if (mContent.length() < 12) {
			String add = "";
			for (int i = 0; i < 12-mContent.length(); i++) {
				add += "0";
			}
			code = mContent + add;
		} else if (mContent.length() > 12) {
			code = mContent.substring(0, 12);
		} else {
			code = mContent; 
		}
		mContent = code;
		
		for (int i = 0; i < code.length(); i++) {
			if (i%2 == 0) {
				odd += Integer.parseInt(code.substring(i, i+1));
			} else {
				even += Integer.parseInt(code.substring(i, i+1));
			}
			
		}
		int temp = odd + even * 3;
		int sum = 10 - temp%10;
		if (sum >= 10) {
			sum = 0;
		}
		code += sum;
		return code;
	}
	
	/**
	 * EAN8只支持8位長度
	 * @return
	 */
	private String checkLen() {
		int len = mContent.length();
		if (len < 8) {
			for (int i = 0; i < 8 - len; i++) {
				mContent += "0";
			}
		} else if (mContent.length() > 8) {
			return mContent.substring(0, 8);
		}
		return mContent;
	}
	
	public String toString()
	{
		int dots = SystemConfigFile.getInstance(mContext).getParam(39);
		float prop = dots/Configs.gDots;
		String str="";
		//str += BaseObject.intToFormatString(mIndex, 3)+"^";
		str += mId+"^";
		str += BaseObject.floatToFormatString(getX()*2*prop, 5)+"^";
		str += BaseObject.floatToFormatString(getY()*2*prop, 5)+"^";
		str += BaseObject.floatToFormatString(getXEnd()*2*prop, 5)+"^";
		//str += BaseObject.floatToFormatString(getY() + (getYEnd()-getY())*2, 5)+"^";
		str += BaseObject.floatToFormatString(getYEnd()*2*prop, 5)+"^";
		str += BaseObject.intToFormatString(0, 1)+"^";
		str += BaseObject.boolToFormatString(mDragable, 3)+"^";
		str += BaseObject.floatToFormatString(mContent.length(), 3)+"^";
		str += mCode +"^";
		str += "000^";
		str += BaseObject.boolToFormatString(mShow, 3)+"^";
		str += mContent+"^";
		str += BaseObject.boolToFormatString(mSource, 8) + "^";
		str += "00000000^00000000^00000000^0000^0000^" + mFont + "^000^000";
		System.out.println("file string ["+str+"]");
		return str;
	}
}
