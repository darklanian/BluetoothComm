package com.lanian.btbeacon;

import java.io.IOException;
import java.lang.Thread.State;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BeaconService extends Service implements Runnable {
	static final String SERVICE_NAME = "BlueBeacon";
	public static final UUID SERVICE_UUID = UUID.fromString("c9faf940-e20c-11e3-9ffa-0002a5d5c51b");
	public static final String EXTRA_BT_ADDRESS = "extra_bt_address";
	
	public static final int MSG_HELLO = 1;
	
	Thread listenerThread;
	BluetoothServerSocket serverSocket;
	Vector<BluetoothSocket> clientSockets = new Vector<BluetoothSocket>();
	Messenger replyTo;
	
	static class SimpleHandler extends Handler {
		WeakReference<BeaconService> target;
		
		public SimpleHandler(BeaconService service) {
			target = new WeakReference<BeaconService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			boolean messageHandled = false;
			BeaconService service = target.get();
			if (service != null)
				messageHandled = service.handlerMessage(msg);
			
			if (!messageHandled)
				super.handleMessage(msg);
		}
	}
	
	Messenger messenger = new Messenger(new SimpleHandler(this));
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(SERVICE_NAME, "onBind");
		return messenger.getBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(SERVICE_NAME, "onUnbind");
		replyTo = null;
		return super.onUnbind(intent);
	}

	public boolean handlerMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO:
			this.replyTo = msg.replyTo;
			if (this.replyTo != null) {
				try {
					this.replyTo.send(Message.obtain(null, MainActivity.MSG_HELLO));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(SERVICE_NAME, "onStartCommand");
		
		if (listenerThread == null)
			listenerThread = new Thread(this);
		
		if (listenerThread.getState() == State.NEW) 
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
				clientSockets.add(clientSocket);
				startActivity(new Intent(this, MainActivity.class).putExtra(EXTRA_BT_ADDRESS, clientSocket.getRemoteDevice().getAddress()));
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
