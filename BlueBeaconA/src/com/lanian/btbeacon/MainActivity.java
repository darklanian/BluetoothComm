package com.lanian.btbeacon;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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

public class MainActivity extends Activity implements ScannedBeaconListFragment.OnBeaconClickListener, BeaconServiceManager {
	static final String TAG = "BlueBeacon";
	
	public static final int MSG_HELLO = 1;
	public static final int MSG_SHOW_CHAT_VIEW = 2;
	public static final String MSG_DATA_ADDRESS = "address";
	
	static class SimpleHandler extends Handler {
		WeakReference<MainActivity> target;
		
		public SimpleHandler(MainActivity activity) {
			target = new WeakReference<MainActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			boolean msgHandled = false;
			MainActivity activity = target.get();
			if (activity != null)
				msgHandled = activity.handleMessage(msg);
			if (!msgHandled)
				super.handleMessage(msg);
		}
	}
	
	Messenger beaconService;
	Messenger handler = new Messenger(new SimpleHandler(this));
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ScannedBeaconListFragment()).commit();
		}
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO:
			Log.d(TAG, "OK");
			break;
		case MSG_SHOW_CHAT_VIEW:
			//getFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment().setRemoteAddress(msg.getData().getString(MSG_DATA_ADDRESS)).setBeaconServiceManager(this)).addToBackStack("ChatFragment").commit();
			break;
		default:
			return false;
		}
		return true;
	}

	ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			beaconService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			beaconService = new Messenger(service);
			try {
				Message message = Message.obtain(null, BeaconService.MSG_HELLO);
				message.replyTo = handler;
				beaconService.send(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		
		bindService(new Intent(this, BeaconService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (beaconService != null)
			unbindService(serviceConnection);
	}
	
	@Override
	public void onBeaconClick(BluetoothDevice dev) {
		//getFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment().setRemoteAddress(dev.getAddress()).setBeaconServiceManager(this)).addToBackStack("ChatFragment").commit();
		
	}

	public boolean sendMessageTo(String address, String message) {
		Bundle data = new Bundle();
		data.putString(BeaconService.MSG_DATA_ADDRESS, address);
		data.putString(BeaconService.MSG_DATA_MESSAGE, message);
		
		Message msg = Message.obtain(null, BeaconService.MSG_SEND_MESSAGE);
		msg.setData(data);
		
		try {
			beaconService.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}


}
