package com.lanian.btbeacon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BeaconClient implements Runnable {
	static final String TAG = "BlueBeacon";
	
	public static final int CMD_SEND_MESSAGE = 1;
	
	BluetoothDevice dev = null;
	BluetoothSocket socket = null;
	BeaconClientCmdHandler cmdHandler;
	
	public static interface BeaconClientCmdHandler {
		public void onReceiveMessage(String message);
	}
	
	public BeaconClient(BluetoothDevice dev, BeaconClientCmdHandler handler) {
		this.dev = dev;
		this.cmdHandler = handler;
	}
	
	public BeaconClient(BluetoothSocket socket, BeaconClientCmdHandler handler) {
		this.socket = socket;
		this.dev = socket.getRemoteDevice();
		this.cmdHandler = handler;
	}
	
	@Override
	public void run() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			while (true)
				handleCmd(in.readByte(), in);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void handleCmd(byte cmd, DataInputStream in) throws IOException {
		switch (cmd) {
		case CMD_SEND_MESSAGE:
			onReceiveMessage(in.readUTF());
			break;
		}
	}

	private void onReceiveMessage(String message) {
		Log.d(TAG, message);
		if (cmdHandler != null)
			cmdHandler.onReceiveMessage(message);
	}

	public boolean connect() {
		if (socket == null) {
			if (dev == null)
				return false;
			try {
				socket = dev.createRfcommSocketToServiceRecord(BeaconService.SERVICE_UUID);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		if (!socket.isConnected()) {
			int tryCount = 0;
			while (tryCount < 5) {
				try {
					socket.connect();
					new Thread(this).start();
					return true;
				} catch (IOException e) {
					tryCount++;
				}
			}
		}
		
		return false;
	}

	public void sendMessage(String message) {
		if (!connect())
			return;
		
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeByte(CMD_SEND_MESSAGE);
			out.writeUTF(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				socket = null;
			}
		}
	}
}
