package com.industry.printer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.friendlyarm.AndroidSDK.GPIOEnum;
import com.friendlyarm.AndroidSDK.HardwareControler;
import com.industry.printer.FileFormat.DotMatrixFont;
import com.industry.printer.FileFormat.QRReader;
import com.industry.printer.FileFormat.SystemConfigFile;
import com.industry.printer.Socket_Server.Network;
import com.industry.printer.Socket_Server.Paths_Create;
import com.industry.printer.Socket_Server.Printer_Database;
import com.industry.printer.Utils.ConfigPath;
import com.industry.printer.Utils.Configs;
import com.industry.printer.Utils.Debug;
import com.industry.printer.Utils.PlatformInfo;
import com.industry.printer.Utils.PrinterDBHelper;
import com.industry.printer.Utils.RFIDAsyncTask;
import com.industry.printer.Utils.SystemPropertiesProxy;
import com.industry.printer.data.BinCreater;
import com.industry.printer.data.BinFromBitmap;
import com.industry.printer.data.DataTask;
import com.industry.printer.hardware.ExtGpio;
import com.industry.printer.hardware.FpgaGpioOperation;
import com.industry.printer.hardware.LRADCBattery;
import com.industry.printer.hardware.PWMAudio;
import com.industry.printer.hardware.RFIDDevice;
import com.industry.printer.hardware.RFIDManager;
import com.industry.printer.hardware.RTCDevice;
import com.industry.printer.hardware.UsbSerial;
import com.industry.printer.object.BarcodeObject;
import com.industry.printer.object.BaseObject;
import com.industry.printer.object.CounterObject;
import com.industry.printer.object.TlkObject;
import com.industry.printer.ui.ExtendMessageTitleFragment;
import com.industry.printer.ui.CustomerAdapter.PreviewAdapter;
import com.industry.printer.ui.CustomerDialog.CustomerDialogBase.OnPositiveListener;
import com.industry.printer.ui.CustomerDialog.FontSelectDialog;
import com.industry.printer.ui.CustomerDialog.MessageBrowserDialog;
import com.industry.printer.R;
import com.industry.printer.ControlTabActivity.ServerThread;
import com.industry.printer.ControlTabActivity.Service;

import android.R.bool;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ControlTabActivity extends Fragment implements OnClickListener, InkLevelListener, OnTouchListener, DataTransferThread.Callback {
	public static final String TAG="ControlTabActivity";
	
	public static final String ACTION_REOPEN_SERIAL="com.industry.printer.ACTION_REOPEN_SERIAL";
	public static final String ACTION_CLOSE_SERIAL="com.industry.printer.ACTION_CLOSE_SERIAL";
	public static final String ACTION_BOOT_COMPLETE="com.industry.printer.ACTION_BOOT_COMPLETED";
	
	public Context mContext;
	public ExtendMessageTitleFragment mMsgTitle;
	public int mCounter;
	public RelativeLayout mBtnStart;
	public TextView		  mTvStart; 
	public RelativeLayout mBtnStop;
	public TextView		  mTvStop;
	public RelativeLayout mBtnClean;
	public TextView		  mTvClean;
	public Button mBtnOpen;
	public TextView		  mTvOpen;
	//public Button mGoto;
	//public EditText mDstline;
	
	public RelativeLayout	mBtnOpenfile;
	public LinearLayout mllPreview;
	public HorizontalScrollView mScrollView;
	public TextView mMsgFile;
	private TextView tvMsg;
	// public EditText mMsgPreview;
	public TextView mMsgPreview;
	public ImageView mMsgPreImg;
	public Button 	mBtnview;
	public RelativeLayout	mForward;
	public RelativeLayout 	mBackward;
	
	public TextView mRecords;
	
	public LinkedList<Map<String, String>>	mMessageMap;
	public PreviewAdapter mMessageAdapter;
	public ListView mMessageList;
	
	public PreviewScrollView mPreview;
	public ArrayList<BaseObject> mObjList;
	
	public static int mFd;
	
	public BinInfo mBg;
	BroadcastReceiver mReceiver;
	public Handler mCallback;
	
	public static FileInputStream mFileInputStream;
	Vector<Vector<TlkObject>> mTlkList;
	Map<Vector<TlkObject>, byte[]> mBinBuffer;
	/*
	 * whether the print-head is doing print work
	 * if no, poll state Thread will read print-header state
	 * 
	 */
	public boolean isRunning;
	// public PrintingThread mPrintThread;
	public DataTransferThread mDTransThread;
	
	public int mIndex;
	public TextView mPrintStatus;
	public TextView mtvInk;
	public TextView mInkLevel;
	public TextView mInkLevel2;
	public TextView mTVPrinting;
	public TextView mTVStopped;
	public TextView mPhotocellState;
	public TextView mEncoderState;
	public TextView mPrintState;
	public TextView mPower;
	public TextView mPowerV;
	public TextView mTime;
	
	public SystemConfigFile mSysconfig;
	/**
	 * UsbSerial device name
	 */
	public String mSerialdev;
	
	private RFIDDevice mRfidDevice;
	private RFIDManager mRfidManager;
	/**
	 * current tlk path opened
	 */
	public String mObjPath=null;

	private int mRfid = 100;
	/**
	 * MESSAGE_OPEN_TLKFILE
	 *   message tobe sent when open tlk file
	 */
	public static final int MESSAGE_OPEN_TLKFILE=0;
	/**
	 * MESSAGE_UPDATE_PRINTSTATE
	 *   message tobe sent when update print state
	 */
	public static final int MESSAGE_UPDATE_PRINTSTATE=1;
	/**
	 * MESSAGE_UPDATE_INKLEVEL
	 *   message tobe sent when update ink level
	 */
	public static final int MESSAGE_UPDATE_INKLEVEL=2;
	/**
	 * MESSAGE_DISMISS_DIALOG
	 *   message tobe sent when dismiss loading dialog 
	 */
	public static final int MESSAGE_DISMISS_DIALOG=3;
	
	/**
	 * MESSAGE_PAOMADENG_TEST
	 *   message tobe sent when dismiss loading dialog 
	 */
	public static final int MESSAGE_PAOMADENG_TEST=4;

	/**
	 * MESSAGE_PRINT_START
	 *   message tobe sent when dismiss loading dialog 
	 */
	public static final int MESSAGE_PRINT_START = 5;
	public static final int MESSAGE_PRINT_CHECK_UID = 15;
	
	/**
	 * MESSAGE_PRINT_STOP
	 *   message tobe sent when dismiss loading dialog 
	 */
	public static final int MESSAGE_PRINT_STOP = 6;
	
	public static final int MESSAGE_PRINT_END = 14;
	
	/**
	 * MESSAGE_INKLEVEL_DOWN
	 *   message tobe sent when ink level change 
	 */
	public static final int MESSAGE_INKLEVEL_CHANGE = 7;
	
	/**
	 * MESSAGE_COUNT_CHANGE
	 *   message tobe sent when count change 
	 */
	public static final int MESSAGE_COUNT_CHANGE = 8;
	
	public static final int MESSAGE_REFRESH_POWERSTAT = 9;
	
	public static final int MESSAGE_SWITCH_RFID = 10;
	
	
	public static final int MESSAGE_RFID_LOW = 11;
	
	public static final int MESSAGE_RFID_ZERO = 12;
	
	public static final int MESSAGE_RFID_ALARM = 13;
	
	
	
	/**
	 * the bitmap for preview
	 */
	private Bitmap mPreBitmap;
	/**
	 * 
	 */
	public int[]	mPreBytes;
	
	/**
	 * background buffer
	 *   used for save the background bin buffer
	 *   fill the variable buffer into this background buffer so we get printing buffer
	 */
	public byte[] mBgBuffer;
	/**
	 *printing buffer
	 *	you should use this buffer for print
	 */
	public byte[] mPrintBuffer;
	
	/**
	 * 瑜版挸澧犻幍鎾冲祪娴犺濮�
	 */
	public MessageTask mMsgTask;
	/**
	 * preview buffer
	 * 	you should use this buffer for preview
	 */
	public byte[] mPreviewBuffer;
	
	private boolean mFeatureCorrect = false;
	//Socket___________________________________________________________________________________________
		private Network Net;//checking net;
		private String hostip,aimip;// ip addr
		private Handler myHandler=null;//rec infor prpcess handle
		private String Commands="";// command word;
		private static final int PORT =3550; // port number;
		private volatile ServerSocket server=null; //socket service object
		private ExecutorService mExecutorService = null; //hnadle ExecutorService
		private List<Socket> mList = new ArrayList<Socket>(); //socket list
		private volatile boolean flag= true;// status flag
		private String PrnComd="";//printing word
		private Printer_Database Querydb;// database class
		private Paths_Create Paths=new Paths_Create();//get and creat path class
		private String AddPaths;//create paths
		private String Scounts;//add counter
		private Stack<String> StrInfo_Stack  = new Stack<String>();// str stack infor
		private PackageInfo pi; //system infor pack
		private StringBuffer sb = new StringBuffer(); //str area word
		private HashMap<String, String> map = new HashMap<String, String>();//map area word
		private int PrinterFlag=0;
		private int SendFileFlag=0;
		private int CleanFlag=0;
		private int StopFlag=0;
		private Socket Gsocket;  
		
		//Socket___________________________________________________________________________________________
	
	public ControlTabActivity() {
		//mMsgTitle = (ExtendMessageTitleFragment)fragment;
		mCounter = 0;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		return inflater.inflate(R.layout.control_frame, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		mIndex=0;
		mTlkList = new Vector<Vector<TlkObject>>();
		mBinBuffer = new HashMap<Vector<TlkObject>, byte[]>();
		mObjList = new ArrayList<BaseObject>();
		mContext = this.getActivity();
		mSysconfig = SystemConfigFile.getInstance(mContext);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_REOPEN_SERIAL);
		filter.addAction(ACTION_CLOSE_SERIAL);
		filter.addAction(ACTION_BOOT_COMPLETE);
		mReceiver = new SerialEventReceiver(); 
		mContext.registerReceiver(mReceiver, filter);
		
		mMsgFile = (TextView) getView().findViewById(R.id.opened_msg_name);
		tvMsg = (TextView) getView().findViewById(R.id.tv_msg_name);
		
		mPreview = (PreviewScrollView ) getView().findViewById(R.id.sv_preview);
		
		mBtnStart = (RelativeLayout) getView().findViewById(R.id.StartPrint);
		mBtnStart.setOnClickListener(this);
		mBtnStart.setOnTouchListener(this);
		mTvStart = (TextView) getView().findViewById(R.id.tv_start);
		
		mBtnStop = (RelativeLayout) getView().findViewById(R.id.StopPrint);
		mBtnStop.setOnClickListener(this);
		mBtnStop.setOnTouchListener(this);
		mTvStop = (TextView) getView().findViewById(R.id.tv_stop);
		//mRecords = (TextView) getView().findViewById(R.id.tv_records);
		/*
		 *clean the print head
		 *this command unsupported now 
		 */
		
		mBtnClean = (RelativeLayout) getView().findViewById(R.id.btnFlush);
		mBtnClean.setOnClickListener(this);
		mBtnClean.setOnTouchListener(this);
		mTvClean = (TextView) getView().findViewById(R.id.tv_flush);
				
		mBtnOpenfile = (RelativeLayout) getView().findViewById(R.id.btnBinfile);
		mBtnOpenfile.setOnClickListener(this);
		mBtnOpenfile.setOnTouchListener(this);
		mTvOpen = (TextView) getView().findViewById(R.id.tv_binfile);
		
		setupViews();
		
		mTVPrinting = (TextView) getView().findViewById(R.id.tv_printState);
		mTVStopped = (TextView) getView().findViewById(R.id.tv_stopState);
		
		
		switchState(STATE_STOPPED);
		mScrollView = (HorizontalScrollView) getView().findViewById(R.id.preview_scroll);
		mllPreview = (LinearLayout) getView().findViewById(R.id.ll_preview);
		// mMsgPreview = (TextView) getView().findViewById(R.id.message_preview);
		// mMsgPreImg = (ImageView) getView().findViewById(R.id.message_prev_img);
		//
//		mPrintState = (TextView) findViewById(R.id.tvprintState);
		mtvInk = (TextView) getView().findViewById(R.id.tv_inkValue);
		mInkLevel = (TextView) getView().findViewById(R.id.ink_value);
		mInkLevel2 = (TextView) getView().findViewById(R.id.ink_value2);
		
		mPower = (TextView) getView().findViewById(R.id.power_state);
		mPowerV = (TextView) getView().findViewById(R.id.powerV);
		mTime = (TextView) getView().findViewById(R.id.time);
		
		
		refreshPower();
		//  閸旂姾娴囬幍鎾冲祪鐠佲剝鏆�
		PrinterDBHelper db = PrinterDBHelper.getInstance(mContext);
		//mCounter = db.getCount(mContext);
		RTCDevice rtcDevice = RTCDevice.getInstance(mContext);

		// 婵″倹鐏夐弰顖滎儑娑擄拷濞嗏�虫儙閸旑煉绱濋崥鎱燭C閻ㄥ嚞VRAM閸愭瑥鍙�0
		if (PlatformInfo.PRODUCT_SMFY_SUPER3.equalsIgnoreCase(PlatformInfo.getProduct())) {
			rtcDevice.initSystemTime(mContext);
			mCounter = rtcDevice.readCounter(mContext);
			if (mCounter == 0) {
				rtcDevice.writeCounter(mContext, 0);
				db.setFirstBoot(mContext, false);
			}
		}
		/* 婵″倹鐏夌懛顓犵枂閸欏啯鏆�32閻栫灒n閿涘矁鈻撻弫绋挎珤闁插秶鐤� */
		if (mSysconfig.getParam(31) == 1) {
			mCounter = 0;
		}
		/***PG1 PG2鏉堟挸鍤悩鑸碉拷浣疯礋 0x11閿涘本绔婚梿鑸的佸锟�**/
		FpgaGpioOperation.clean();
		
		//Debug.d(TAG, "===>loadMessage");
		// 闁俺绻冮惄鎴濇儔缁崵绮洪獮鎸庢尡閸旂姾娴�
		loadMessage();
		
		/****閸掓繂顫愰崠鏈ID****/
		mRfidManager = RFIDManager.getInstance(mContext);
		mHandler.sendEmptyMessageDelayed(RFIDManager.MSG_RFID_INIT, 1000);
		
		refreshCount();
		SocketBegin();// Beging Socket service start;
		Querydb=new Printer_Database(mContext);
	}
	
	public void onConfigureChanged() {
		if (mMsgTask != null) {
			mMsgFile.setText(mMsgTask.getName());
		}
		tvMsg.setText(R.string.str_msg_name);
		mTvStart.setText(R.string.str_btn_print);
		mTvStop.setText(R.string.str_btn_stop);
		mTvOpen.setText(R.string.str_btn_open);
		mTvClean.setText(R.string.str_btn_clean);
		mTVPrinting.setText(R.string.str_state_printing);
		mTVStopped.setText(R.string.str_state_stopped);
		mtvInk.setText(R.string.str_state_inklevel);
	}
	
	private void setupViews() {
		if (PlatformInfo.PRODUCT_FRIENDLY_4412.equalsIgnoreCase(PlatformInfo.getProduct())) {
			mForward.setVisibility(View.GONE);
			mBackward.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroy()
	{
		mContext.unregisterReceiver(mReceiver);
		super.onDestroy();
		//UsbSerial.close(mFd);
	}
	
	public void loadMessage() {
		String f = mSysconfig.getLastMsg();
		Debug.d(TAG, "===>load message: " + f);
		if (f == null || f.isEmpty() || !new File(ConfigPath.getTlkDir(f)).exists()) {
			return;
		}
		Message msg = mHandler.obtainMessage(MESSAGE_OPEN_TLKFILE);
		Bundle bundle = new Bundle();
		bundle.putString("file", f);
		msg.setData(bundle);
		mHandler.sendMessageDelayed(msg, 1000);
	}
	
	
	private void switchRfid() {
		mRfid += 1;
		if (mRfid >= RFIDManager.TOTAL_RFID_DEVICES || mRfid >= mSysconfig.getHeads()) {
			mRfid = 0;
		}
		Debug.d(TAG, "--->switchRfid to: " + mRfid);
		refreshInk();
		// refreshCount();
		mHandler.sendEmptyMessageDelayed(MESSAGE_SWITCH_RFID, 3000);
	}
	
	boolean mInkLow = false;
	boolean mInkZero = false;
	
	private void refreshInk() {
		
		float ink = mRfidManager.getLocalInk(mRfid);
		Debug.d(TAG, "--->refresh ink: " + mRfid + " = " + ink);
		String level = String.valueOf(mRfid + 1) + "-" + (String.format("%.1f", ink) + "%");
		
		if (!mRfidManager.isValid(mRfid)) {
			mInkLevel.setBackgroundColor(Color.RED);
			mInkLevel.setText(String.valueOf(mRfid + 1) + "--");
			
		} else if (ink > 0){
			//mInkLevel.clearAnimation();
			mInkLevel.setBackgroundColor(0x436EEE);
			mInkLevel.setText(level);
		} else {
			mInkLevel.setBackgroundColor(Color.RED);
			mInkLevel.setText(level);
			//闁规牕锟借偐鍩�0閸嬫粍顒涢幍鎾冲祪
			if (mDTransThread != null && mDTransThread.isRunning()) {
				mHandler.sendEmptyMessage(MESSAGE_PRINT_STOP);
			}
			
		}
		// Debug.e(TAG, "--->ink = " + ink + ", " + (ink <= 1.0f) + ", " + (ink > 0f));
		// Debug.e(TAG, "--->ink = " + ink + ", " + (ink <= 0f));
		if (ink <= 1.0f && ink > 0f && mInkLow == false) {
			mInkLow = true;
			mHandler.sendEmptyMessageDelayed(MESSAGE_RFID_LOW, 5000);
		} else if (ink <= 0f && mInkZero == false) {
			mInkZero = true;
			mHandler.removeMessages(MESSAGE_RFID_LOW);
			mHandler.sendEmptyMessageDelayed(MESSAGE_RFID_ZERO, 2000);
		}
		refreshVoltage();
		refreshPulse();
		
	}
	
	private void refreshCount() {
		float count = 0;
		// String cFormat = getResources().getString(R.string.str_print_count);
		// ((MainActivity)getActivity()).mCtrlTitle.setText(String.format(cFormat, mCounter));
		
		RFIDDevice device = mRfidManager.getDevice(mRfid);
		if (device != null && mDTransThread != null) {
			count = device.getLocalInk() - 1;
			count = count * mDTransThread.getInkThreshold() + mDTransThread.getCount();
		}
		if (count < 0) {
			count = 0;
		}
		Debug.d(TAG, "--->refreshCount: " + count);
		((MainActivity) getActivity()).setCtrlExtra(mCounter, (int) count);
	}
	
	/**
	 * 濞擃剝鈹傜�碉箓娈幆鍛閻栫灓ower閸婄厧婀�35-44娑斿鏋旈敍灞界毆鐎碉箓娈崐濂革拷鑼额攽鐏忓秵鍣�
	 */
	private void refreshPower() {
		Debug.d(TAG, "--->refreshPower");
		if (PlatformInfo.PRODUCT_SMFY_SUPER3.equalsIgnoreCase(PlatformInfo.getProduct())) {
			int power = LRADCBattery.getPower();
			Debug.d(TAG, "--->power: " + power);
			if (power >= 41) {
				mPower.setText(String.valueOf(100));
			} else if (power >= 38) {
				mPower.setText(String.valueOf(75));
			} else if (power >= 36) {
				mPower.setText(String.valueOf(50));
			} else if (power >= 35) {
				mPower.setText(String.valueOf(25));
			} else if (power >= 33) {
				mPower.setText(String.valueOf(0));
			} else {
				mPower.setText("--");
			}
			//mPowerV.setText(String.valueOf(power));
			// mTime.setText("0");
			// display Voltage & pulse width
			
			mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_POWERSTAT, 5*60*1000);
		}
	}
	
	/**
	 * if setting param25 == on, read from RFID feature 5
	 * if setting param25 == off, read from setting param 26
	 */
	private void refreshVoltage() {
		boolean auto = false;
		if (mRfidManager == null) {
			auto = false;
		} else {
			int vol = mSysconfig.getParam(24);
			if (vol > 0) {
				auto = true;
			}
		}
		
		if (auto) {
			RFIDDevice device = mRfidManager.getDevice(0);
			int vol = device.getFeature(4);
			mPowerV.setText(String.valueOf(vol));
		} else {
			mPowerV.setText(String.valueOf(mSysconfig.getParam(25)));
		}
	}

	/**
	 * if setting param27 == on, read from RFID feature 4
	 * if setting param27 == off, read from setting param 28
	 */
	private void refreshPulse() {
		boolean auto = false;
		if (mRfidManager == null) {
			auto = false;
		} else {
			int p = mSysconfig.getParam(26);
			if (p > 0) {
				auto = true;
			}
		}
		if (auto) {
			RFIDDevice device = mRfidManager.getDevice(0);
			int pulse = device.getFeature(3);
			mTime.setText(String.valueOf(pulse));
		} else {
			mTime.setText(String.valueOf(mSysconfig.getParam(27)));
		}
	}
	
	public int testdata=0;
	public Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
				case MESSAGE_OPEN_TLKFILE:		//
					progressDialog();
					mObjPath = msg.getData().getString("file", null);
					Debug.d(TAG, "open tlk :" + mObjPath );
					//startPreview();
					if (mObjPath == null) {
						break;
					}
					
					//閺傝顢�2閿涙矮绮爐lk閺傚洣娆㈤柌宥嗘煀缂佹ê鍩楅崶鍓у閿涘瞼鍔ч崥搴ば掗弸鎰晸閹存亣uffer
					//parseTlk(f);
					//initBgBuffer();
					/**閼惧嘲褰囬幍鎾冲祪缂傗晝鏆愰崶鎾呯礉閻€劋绨０鍕潔鐏炴洜骞�**/
					mMsgTask = new MessageTask(mContext, mObjPath);
					mObjList = mMsgTask.getObjects();
					//TLKFileParser parser = new TLKFileParser(mContext, mObjPath);
					//String preview = parser.getContentAbatract();
					String preview = mMsgTask.getAbstract();
					if (preview == null) {
						preview = getString(R.string.str_message_no_content);
					}
					// mMsgPreview.setText(new SpanableStringFormator(mObjList));
					// mMsgPreImg.setImageURI(Uri.parse("file://" + mMsgTask.getPreview()));
					if (mPreBitmap != null) {
						BinFromBitmap.recyleBitmap(mPreBitmap);
					}
					//閺傝顢�1閿涙矮绮燽in閺傚洣娆㈤悽鐔稿灇buffer
					initDTThread();
					Debug.d(TAG, "--->init thread ok");
					// mPreBitmap = BitmapFactory.decodeFile(mMsgTask.getPreview());
					mPreBitmap = mDTransThread.mDataTask.getPreview();
					/*婵″倹鐏夐崷鏍鐏忓搫顕柆搴°亣鐏忚京鍔囧▔鏇⑩�欑粈锟�*/
//					if (mPreBitmap.getWidth() > 1280) {
//						Bitmap b = Bitmap.createBitmap(mPreBitmap, 0, 0, 1280, mPreBitmap.getHeight());
//						BinFromBitmap.recyleBitmap(mPreBitmap);
//						mPreBitmap = b;
//					}
					//mMsgPreImg.setImageBitmap(mPreBitmap);
					dispPreview(mPreBitmap);
					// BinCreater.saveBitmap(mPreBitmap, "prev.png");
					// mMsgPreImg.setImageURI(Uri.parse("file://" + "/mnt/usbhost0/MSG1/100/1.bmp"));
					refreshCount();
					mMsgFile.setText(mMsgTask.getName());
					mSysconfig.saveLastMsg(mObjPath);
					dismissProgressDialog();
					if("100".equals(PrnComd))	
					{
						 msg = mHandler.obtainMessage(MESSAGE_PRINT_START);
						 mHandler.sendMessage(msg);
						
						PrnComd="";
					}
					break;
				case MESSAGE_UPDATE_PRINTSTATE:
					String text = msg.getData().getString("text");
					mPrintStatus.setText("result: "+text);
					break;
				case MESSAGE_UPDATE_INKLEVEL:
					Bundle bundle = msg.getData();
					int level = bundle.getInt("ink_level");
					mFeatureCorrect = bundle.getBoolean("feature", true);
					refreshInk();
					break;
				case MESSAGE_DISMISS_DIALOG:
					mLoadingDialog.dismiss();
					break;
				case MESSAGE_PAOMADENG_TEST:
					
					char[] data = new char[32];
					for (char i = 0; i < 15; i++) {
						data[2*i] = (char)(0x01<<i);
						data[2*i+1] = 0xffff;
					}
					data[30] = 0xff;
					data[31] = 0xff;
//					char[] data = new char[2];
//					if (testdata < 0 || testdata > 15)
//						testdata = 0;
//					data[0] = (char) (0x0001 << testdata);
//					data[1] = (char) (0x0001 << testdata);
//					testdata++;
					FpgaGpioOperation.writeData(FpgaGpioOperation.FPGA_STATE_OUTPUT,data, data.length*2);
					mHandler.sendEmptyMessageDelayed(MESSAGE_PAOMADENG_TEST, 1000);
					break;
				case MESSAGE_PRINT_CHECK_UID:
					//Debug.d(TAG, "--->initDTThread");
					if (mDTransThread == null) {
						initDTThread();
					}
					Debug.d(TAG, "--->prepare buffer");
					DataTask dt = mDTransThread.getData();
					mRfidManager.checkUID(dt.getHeads());
					break;
				case RFIDManager.MSG_RFID_CHECK_FAIL:
					Toast.makeText(mContext, "Rfid changed", Toast.LENGTH_SHORT).show();
					break;
				case RFIDManager.MSG_RFID_CHECK_SUCCESS:
				case MESSAGE_PRINT_START: 
					if (!checkRfid()) {
						Toast.makeText(mContext, R.string.str_toast_no_ink, Toast.LENGTH_LONG).show();
						return;
					}
					if (mDTransThread != null && mDTransThread.isRunning()) {
						Toast.makeText(mContext, R.string.str_print_printing, Toast.LENGTH_LONG).show();
						break;
					}
					if (mObjPath == null || mObjPath.isEmpty()) {
						Toast.makeText(mContext, R.string.str_toast_no_message, Toast.LENGTH_LONG).show();
						break;
					}
					if (!checkQRFile()) {
						Toast.makeText(mContext, R.string.str_toast_no_qrfile, Toast.LENGTH_LONG).show();
						/* 濞屾帗婀丵R.txt閹存湣R.csv閺傚洣娆㈢亸鍗炵墾鐠�锟� */
						mHandler.sendEmptyMessage(MESSAGE_RFID_ALARM);
						break;
					}
					DataTask task = mDTransThread.getData();
					if (task == null || task.getObjList() == null || task.getObjList().size() == 0) {
						Toast.makeText(mContext, R.string.str_toast_emptycontent, Toast.LENGTH_LONG).show();
						break;
					}
					/**
					 * 濞村鐦痓uffer閻㈢喐鍨氶弰顖氭儊濮濓絿鈥橀敍灞惧瘻閹垫挸宓冮幐澶愭尦閹跺﹥澧﹂崡鏉垮敶鐎归�涚箽鐎涙ê鍩寀閻╋拷
					 */
//					char[] buf = dt.getPrintBuffer();
//					Debug.d(TAG, "--->save print bin");
//					ArrayList<String> usbs = ConfigPath.getMountedUsb();
//					if (usbs != null && usbs.size() > 0) {
//						String path = usbs.get(0);
//						File file = new File(path + "/print.bin");
//						if (file.exists()) {
//							file.delete();
//						}
//						BinCreater.saveBin( path + "/print.bin", buf, dt.mBinInfo.getBytesFeed() * 8);
//					}
					Debug.d(TAG, "--->clean");
					/**
					 * 閸氼垰濮╅幍鎾冲祪閸氬氦顩︾�瑰本鍨氶惃鍕殤娑擃亜浼愭担婊愮窗
					 * 1閵嗕焦鐦″▎鈩冨ⅵ閸楀府绱�  閸忓牊绔荤粚锟� 閿涘牐顫嗛弬鍥︽閿涘绱� 閻掕泛鎮� 閸欐垼顔曠純锟�
					 * 2閵嗕礁鎯庨崝鈥昦taTransfer缁捐法鈻奸敍宀�鏁撻幋鎰ⅵ閸楃櫚uffer閿涘苯鑻熸稉瀣絺閺佺増宓�
					 * 3閵嗕浇鐨熼悽鈺ctl閸氼垰濮╅崘鍛壋缁捐法鈻奸敍灞界磻婵鐤嗙拋鐠匬GA閻樿埖锟斤拷
					 */
					/*閹垫挸宓冩潻鍥┾柤娑擃厾顩﹀銏犲瀼閹广垺澧﹂崡鏉款嚠鐠烇拷*/
					switchState(STATE_PRINTING);
					FpgaGpioOperation.clean();
					Debug.d(TAG, "--->update settings");
					FpgaGpioOperation.updateSettings(mContext, task, FpgaGpioOperation.SETTING_TYPE_NORMAL);
					Debug.d(TAG, "--->launch thread");
					/*閹垫挸宓冪�电钖勯崷鈺玴enfile閺冭泛鍑＄紒蹇氼啎缂冾噯绱濋幍锟芥禒銉ㄧ箹闁插瞼娲块幒銉ユ儙閸斻劍澧﹂崡棰佹崲閸斺�冲祮閸欙拷*/
					if (!mDTransThread.launch(mContext)) {
						Toast.makeText(mContext, R.string.str_toast_no_bin, Toast.LENGTH_LONG);
						break;
					}
					Debug.d(TAG, "--->finish TrheadId=" + Thread.currentThread().getId());
					// FpgaGpioOperation.init();
					Toast.makeText(mContext, R.string.str_print_startok, Toast.LENGTH_LONG).show();
					break;
				case MESSAGE_PRINT_STOP:
					/**
					 * 閸嬫粍顒涢幍鎾冲祪閸氬氦顩︾�瑰本鍨氶惃鍕殤娑擃亜浼愭担婊愮窗
					 * 1閵嗕浇鐨熼悽鈺ctl閸嬫粍顒涢崘鍛壋缁捐法鈻奸敍灞戒粻濮濄垼鐤嗙拋鐠匬GA閻樿埖锟斤拷
					 * 2閵嗕礁浠犲顢猘taTransfer缁捐法鈻�
					 */
					FpgaGpioOperation.uninit();
					if (mDTransThread != null) {
						mDTransThread.finish();
						mDTransThread = null;
						initDTThread();
					}
					/*閹垫挸宓冩禒璇插閸嬫粍顒涢崥搴″帒鐠佺鍨忛幑銏″ⅵ閸楁澘顕挒锟�*/
					switchState(STATE_STOPPED);
					
					Toast.makeText(mContext, R.string.str_print_stopok, Toast.LENGTH_LONG).show();
					FpgaGpioOperation.clean();
					/* 婵″倹鐏夐悾璺哄閹垫挸宓冩穱鈩冧紖娑擃厽婀佺懛鍫熸毄閸ｎ煉绱濋棁锟界憰浣筋灳闁峰嫮鏆ラ崜宥咃拷鐓庡煂TLK閺傚洣娆㈡稉锟�*/
					updateCntIfNeed();
					
					break;
				case MESSAGE_PRINT_END:
					FpgaGpioOperation.uninit();
					switchState(STATE_STOPPED);
					FpgaGpioOperation.clean();
					break;
				case MESSAGE_INKLEVEL_CHANGE:
					
					for (int i = 0; i < mSysconfig.getHeads(); i++) {
						mRfidManager.downLocal(i);
					}
					/*閹垫挸宓冮弲鍌欑瑝閸愬秴顕涢弲鍌涙纯閺傛澘鈪峰鎾櫤*/
					// refreshInk();
					// mRfidManager.write(mHandler);
					break;
				case MESSAGE_COUNT_CHANGE:
					mCounter++;
					refreshCount();
					//PrinterDBHelper db = PrinterDBHelper.getInstance(mContext);
					//db.updateCount(mContext, (int) mCounter);
					RTCDevice device = RTCDevice.getInstance(mContext);
					device.writeCounter(mContext, mCounter);
					break;
				case MESSAGE_REFRESH_POWERSTAT:
					refreshPower();
					break;
				case MESSAGE_SWITCH_RFID:
					switchRfid();
					break;
				case RFIDManager.MSG_RFID_INIT:
					mRfidManager.init(mHandler);
					break;
				case RFIDManager.MSG_RFID_INIT_SUCCESS:
					// mRfidManager.read(mHandler);
					break;
				case RFIDManager.MSG_RFID_READ_SUCCESS:
					boolean ready = true;
					Bundle bd = (Bundle) msg.getData();
					for (int i=0; i < mSysconfig.getHeads(); i++) {
						RFIDDevice dev = mRfidManager.getDevice(i);
						if (dev.getLocalInk() <= 0) {
							ready = false;
							break;
						}
					}
					if (!ready) {
						mHandler.sendEmptyMessageDelayed(RFIDManager.MSG_RFID_INIT, 5000);
					} else {
						mHandler.removeMessages(MESSAGE_RFID_ZERO);
						ExtGpio.writeGpio('h', 7, 0);
					}
					if (mRfidInit == false) {
						switchRfid();
						refreshCount();
						mRfidInit = true;
					}
					break;
				case RFIDManager.MSG_RFID_WRITE_SUCCESS:
					float ink = mRfidManager.getLocalInk(0);
					refreshInk();
					break;
				case MESSAGE_RFID_LOW:
					Debug.e(TAG, "--->low: play error");
					mHandler.sendEmptyMessage(MESSAGE_RFID_ALARM);
					mHandler.sendEmptyMessageDelayed(MESSAGE_RFID_LOW, 5000);
					break;
				case MESSAGE_RFID_ZERO:
					Debug.e(TAG, "--->zero: play error");
					mHandler.sendEmptyMessage(MESSAGE_RFID_ALARM);
					mHandler.sendEmptyMessageDelayed(MESSAGE_RFID_ZERO, 2000);
					break;
				case MESSAGE_RFID_ALARM:
					Debug.d(TAG, "--->alarm");
					ExtGpio.writeGpio('h', 7, 1);
					if (mRfiAlarmTimes++ < 3) {
						ExtGpio.playClick();
						mHandler.sendEmptyMessageDelayed(MESSAGE_RFID_ALARM, 150);						
					} else {
						mRfiAlarmTimes = 0;
					}
					
					break;
				default:
					break;
			}
		}
	};
	private boolean checkRfid() {
		boolean ready = true;
		if (mDTransThread == null) {
			return true;
		}
		DataTask task = mDTransThread.getData();
		int heads = SystemConfigFile.getInstance(mContext).getHeads();// task.getHeads();
		for (int i = 0; i < heads; i++) {
			float ink = mRfidManager.getLocalInk(i);
			if (ink <= 0) {
				ready = false;
			}
		}
		return ready;
	}
	
	private boolean checkQRFile() {
		boolean ready = true;
		if (mDTransThread == null) {
			return true;
		}
		QRReader reader = QRReader.getInstance(mContext);
		boolean qrReady = reader.isReady();
		DataTask task = mDTransThread.getData();
		for (BaseObject obj : task.getObjList()) {
			if (!(obj instanceof BarcodeObject)) {
				continue;
			}
			if (!((BarcodeObject) obj).isQRCode() || mSysconfig.getParam(16) == 0) {
				continue;
			}
			ready = qrReady;
		}
		return ready;
	}
	
	private void dispPreview(Bitmap bmp) {
		int x=0,y=0;
		int cutWidth = 0;
		float scale = 1;
		Debug.d(TAG, "--->dispPreview: " + mllPreview.getHeight());
		String product = SystemPropertiesProxy.get(mContext, "ro.product.name");
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		Debug.d(TAG, "--->screen width: " + dm.widthPixels + " height: " + dm.heightPixels + "  dpi= " + dm.densityDpi);
		float height = mllPreview.getHeight();
		scale = (height/bmp.getHeight());
		mllPreview.removeAllViews();
			for (int i = 0;x < bmp.getWidth(); i++) {
				if (x + 1200 > bmp.getWidth()) {
					cutWidth = bmp.getWidth() - x;
				} else {
					cutWidth =1200;
					
				}
				Bitmap child = Bitmap.createBitmap(bmp, x, 0, cutWidth, bmp.getHeight());
				Debug.d(TAG, "-->child: " + child.getWidth() + "  " + child.getHeight() + "   view h: " + mllPreview.getHeight());
				 if (child.getWidth() <= 0 || child.getHeight() <= 0 
		                   || cutWidth * scale <= 0 || bmp.getHeight() * scale <= 0) {
		                    break;
		                }
						Bitmap scaledChild = Bitmap.createScaledBitmap(child, (int) (cutWidth*scale), (int) (bmp.getHeight() * scale), true);
						child.recycle();
						x += cutWidth;
						ImageView imgView = new ImageView(mContext);
						imgView.setScaleType(ScaleType.FIT_XY);
//						if (density == 1) {
							imgView.setLayoutParams(new LayoutParams(scaledChild.getWidth(),scaledChild.getHeight()));
//						} else {
//							imgView.setLayoutParams(new LayoutParams(cutWidth,LayoutParams.MATCH_PARENT));
//						}
						
						imgView.setBackgroundColor(Color.WHITE);
						imgView.setImageBitmap(scaledChild);
						mllPreview.addView(imgView);
			}
	}
	
	private int mRfiAlarmTimes = 0;
	private boolean mRfidInit = false;
	
	private void updateCntIfNeed() {
		for (BaseObject object : mMsgTask.getObjects()) {
			if (object instanceof CounterObject) {
				Message msg = new Message();
				msg.what = MainActivity.UPDATE_COUNTER;
				msg.arg1 = Integer.valueOf(((CounterObject) object).getContent());
				mCallback.sendMessage(msg);
				break;
			}
		}
	}
	
	public void initDTThread() {
		
		if (mDTransThread == null) {
			Debug.d(TAG, "--->Print thread ready");
			mDTransThread = DataTransferThread.getInstance();
			mDTransThread.setCallback(this);
		}
		Debug.d(TAG, "--->init");
		
		// 閸掓繂顫愰崠鏈緐ffer
		mDTransThread.initDataBuffer(mContext, mMsgTask);
		// TLKFileParser parser = new TLKFileParser(mContext, mObjPath);
		// 鐠佸墽鐤哾ot count
		mDTransThread.setDotCount(mMsgTask.getDots());
		// 鐠佸墽鐤哢I閸ョ偠鐨�
		mDTransThread.setOnInkChangeListener(this);
		
	}
	
	private final int STATE_PRINTING = 0;
	private final int STATE_STOPPED = 1;
	
	public void switchState(int state) {
		Debug.d(TAG, "--->switchState=" + state);
		switch(state) {
			case STATE_PRINTING:
				mBtnStart.setClickable(false);
				mTvStart.setTextColor(Color.DKGRAY);
				mBtnStop.setClickable(true);
				mTvStop.setTextColor(Color.BLACK);
				mBtnOpenfile.setClickable(false);
				mTvOpen.setTextColor(Color.DKGRAY);
				mTVPrinting.setVisibility(View.VISIBLE);
				mTVStopped.setVisibility(View.GONE);
				mBtnClean.setEnabled(false);
				mTvClean.setTextColor(Color.DKGRAY);
				ExtGpio.writeGpio('b', 11, 1);
				break;
			case STATE_STOPPED:
				mBtnStart.setClickable(true);
				mTvStart.setTextColor(Color.BLACK);
				mBtnStop.setClickable(false);
				mTvStop.setTextColor(Color.DKGRAY);
				mBtnOpenfile.setClickable(true);
				mTvOpen.setTextColor(Color.BLACK);
				mTVPrinting.setVisibility(View.GONE);
				mTVStopped.setVisibility(View.VISIBLE);
				mBtnClean.setEnabled(true);
				mTvClean.setTextColor(Color.BLACK);
				ExtGpio.writeGpio('b', 11, 0);
				break;
			default:
				Debug.d(TAG, "--->unknown state");
		}
	}
	
	
	public void startPreview()
	{
		Debug.d(TAG, "===>startPreview");
		
		try{
			mPreviewBuffer = Arrays.copyOf(mPrintBuffer, mPrintBuffer.length);
			BinInfo.Matrix880Revert(mPreviewBuffer);
			mPreBytes = new int[mPreviewBuffer.length*8];
			// BinCreater.bin2byte(mPreviewBuffer, mPreBytes);
			mPreview.createBitmap(mPreBytes, mBgBuffer.length/110, Configs.gDots);
			mPreview.invalidate();
			
			//mPreviewRefreshHandler.sendEmptyMessage(0);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public void onCheckUsbSerial()
	{
		mSerialdev = null;
		File file = new File("/dev");
		if(file.listFiles() == null)
		{
			return ;
		}
		File[] files = file.listFiles(new PrinterFileFilter("ttyACM"));
		for(File f : files)
		{
			if(f == null)
			{
				break;
			}
			Debug.d(TAG, "file = "+f.getName());
			int fd = UsbSerial.open("/dev/"+f.getName());
			Debug.d(TAG, "open /dev/"+f.getName()+" return "+fd);
			if(fd < 0)
			{
				Debug.d(TAG, "open usbserial /dev/"+f.getName()+" fail");
				continue;
			}
			UsbSerial.close(fd);
			mSerialdev = "/dev/"+f.getName();
			break;
		}
	}

	public class SerialEventReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Debug.d(TAG, "******intent="+intent.getAction());
			if(ACTION_REOPEN_SERIAL.equals(intent.getAction()))
			{
				onCheckUsbSerial();
			}
			else if(ACTION_CLOSE_SERIAL.equals(intent.getAction()))
			{
				Debug.d(TAG, "******close");
				mSerialdev = null;
			}
			else if(ACTION_BOOT_COMPLETE.equals(intent.getAction()))
			{
				onCheckUsbSerial();
				byte info[] = new byte[23];
				UsbSerial.printStart(mSerialdev);
				UsbSerial.getInfo(mSerialdev, info);
				//updateInkLevel(info);
				UsbSerial.printStop(mSerialdev);
			}
		}
		
	}
	
	
	public int calculateBufsize(Vector<TlkObject> list)
	{
		int length=0;
		for(int i=0; i<list.size(); i++)
		{
			Debug.d(TAG,"calculateBufsize list i="+i);
			TlkObject o = list.get(i);
			if(o.isTextObject())	//each text object take over 16*16/8 * length=32Bytes*length
			{
				Debug.d(TAG,"content="+o.mContent);
				DotMatrixFont font = new DotMatrixFont(DotMatrixFont.FONT_FILE_PATH+o.font+".txt");
				int l = font.getColumns();				
				length = (l*o.mContent.length()+o.x) > length?(l*o.mContent.length()+o.x):length;
			}
			else if(o.isPicObject()) //each picture object take over 32*32/8=128bytes
			{
				length = (o.x+128) > length?(o.x+128):length;
			}
		}
		return length;
	}
	
	/*
	 * make set param buffer
	 * 1.  Byte 2-3,param 00,	reserved
	 * 2.  Byte 4-5,param 01,	print speed, unit HZ,43kHZ for highest
	 * 3.  Byte 6-7, param 02,	delay,unit: 0.1mmm
	 * 13. Byte 8-9, param 03,	reserved
	 * 14. Byte 10-11, param 04,triger 00 00  on, 00 01 off
	 * 15. Byte 12-13, param 05,sync  00 00  on, 00 01 off
	 * 16. Byte 14-15, param 06
	 * 17. Byte 16-17, param 07, length, unit 0.1mm
	 * 18. Byte 18-19, param 08, timer, unit ms
	 * 19. Byte 20-21, param 09, print head Temperature
	 * 20. Byte 20-21, param 10,  Ink cartridge Temperature
	 * 21. others reserved  
	 */
	public static void makeParams(Context context, byte[] params)
	{
		if(params==null || params.length<128)
		{
			Debug.d(TAG,"params is null or less than 128, realloc it");
			params = new byte[128];
		}
		SharedPreferences preference = context.getSharedPreferences(SettingsTabActivity.PREFERENCE_NAME, 0);
		int speed = preference.getInt(SettingsTabActivity.PREF_PRINTSPEED, 0);
		params[2] = (byte) ((speed>>8)&0xff);
		params[3] = (byte) ((speed)&0xff);
		int delay = preference.getInt(SettingsTabActivity.PREF_DELAY, 0);
		params[4] = (byte) ((delay>>8)&0xff);
		params[5] = (byte) ((delay)&0xff);
		int triger = (int)preference.getLong(SettingsTabActivity.PREF_TRIGER, 0);
		params[8] = 0x00;
		params[9] = (byte) (triger==0?0x00:0x01);
		int encoder = (int)preference.getLong(SettingsTabActivity.PREF_ENCODER, 0);
		params[10] = 0x00;
		params[11] = (byte) (encoder==0?0x00:0x01);
		int bold = preference.getInt(SettingsTabActivity.PREF_BOLD, 0);
		params[12] = (byte) ((bold>>8)&0xff);
		params[13] = (byte) ((bold)&0xff);
		int fixlen = preference.getInt(SettingsTabActivity.PREF_FIX_LEN, 0);
		params[14] = (byte) ((fixlen>>8)&0xff);
		params[15] = (byte) ((fixlen)&0xff);
		int fixtime= preference.getInt(SettingsTabActivity.PREF_FIX_TIME, 0);
		params[16] = (byte) ((fixtime>>8)&0xff);
		params[17] = (byte) ((fixtime)&0xff);
		int headtemp = preference.getInt(SettingsTabActivity.PREF_HEAD_TEMP, 0);
		params[18] = (byte) ((headtemp>>8)&0xff);
		params[19] = (byte) ((headtemp)&0xff);
		int resvtemp = preference.getInt(SettingsTabActivity.PREF_RESV_TEMP, 0);
		params[20] = (byte) ((resvtemp>>8)&0xff);
		params[21] = (byte) ((resvtemp)&0xff);
		int fontwidth = preference.getInt(SettingsTabActivity.PREF_FONT_WIDTH, 0);
		params[22] = (byte) ((fontwidth>>8)&0xff);
		params[23] = (byte) ((fontwidth)&0xff);
		int dots = preference.getInt(SettingsTabActivity.PREF_DOT_NUMBER, 0);
		params[24] = (byte) ((dots>>8)&0xff);
		params[25] = (byte) ((dots)&0xff);
		int resv12 = preference.getInt(SettingsTabActivity.PREF_RESERVED_12, 0);
		params[26] = (byte) ((resv12>>8)&0xff);
		params[27] = (byte) ((resv12)&0xff);
		int resv13 = preference.getInt(SettingsTabActivity.PREF_RESERVED_13, 0);
		params[28] = (byte) ((resv13>>8)&0xff);
		params[29] = (byte) ((resv13)&0xff);
		int resv14 = preference.getInt(SettingsTabActivity.PREF_RESERVED_14, 0);
		params[30] = (byte) ((resv14>>8)&0xff);
		params[31] = (byte) ((resv14)&0xff);
		int resv15 = preference.getInt(SettingsTabActivity.PREF_RESERVED_15, 0);
		params[32] = (byte) ((resv15>>8)&0xff);
		params[33] = (byte) ((resv15)&0xff);
		int resv16 = preference.getInt(SettingsTabActivity.PREF_RESERVED_16, 0);
		params[34] = (byte) ((resv16>>8)&0xff);
		params[35] = (byte) ((resv16)&0xff);
		int resv17 = preference.getInt(SettingsTabActivity.PREF_RESERVED_17, 0);
		params[36] = (byte) ((resv17>>8)&0xff);
		params[37] = (byte) ((resv17)&0xff);
		int resv18 = preference.getInt(SettingsTabActivity.PREF_RESERVED_18, 0);
		params[38] = (byte) ((resv18>>8)&0xff);
		params[39] = (byte) ((resv18)&0xff);
		int resv19 = preference.getInt(SettingsTabActivity.PREF_RESERVED_19, 0);
		params[40] = (byte) ((resv19>>8)&0xff);
		params[41] = (byte) ((resv19)&0xff);
		int resv20 = preference.getInt(SettingsTabActivity.PREF_RESERVED_20, 0);
		params[42] = (byte) ((resv20>>8)&0xff);
		params[43] = (byte) ((resv20)&0xff);
		int resv21 = preference.getInt(SettingsTabActivity.PREF_RESERVED_21, 0);
		params[44] = (byte) ((resv21>>8)&0xff);
		params[45] = (byte) ((resv21)&0xff);
		int resv22 = preference.getInt(SettingsTabActivity.PREF_RESERVED_22, 0);
		params[46] = (byte) ((resv22>>8)&0xff);
		params[47] = (byte) ((resv22)&0xff);
		int resv23 = preference.getInt(SettingsTabActivity.PREF_RESERVED_23, 0);
		params[48] = (byte) ((resv23>>8)&0xff);
		params[49] = (byte) ((resv23)&0xff);
		
	}
	
	/**
	 * the loading dialog
	 */
	public ProgressDialog mLoadingDialog;
	public Thread mProgressThread;
	public boolean mProgressShowing;
	public void progressDialog()
	{
		if (mProgressShowing || (mLoadingDialog != null && mLoadingDialog.isShowing())) {
			return;
		}
		mLoadingDialog = ProgressDialog.show(mContext, "", getResources().getString(R.string.strLoading), true,false);
		Debug.d(TAG, "===>show loading");
		mProgressShowing = true;
		mProgressThread = new Thread(){
			
			@Override
			public void run(){
				
				try{
					for(;mProgressShowing==true;)
					{
						Thread.sleep(2000);
					}
					Debug.d(TAG, "===>dismiss loading");
					mHandler.sendEmptyMessage(MESSAGE_DISMISS_DIALOG);
				}catch(Exception e)
				{
					
				}
			}
		};
		mProgressThread.start();
	}
	
	public void dismissProgressDialog()
	{
		mProgressShowing=false;
	}
	
	public int currentRfid = 0;
	@Override
	public void onClick(View v) {
		
		// ExtGpio.playClick();
		switch (v.getId()) {
			case R.id.StartPrint:
				
				mHandler.sendEmptyMessage(MESSAGE_PRINT_CHECK_UID);
				//MessageForPc mfp = new MessageForPc(mContext, "/mnt/usbhost1/MSG1/1/1.TLK", "5");
				//mfp.reCreate(mContext);
				break;
			case R.id.StopPrint:
				// mHandler.removeMessages(MESSAGE_PAOMADENG_TEST);
					mHandler.sendEmptyMessage(MESSAGE_PRINT_STOP);
				break;
			/*濞撳懏绀傞幍鎾冲祪婢惰揪绱欐稉锟芥稉顏嗗濞堝﹦娈戦幍鎾冲祪娴犺濮熼敍澶涚礉闂囷拷鐟曚礁宕熼悪顒傛畱鐠佸墽鐤嗛敍姘棘閺侊拷2韫囧懘銆忔稉锟� 4閿涘苯寮弫锟�4娑擄拷200閿涳拷 閸欏倹鏆�5娑擄拷20閿涳拷*/
			case R.id.btnFlush:
				DataTransferThread thread = DataTransferThread.getInstance();
				thread.purge(mContext);
				break;
			case R.id.btnBinfile:
				MessageBrowserDialog dialog = new MessageBrowserDialog(mContext);
				dialog.setOnPositiveClickedListener(new OnPositiveListener() {
					
					@Override
					public void onClick() {
						String f = MessageBrowserDialog.getSelected();
						if (f==null || f.isEmpty()) {
							return;
						}
						Message msg = mHandler.obtainMessage(MESSAGE_OPEN_TLKFILE);

						Bundle bundle = new Bundle();
						bundle.putString("file", f);
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}

					@Override
					public void onClick(String content) {
						// TODO Auto-generated method stub
						
					}
					
				});
				dialog.show();
				break;
			case R.id.btn_page_forward:
				mScrollView.smoothScrollBy(-400, 0);
				break;
			case R.id.btn_page_backward:
				mScrollView.smoothScrollBy(400, 0);
				break;
			default:
				break;
		}
		
	}

	@Override
	public void onInkLevelDown() {
		mHandler.sendEmptyMessage(MESSAGE_INKLEVEL_CHANGE);
	}

	@Override
	public void onInkEmpty() {
		
	}

	@Override
	public void onCountChanged() {
		mHandler.sendEmptyMessage(MESSAGE_COUNT_CHANGE);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch(view.getId()) {
			case R.id.StartPrint:
			case R.id.StopPrint:
			case R.id.btnFlush:
			case R.id.btnBinfile:
			case R.id.btn_page_forward:
			case R.id.btn_page_backward:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					PWMAudio.Play();
				}
			default:
				break;
		}
		return false;
	}
	
	
	public void setCallback(Handler callback) {
		mCallback = callback;
	}
	
	public void onConfigChange() {
		if (mDTransThread == null) {
			return;
		}
		mDTransThread.refreshCount();
		refreshCount();
	}
	
	@Override
	public void OnFinished(int code) {
		Debug.d(TAG, "--->onFinished");
		mHandler.sendEmptyMessage(MESSAGE_PRINT_STOP);
		this.getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mContext, R.string.str_barcode_end, Toast.LENGTH_LONG).show();	
			}
		});
		
	}
	
	//Soect_____________________________________________________________________________________________________________________________
			//通讯 开始
			private void SocketBegin()
			{
				//Net = new Network();
				int nRet = 0;
			//	if (!Net.checkNetWork(mContext)) {
				//	Toast.makeText(mContext, "没有开启网络...!", Toast.LENGTH_LONG).show();
				//	return;
			//	}
				hostip = getLocalIpAddress(); //获取本机
				
				
				ServerThread serverThread=new ServerThread();
				//flag=true;
				serverThread.start();//线程开始
				

				
		//接收线程处理
			myHandler =new Handler(){	
			public void handleMessage(Message msg)
				{ 
					if(msg.what==0x1234)
					{
						 String ss=msg.obj.toString();
						// RecInfo(msg.obj.toString());
					}
					else
					{
					 String ss=msg.obj.toString();
					}
				}
				};
			}
			public static String toStringHex(String s) {  
			    byte[] baKeyword = new byte[s.length() / 2];  
			    for (int i = 0; i < baKeyword.length; i++) {  
			        try {  
			            baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));  
			        } catch (Exception e) {  
			            
			        }  
			    }  
			    try {  
			        s = new String(baKeyword, "utf-8");// UTF-16le:Not  
			    } catch (Exception e1) {  
			         
			    }  
			    return s;  
			}  
			//这个函数根据命令处理____________________________________________________________________________________
			/*
			private void RecInfo(String relus)
			{
				String msg= toStringHex(relus);
				String sValues;
				if(msg.indexOf("|")<=0)
				{
					return;
				}
				sValues=msg.substring(msg.indexOf("|")+1).substring(msg.indexOf("|")+1).substring(0, msg.substring(msg.indexOf("|")+1).indexOf("|")-1);
				switch(Integer.parseInt(sValues))
				{
				case 100:
				{
					mObjPath=getSDPath()+ msg.substring(msg.indexOf("/")+1, msg.lastIndexOf("/"));
					mHandler.sendEmptyMessage(MESSAGE_PRINT_START);
				
					
					break;
				}
				case 200:
				{
					refreshInk();
					break;
				}
			
			
				case 500:
				{
					if (mDTransThread != null && mDTransThread.isRunning()) {
						mHandler.sendEmptyMessage(MESSAGE_PRINT_STOP);
					}
					break;
				}
				case 600:
				{
					Commands="600";
					msg = msg.replace("|", " ");
					String[] strArray = msg.split("\\s");
					StrInfo_Stack.push(strArray[3]);//用堆栈存储收的信息，先进称出;
					
					
					for (Map.Entry<String, String> entry : obtainSimpleInfo(mContext).entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						sb.append(key).append(" = ").append(value).append("\n");
					}  
					sb.append("IP").append(" = ").append(getLocalIpAddress()).append("\n");
					
					break;
				}
				default:
				}
			}*/
			//_______________________________________________________________________________________________________________
			//获取本机地址
			public static String getLocalIpAddress() {  
			        try {  
			            for (Enumeration<NetworkInterface> en = NetworkInterface  
			                            .getNetworkInterfaces(); en.hasMoreElements();) {  
			                        NetworkInterface intf = en.nextElement();  
			                       for (Enumeration<InetAddress> enumIpAddr = intf  
			                                .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
			                            InetAddress inetAddress = enumIpAddr.nextElement();  
			                            if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {  
			                            return inetAddress.getHostAddress().toString();  
			                            }  
			                       }  
			                    }  
			                } catch (SocketException ex) {  
			                    Log.e("WifiPreference IpAddress", ex.toString());  
			                }  
			             return null; 
			 }

			
			   
			//Server服务
		    class ServerThread extends Thread {  
		          
		        public void stopServer(){  
		            try {                
		                if(server!=null){                   
		                	server.close();  
		                    System.out.println("close task successed");    
		                }  
		            } catch (IOException e) {               
		                System.out.println("close task failded");          
		                }  
		        }  
		    public void run() {  
		              
		                try {  
		                	server = new ServerSocket(PORT);  
		                } catch (IOException e1) {  
		                    // TODO Auto-generated catch block  
		                    System.out.println("S2: Error");  
		                    e1.printStackTrace();  
		                }  
		                mExecutorService = Executors.newCachedThreadPool();  //鍒涘缓涓?涓嚎绋嬫睜  
		                System.out.println("鏈嶅姟鍣ㄥ凡鍚姩...");  
		                Socket client = null;  
		                while(flag) {  
		                    try {  
		                        System.out.println("S3: Error");  
		                    client = server.accept(); 
		                    //client.setSoTimeout(5000);
		                 //   System.out.println("S4: Error");  
		                    //鎶婂鎴风鏀惧叆瀹㈡埛绔泦鍚堜腑  
		                    mList.add(client);  
		                    mExecutorService.execute(new Service(client)); //鍚姩涓?涓柊鐨勭嚎绋嬫潵澶勭悊杩炴帴  
		                     }catch ( IOException e) {  
		                         System.out.println("S1: Error");  
		                        e.printStackTrace();  
		                    }  
		                }  
		             
		              
		        }  
		    }      
		    
		   
		    //线程池，子线程
		    class Service implements Runnable {  
		         private volatile boolean kk=true;  
		      
		         private BufferedReader in = null;  
		         private String msg = "";  
		           
		         public Service(Socket socket) {  
		        	 Gsocket = socket;  
		             try {  
		                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
		                 
		         		 	 
		         		//map=obtainSimpleInfo(mContext); 
		         		//msg=map.toString();
		         		
		                 //this.sendmsg(Querydb.QuerySqlData("select * from System"));  
		                 this.sendmsg("connected success!!!");  
		             } catch (IOException e) {  
		                 e.printStackTrace();  
		             }  
		               
		         }  
		  
		         public void run() {  
		               
		                 while(kk) {  
		                     try {  
		                        if((msg = in.readLine())!= null) {  
		                             //100是打印  
		                          //	msg= toStringHex(msg);  
		                            if(msg.indexOf("100")>=0) { 
		                            	if(PrinterFlag==0)
		                            	{
		                            		PrnComd="100";
		                            	    PrinterFlag=1;
		                            		StopFlag=1;
		                            		CleanFlag=0;
		                            		String[] Apath = msg.split("\\|");
		                                 	mObjPath= Apath[3];
		                                 	int nRet=Paths.ListDirFiles( Apath[3]);
		                                 	//if(nRet==1)
		                                 	//{
		                                 	Message msg = mHandler.obtainMessage(MESSAGE_OPEN_TLKFILE);
		                                 	Bundle bundle = new Bundle();
		         							bundle.putString("file", mObjPath);  // f表示信息名称
		         							msg.setData(bundle);
		         							mHandler.sendMessage(msg);
		         							msg = mHandler.obtainMessage(MESSAGE_OPEN_TLKFILE);
		         							this.sendmsg(msg+"recv success!");
		                                 	//}
		                                 //	else
		                                 	//{
		                                 		//this.sendmsg(msg+"recv success!");
		                                 	//}
		                            	}
		                            } 
		                            else if(msg.indexOf("200")>=0)
		                            {
		                            	//200是清洗
		                            	
		                            		CleanFlag=1;
		                            	DataTransferThread thread = DataTransferThread.getInstance();
		                				thread.purge(mContext);
		                				this.sendmsg(msg+"recv success!");
		                            	
		                            }
		                            else if(msg.indexOf("300")>=0)
		                            {
		                            	//300发文件
		                            	AddPaths="";
		                            	if(SendFileFlag==0)
		                            	{
		                            		SendFileFlag=1;
		                            	this.sendmsg(WriteFiles(Gsocket,msg));
		                            	}
		                           
		                            }
		                            else if(msg.indexOf("400")>=0)
		                            {
		                            	//400取计数器
		                            	for(int i=0;i<7;i++)
		                            	{
		                            	sendmsg(mCounter+" |\r\nink|"+mRfidManager.getLocalInk(i)+"|\r\n"+mMsgTask.getName()+"|\r\n");
		                            	this.sendmsg(msg+"recv success!");
		                            	}
		                            }
		                            else if(msg.indexOf("500")>=0)
		                            {
		                            	//500停止打印
		                            	if(StopFlag==1)
		                            	{
		                            		StopFlag=0;
		                            		PrinterFlag=0;
		                            	mHandler.sendEmptyMessage(MESSAGE_PRINT_STOP);
		                            	this.sendmsg(msg+"recv success!");
		                            	
		                            	}
		                            }
		                            else if(msg.indexOf("600")>=0)
		                            {
		                           //600字符串长成所需文件
		                    			String[] strArray = msg.split("\\|");
		                    			
		                    			StrInfo_Stack.push(strArray[3]);//用堆栈存储收的信息，先进称出;
		                    			/*MessageForPc message = new MessageForPc(mContext,strArray[3]);
		                    			TextObject text = new TextObject(mContext, strArray[3].length());
		                    			message.insert(text);
		                    			message.save();*/
		                    			this.sendmsg(msg+"recv success!");
		                            }
		                            else if(msg.indexOf("700")>=0)
		                            {
		                           //600字符串长成所需文件
		                    			//String[] strArray = msg.split("\\|");
		                    			
		                    			//StrInfo_Stack.push(strArray[3]);//用堆栈存储收的信息，先进称出;
		                    			/*MessageForPc message = new MessageForPc(mContext,strArray[3]);
		                    			TextObject text = new TextObject(mContext, strArray[3].length());
		                    			message.insert(text);
		                    			message.save();*/
		                            	MakeTlk(msg);
		                    			this.sendmsg(msg+"recv success!");
		                            }
		                            else if(msg.indexOf("800")>=0)
		                            {
		                           //600字符串长成所需文件
		                    			//String[] strArray = msg.split("\\|");
		                    			
		                    			//StrInfo_Stack.push(strArray[3]);//用堆栈存储收的信息，先进称出;
		                    			/*MessageForPc message = new MessageForPc(mContext,strArray[3]);
		                    			TextObject text = new TextObject(mContext, strArray[3].length());
		                    			message.insert(text);
		                    			message.save();*/
		                            	deleteFile(msg);
		                    			this.sendmsg(msg+"Delete success!");
		                            }
		                            else if(msg.indexOf("900")>=0)
		                            {
		                           //600字符串长成所需文件
		                    			//String[] strArray = msg.split("\\|");
		                    			
		                    			//StrInfo_Stack.push(strArray[3]);//用堆栈存储收的信息，先进称出;
		                    			/*MessageForPc message = new MessageForPc(mContext,strArray[3]);
		                    			TextObject text = new TextObject(mContext, strArray[3].length());
		                    			message.insert(text);
		                    			message.save();*/
		                            	deleteDirectory(msg);
		                    			this.sendmsg(msg+"Delete success!");
		                            }
		                            else {  
		                                 Message msgLocal = new Message();  
		                                 msgLocal.what = 0x1234;  
		                                 msgLocal.obj =msg+"" ;  
		                                 System.out.println(msgLocal.obj.toString());  
		                                 System.out.println(msg);  
		                                 myHandler.sendMessage(msgLocal);  
		                               
		                                 this.sendmsg(msg+"command error or Execution execution");  
		                                    }  
		                                          
		                                 }  
		                 } catch (IOException e) {  
		                        System.out.println("close");  
		                        kk=false;  
		                        // TODO Auto-generated catch block  
		                        e.printStackTrace(); 
		                        this.sendmsg(msg+"Socket fail");
		                        return;
		                    }  
		                     
		                 }  
		                         
		             
		         }  
		         //向客户端发信息
		         public void sendmsg(String msg) {  
		            //System.out.println(msg);
		        
		             PrintWriter pout = null;  
		             try {  
		                 pout = new PrintWriter(new BufferedWriter(  
		                         new OutputStreamWriter(Gsocket.getOutputStream())),true);  
		                 pout.println(msg);  
		             }catch (IOException e) {  
		                 e.printStackTrace();  
		             }  
		      }  
		       

		}  
		//获取设备信息
		    private HashMap<String, String> obtainSimpleInfo(Context context){
				//HashMap<String, String> map = new HashMap<String, String>();
				PackageManager mPackageManager = context.getPackageManager();
				PackageInfo mPackageInfo = null;
				try {
					mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				 PackageManager pm = mContext.getPackageManager();
			        try {
						pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			   
			       

			        
			 
				map.put("versionName", mPackageInfo.versionName);
				map.put("versionCode", "_" + mPackageInfo.versionCode);
				map.put("Build_version", "_" + Build.VERSION.RELEASE);
				
				map.put("CPU ABI", "_" + Build.CPU_ABI);
			    map.put("Vendor", "_" + Build.MANUFACTURER);
				map.put("MODEL", "_" + Build.MODEL);
				map.put("SDK_INT", "_" + Build.VERSION.SDK_INT);
				map.put("PRODUCT", "_" +  Build.PRODUCT);
				
				return map;
			}
			//接收信息，并写文件	
			private String WriteFiles(Socket socket,String msg ) {
				
		        if (socket == null)
		        {
		            return "";
		        }
		       
		        InputStream in=null; 
		        
		        try {
		            //
		 
		        	String savePath=msg.substring(msg.indexOf("/")+1,msg.lastIndexOf("/"));

		        	String[] Apath = savePath.split("\\/");
		        	 
		        	String TmpFiles=msg.substring(msg.lastIndexOf("/"));
		        	TmpFiles=TmpFiles.substring(TmpFiles.indexOf("/")+1,TmpFiles.indexOf("|"));

		        	String TmpsavePath= Paths.CreateDir(msg);
		        	       
		        
		        	savePath=TmpsavePath+TmpFiles;
				 InputStream inb=null; 
				 AddPaths="";
			        	inb = socket.getInputStream();
				    	
			        	
			        	
				FileOutputStream file = new FileOutputStream(savePath, false);
				
				byte[] buffer = new byte[8192];
				
				int size = -1;
				
				
				  while (true) {
		              int read = 0;
		              if (inb != null) {
		                  read = inb.read(buffer);
		              }
		              //passedlen += read;
		              if (read == -1) {
		                  break;
		              }
		              //下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
		              //System.out.println("文件接收了" +  (passedlen * 100/ len) + "%\n");
		              file.write(buffer, 0, read);
		          }
		         
				
				
				
				/*try{
				while ((size = inb.read(buffer)) != -1){
					file.write(buffer, 0 ,size);
				}
				}
				catch(Exception e)
				{
				file.close();
				}*/
				file.close();
				file.flush();
				//socket.close();
				//dataStream.close();
				//data.close();
				//SendMessage(0, "1188.rar" + "鎺ユ敹瀹屾垚");
				//socket.close();
			}catch(Exception e){
				//SendMessage(0, "鎺ユ敹閿欒:\n" + e.getMessage());
			}
		        SendFileFlag=0;
		 return "File Recv success";
	}
	private void MakeTlk(String msg)
	{
		String tlk =msg.substring(msg.indexOf("/"), msg.lastIndexOf("/"));
		String Name=tlk.substring(msg.indexOf("/"),tlk.lastIndexOf("/"));
		Name=Name.substring(Name.lastIndexOf("/")+1);
		tlk=tlk.replace("msg", "MSG");
				MessageForPc message = new MessageForPc(mContext, tlk,Name);
				message.reCreate(mContext);
	}
	public boolean deleteFile(String filePath) {
		//delete file
		//getPath2();
	    File file = new File(filePath.substring(filePath.indexOf("/"), filePath.lastIndexOf("/")));
	    //file.setExecutable(true,false); 
	   // file.setReadable(true,false); 
	    //file.setWritable(true,false);
	    if(file.exists()) {
	    if(file.isFile()){
	       file.delete();
	       System.gc();
	       return true; 
	        }
	       
	}
	    return false;
	}
	    /**
	     * 删除文件夹以及目录下的文件
	     * @param   filePath 被删除目录的文件路径
	     * @return  目录删除成功返回true，否则返回false
	     */
	    public boolean deleteDirectory(String filePath) {
	    boolean flag = false;
	        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
	    filePath=filePath.substring(filePath.indexOf("/"), filePath.lastIndexOf("/"));
	        if (!filePath.endsWith(File.separator)) {
	            filePath = filePath + File.separator;
	        }
	        File dirFile = new File(filePath);
	        if (!dirFile.exists() || !dirFile.isDirectory()) {
	            return false;
	        }
	        flag = true;
	        File[] files = dirFile.listFiles();
	        //遍历删除文件夹下的所有文件(包括子目录)
	        for (int i = 0; i < files.length; i++) {
	            if (files[i].isFile()) {
	            //删除子文件
	                flag = DeleteFolderFile(files[i].getAbsolutePath());
	                if (!flag) break;
	            } else {
	            //删除子目录
	                flag = deleteDirectory(files[i].getAbsolutePath());
	                if (!flag) break;
	            }
	        }
	        if (!flag) return false;
	        //删除当前空目录
	        return dirFile.delete();
	    }

	    /**
	     *  根据路径删除指定的目录或文件，无论存在与否
	     *@param filePath  要删除的目录或文件
	     *@return 删除成功返回 true，否则返回 false。
	     */
	    public boolean DeleteFolder(String filePath) {
	    File file = new File(filePath);
	        if (!file.exists()) {
	            return false;
	        } else {
	            if (file.isFile()) {
	            // 为文件时调用删除文件方法
	                return DeleteFolderFile(filePath);
	            } else {
	            // 为目录时调用删除目录方法
	                return deleteDirectory(filePath);
	            }
	        }
	    }
	    public boolean  DeleteFolderFile(String filePath) {
	    	//delete file
	    	//getPath2();
	        File file = new File(filePath);
	        //file.setExecutable(true,false); 
	       // file.setReadable(true,false); 
	        //file.setWritable(true,false);
	        if(file.exists()) {
	        if(file.isFile()){
	           file.delete();
	           System.gc();
	           return true; 
	            }
	           
	    }
	        return false;
	    }
	    public String getPath2() {
			String sdcard_path = null;
			String sd_default = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			Log.d("text", sd_default);
			if (sd_default.endsWith("/")) {
				sd_default = sd_default.substring(0, sd_default.length() - 1);
			}
			// 得到路径
			try {
				Runtime runtime = Runtime.getRuntime();
				Process proc = runtime.exec("mount");
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				String line;
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					if (line.contains("secure"))
						continue;
					if (line.contains("asec"))
						continue;
					if (line.contains("fat") && line.contains("/mnt/")) {
						String columns[] = line.split(" ");
						if (columns != null && columns.length > 1) {
							if (sd_default.trim().equals(columns[1].trim())) {
								continue;
							}
							sdcard_path = columns[1];
						}
					} else if (line.contains("fuse") && line.contains("/mnt/")) {
						String columns[] = line.split(" ");
						if (columns != null && columns.length > 1) {
							if (sd_default.trim().equals(columns[1].trim())) {
								continue;
							}
							sdcard_path = columns[1];
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d("text", sdcard_path);
			return sdcard_path;
		}

	    public void onComplete() {
			String msg=mCounter+" \r\nink"+mRfidManager.getLocalInk(0)+"\r\n"+mMsgTask.getName()+"\r\n";
			
			 PrintWriter pout = null;  
	         try {  
	             pout = new PrintWriter(new BufferedWriter(  
	                     new OutputStreamWriter(Gsocket.getOutputStream())),true);  
	             pout.println(msg);  
	         }catch (IOException e) {  
	             e.printStackTrace();  
	         }  
		}
	//Socket________________________________________________________________________________________________________________________________
	}

