package com.lanian.btbeacon;

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BeaconServiceProxy {
	static final String TAG = "BlueBeacon";
	
	public static final int MSG_HELLO = 1;
	public static final int MSG_SHOW_CHAT_VIEW = 2;
	public static final String MSG_DATA_ADDRESS = "address";
	
	static class SimpleHandler extends Handler {
		WeakReference<BeaconServiceProxy> target;
		
		public SimpleHandler(BeaconServiceProxy manager) {
			target = new WeakReference<BeaconServiceProxy>(manager);
		}
		@Override
		public void handleMessage(Message msg) {
			boolean msgHandled = false;
			BeaconServiceProxy manager = target.get();
			if (manager != null)
				msgHandled = manager.handleMessage(msg);
			if (!msgHandled)
				super.handleMessage(msg);
		}
	}
	
	Messenger serviceMessenger;
	Messenger messenger = new Messenger(new SimpleHandler(this));
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO:
			Log.d(TAG, "OK");
			break;
		case MSG_SHOW_CHAT_VIEW:
			
			break;
		default:
			return false;
		}
		return true;
	}
	
	ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceMessenger = null;
			if (serviceConnection2 != null)
				serviceConnection2.onServiceDisconnected(name);
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected: "+name);
			serviceMessenger = new Messenger(service);
			try {
				Message message = Message.obtain(null, BeaconService.MSG_HELLO);
				message.replyTo = messenger;
				serviceMessenger.send(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (serviceConnection2 != null)
				serviceConnection2.onServiceConnected(name, service);
		}
	};
	ServiceConnection serviceConnection2;
	
	public boolean sendMessageTo(String address, String message) {
		Bundle data = new Bundle();
		data.putString(BeaconService.MSG_DATA_ADDRESS, address);
		data.putString(BeaconService.MSG_DATA_MESSAGE, message);
		
		Message msg = Message.obtain(null, BeaconService.MSG_SEND_MESSAGE);
		msg.setData(data);
		
		try {
			serviceMessenger.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void bind(Context context) {
		context.bindService(new Intent(context, BeaconService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbind(Context context) {
		if (serviceMessenger != null)
			context.unbindService(serviceConnection);
	}
	
	public void notifyChatActivityState(boolean on) {
		if (serviceMessenger == null) {
			Log.e(TAG, "serviceMessenger is null");
			return;
		}
		Bundle data = new Bundle();
		data.putBoolean(BeaconService.MSG_DATA_CHAT_ACTIVITY_STATE, on);
		Message msg = Message.obtain(null, BeaconService.MSG_NOTIFY_CHAT_ACTIVITY_STATE);
		msg.setData(data);
		try {
			serviceMessenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BeaconServiceProxy(ServiceConnection connection) {
		super();
		serviceConnection2 = connection;
	}
}

