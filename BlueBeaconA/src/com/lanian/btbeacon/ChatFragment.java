package com.lanian.btbeacon;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChatFragment extends Fragment implements BeaconClient.BeaconClientCmdHandler {

	BeaconClient client;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chat, container, false);
	}
	
	public ChatFragment setBluetoothDevice(BluetoothDevice dev) {
		if (client != null)
			client.close();
		client = new BeaconClient(dev, this);
		return this;
	}
	
	public ChatFragment setBluetoothSocket(BluetoothSocket socket) {
		if (client != null)
			client.close();
		client = new BeaconClient(socket, this);
		return this;
	}

	@Override
	public void onReceiveMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	
	
}
