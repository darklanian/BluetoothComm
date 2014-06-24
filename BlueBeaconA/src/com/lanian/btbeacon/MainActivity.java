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

public class MainActivity extends Activity implements BeaconListFragment.OnBeaconClickListener {
	static final String TAG = "BlueBeacon";
	
	public static final int MSG_HELLO = 1;
	
	Messenger beaconService;
	Messenger handler = new Messenger(new SimpleHandler(this));
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new BeaconListFragment()).commit();
		}
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO:
			Log.d(TAG, "OK");
			break;
		}
		return false;
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
		getFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment().setBluetoothDevice(dev)).addToBackStack("ChatFragment").commit();
	}


}
