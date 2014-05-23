package com.lanian.bttestandroid;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

public class BluetoothClient {
	private final String TAG = "BluetoothClient";
	//private final UUID uuid = UUID.fromString("c9faf940-e20c-11e3-9ffa-0002a5d5c51b");
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	BluetoothSocket socket;
	OutputStreamWriter writer;
	
	public boolean connect() {
		
		BluetoothDevice dev = findSupportedDevice(uuid);
		if (dev == null) {
			Log.e(TAG, "Not found bluetooth device supporting desired service");
			return false;
		}
		
		try {
			socket = dev.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		
		int tries = 0;
		while (tries < 10) {
			try {
				
				socket.connect();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				tries++;
			}
		}
		
		try {
			writer = new OutputStreamWriter(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	private BluetoothDevice findSupportedDevice(UUID targetUuid) {
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		
		for (BluetoothDevice dev : bt.getBondedDevices()) {
			Log.i(TAG, dev.getName()+"/"+dev.getAddress());
			if (!dev.fetchUuidsWithSdp())
				Log.e(TAG, "fetchUuidsWithSdp() failed");
			for (ParcelUuid uuid : dev.getUuids()) {
				Log.i(TAG, uuid.toString());
				if (uuid.getUuid().equals(targetUuid))
					return dev;
			}
		}
		return null;
	}
	
	public void send(String msg) {
		if (socket == null || !socket.isConnected() || writer == null)
			return;
		
		try {
			writer.write(msg);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		if (socket == null || !socket.isConnected())
			return;
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		if (socket == null || !socket.isConnected())
			return false;
		
		return true;
	}
	
}
