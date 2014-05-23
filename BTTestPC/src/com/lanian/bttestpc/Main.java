package com.lanian.bttestpc;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new MainFrame().setVisible(true);
				new Thread(new BluetoothServer()).start();
			}
		});
	}

}
