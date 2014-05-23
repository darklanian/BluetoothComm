package com.lanian.bttestpc;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;


public class BluetoothServer implements Runnable {
	
	private final UUID SERVER_UUID = new UUID("c9faf940e20c11e39ffa0002a5d5c51b", false);
	//private final UUID SERVER_UUID = new UUID("1101", true);
	
	private final String url = "btspp://localhost:"+SERVER_UUID.toString()+";name=SimpleBluetoohServer;";
	
	@Override
	public void run() {
		listen();
		System.err.println("bluetooth server stopped.");
	}
	
	private boolean listen() {
		
		try {
			LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
		} catch (BluetoothStateException e1) {
			e1.printStackTrace();
		}
		
		StreamConnectionNotifier notifier;
		try {
			notifier = (StreamConnectionNotifier) Connector.open(url);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		while (notifier != null) {
			System.out.println(String.format("listening on %s", url));
			try {
				new Thread(new CommandProcessor(notifier.acceptAndOpen())).start();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}

}
