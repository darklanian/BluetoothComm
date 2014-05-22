package com.lanian.bttestpc;

import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.StreamConnection;

public class CommandProcessor implements Runnable {

	private StreamConnection connection;
	
	public CommandProcessor(StreamConnection conn) {
		this.connection = conn;		
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
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println(String.format("command processor stopped. (%d)", this.hashCode()));
	}

}
