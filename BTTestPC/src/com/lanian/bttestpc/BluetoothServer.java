package com.lanian.bttestpc;

import java.io.IOException;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;


public class BluetoothServer implements Runnable {
	
	private final UUID SERVER_UUID = new UUID("1101", true);
	private final String url = "btspp://localhost:"+SERVER_UUID.toString()+";name=SimpleBluetoohServer";
	
	@Override
	public void run() {
		listen();
		System.err.println("bluetooth server stopped.");
	}
	
	private boolean listen() {
		
		StreamConnectionNotifier notifier;
		try {
			System.out.println(String.format("listening on %s", url));
			notifier = (StreamConnectionNotifier) Connector.open(url);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		while (notifier != null) {
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
