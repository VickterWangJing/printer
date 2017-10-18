package com.industry.printer.hardware;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.friendlyarm.AndroidSDK.HardwareControler;
import com.industry.printer.Utils.PlatformInfo;

import android.drm.DrmStore.Playback;

public class PWMAudio {

	public static ExecutorService mThreadPoll = Executors.newSingleThreadExecutor();
	
	public static void Play() {
		mThreadPoll.execute(new Runnable() {
			
			@Override
			public void run() {
				if (PlatformInfo.PRODUCT_FRIENDLY_4412.equalsIgnoreCase(PlatformInfo.getProduct())) {
					HardwareControler.PWMPlay(1000);
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (PlatformInfo.PRODUCT_FRIENDLY_4412.equalsIgnoreCase(PlatformInfo.getProduct())) {
					HardwareControler.PWMStop();
				}
				
			}
		});
		// mThreadPoll.shutdown();
		
	}
	
	public static void PlayLong() {
		mThreadPoll.execute(new Runnable() {
			
			@Override
			public void run() {
				if (PlatformInfo.PRODUCT_FRIENDLY_4412.equalsIgnoreCase(PlatformInfo.getProduct())) {
					HardwareControler.PWMPlay(1000);
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (PlatformInfo.PRODUCT_FRIENDLY_4412.equalsIgnoreCase(PlatformInfo.getProduct())) {
					HardwareControler.PWMStop();
				}
			}
		});
		// mThreadPoll.shutdown();
		
	}
	
	
}
