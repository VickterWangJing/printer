package com.industry.printer.object;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.industry.printer.ImageConverter;
import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.data.BinCreater;
import com.industry.printer.data.BinFromBitmap;

public class GraphicObject  extends BaseObject{

	public static String TAG="GraphicObject";
	
	public Bitmap mBitmap;
	private String mImage;
	
	public GraphicObject(Context context) {
		super(context, BaseObject.OBJECT_TYPE_GRAPHIC, 0);
		// TODO Auto-generated constructor stub
	}
	
	public GraphicObject(Context context, float x) {
		super(context, BaseObject.OBJECT_TYPE_GRAPHIC, x);
		// TODO Auto-generated constructor stub
	}

	public void setImage(String file)
	{
		File f = new File(file);
		if( !f.isFile() || !isPic(file))
		{
			Debug.d(TAG, "please select a correct file");
			return;
		}
		Debug.d(TAG, "setImage file: "+file);
		mContent = f.getName();
		Bitmap b = BitmapFactory.decodeFile(file);
		mBitmap = ImageConverter.convertGreyImg(b);
		BinFromBitmap.recyleBitmap(b);
	}
	
	public String getImage() {
		return mContent;
	}
	
	
	public Bitmap getScaledBitmap(Context context)
	{
		return mBitmap;
	}
	
	public static String[] pic_formats={".png", ".jpg", ".jpeg", ".bmp"};
	
	public static boolean isPic(String f)
	{
		if(f==null)
			return false;
		int index = f.lastIndexOf(".");
		if(index<0 || index >= f.length())
			return false;
		String suffix = f.substring(index);
		for(String s : pic_formats)
		{
			if(s.equalsIgnoreCase(suffix))
				return true;
		}
		return false;
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
		str += BaseObject.intToFormatString(mContent.length(), 3)+"^";
		str += "000^000^000^";
		str += mContent + "^00000000^00000000^00000000^00000000^0000^0000^0000^000^000";
		System.out.println("file string ["+str+"]");
		return str;
	}
}
