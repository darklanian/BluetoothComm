package com.lanian.btbeacon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

public class BeaconConnection implements Runnable {
	static final String TAG = "BlueBeacon";
	
	public static final int CMD_SEND_MESSAGE = 1;
	
	public interface BeaconConnectionListener {
		public void onDisconnected(BeaconConnection conn);
		public void onReceiveMessage(BeaconConnection conn, String message);
	}
	
	Context context;
	BluetoothSocket socket;
	BluetoothDevice dev;
	BeaconConnectionListener listener;
	String remoteAddress;
	
	public BeaconConnection(Context context, BluetoothSocket s, BluetoothDevice d, BeaconConnectionListener l) {
		this.context = context;
		this.socket = s;
		this.dev = d;
		this.remoteAddress = this.dev.getAddress();
		this.listener = l;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			while (true) {
				handleCmd(in.readUnsignedByte(), in);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (listener != null)
				listener.onDisconnected(this);
		}
	}

	private void handleCmd(int cmd, DataInputStream in) throws IOException {
		switch (cmd) {
		case CMD_SEND_MESSAGE:
			onReceiveMessage(in);
			break;
		}
	}

	private void storeMessage(String message, int direction) {
		ContentValues values = new ContentValues();
		values.put(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS, remoteAddress);
		values.put(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_DIRECTION, direction);
		values.put(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_MESSAGE, message);
		Time time = new Time();
		time.setToNow();
		values.put(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_TIME, time.format("%Y-%m-%dT%H:%M:%S"));
		Uri uri = context.getContentResolver().insert(BlueBeaconProvider.CONTENT_URI_MESSAGE, values);
		if (uri == null)
			Log.e(TAG, "couldn't insert message to DB");
	}
	
	private void onReceiveMessage(DataInputStream in) throws IOException {
		String message = in.readUTF();
		Log.d(TAG, "onReceiveMessage: "+message);
		storeMessage(message, BlueBeaconDBHelper.MessageEntry.COLUMN_VALUE_DIRECTION_RECEIVE);
		if (listener != null)
			listener.onReceiveMessage(this, message);
	}
	
	public boolean sendMessage(String message) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeByte(CMD_SEND_MESSAGE);
			out.writeUTF(message);
			out.flush();
			storeMessage(message, BlueBeaconDBHelper.MessageEntry.COLUMN_VALUE_DIRECTION_SEND);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	
}
