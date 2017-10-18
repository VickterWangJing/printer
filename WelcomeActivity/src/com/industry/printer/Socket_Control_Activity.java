package com.industry.printer;





import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.industry.printer.Socket_Server.ClientThread;
import com.industry.printer.Socket_Server.PrintInterface;
import com.industry.printer.Socket_Server.Db.Server_Socket_Database;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class Socket_Control_Activity extends Activity implements PrintInterface{
	private Button ButBack;
	private ListView Grid;
	ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
	HashMap<String, Object> map = new HashMap<String, Object>();

	public static String commands; 
    private String device_id="0";
	
	TextView textView;
	
	EditText editText;
	
	Button btn;
	
	
	 private Server_Socket_Database Db;
     private Cursor Cr;
     private AlertDialog dialog;
     private EditText Ip_Add;
     private EditText Ports;
     
     private  TextView Number;
     private TextView Counts;
     private TextView Ink;
     private Button Print;
     private Button Stop;
     private TextView Purge;
     private Button Set;
     public static Button Connect;
     String InsertSql;
     String QuerySql;
     private String rxTextString = "";
 	private ClientThread rxListenerThread;
 	private Message message;
 	public static Handler mainHandler;
 	
	public static String sIP;
	public static String sPORT;
    
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.socket_control_activity);
		Grid = (ListView)findViewById(R.id.grid);
		
		Db=new Server_Socket_Database(this);
		
		Fill();
		
	
		
	
		
		ButBack=(Button)findViewById(R.id.ButBack);
		ButBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		

		
	
		
		Socket_Control_Activity_Item adapter = new Socket_Control_Activity_Item(Socket_Control_Activity.this, list);
		Grid.setAdapter(adapter);
		
		Grid.setOnItemClickListener(new OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> arg0, View view, int arg2,
	                long arg3) {
	            TextView tv=(TextView)view.findViewById(R.id.Print);
	            TextView st=(TextView)view.findViewById(R.id.Stop);
	            Toast.makeText(getBaseContext(), "您点击了第" + tv.getText()+"行"+arg2+"行"+arg3+"列", 2000).show();
	        }
	    });
	
	}
	
	public static void setListViewFullHeight(ListView lv, BaseAdapter adapter) {
		int h = 0;
		final int cnt = adapter.getCount();
		for (int i = 0; i < cnt; i++) {
			View item = adapter.getView(i, null, lv);
			item.measure(0, 0);
			h += item.getMeasuredHeight();
		}
		ViewGroup.LayoutParams lp = lv.getLayoutParams();
		lp.height = h + (lv.getDividerHeight() * (cnt - 1));
		lv.setLayoutParams(lp);
	}

	void initMainHandler(){
		 
		 mainHandler = new Handler() {
			 
	            @Override
	            /**
	             * 主线程消息处理中心
	             */
	            public void handleMessage(Message msg) {
	                
	                // 接收子线程的消息
	                
	            }
	 
	        };
		
		
	}
void Fill()
{
	for (int i = 0; i < 5; i++) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Number", ""+i);
		map.put("Counts", "100");
		map.put("Ink", "1000");
		map.put("Print", "Print");
		map.put("Stop", "Stop");
		map.put("Purge", "Purge");
		map.put("Set", "Set");
		
		list.add(map);
		
	}
}
public class Socket_Control_Activity_Item extends BaseAdapter{
	
	Activity mConext;
	private final LayoutInflater layoutFlater;
	private List<HashMap<String, Object>> list;
	
	public Socket_Control_Activity_Item(Activity context,List<HashMap<String, Object>> mList){
		mConext = context;
		layoutFlater = (LayoutInflater) mConext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		list = mList;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		
		if(convertView == null){
			convertView = layoutFlater.inflate(R.layout.socket_control_activity_item, null);
			
			Number = (TextView)convertView.findViewById(R.id.Number);
			Counts = (TextView)convertView.findViewById(R.id.Counts);
			Ink = (TextView)convertView.findViewById(R.id.Ink);
			Print = (Button)convertView.findViewById(R.id.Print);
			Stop = (Button)convertView.findViewById(R.id.Stop);
			Purge = (TextView)convertView.findViewById(R.id.Purge);
			Set = (Button)convertView.findViewById(R.id.Set);
			Connect= (Button)convertView.findViewById(R.id.Connect);
		
			convertView.setTag(convertView);
		} else {
			convertView.getTag();
		}
		
		Number.setText((String)list.get(position).get("Number"));
		Counts.setText((String)list.get(position).get("Counts"));
		Ink.setText((String)list.get(position).get("Ink"));

		Stop.setText((String)list.get(position).get("Stop"));
		Purge.setText((String)list.get(position).get("Purge"));
		Set.setText((String)list.get(position).get("Set"));
		
		Print.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
			
						
						if (("1".equals(device_id))||(device_id.equals(Number.getText().toString())))
						{
							 commands="000B|0000|100|/4/|0|3702|0000|0|0000|0D0A";
							sendMessage(commands+"\n\r");//发送消息
						
					   }
						else
						{
							Toast.makeText(getBaseContext(), "Select Connect Button Please...", 2000).show();
							return;
						}
					
					
					
                  
                    Toast.makeText(getBaseContext(), "Printing，，，", 2000).show();
			}
		});
		
		Stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (("1".equals(device_id))||(device_id.equals(Number.getText().toString())))
				{
					 commands="000B|0000|500|0|0|0000|0|0000|0D0A";
					   sendMessage(commands+"\n\r");//发送消息
				
			   }
				else
				{
					Toast.makeText(getBaseContext(), "Select Connect Button Please...", 2000).show();
					return;
				}
			
			
				
          
					
					
			   Toast.makeText(getBaseContext(), "Stoped.", 2000).show();
                  
              
		
			}
		});
		
		
		Purge.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (("1".equals(device_id))||(device_id.equals(Number.getText().toString())))
				{
					 commands="000B|0000|200|0|0|0000|0|0000|0D0A";
					   sendMessage(commands+"\n\r");//发送消息
							
				
			   }
				else
				{
					Toast.makeText(getBaseContext(), "Select Connect Button Please...", 2000).show();
					return;
				}
			
			
				
					
              // 将用户在文本框内输入的内容写入网络  
                  
            
				Toast.makeText(getBaseContext(), "Purge...", 2000).show();

		
			}
		});
		
		Connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				device_id=Number.getText().toString();
				
				try  
                {  
					if("1".equals(device_id))
					{
					  onDestroy();
					}
					
					Cr=Db.AllData_SqlData("Select * from device_info where device_id='"+position+"'");
					Cr.moveToFirst();
					int nRet=Cr.getCount();
					if(nRet>0)
					{
						sIP=Cr.getString(Cr.getColumnIndex("device_ip"));
						sPORT=Cr.getString(Cr.getColumnIndex("device_port"));
						Cr.close();
						rxListenerThread = new ClientThread(Socket_Control_Activity.this);//建立客户端线程
						rxListenerThread.start();
						
					}
					else
					{
						Toast.makeText(getBaseContext(), "Set IP Address&Ports Please.", 2000).show();
						Cr.close();
						return;
						
					}
					
					
				
                }  
                catch (Exception e)  
                {  
                    e.printStackTrace(); 
                    Toast.makeText(getBaseContext(), "Connection failed.", 2000).show();
                    return ;
                }  
				device_id="1";
				Toast.makeText(getBaseContext(), "connected Success.", 2000).show();

		
			}
		});
		//弹出窗品，设备IP&PORT
		Set.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String conuts=Counts.getText().toString();
				String inks=Ink.getText().toString();
				inputTitleDialog(position,conuts,inks);
				
			}});
		return convertView;
	}
	
}
void sendMessage(String str){
	
	//通知客户端线程 发送消息
	message = ClientThread.childHandler.obtainMessage(0, str);
	ClientThread.childHandler.sendMessage(message);
	
	
}

public void inputTitleDialog(int position,String count,String inks) {
     dialog = new AlertDialog.Builder(this).create();  
     dialog.setView(LayoutInflater.from(this).inflate(R.layout.dialogshow_two_button, null));  
     dialog.show();  
    // dialog.getWindow().setContentView(R.layout.alert_dialog);  
     Button BtnSave = (Button) dialog.findViewById(R.id.BtnSave);  
     Button BtnCancel = (Button) dialog.findViewById(R.id.BtnCancel);  
       Ip_Add = (EditText) dialog.findViewById(R.id.Ip_Add);  
       Ports= (EditText) dialog.findViewById(R.id.Ports);  
       InsertSql = "insert into device_info values(null,'"+position+"','"+count+"','"+inks+"','";
       QuerySql = "update device_info set device_id ='"+position+"',device_ip='"+Ip_Add.getText().toString()+"',device_port='"+Ports.getText().toString()+"'"+" where device_id='"+position+"'";
     
     BtnSave.setOnClickListener(new OnClickListener() {  

         public void onClick(View arg0) {  
             String str = Ip_Add.getText().toString();  
             if (isNullEmptyBlank(str)) {  
            	 Ip_Add.setError("Ip address can not be empty"); 
            	 return;
             }
             String strPort = Ports.getText().toString();  
             if (isNullEmptyBlank(strPort)) {  
            	 Ip_Add.setError("The port can not be empty"); 
            	 return;
             }
            	 InsertSql +=Ip_Add.getText().toString()+"','"+Ports.getText().toString()+"');";
            	 Db.InsertData(InsertSql,QuerySql);
            	 dialog.dismiss();
              
              
         }  
     });  
     BtnCancel.setOnClickListener(new OnClickListener() {  

         public void onClick(View arg0) {  
             dialog.dismiss();  
         }  
     });  
		 
    
} 
private static boolean isNullEmptyBlank(String str) {  
    if (str == null || "".equals(str) || "".equals(str.trim()))  
        return true;  
    return false;  
} 

 



protected void onDestroy(){
	
	ClientThread.childHandler.sendMessage(ClientThread.childHandler.obtainMessage(1));
	
	super.onDestroy();
}

@Override
public void printf(String str) {
	// TODO Auto-generated method stub
	
}

}
