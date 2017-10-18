package com.industry.printer.ui.CustomerDialog;
 

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
 



import com.industry.printer.R;
import com.industry.printer.Utils.ConfigPath;
import com.industry.printer.Utils.Debug;
import com.industry.printer.Utils.PlatformInfo;
import com.industry.printer.Utils.StringUtil;
import com.industry.printer.object.TLKFileParser;
import com.industry.printer.ui.CustomerAdapter.ListViewButtonAdapter;
import com.industry.printer.ui.CustomerAdapter.MessageListAdater;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageBrowserDialog extends CustomerDialogBase implements android.view.View.OnClickListener, OnItemClickListener, OnTouchListener, OnScrollListener, TextWatcher {

		private final String TAG = MessageBrowserDialog.class.getSimpleName();
		
		public RelativeLayout mConfirm;
		public RelativeLayout mCancel;
		public RelativeLayout mPagePrev;
		public RelativeLayout mPageNext;
		public TextView 	mDelete;
		public ImageView 	mLoading;
		public RelativeLayout 	mLoadingLy;
		
		private Animation mOperating;
		
		
		public EditText		  mSearch;
		public static String mTitle;
		
		public ListView mMessageList;
		public View mVSelected;
		
		public boolean isTop;
		public boolean isBottom;
		
		public MessageListAdater mFileAdapter;
		public LinkedList<Map<String, Object>> mContent;
		public LinkedList<Map<String, Object>> mFilterContent;
		
		private static final int MSG_FILTER_CHANGED = 1;
		private static final int MSG_LOADED = 2;

		private static final int MSG_REF = 3;
		
		public Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_FILTER_CHANGED:
					Bundle bundle = msg.getData();
					String title = bundle.getString("title");
					filterAfter(title);
					break;
				case MSG_LOADED:
					mMessageList.setAdapter(mFileAdapter);
					mFileAdapter.notifyDataSetChanged();
					hideLoading();
					break;
				case MSG_REF:
					mMessageList.setAdapter(mFileAdapter);
					mFileAdapter.notifyDataSetChanged();	
					break;
				default:
					break;
				}
			}
		};
		
		public MessageBrowserDialog(Context context) {
			super(context, R.style.Dialog_Fullscreen);
			
			mVSelected = null;
			mContent = new LinkedList<Map<String, Object>>();
			mFilterContent = new LinkedList<Map<String, Object>>();
			isTop = false;
			isBottom = false;
			mFileAdapter = new MessageListAdater(context, 
					mContent, 
					R.layout.message_item_layout, 
					new String[]{"title", "abstract", ""}, 
					// new int[]{R.id.tv_message_title, R.id.tv_message_abstract
					new int[]{R.id.tv_msg_title, R.id.ll_preview, R.id.image_selected});
			
			mOperating = AnimationUtils.loadAnimation(context, R.anim.loading_anim);
			LinearInterpolator lin = new LinearInterpolator();  
			mOperating.setInterpolator(lin);
		}
		
		@Override
		 protected void onCreate(Bundle savedInstanceState) {
			Debug.d(TAG, "===>oncreate super");
			 super.onCreate(savedInstanceState);
			 Debug.d(TAG, "===>oncreate");
			 this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			 this.setContentView(R.layout.message_list_layout);
			 
			 mConfirm = (RelativeLayout) findViewById(R.id.btn_ok_message_list);
			 mConfirm.setOnClickListener(this);
			 
			 mCancel = (RelativeLayout) findViewById(R.id.btn_cancel_message_list);
			 mCancel.setOnClickListener(this);
			 
			 mDelete = (TextView) findViewById(R.id.btn_delete);
			 mDelete.setOnClickListener(this);
			 
			 mPagePrev = (RelativeLayout) findViewById(R.id.btn_page_prev);
			 mPagePrev.setOnClickListener(this);
			 
			 mPageNext = (RelativeLayout) findViewById(R.id.btn_page_next);
			 mPageNext.setOnClickListener(this);
			 
			 mSearch = (EditText) findViewById(R.id.et_search);
			 mSearch.addTextChangedListener(this);
			 mMessageList = (ListView) findViewById(R.id.message_listview);
			 mMessageList.setOnItemClickListener(this);
			 
			 mMessageList.setOnTouchListener(this);
			 mMessageList.setOnScrollListener(this);
			 
			 
			 mLoadingLy = (RelativeLayout) findViewById(R.id.loading);
			 mLoading = (ImageView) findViewById(R.id.loading_img);
			 loadMessages();
			 
			 setupViews();
			
		 }
		
		private void setupViews() {
			if (PlatformInfo.PRODUCT_FRIENDLY_4412.equals(PlatformInfo.getProduct())) {
				mPagePrev.setVisibility(View.GONE);
				mPageNext.setVisibility(View.GONE);
			}
		}
		
		

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
				case R.id.btn_ok_message_list:
					
					dismiss();
					if (pListener != null) {
						pListener.onClick();
					}
					break;
				case R.id.btn_cancel_message_list:
					dismiss();
					if (nListener != null) {
						nListener.onClick();
					}
					break;
				case R.id.btn_page_prev:
					mMessageList.smoothScrollBy(-200, 50);
					break;
				case R.id.btn_page_next:
					mMessageList.smoothScrollBy(200, 50);
					break;
				case R.id.btn_delete:
					mFileAdapter.delete();
					break;
			}
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Map<String, Object> selected = mContent.get(position);
			mFileAdapter.setSelected(position);
			mFileAdapter.notifyDataSetChanged();
			mTitle = (String) selected.get("title");
			//addbylk
		//	mMessageList.setAdapter(mFileAdapter);
			mFileAdapter.notifyDataSetChanged();
			/*
			if(mVSelected == null)
			{
				view.setBackgroundColor(R.color.message_selected_color);
				mVSelected = view;
			}
			else
			{
				mVSelected.setBackgroundColor(Color.WHITE);
				view.setBackgroundColor(R.color.message_selected_color);
				mVSelected = view;
			}*/
			
		}
		
		@SuppressWarnings("unchecked")
		public void loadMessages()
		{
			showLoading();
			mHandler.post(
					new Runnable() {
				
				@Override
				public void run() {
					TLKFileParser parser = new TLKFileParser(getContext(), null);
					String tlkPath = ConfigPath.getTlkPath();
					if (tlkPath == null) {
						return ;
					}
					Debug.d(TAG, "--->load message begin");
					File rootpath = new File(tlkPath);
					// File[] Tlks = rootpath.listFiles();
					String[] Tlks = rootpath.list();
					if (Tlks == null) {
						return ;
					}
					Arrays.sort( Tlks,
							     new Comparator() 
										{
									public int compare(Object arg0, Object arg1)
									{
										int cp1 = 0;
										int cp2 = 0;
										try {
									    	cp1 = Integer.parseInt((String) arg0);
									    	cp2 = Integer.parseInt((String) arg1);
									    } catch(NumberFormatException e) {
									    	e.printStackTrace();
									    }
									    if (cp1 > cp2) {
									    	return 1;
									    } else if(cp1 == cp2) {
									    	return 0;
									    }
									    return -1;
									}
								}
							);
					
					Debug.d(TAG, "--->load message sort ok");
					for (String t:Tlks) {
						
						File file = new File(tlkPath, t);
						if (!file.isDirectory()) {
							continue;
						}
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("title", t);
						mContent.add(map);
						mFilterContent.add(map);
					}
					// mMessageList.setAdapter(mFileAdapter);
					Debug.d(TAG, "--->load message load success");
					mHandler.sendEmptyMessage(MSG_LOADED);
				}
			}
					);
			
			
		}
		
		private void showLoading() {
			mLoadingLy.setVisibility(View.VISIBLE);
			mLoading.startAnimation(mOperating);
		}
		
		private void hideLoading() {
			mLoading.clearAnimation();
			mLoadingLy.setVisibility(View.GONE);
		}
		
		public static String getSelected() {
			if (mTitle == null) {
				return "";
			} else {
				return mTitle;
			}
		}

		float mdownx,mdowny;
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			
			float curX,curY;
			switch( arg1.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				Debug.e(TAG, "------ACTION_DOWN"+ arg1.getX());	
				mdownx=arg1.getX();
				mdowny=arg1.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				Debug.e(TAG, "------ACTION_MOVE"+ arg1.getX());					

				break;
			case MotionEvent.ACTION_UP:
				Debug.e(TAG, "------ACTION_UP"+ arg1.getX());					
				curX=arg1.getX();
				curY=arg1.getY();
						
				
				if(( Math.abs(curX-mdownx )  )>  (Math.abs(curY-mdowny ))  )//横向滑动
				{
					/*
					if( curX>mdownx )
					{
						mFileAdapter.Scroll(1);
					}
					else
					{
						mFileAdapter.Scroll(2);			
					}
					*/
					mFileAdapter.Scroll( (int)( mdownx-curX ) );			
					
				mHandler.sendEmptyMessage(MSG_LOADED);					
				}
				
				

				// mFileAdapter.Scroll(1);
			//	mFileAdapter.notifyDataSetChanged();
				break;			
			
			}
			
			return false;
		}

		@Override
		public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if(firstVisibleItem==0){
				Debug.e(TAG, "滑到顶部");
				isTop = true;
            } else {
            	isTop = false;
            }
			
			if(visibleItemCount+firstVisibleItem==totalItemCount){
            	Debug.e(TAG, "滑到底部");
            	isBottom = true;
            } else {
            	isBottom = false;
            }
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int state) {
			switch (state) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				Debug.d(TAG, "===>idle");
				
				break;
			case OnScrollListener.SCROLL_STATE_FLING:
				Debug.d(TAG, "===>fling");
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				Debug.d(TAG, "===>touch scroll");
				break;
			default:
				break;
			}
		}
		

		/**
		 * 過濾信息名匹配的
		 * @param filter
		 */
		public void filter(String filter) {
			mContent.clear();
			mFileAdapter.setSelected(-1);
			for (int i = 0; i < mFilterContent.size(); i++) {
				mContent.add(mFilterContent.get(i));
			}
			Debug.d(TAG, "mcontent.size=" + mContent.size());
			
			if (StringUtil.isEmpty(filter)) {
				mFileAdapter.notifyDataSetChanged();
				return;
			}
			
			for (int i = 0; i < mContent.size();) {
				HashMap<String, Object> item = (HashMap<String, Object>) mContent.get(i);
				String title = (String) item.get("title");
				Debug.d(TAG, "title=" + title);
				if (!title.startsWith(filter)) {
					Debug.d(TAG, "is match: " + title + ", filter: " + filter);
					mContent.remove(item);
				} else {
					i++;
				}
			}
			Debug.d(TAG, "mcontent.size=" + mContent.size());
			mMessageList.setAdapter(mFileAdapter);
			mFileAdapter.notifyDataSetChanged();
		}
		
		/**
		 * 過濾結果爲從第一個開始匹配的信息
		 * @param filter
		 */
		public void filterAfter(String filter) {
			mContent.clear();
			mFileAdapter.setSelected(-1);
			for (int i = 0; i < mFilterContent.size(); i++) {
				mContent.add(mFilterContent.get(i));
			}
			Debug.d(TAG, "mcontent.size=" + mContent.size());
			
			if (StringUtil.isEmpty(filter)) {
				mFileAdapter.notifyDataSetChanged();
				return;
			}
			
			for (int i = 0; i < mContent.size();) {
				HashMap<String, Object> item = (HashMap<String, Object>) mContent.get(i);
				String title = (String) item.get("title");
				Debug.d(TAG, "title=" + title);
				if (!title.startsWith(filter)) {
					Debug.d(TAG, "is match: " + title + ", filter: " + filter);
					mContent.remove(item);
				} else {
					break;
				}
			}
			Debug.d(TAG, "mcontent.size=" + mContent.size());
			mMessageList.setAdapter(mFileAdapter);
			mFileAdapter.notifyDataSetChanged();
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			String text = arg0.toString();
			Debug.d(TAG, "filter: " + text);
			mHandler.removeMessages(MSG_FILTER_CHANGED);
			Message msg = mHandler.obtainMessage(MSG_FILTER_CHANGED);
			Bundle bundle = new Bundle();
			bundle.putString("title", text);
			msg.setData(bundle);
			mHandler.sendMessageDelayed(msg, 2000);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			
		}
		
}
