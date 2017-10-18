package com.industry.printer;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.app.PendingIntent.OnFinished;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Rfid.RfidScheduler;
import com.industry.printer.Rfid.RfidTask;
import com.industry.printer.Utils.ConfigPath;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.Utils.PlatformInfo;
import com.industry.printer.data.BinCreater;
import com.industry.printer.data.DataTask;
import com.industry.printer.hardware.FpgaGpioOperation;
import com.industry.printer.object.BaseObject;
import com.industry.printer.object.CounterObject;

/**
 * class DataTransferThread
 * 用一个独立的线程读取fpga的buffer状态，
 * 如果kernel已经把打印数据发送给FPGA，那么kernel的Buffer状态为空，可写
 * 此时，需要把下一条打印数据下发给kernel Buffer；
 * 如果kernel的buffer状态不为空，不可写
 * 此时，线程轮训buffer，直到kernel buffer状态为空；
 * @author kevin
 *
 */
public class DataTransferThread extends Thread {
	
	public static final String TAG = DataTransferThread.class.getSimpleName();
	private static final int MESSAGE_EXCEED_TIMEOUT = 60 * 1000;
	
	public static boolean mRunning;
	
	public static DataTransferThread mInstance;
	
	private Context mContext;
	
	public boolean mNeedUpdate=false;
	private boolean isBufferReady = false;
	
	private int mcountdown = 0;
	/**打印数据buffer**/
	public DataTask mDataTask;
	RfidScheduler	mScheduler;
	private static long mInterval = 0;
	private int mThreshold;
	
	private InkLevelListener mInkListener = null;
	
	public static DataTransferThread getInstance() {
		if(mInstance == null) {
			Debug.d(TAG, "===>new thread");
			mInstance = new DataTransferThread();
			
		}
		return mInstance;
	}
	
	public DataTransferThread() {
	}
	
	/**
	 * 数据更新机制：
	 * 每次发送数据时同时触发一个delay 10s的handler
	 * 如果pollState返回不为0，即数据打印完毕，则remove handlermessage
	 * 否则，处理这个message并置数据更新状态为true
	 * run函数中一旦检测到数据更新状态变为true，就重新生成buffer并下发
	 */
	
	@Override
	public void run() {
		
		char[] buffer;
		long last = 0;
		/*逻辑要求，必须先发数据*/
		buffer = mDataTask.getPrintBuffer();
		Debug.d(TAG, "--->runing getBuffer ok");
		ArrayList<String> usbs = ConfigPath.getMountedUsb();
		if (usbs != null && usbs.size() > 0) {
			String path = usbs.get(0);
			path = path + "/print.bin";
			BinCreater.saveBin(path, buffer, mDataTask.getInfo().mBytesPerHFeed*8*mDataTask.getHeads());
		}
		Debug.e(TAG, "--->write data");
		FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_OUTPUT, buffer, buffer.length*2);
		last = SystemClock.currentThreadTimeMillis();
		Debug.e(TAG, "--->start print " + mRunning);
		FpgaGpioOperation.init();
		while(mRunning == true) {
			
			// FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_OUTPUT, buffer, buffer.length*2);
			
			int writable = FpgaGpioOperation.pollState();
			// writable = 1;
			if (writable == 0) { //timeout
			} else if (writable == -1) {
			} else {
				mInterval = SystemClock.currentThreadTimeMillis() - last;
				mHandler.removeMessages(MESSAGE_DATA_UPDATE);
				mNeedUpdate = false;
				buffer = mDataTask.getPrintBuffer();
				if (!mDataTask.isReady) {
					mRunning = false;
					if (mCallback != null) {
						mCallback.OnFinished(CODE_BARFILE_END);
					}
					break;
				}
				// Debug.d(TAG, "===>buffer size="+buffer.length);
				FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_OUTPUT, buffer, buffer.length*2);
				last = SystemClock.currentThreadTimeMillis();
				countDown();
				
				mInkListener.onCountChanged();
				
				mScheduler.schedule();
				
				if (mCallback != null) {
					mCallback.onComplete();
				}
			}
			
			if(mNeedUpdate == true) {
				mHandler.removeMessages(MESSAGE_DATA_UPDATE);
				//在此处发生打印数据，同时
				buffer = mDataTask.getPrintBuffer();
				Debug.d(TAG, "===>buffer size="+buffer.length);
				FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_OUTPUT, buffer, buffer.length*2);
				mHandler.sendEmptyMessageDelayed(MESSAGE_DATA_UPDATE, MESSAGE_EXCEED_TIMEOUT);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Debug.d(TAG, "===>kernel buffer empty, fill it");
			//TO-DO list 下面需要把打印数据下发
		}
		
	}
	
	public void purge(final Context context) {
		ThreadPoolManager.mThreads.execute(new Runnable() {
			
			@Override
			public void run() {
				DataTask task = new DataTask(context, null);
				Debug.e(TAG, "--->task: " + task);
				char[] buffer = task.preparePurgeBuffer();
				FpgaGpioOperation.updateSettings(context, task, FpgaGpioOperation.SETTING_TYPE_PURGE1);
				FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_PURGE, buffer, buffer.length*2);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				FpgaGpioOperation.updateSettings(context, task, FpgaGpioOperation.SETTING_TYPE_PURGE2);
				FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_PURGE, buffer, buffer.length*2);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				FpgaGpioOperation.clean();
			}
		});
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public boolean launch(Context ctx) {
		mRunning = true;
		DataTransferThread thread = getInstance();
		if (!isBufferReady || mDataTask == null) {
			return false;
		}
		
		if (mScheduler == null) {
			mScheduler = new RfidScheduler(mContext);
		}
		
		SystemConfigFile configFile = SystemConfigFile.getInstance(ctx);
		mScheduler.init();
		for (int i = 0; i < configFile.getHeads(); i++) {
			mScheduler.add(new RfidTask(i, mContext));
		}
		mScheduler.load();
		
		thread.start();
		return true;
	}
	
	public void finish() {
		mRunning = false;
		
		DataTransferThread t = mInstance;
		mInstance = null;
		mHandler.removeMessages(MESSAGE_DATA_UPDATE);
		if (t != null) {
			t.interrupt();
		}
		mScheduler.doAfterPrint();
	}
	
	public void setOnInkChangeListener(InkLevelListener listener) {
		mInkListener = listener;
	}
	
	
	public static final int MESSAGE_DATA_UPDATE = 1;
	
	public Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_DATA_UPDATE:
					mNeedUpdate = true;
					break;
			}
		}
	};
	
	public void initDataBuffer(Context context, MessageTask task) {
		if (mDataTask == null) {
			mDataTask = new DataTask(context, task);
		} else {
			mDataTask.setTask(task);
		}
		Debug.d(TAG, "--->prepare buffer");
		
		isBufferReady = mDataTask.prepareBackgroudBuffer();
	}
	
	public DataTask getData() {
		return mDataTask;
	}
																																																																																																																
	public void setDotCount(int count) {
		if (mDataTask == null) {
			return;
		}
		mDataTask.setDots(count);
		mcountdown = getInkThreshold();
	}
	
	public int getDotCount() {
		if (mDataTask == null) {
			return 1;
		}
			
		return mDataTask.getDots();
	}
	
	/**
	 * 倒计数，当计数倒零时表示墨水量需要减1，同时倒计数回归
	 * @return true 墨水量需要减1； false 墨水量不变
	 */
	private boolean countDown() {
		mcountdown--;
		if (mcountdown <= 0) {
			// 赋初值
			mcountdown = getInkThreshold();
			mInkListener.onInkLevelDown();
			return true;
		}
		return false;
	}
	
	public int getCount() {
		
		return mcountdown;
	}
	
	/**
	 * 通过dot count计算RFID减1的阀值
	 * @return
	 */
	public int getInkThreshold() {
		int bold = 1;
		if (getDotCount() <= 0) {
			return 1;
		}
		SystemConfigFile config = SystemConfigFile.getInstance(mContext);
		if (config.getParam(2) <= 0) {
			bold = 1;
		} else {
			bold = config.getParam(2)/150;
		}
		return Configs.DOTS_PER_PRINT*getHeads()/(getDotCount() * bold);
	}
	
	public int getHeads() {
		return mDataTask.getHeads();
	}
	/**
	 * 打印間隔0~100ms（每秒鐘打印 > 20次），爲高速打印，每個打印間隔只執行1步操作
	 * 打印間隔100~200ms（每秒鐘打印 > 20次），爲高速打印，每個打印間隔只執行2步操作
	 * 打印間隔200~500ms（每秒鐘打印 > 20次），爲高速打印，每個打印間隔只執行4步操作
	 * 打印間隔500~1000ms（每秒鐘打印 > 20次），爲高速打印，每個打印間隔只執行8步操作
	 * @return
	 */
	public static long getInterval() {
		if (mInterval >= 1000) {
			return 8;
		} else if (mInterval >= 500) {
			return 4;
		} else if (mInterval >= 200) {
			return 2;
		} else {
			return 1;
		}
	}

	public void refreshCount() {
		mcountdown = getInkThreshold();
	}
	
	private Callback mCallback;
	
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	
	
	public interface Callback {
		/**
		 * 整個任務打印完成
		 */
		public void OnFinished(int code);
		/**
		 * 一個任務打印完成
		 */
		public void onComplete();
	}
	
	public static final int CODE_BARFILE_END = 1;
	public static final int CODE_NO_BARFILE = 2;
}
