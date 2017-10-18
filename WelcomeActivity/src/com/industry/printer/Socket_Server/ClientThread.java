package com.industry.printer.Socket_Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.industry.printer.Socket_Control_Activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class ClientThread extends Thread {
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	private Socket socket;
	private SocketAddress socketAddress;
	public static Handler childHandler;
	private boolean key = true;
	PrintInterface printClass;
	private RxThread rxThread;

	public ClientThread(PrintInterface printClass) {

		this.printClass = printClass;
	}

	/**
	 * ����
	 */
	void connect() {
		key = true;
		socketAddress = new InetSocketAddress(Socket_Control_Activity.sIP
				.toString(), Integer.parseInt(Socket_Control_Activity.sPORT));
		socket = new Socket();

		try {

			//socket.connect(socketAddress, 5000);
			socket=new Socket(Socket_Control_Activity.sIP.toString(), Integer.parseInt(Socket_Control_Activity.sPORT));
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			printClass.printf("���ӳɹ�");

			
			
			rxThread = new RxThread();
			rxThread.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			printClass.printf("�޷����ӵ�������");

			// Log.d("Error", "����������ʧ��...");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block

		}

	}

	void initChildHandler() {

		// �����߳��д���Handler�����ʼ��Looper
		Looper.prepare();

		childHandler = new Handler() {
			/**
			 * ���߳���Ϣ��������
			 */
			public void handleMessage(Message msg) {

				// �������̼߳������̵߳���Ϣ������...
				switch (msg.what) {
				case 0:

					try {
						outputStream.write(((String) (msg.obj)).getBytes());
						outputStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;

				case 1:

					key = false;
					try {
						inputStream.close();
						outputStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					childHandler.getLooper().quit();// ������Ϣ����
					


					break;

				default:
					break;
				}

			}
		};

		// �������̵߳���Ϣ����
		Looper.loop();

	}

	public void run() {
		connect();
		initChildHandler();
		printClass.printf("��������Ͽ�����");

	}

	public class RxThread extends Thread {

		public void run() {

			//printClass.printf("���������߳�");
			byte[] buffer = new byte[1024];

			while (key) {

				try {
					int readSize = inputStream.read(buffer);
					if (readSize > 0) {
						String str = new String(buffer, 0, readSize);

						Log.d("Message:", str);
						printClass.printf("<< " + str);

					} else {

						inputStream.close();
						Log.d("error:", "close connect...");
						printClass.printf("��������Ͽ�����");
						break;

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				if (socket.isConnected())
					socket.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		}

	}

}