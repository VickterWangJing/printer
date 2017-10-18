package com.industry.printer;

import java.io.File;
import java.util.ArrayList;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.FileFormat.TlkFileWriter;
import com.industry.printer.Utils.ConfigPath;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.Utils.FileUtil;
import com.industry.printer.Utils.PlatformInfo;
import com.industry.printer.data.BinFileMaker;
import com.industry.printer.data.BinFromBitmap;
import com.industry.printer.object.BarcodeObject;
import com.industry.printer.object.BaseObject;
import com.industry.printer.object.CounterObject;
import com.industry.printer.object.GraphicObject;
import com.industry.printer.object.JulianDayObject;
import com.industry.printer.object.LetterHourObject;
import com.industry.printer.object.MessageObject;
import com.industry.printer.object.ObjectsFromString;
import com.industry.printer.object.RealtimeObject;
import com.industry.printer.object.ShiftObject;
import com.industry.printer.object.TLKFileParser;
import com.industry.printer.object.TextObject;
import com.industry.printer.object.data.BitmapWriter;

/**
 * MessageTask包含多个object
 * @author zhaotongkai
 *
 */
public class MessageTask {

	private static final String TAG = MessageTask.class.getSimpleName();
	public static final String MSG_PREV_IMAGE = "/1.bmp";
	private Context mContext;
	private int mDots=0; 
	private String mName;
	private ArrayList<BaseObject> mObjects;

	public int mType;
	private int mIndex;
	
	public MessageTask(Context context) {
		mName="";
		mContext = context;
		mObjects = new ArrayList<BaseObject>();
	}
	
	/**
	 * 通过tlk文件解析构造出Task
	 * @param tlk  tlk path, absolute path
	 * @param name
	 */
	public MessageTask(Context context, String tlk, String name) {
		this(context);
		String tlkPath="";
		File file = new File(tlk);
		setName(name);
		TLKFileParser parser = new TLKFileParser(context, mName);
		parser.setTlk(tlk);
		parser.parse(context, this, mObjects);                                      
		mDots = parser.getDots();
	}
	
	/**
	 * 通过tlk名稱解析构造出Task
	 * @param context
	 * @param tlk
	 */
	public MessageTask(Context context, String tlk) {
		this(context);
		String tlkPath="";
		if (tlk.startsWith(ConfigPath.getTlkPath())) {
			File file = new File(tlk);
			setName(file.getName());
		} else {
			setName(tlk);
		}
		
		TLKFileParser parser = new TLKFileParser(context, mName);
		parser.parse(context, this, mObjects);                                      
		mDots = parser.getDots();
	}
	
	/**
	 * 通过字符串解析构造出Task，
	 * @param tlk tlk信息名称
	 * @param content 树莓3上编辑的字符串内容
	 */
//	public MessageTask(Context context, String tlk, String content) {
//		this(context);
//		String tlkPath="";
//		setName(tlk);
//		
//		mObjects = ObjectsFromString.makeObjs(mContext, content);
//	}
	/**
	 * set Task name
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}
	
	/**
	 * get Task name
	 * @return
	 */
	public String getName() {
		return mName;
	}
	
	public ArrayList<BaseObject> getObjects() {
		Debug.d(TAG, "--->size:" + mObjects.size());
		return mObjects;
	}
	/**
	 * 添加object到Task
	 * @param object
	 */
	public void addObject(BaseObject object) {
		if (mObjects == null) {
			mObjects = new ArrayList<BaseObject>();
		}
		object.setTask(this);
		mObjects.add(object);
		Debug.d(TAG, "--->size:" + mObjects.size());
	}
	
	/**
	 * 从Task中删除指定object
	 * @param object
	 */
	public void removeObject(BaseObject object) {
		if (mObjects == null) {
			return;
		}
		mObjects.remove(object);
	}
	
	/**
	 * 删除Task中所有objects
	 */
	public void removeAll() {
		if (mObjects == null) {
			return;
		}
		mObjects.clear();
	}
	
	/**
	 * 获取当前打印信息的墨点数
	 * 此API只对树莓小板系统有效
	 * @return
	 */
	public int getDots() {
		return mDots;
	}
	
	/**
	 * 获取打印对象的内容缩略信息
	 * 暂时只支持文本对象，后续会添加对变量的支持
	 */
	public String getAbstract() {
		
		String objString="";
		String content="";
		if (mObjects == null) {
			return null;
		}
		for (BaseObject item : mObjects) {
			if (item instanceof TextObject) {
        		objString = item.getContent();
        	} else if (item instanceof RealtimeObject) {
				objString = item.getContent();
			} else if (item instanceof CounterObject) {
				objString = item.getContent();
			} else {
				continue;
			}
        	content += objString;
		}
		return content;
	}
	
	public void saveTlk(Context context) {
		for(BaseObject o:mObjects)
		{
			if((o instanceof MessageObject)	) {
				((MessageObject) o).setDotCount(mDots);
				break;
			}
		}
		TlkFileWriter tlkFile = new TlkFileWriter(context, this);
		tlkFile.write();
	}
	
	public boolean createTaskFolderIfNeed() {
		File dir = new File(ConfigPath.getTlkDir(mName));
		if(!dir.exists() && !dir.mkdirs())
		{
			Debug.d(TAG, "create dir error "+dir.getPath());
			return false;
		}
		return true;
	}
	
	public void saveVarBin() {
		if (mObjects == null || mObjects.size() <= 0) {
			return;
		}
		for (BaseObject object : mObjects) {
			if((object instanceof CounterObject) || (object instanceof RealtimeObject) ||
					(object instanceof JulianDayObject) || (object instanceof ShiftObject))
			{
				if(PlatformInfo.isBufferFromDotMatrix()==1) {
					object.generateVarbinFromMatrix(ConfigPath.getTlkDir(mName));
				} else {
					mDots += object.drawVarBitmap();
				}
			} else if ((object instanceof LetterHourObject)) {
				mDots += ((LetterHourObject) object).drawVarBitmap();
			} else if (object instanceof BarcodeObject && object.getSource() == true) {
				int dots = ((BarcodeObject) object).getDotcount();
				MessageObject msg = getMsgObject();
				if (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_FAST 
						|| msg.getType() == MessageType.MESSAGE_TYPE_1_INCH ) {
					mDots += dots * 2;
				} else if (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL 
						|| msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST ) {
					mDots += dots * 2 * 4;
				} else {
					mDots += dots/2;
				}
				
			}
		}
	}
	
	public void saveBin() {
		if (PlatformInfo.isBufferFromDotMatrix()==1) {
			saveBinDotMatrix();
		} else {
			saveObjectBin();
		}
		
	}
	
	/**
	 * 使用系统字库，生成bitmap，然后通过灰度化和二值化之后提取点阵生成buffer
	 * @param f
	 */
	private void saveObjectBin()
	{
		int width=0;
		Paint p=new Paint();
		if(mObjects==null || mObjects.size() <= 0)
			return ;
		for(BaseObject o:mObjects)
		{
			if (o instanceof MessageObject) {
				continue;
			}
			width = (int)(width > o.getXEnd() ? width : o.getXEnd());
		}
		float div = (float) (2.0/getHeads());
		
		Bitmap bmp = Bitmap.createBitmap(width , Configs.gDots, Bitmap.Config.ARGB_8888);
		Debug.d(TAG, "drawAllBmp width="+width+", height="+Configs.gDots);
		Canvas can = new Canvas(bmp);
		can.drawColor(Color.WHITE);
		for(BaseObject o:mObjects)
		{
			if((o instanceof MessageObject)	)
				continue;
			
			if(o instanceof CounterObject)
			{
				// o.drawVarBitmap();
			}
			else if(o instanceof RealtimeObject)
			{
				Bitmap t = ((RealtimeObject)o).getBgBitmap(mContext);
				can.drawBitmap(t, o.getX(), o.getY(), p);
				BinFromBitmap.recyleBitmap(t);
			} else if(o instanceof JulianDayObject) {
				// o.drawVarBitmap();
			} else if (o instanceof BarcodeObject && o.getSource()) {
				
			} else if(o instanceof ShiftObject)	{
				// o.drawVarBitmap();
			} else if (o instanceof LetterHourObject) {
				
			} else if (o instanceof BarcodeObject) {
				Bitmap t = ((BarcodeObject) o).getScaledBitmap(mContext);
				can.drawBitmap(t, o.getX(), o.getY(), p);
			} else if (o instanceof GraphicObject) {
				Bitmap t = ((GraphicObject) o).getScaledBitmap(mContext);
				if (t != null) {
					can.drawBitmap(t, o.getX(), o.getY(), p);
				}
			} else {
				Bitmap t = o.getScaledBitmap(mContext);
				can.drawBitmap(t, o.getX(), o.getY(), p);
				// BinFromBitmap.recyleBitmap(t);
			}
		//can.drawText(mContent, 0, height-30, mPaint);
		}
		/**
		 * 爲了兼容128點，152點和384點高的三種列高信息，需要計算等比例縮放比例
		 */
		float dots = 152;///SystemConfigFile.getInstance(mContext).getParam(39);
		/*對於320列高的 1 Inch打印頭，不使用參數40的設置*/
		MessageObject msg = getMsgObject();
		if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH
				|| msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_FAST
				|| msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL
				|| msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST)) {
			dots = 304;
		}
		Debug.d(TAG, "+++dots=" + dots);
		float prop = dots/Configs.gDots;
		Debug.d(TAG, "+++prop=" + prop);
		/** 生成bin的bitmap要进行处理，高度根据message的类型调整
		 * 注： 为了跟PC端保持一致，生成的bin文件宽度为1.tlk中坐标的四分之一，在提取点阵之前先对原始Bitmap进行X坐标缩放（为原图的1/4）
		 * 	  然后进行灰度和二值化处理；
		 */
		Debug.d(TAG, "--->div=" + div + "  h=" + bmp.getHeight() + "  prop = " + prop);
		Bitmap bitmap = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth()/div * prop), (int) (bmp.getHeight() * getHeads() * prop), true);
		/*對於320列高的 1 Inch打印頭，不使用參數40的設置*/
		if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_FAST)) {
			Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), 320, Bitmap.Config.ARGB_8888);
			can.setBitmap(b);
			can.drawColor(Color.WHITE);
			can.drawBitmap(bitmap, 0, 0, p);
			bitmap.recycle();
			bitmap = b;
		} else if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL || msg.getType() == MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST)) {
			Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), 640, Bitmap.Config.ARGB_8888);
			// can.setBitmap(b);
			Canvas c = new Canvas(b);
			c.drawColor(Color.WHITE);
			int h = bitmap.getHeight()/2;
			int dstH = b.getHeight()/2;
			c.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), h), new Rect(0, 0, b.getWidth(), 308), p);
			c.drawBitmap(bitmap, new Rect(0, h, bitmap.getWidth(), h*2), new Rect(0, 320, b.getWidth(), 320 + 308), p);
			// c.drawBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight()/2), new Matrix(), null);
			// c.drawBitmap(Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2, bitmap.getWidth(), bitmap.getHeight()/2), 0, 320, null);
			bitmap.recycle();
			bitmap = b;
		}else if (msg != null && (msg.getType() == MessageType.MESSAGE_TYPE_16_3  ) )
		{ //add by lk 170418
			bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), 128, true);
			Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), 128, Bitmap.Config.ARGB_8888);
			can.setBitmap(b);
			can.drawColor(Color.WHITE);
			can.drawBitmap(bitmap, 0, 0, p);
			bitmap.recycle();
			bitmap = b;
  		//add by lk 170418 end						
		}
		// 生成bin文件
		BinFileMaker maker = new BinFileMaker(mContext);
		mDots = maker.extract(bitmap);
		// 保存bin文件
		maker.save(ConfigPath.getBinAbsolute(mName));
		
		return ;
	}
	
	/**
	 * 从16*16的点阵字库中提取点阵，生成打印buffer
	 * @param f
	 */
	private void saveBinDotMatrix() {
		if(mObjects==null || mObjects.size() <= 0)
			return ;
		
		String content="";
		for(BaseObject o:mObjects)
		{
			if((o instanceof MessageObject)	)
				continue;
			
			if(o instanceof CounterObject)
			{
				content += o.getContent();
			}
			else if(o instanceof RealtimeObject)
			{
				content += o.getContent();
				Debug.d(TAG, "--->realtime: " + content);
			}
			else if(o instanceof JulianDayObject)
			{
				content += o.getContent();
			}
			else if(o instanceof ShiftObject)
			{
				content += o.getContent();
			}
			else
			{
				content += o.getContent();
			}
		//can.drawText(mContent, 0, height-30, mPaint);
		}
		// 生成bin文件
		BinFileMaker maker = new BinFileMaker(mContext);
		mDots = maker.extract(content);
		// 保存bin文件
		maker.save(ConfigPath.getTlkDir(mName) + "/1.bin");
		for(BaseObject o:mObjects)
		{
			if((o instanceof MessageObject)	) {
				((MessageObject) o).setDotCount(mDots);
				break;
			}
		}
		
		return ;
	}

//	public void savePreview() {
//		int width=0;
//		Paint p=new Paint();
//		if(mObjects==null || mObjects.size() <= 0)
//			return ;
//		for(BaseObject o:mObjects)
//		{
//			width = (int)(width > o.getXEnd() ? width : o.getXEnd());
//		}
//
//		Bitmap bmp = Bitmap.createBitmap(width , Configs.gDots, Bitmap.Config.ARGB_8888);
//		Debug.d(TAG, "drawAllBmp width="+width+", height="+Configs.gDots);
//		Canvas can = new Canvas(bmp);
//		can.drawColor(Color.WHITE);
//						
//		String content="";
//
//		for(BaseObject o:mObjects)
//		{
//			if (o instanceof MessageObject) {
//				continue;
//			}
//			
//			if(o instanceof CounterObject)
//			{						
//		//	o.setContent2(str_new_content)		;
//				Bitmap  t  = o.getpreviewbmp();
//
//				if (t== null) {
//					continue;
//					}
//				
//					can.drawBitmap(t, o.getX(), o.getY(), p);			
//			}
//
//			else if(o instanceof RealtimeObject)
//			{	Debug.e(TAG, "RealtimeObject");		
//				Bitmap  t  = o.getpreviewbmp();
//
//				if (t== null) {
//					continue;
//					}
//				can.drawBitmap(t, o.getX(), o.getY(), p);				
//			}			
//			else if(o instanceof JulianDayObject)
//			{
//				Bitmap  t  = o.getpreviewbmp();
//
//				if (t== null) {
//					continue;
//					}
//				can.drawBitmap(t, o.getX(), o.getY(), p);				
//			}else if(o instanceof TextObject)
//			{
//				Bitmap  t  = o.getpreviewbmp();
//
//				if (t== null) {
//					continue;
//					}
//				can.drawBitmap(t, o.getX(), o.getY(), p);				
//			}	
//					
//			/*	
//				TextObject
//			{
//				Bitmap t  = o.getScaledBitmap(mContext);
//					if (t== null) {
//					continue;
//				}	
//				can.drawBitmap(t, o.getX(), o.getY(), p);
//			
//			}*/
//			
//			/*	
//			else if(o instanceof ShiftObject)
//			{
//				content += o.getContent();
//			}
//			*/
//
//		//can.drawText(mContent, 0, height-30, mPaint);	
//			
//		//	if(o instanceof CounterObject)
//		//	{
//		//		o.setContent("lkaa");//mContext="liukun";
//		//	}	
//			
//		///	String o.setContent2("lkaa");//mContext="liukun";
//			
//		}
//		// Bitmap.createScaledBitmap();
//		float scale = bmp.getHeight() / 100;
//		width = (int) (width / scale);
//		
//		width=width/2; //addbylk 减半输出 
//		
//		Bitmap nBmp = Bitmap.createScaledBitmap(bmp, width, 100, false);
//		BitmapWriter.saveBitmap(nBmp, ConfigPath.getTlkDir(getName()), "1.bmp");
//	}

	public void savePreview() {
		int width=0;
		Paint p=new Paint();
		if(mObjects==null || mObjects.size() <= 0)
		return ;
		for(BaseObject o:mObjects)
		{
		width = (int)(width > o.getXEnd() ? width : o.getXEnd());
		}

		Bitmap bmp = Bitmap.createBitmap(width , Configs.gDots, Bitmap.Config.ARGB_8888);
		Debug.d(TAG, "drawAllBmp width="+width+", height="+Configs.gDots);
		Canvas can = new Canvas(bmp);
		can.drawColor(Color.WHITE);

		String content="";

		for(BaseObject o:mObjects)
		{
		if (o instanceof MessageObject) {
		continue;
		}

		if(o instanceof CounterObject)
		{
//			o.setContent2(str_new_content)	 ;
		Bitmap  t  = o.getpreviewbmp();

		if (t== null) {
		continue;
		}

		can.drawBitmap(t, o.getX(), o.getY(), p);
		}

		else if(o instanceof RealtimeObject)
		{	Debug.e(TAG, "RealtimeObject");
		Bitmap  t  = o.getpreviewbmp();

		if (t== null) {
		continue;
		}
		can.drawBitmap(t, o.getX(), o.getY(), p);
		}
		else if(o instanceof JulianDayObject)
		{
		Bitmap  t  = o.getpreviewbmp();

		if (t== null) {
		continue;
		}
		can.drawBitmap(t, o.getX(), o.getY(), p);
		}else if(o instanceof TextObject)
		{
		Bitmap  t  = o.getpreviewbmp();

		if (t== null) {
		continue;
		}
		can.drawBitmap(t, o.getX(), o.getY(), p);
		}
		else //addbylk
		{
		Bitmap t = o.getScaledBitmap(mContext);
		if (t== null) {
		continue;
		}
		can.drawBitmap(t, o.getX(), o.getY(), p);


		}

		/*
		TextObject
		{
		Bitmap t  = o.getScaledBitmap(mContext);
		if (t== null) {
		continue;
		}
		can.drawBitmap(t, o.getX(), o.getY(), p);

		}*/

		/*
		else if(o instanceof ShiftObject)
		{
		content += o.getContent();
		}
		*/

		//can.drawText(mContent, 0, height-30, mPaint);

//			if(o instanceof CounterObject)
//			{
//			 o.setContent("lkaa");//mContext="liukun";
//			}

		///	String o.setContent2("lkaa");//mContext="liukun";

		}
		// Bitmap.createScaledBitmap();
		float scale = bmp.getHeight() / 100;
		width = (int) (width / scale);

		width=width/2; //addbylk 减半输出 

		Bitmap nBmp = Bitmap.createScaledBitmap(bmp, width, 100, false);
		BitmapWriter.saveBitmap(nBmp, ConfigPath.getTlkDir(getName()), "1.bmp");
	}
	/**
	 * save picture to tlk dir
	 */
	private void saveExtras() {
		for (BaseObject object : getObjects()) {
			if (object instanceof GraphicObject) {
				String source = ConfigPath.getPictureDir() + ((GraphicObject)object).getImage();
				String dst = getPath() + "/" + ((GraphicObject)object).getImage();
				Debug.d(TAG, "--->source: " + source);
				Debug.d(TAG, "--->dst: " + dst);
				// if file is exist, dont copy again
				File file = new File(dst);
				if (file.exists()) {
					continue;
				} else {
					FileUtil.copyFile(source, dst);
					((GraphicObject)object).setImage(dst);
				}
			}
		}
	}

	public void save() {
		
		resetIndexs();
		//保存1.TLK文件
		// saveTlk(mContext);
		//保存1.bin文件
		saveBin();
		
		//保存其他必要的文件
		saveExtras();
		
		//保存vx.bin文件
		saveVarBin();
		
		//保存1.TLK文件
		saveTlk(mContext);
				
		//保存1.bmp文件
		savePreview();
	}
	
	private void resetIndexs() {
		int i = 0;
		for (BaseObject o : mObjects) {
			o.setIndex(i + 1);
			if (o instanceof RealtimeObject) {
				i = o.getIndex() + ((RealtimeObject) o).getSubObjs().size();
			} else {
				i = o.getIndex();
			}
		}
	}
	
	
	public MessageObject getMsgObject() {
		MessageObject msg = null;
		for (BaseObject object: mObjects) {
			if (object instanceof MessageObject) {
				msg = (MessageObject) object;
				break;
			}
		}
		return msg;
	}
	
	public int getHeads() {
		int height = 1;
		MessageObject obj = getMsgObject();
		if (obj == null) {
			return height;
		}
		// Debug.d(TAG, "--->head type: " + obj.getType());
		switch (obj.getType()) {
			case MessageType.MESSAGE_TYPE_12_7:
			case MessageType.MESSAGE_TYPE_12_7_S:
			case MessageType.MESSAGE_TYPE_16_3:
			case MessageType.MESSAGE_TYPE_1_INCH:
			case MessageType.MESSAGE_TYPE_1_INCH_FAST:
				height = 1;
				break;
			case MessageType.MESSAGE_TYPE_25_4:
			case MessageType.MESSAGE_TYPE_33:
			case MessageType.MESSAGE_TYPE_1_INCH_DUAL:
			case MessageType.MESSAGE_TYPE_1_INCH_DUAL_FAST:
				height = 2;
				break;
			case MessageType.MESSAGE_TYPE_38_1:
				height = 3;
				break;
			case MessageType.MESSAGE_TYPE_50_8:
				height = 4;
				break;
			default:
				break;
		}
		return height;
	}

	public String getPreview() {
		return ConfigPath.getTlkDir(mName) + MSG_PREV_IMAGE;
	}
	
	public String getPath() {
		return ConfigPath.getTlkDir(mName);
	}
	
	public float getDiv() {
		return 4f/getHeads();
	}

	
	public static class MessageType {
		public static final int MESSAGE_TYPE_12_7 	= 0;
		public static final int MESSAGE_TYPE_12_7_S = 1;
		public static final int MESSAGE_TYPE_25_4 	= 2;
		public static final int MESSAGE_TYPE_16_3 	= 3;
		public static final int MESSAGE_TYPE_33	   	= 4;
		public static final int MESSAGE_TYPE_38_1  	= 5;
		public static final int MESSAGE_TYPE_50_8  	= 6;
		public static final int MESSAGE_TYPE_HZK_16_16  = 7;
		public static final int MESSAGE_TYPE_HZK_32_32  = 8;				
		public static final int MESSAGE_TYPE_1_INCH = 9; //320點每列的噴頭
		public static final int MESSAGE_TYPE_1_INCH_FAST = 10; //320點每列的噴頭
		public static final int MESSAGE_TYPE_1_INCH_DUAL = 11; //320點每列的噴頭,雙頭
		public static final int MESSAGE_TYPE_1_INCH_DUAL_FAST = 12; //320點每列的噴頭,雙頭
	}
	
}
