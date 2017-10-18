package com.industry.printer.object;

import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Utils.Configs;

import android.content.Context;
import android.text.format.Time;

public class RealtimeHour extends BaseObject {

	public RealtimeHour(Context context, float x) {
		super(context, BaseObject.OBJECT_TYPE_RT_HOUR, x);
		Time t = new Time();
		t.set(System.currentTimeMillis());
		setContent(BaseObject.intToFormatString(t.hour, 2));
	}

	@Override
	public String getContent()
	{
		Time t = new Time();
		t.set(System.currentTimeMillis());
		setContent(BaseObject.intToFormatString(t.hour, 2));
		return mContent;
	}
	
	public String toString()
	{
		float prop = getProportion();
		String str="";
		//str += BaseObject.intToFormatString(mIndex, 3)+"^";
		str += mId+"^";
		str += BaseObject.floatToFormatString(getX() * prop, 5)+"^";
		str += BaseObject.floatToFormatString(getY()*2 * prop, 5)+"^";
		str += BaseObject.floatToFormatString(getXEnd() * prop, 5)+"^";
		//str += BaseObject.floatToFormatString(getY() + (getYEnd()-getY())*2, 5)+"^";
		str += BaseObject.floatToFormatString(getYEnd()*2 * prop, 5)+"^";
		str += BaseObject.intToFormatString(0, 1)+"^";
		str += BaseObject.boolToFormatString(mDragable, 3)+"^";
		//str += BaseObject.intToFormatString(mContent.length(), 3)+"^";
		str += "000^000^000^000^000^00000000^00000000^00000000^00000000^0000^0000^" + mFont + "^000^000";
		System.out.println("file string ["+str+"]");
		return str;
	}

}
