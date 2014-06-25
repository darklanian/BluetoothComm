package com.lanian.btbeacon;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BeaconService extends Service implements Runnable, BeaconConnection.BeaconConnectionListener {
	static final String SERVICE_NAME = "BlueBeacon";
	public static final UUID SERVICE_UUID = UUID.fromString("c9faf940-e20c-11e3-9ffa-0002a5d5c51b");
	public static final String EXTRA_BT_ADDRESS = "extra_bt_address";
	
	public static final int MSG_HELLO = 1;
	public static final int MSG_SEND_MESSAGE = 2;
	public static final String MSG_DATA_ADDRESS = "address";
	public static final String MSG_DATA_MESSAGE = "message";
	
	BluetoothServerSocket serverSocket;
	Vector<BeaconConnection> connections = new Vector<BeaconConnection>();
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
		case MSG_SEND_MESSAGE:
			sendMessageTo(msg.getData());
			return true;
		}
		return false;
	}
	
	private boolean sendMessageTo(Bundle data) {
		String address = data.getString(MSG_DATA_ADDRESS);
		String message = data.getString(MSG_DATA_MESSAGE);
		
		BeaconConnection connection = null;
		for (BeaconConnection conn : connections) {
			if (conn.dev.getAddress().equals(address)) {
				connection = conn;
				break;
			}
		}
		if (connection == null) {
			connection = connect(address);
		}
		
		if (connection == null)
			return false;
		
		return connection.sendMessage(message);
	}

	@Override
	public void run() {
		try {
			serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
			
			while (true) {
				Log.d(SERVICE_NAME, "BlueBeacon is waiting for a client");
				BluetoothSocket clientSocket = serverSocket.accept();
				Log.d(SERVICE_NAME, "A client is connected: "+clientSocket.getRemoteDevice().getAddress());
				connections.add(new BeaconConnection(this, clientSocket, clientSocket.getRemoteDevice(), this));
				showChatView(clientSocket.getRemoteDevice().getAddress());
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(SERVICE_NAME, "Listening thread stopped.");
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(SERVICE_NAME, "onCreate");
		new Thread(this).start();
	}

	@Override
	public void onDestroy() {
		Log.d(SERVICE_NAME, "onDestroy");
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		disconnectAll();
		super.onDestroy();
	}
	
	private void disconnectAll() {
		synchronized (connections) {
			Log.d(SERVICE_NAME, String.format("disconnecting all connections...(%d)", connections.size()));
			for (BeaconConnection conn : connections) {
				conn.disconnect();
			}
		}
	}

	@Override
	public void onDisconnected(BeaconConnection conn) {
		synchronized (connections) {
			Log.d(SERVICE_NAME, "onDisconnected: "+conn.getRemoteAddress());
			if (!connections.remove(conn)) 
				Log.e(SERVICE_NAME, "onDisconnected: disconnected connection is not found in connection list");
			Log.d(SERVICE_NAME, "onDisconnected: number of remaining connection(s) = "+connections.size());
		}
	}
	
	private BeaconConnection connect(BluetoothDevice dev) {
		if (dev == null)
			return null;
		
		BluetoothSocket socket;
		try {
			socket = dev.createRfcommSocketToServiceRecord(SERVICE_UUID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		int tryCount = 0;
		while (tryCount < 5) {
			try {
				socket.connect();
				BeaconConnection conn = new BeaconConnection(this, socket, dev, this);
				connections.add(conn);
				return conn;
			} catch (IOException e) {
				tryCount++;
			}
		}
		
		return null;
	}
	
	private BeaconConnection connect(String address) {
		return connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
	}
	
	private void showChatView(String remoteAddress) {
		if (replyTo == null)
			return;
		Bundle data = new Bundle();
		data.putString(MainActivity.MSG_DATA_ADDRESS, remoteAddress);
		Message msg = Message.obtain(null, MainActivity.MSG_SHOW_CHAT_VIEW);
		msg.setData(data);
		try {
			replyTo.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
