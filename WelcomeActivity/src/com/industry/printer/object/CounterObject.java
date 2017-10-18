package com.industry.printer.object;

import com.industry.printer.MainActivity;
import com.industry.printer.R;
import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.renderscript.Sampler.Value;
import android.util.Log;

public class CounterObject extends BaseObject {

	public int mBits;
	public boolean mDirection;
	public int mMax;
	public int mMin;
	public int mValue;
	public int mStepLen;
	//public int mCurVal;
	
	public CounterObject(Context context, float x) {
		super(context, BaseObject.OBJECT_TYPE_CNT, x);
		mMin=0;
		mStepLen=1;
		mDirection = true;
		setBits(5);
	}

	public void setBits(int n)
	{
		mBits = n;
		mValue = 1;
		setContent( BaseObject.intToFormatString(mValue, mBits));
		mMax = (int) Math.pow(10, mBits) -1;
	}
	
//	@Override
//	public void setContent(String content) {
//		super.setContent(content);
//		
//	}
	
	public int getBits()
	{
		return mBits;
	}
	
	public void setMax(int max)
	{
		mMax = max;
	}
	
	public int getMax()
	{
		return mMax;
	}
	
	public void setMin(int min)
	{
		mMin = min;
	}
	
	public int getMin()
	{
		return mMin;
	}
	
	public void setRange(int start, int end) {
		if (start <= end) {
			mDirection = true;
			mMin = start;
			mMax = end;
		} else {
			mDirection = false;
			mMin = end;
			mMax = start;
		}
		Debug.d(TAG, "setRange mMax="+mMax + ",  mMin=" + mMin);
	}
	
	public void setDirection(boolean dir)
	{
		mDirection = dir;
	}
	
	public String getDirection()
	{
		String[] directions = mContext.getResources().getStringArray(R.array.strDirectArray);
		return mDirection ? directions[0] : directions[1];
	}
	
	public void setSteplen(int step)
	{
		if(step >= 0)
		mStepLen = step;
	}
	
	public int getSteplen()
	{
		return mStepLen;
	}
	
	public int getValue()
	{
		return mValue;
	}
	
		
	public void setValue(int value)
	{
		if( mMin < mMax) {
			if(value < mMin || value> mMax) {
				mValue = mMin;
			}
			else {
				mValue = value;
			}
		} else {
			if (value > mMin || value < mMax) {
				mValue = mMin;
			} else {
				mValue = value;
			}
		}
		mContent = BaseObject.intToFormatString(mValue, mBits);
	}
	
	@Override
	public void setContent(String content) {
		try{
			Debug.d(TAG, "--->setContent content="+content);
			int value = Integer.parseInt(content);
			Debug.d(TAG, "setContent value="+value);
			if( mMin < mMax) {
				if(value < mMin || value> mMax) {
					mValue = mMin;
				}
				else {
					mValue = value;
				}
			} else {
				if (value > mMin || value < mMax) {
					mValue = mMin;
				} else {
					mValue = value;
				}
			}
			
		} catch (Exception e) {
			mValue = mMin;
			Debug.d(TAG, "--->setContent exception: " + e.getMessage());
		}
		mContent = BaseObject.intToFormatString(mValue, mBits);
		Debug.d(TAG, "setContent content="+content+", value="+mValue+", mMax="+mMax);
	}
	
	
	public String getNext()
	{
		Debug.d(TAG, "--->getNext mContent="+mContent+", mValue="+mValue+", mSteplen=" + mStepLen + " direction=" + mDirection);
		if(mDirection)	//increase
		{
			if(mValue+mStepLen > mMax || mValue < mMin)
				mValue=mMin;
			else
				mValue += mStepLen;
		}
		else	//decrease
		{
			if(mValue-mStepLen < mMax || mValue > mMin)
				mValue=mMax;
			else
				mValue -= mStepLen;
		}
		String value =mContent;
		setContent( BaseObject.intToFormatString(mValue, mBits));
		Debug.d(TAG, "getNext mContent="+mContent+", mValue="+mValue+", mMax="+mMax);
		return value;
	}
	
	public String toString()
	{
		float prop = getProportion();
		String str="";
		//str += BaseObject.intToFormatString(mIndex, 3)+"^";
		str += mId+"^";
		str += BaseObject.floatToFormatString(getX()*prop, 5)+"^";
		str += BaseObject.floatToFormatString(getY()*2 * prop, 5)+"^";
		str += BaseObject.floatToFormatString(getXEnd() * prop, 5)+"^";
		//str += BaseObject.floatToFormatString(getY() + (getYEnd()-getY())*2, 5)+"^";
		str += BaseObject.floatToFormatString(getYEnd()*2 * prop, 5)+"^";
		str += BaseObject.intToFormatString(0, 1)+"^";
		str += BaseObject.boolToFormatString(mDragable, 3)+"^";
		str += BaseObject.intToFormatString(mBits, 3)+"^";
		str += "000^000^000^000^";
		str += BaseObject.intToFormatString(mMax, 8)+"^";
		str += BaseObject.intToFormatString(mMin, 8)+"^";
		str += BaseObject.intToFormatString(Integer.parseInt(mContent) , 8)+"^";
		str += "00000000^0000^0000^" + mFont + "^000^000";
		System.out.println("counter string ["+str+"]");
		return str;
	}
//////add by lk 
	@Override	 
	public Bitmap getpreviewbmp()
	{
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
		mPaint.setColor(Color.BLUE);//���� ���� �� λͼ �� Ϊ ��ɫ 
	 
		String str_new_content="";
		mContent =	mContent.replace('0', 'c');	
		
		mContent =	mContent.replace('1', 'c');	
		mContent =	mContent.replace('2', 'c');	
		mContent =	mContent.replace('3', 'c');	
		mContent =	mContent.replace('4', 'c');	
		mContent =	mContent.replace('5', 'c');	
		mContent =	mContent.replace('6', 'c');	
		mContent =	mContent.replace('7', 'c');	
		mContent =	mContent.replace('8', 'c');	
		mContent =	mContent.replace('9', 'c');	
		
		
		mCan.drawText(mContent , 0, mHeight-fm.descent, mPaint);
	
		return Bitmap.createScaledBitmap(bitmap, (int)mWidth, (int)mHeight, false);	
	}	
	
}
