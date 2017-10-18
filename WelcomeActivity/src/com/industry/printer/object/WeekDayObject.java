package com.industry.printer.object;

import java.util.Calendar;
import java.util.Locale;

import com.industry.printer.Utils.Debug;

import android.content.Context;

public class WeekDayObject extends BaseObject {

	public WeekDayObject(Context context, float x) {
		super(context, OBJECT_TYPE_WEEKDAY, x);
		Calendar c = Calendar.getInstance();
		int dat = c.get(Calendar.DAY_OF_WEEK);
		setContent(String.valueOf(dat));
	}

	public WeekDayObject(Context context) {
		super(context, OBJECT_TYPE_WEEKDAY);
		Calendar c = Calendar.getInstance();
		int dat = c.get(Calendar.DAY_OF_WEEK);
		setContent(String.valueOf(dat));
	}
	
	@Override
	public String getContent() {
		Calendar c = Calendar.getInstance();
		int dat = c.get(Calendar.DAY_OF_WEEK);
		mContent = String.valueOf(dat);
		return mContent;
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
		str += BaseObject.floatToFormatString(getYEnd()*2 * prop, 5)+"^";
		str += BaseObject.intToFormatString(0, 1)+"^";
		str += BaseObject.boolToFormatString(mDragable, 3)+"^";
		str += "000^000^000^000^000^00000000^00000000^00000000^00000000^0000^0000^0000^000^000";
		Debug.d(TAG,"file string ["+str+"]");
		return str;
	}
	
	
}
