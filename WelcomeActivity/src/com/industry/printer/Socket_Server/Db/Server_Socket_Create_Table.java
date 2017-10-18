package com.industry.printer.Socket_Server.Db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Server_Socket_Create_Table extends SQLiteOpenHelper {
	private static final int VERSION = 1;

	public Server_Socket_Create_Table(Context context) {
		super(context, Server_Socket_Database.DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase DbDatabase) {
		DbDatabase.execSQL("CREATE TABLE  IF NOT EXISTS " + "device_info" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ "device_id" + " TEXT,"
        		+ "device_counts" + " TEXT,"
        		+ "device_ink" + " TEXT,"
                + "device_ip" + " TEXT," 
                + "device_port" + " TEXT"
                + ");");
	/*DbDatabase.execSQL("CREATE TABLE  IF NOT EXISTS " + "User" + " (" + "_id"
            + " INTEGER PRIMARY KEY AUTOINCREMENT," 
    		+ "User_Account" + " TEXT,"
            + "User_Password" + " TEXT," 
            + "User_Date" + " TEXT,"
            + "User_Flag" + " TEXT" 
            + ");");*/
	//2014.11.3MAJINXIN修改
	/*DbDatabase.execSQL("CREATE TABLE  IF NOT EXISTS " + "Bank" + " (" + "_id"
            + " INTEGER PRIMARY KEY AUTOINCREMENT," 
    		+ "B_ISSHOW" + " TEXT,"	
            + "B_NAME" + " TEXT," 
            + "B_QUICK" + " TEXT,"
            + "B_FLAG" + " TEXT," 
            + "B_CODE" + " TEXT"
            + ");");*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	
	
}
