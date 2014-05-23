package com.lanian.bttestpc;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.StreamConnection;

public class CommandProcessor implements Runnable {

	private StreamConnection connection;
	TrayIcon trayIcon;
	
	public CommandProcessor(StreamConnection conn) {
		this.connection = conn;	
		
		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("images/discovering.gif")));
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.err.println(String.format("command processor started. (%d)", this.hashCode()));
		
		try {
			DataInputStream input = this.connection.openDataInputStream();
			byte buf;
			while (true) {
				buf = input.readByte();
				System.out.print(String.format("%02X ", buf));
				if (buf == 't') {
					if (SystemTray.isSupported()) {
						trayIcon.displayMessage("test1", "test2", MessageType.INFO);
					} else {
						System.err.println("SystemTray is not supported.");
					}
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println(String.format("command processor stopped. (%d)", this.hashCode()));
	}

}
