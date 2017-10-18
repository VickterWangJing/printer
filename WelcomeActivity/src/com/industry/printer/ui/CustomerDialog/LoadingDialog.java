package com.industry.printer.ui.CustomerDialog;

import com.industry.printer.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingDialog extends Dialog {

	private Context mContext;
	private ImageView mRotationView;
	private TextView mMessage;
	
	public LoadingDialog(Context context) {
		super(context);
		mContext = context;
		this.setCanceledOnTouchOutside(false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.layout_loading);
		mRotationView = (ImageView) findViewById(R.id.rotationImg);
		mMessage = (TextView) findViewById(R.id.message);
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.loading_anim);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		
	}
	

}
