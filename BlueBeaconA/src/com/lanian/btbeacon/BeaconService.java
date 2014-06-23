package com.lanian.btbeacon;

import java.io.IOException;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BeaconService extends Service implements Runnable {
	static final String SERVICE_NAME = "BlueBeacon";
	public static final UUID SERVICE_UUID = UUID.fromString("c9faf940-e20c-11e3-9ffa-0002a5d5c51b");
	
	Thread listenerThread;
	BluetoothServerSocket serverSocket;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(SERVICE_NAME, "onStartCommand");
		
		if (listenerThread == null)
			listenerThread = new Thread(this);
		
		if (!listenerThread.isAlive())
			listenerThread.start();
		
		return START_STICKY;
	}
	
	@Override
	public void run() {
		try {
			serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
			
			while (true) {
				Log.d(SERVICE_NAME, "BlueBeacon is waiting for a client");
				BluetoothSocket clientSocket = serverSocket.accept();
				Log.d(SERVICE_NAME, "A client is connected: "+clientSocket.getRemoteDevice().getAddress());
				clientSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(SERVICE_NAME, "onDestroy");
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}
}
