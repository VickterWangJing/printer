package com.industry.printer.object;

import java.util.Calendar;

import com.industry.printer.MessageTask.MessageType;
import com.industry.printer.Utils.ConfigPath;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.data.BinFileMaker;
import com.industry.printer.data.BinFromBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;

public class LetterHourObject extends BaseObject {

	private static final String TAG = LetterHourObject.class.getSimpleName();
	
	private static final String[] HOUR_LETTER = {"A","B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
												"Q", "R", "S", "T", "U", "V", "W", "X"};
	public LetterHourObject(Context ctx, float x) {
		super(ctx, BaseObject.OBJECT_TYPE_LETTERHOUR, x);
		super.setContent("H");
	}
	
	@Override
	public void setContent(String content) {
		
		
	}
	
	@Override
	public String getContent() {
		long time = System.currentTimeMillis();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 0 || hour >= HOUR_LETTER.length) {
			hour = 0;
		}
		return HOUR_LETTER[hour];
		
	}
	@Override	 
	public Bitmap getScaledBitmap(Context context) {
		Bitmap bitmap;
		mPaint.setTextSize(getfeed());
		mPaint.setAntiAlias(true); //  
		mPaint.setFilterBitmap(true); //
	
		boolean isCorrect = false;
		// Debug.d(TAG,"--->getBitmap font = " + mFont);
		for (String font : mFonts) {
			if (font.equals(mFont)) {
				isCorrect = true;
				break;
			}
		}
		if (!isCorrect) {
			mFont = DEFAULT_FONT;
		}
		try {
			mPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/"+mFont+".ttf"));
		} catch (Exception e) {}
		int width = (int)mPaint.measureText(getContent());
		Debug.d(TAG, "--->content: " + getContent() + "  width=" + width);
		if (mWidth == 0) {
			setWidth(width);
		}
		bitmap = Bitmap.createBitmap(width , (int)mHeight, Bitmap.Config.ARGB_8888);
		Debug.d(TAG,"--->getBitmap width="+mWidth+", mHeight="+mHeight);
		mCan = new Canvas(bitmap);
		FontMetrics fm = mPaint.getFontMetrics();
		mPaint.setColor(Color.BLUE);
		mCan.drawText(getContent() , 0, mHeight-fm.descent, mPaint);
	
		return Bitmap.createScaledBitmap(bitmap, (int)mWidth, (int)mHeight, false);	
	}
	
	@Override
	public int drawVarBitmap()
	{
		int dots = 0;
		//mPaint.setTextSize(mHeight);
		int singleW; //the width value of each char
		int height = (int)mPaint.getTextSize();
		int width = (int)mPaint.measureText("8");
		FontMetrics fm = mPaint.getFontMetrics();
		float wDiv = (float) (2.0/mTask.getHeads());
		MessageObject msg = mTask.getMsgObject();
		/*對320高的buffer進行單獨處理*/
		if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_FAST)) {
			wDiv = 1;
		} else if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST)) {
			wDiv = 0.5f;
		}
		/*draw Bitmap of single digit*/
		Bitmap bmp = Bitmap.createBitmap(width, (int)mHeight, Bitmap.Config.ARGB_8888);
		Canvas can = new Canvas(bmp);
		
		Debug.d(TAG, "--->id = " + mId + " Width=" + mWidth);
		/*draw 0-9 totally 10 digits Bitmap*/
		singleW = (int)mWidth/mContent.length();
		Debug.d(TAG, "--->singleW=" + singleW);
		singleW = (int) (singleW/wDiv);
		Debug.d(TAG, "--->singleW/div=" + singleW);
		Bitmap gBmp = Bitmap.createBitmap(singleW * HOUR_LETTER.length, Configs.gDots * mTask.getHeads(), Bitmap.Config.ARGB_8888);
		Canvas gCan = new Canvas(gBmp);
		gCan.drawColor(Color.WHITE);	/*white background*/
		for(int i = 0; i < HOUR_LETTER.length; i++)
		{
			/*draw background to white firstly*/
			can.drawColor(Color.WHITE);
			can.drawText(HOUR_LETTER[i], 0, mHeight-fm.descent, mPaint);
			// Bitmap b = Bitmap.createScaledBitmap(bmp, singleW, (int)mHeight, true);
			gCan.drawBitmap(Bitmap.createScaledBitmap(bmp, singleW, (int) (mHeight * mTask.getHeads()), false), i*singleW, (int)getY() * mTask.getHeads(), mPaint);
		}
		BinFromBitmap.recyleBitmap(bmp);
		/*對320高的buffer進行單獨處理*/
		if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_FAST)) {
			gBmp = Bitmap.createScaledBitmap(gBmp, gBmp.getWidth(), 308, true);
			Bitmap b = Bitmap.createBitmap(gBmp.getWidth(), 320, Bitmap.Config.ARGB_8888);
			can.setBitmap(b);
			can.drawColor(Color.WHITE);
			can.drawBitmap(gBmp, 0, 0, mPaint);
			gBmp.recycle();
			gBmp = b;
		} else if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST)) {
			gBmp = Bitmap.createScaledBitmap(gBmp, gBmp.getWidth(), 308*2, true);
			Bitmap b = Bitmap.createBitmap(gBmp.getWidth(), 320*2, Bitmap.Config.ARGB_8888);
			can.setBitmap(b);
			can.drawColor(Color.WHITE);
			int h = gBmp.getHeight()/2;
			/*先把gBmp的上半部分0~307高貼到620高的上半部分（0~319）*/
			can.drawBitmap(gBmp, new Rect(0, 0, gBmp.getWidth(), h), new Rect(0, 0, b.getWidth(), h), null);
			
			/*先把gBmp的下半部分308~615高貼到620高的下半部分（320~619）*/
			// can.drawBitmap(Bitmap.createBitmap(gBmp, 0, 308, gBmp.getWidth(), 308), 0, 320, mPaint);
			can.drawBitmap(gBmp, new Rect(0, h, gBmp.getWidth(), h*2), new Rect(0, 320, b.getWidth(), 320 + h), null);
			gBmp.recycle();
			gBmp = b;
		}
		else if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_16_3)) { //add by lk 170418
			gBmp = Bitmap.createScaledBitmap(gBmp, gBmp.getWidth(), 128, true);
			Bitmap b = Bitmap.createBitmap(gBmp.getWidth(), 128, Bitmap.Config.ARGB_8888);
			can.setBitmap(b);
			can.drawColor(Color.WHITE);
			can.drawBitmap(gBmp, 0, 0, mPaint);
			gBmp.recycle();
			gBmp = b;
		}		//addbylk 170418 end
		
		BinFileMaker maker = new BinFileMaker(mContext);
		dots = maker.extract(gBmp);
		Debug.d(TAG, "--->id: " + mId + " index:  " + mIndex);
		maker.save(ConfigPath.getVBinAbsolute(mTask.getName(), mIndex));
		//
		BinFromBitmap.recyleBitmap(gBmp);
		/*根據變量內容的實際長度計算點數*/
		dots = (dots* getContent().length()/10) + 1;
		return dots;
	}
	
	public String toString()
	{
		float prop = getProportion();
		String str="";
		//str += BaseObject.intToFormatString(mIndex, 3)+"^";
		str += mId+"^";
		str += BaseObject.floatToFormatString(getX()*2 * prop, 5)+"^";
		str += BaseObject.floatToFormatString(getY()*2 * prop, 5)+"^";
		str += BaseObject.floatToFormatString(getXEnd()*2 * prop, 5)+"^";
		//str += BaseObject.floatToFormatString(getY() + (getYEnd()-getY())*2, 5)+"^";
		str += BaseObject.floatToFormatString(getYEnd()*2 * prop, 5)+"^";
		str += BaseObject.intToFormatString(0, 1)+"^";
		str += BaseObject.boolToFormatString(mDragable, 3)+"^";
		str += "000"+"^";
		str += "000^000^000^000^00000000^00000000^00000000^00000000^0000^0000^" + mFont + "^000^"+mContent;
		System.out.println("file string ["+str+"]");
		return str;
	}
	
}
