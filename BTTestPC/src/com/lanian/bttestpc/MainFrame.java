package com.lanian.bttestpc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainFrame extends JFrame implements ActionListener, DiscoveryListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7306030730780501680L;
	private final String ACTION_DISCOVER = "action_discover";
	JLabel lbDiscovering;
	JList<RemoteDevice> listDevices = new JList<RemoteDevice>();
	Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();
	boolean discovering = false;
	
	public MainFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 300);
		initListPanel();
		
	}
	
	private void initListPanel() {
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		
		listPanel.add(new JLabel("Bluetooth devices:"));
		listPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		JButton btDiscover = new JButton("Discover", new ImageIcon(ClassLoader.getSystemResource("images/discover.png")));
		btDiscover.setActionCommand(ACTION_DISCOVER);
		btDiscover.addActionListener(this);
		listPanel.add(btDiscover);
		listPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		listDevices.addListSelectionListener(this);
		listDevices.setCellRenderer(new ListCellRenderer<RemoteDevice>() {

			@Override
			public Component getListCellRendererComponent(
					JList<? extends RemoteDevice> list, RemoteDevice value,
					int index, boolean isSelected, boolean cellHasFocus) {
				JPanel cell = new JPanel();
				cell.setLayout(new BoxLayout(cell, BoxLayout.LINE_AXIS));
				String name;
				try {
					name = value.getFriendlyName(false);
				} catch (IOException e) {
					name = "?";
					e.printStackTrace();
				}
				cell.add(new JLabel(name));
				cell.add(Box.createRigidArea(new Dimension(5, 0)));
				cell.add(new JLabel(value.getBluetoothAddress()));
				if (isSelected) {
					cell.setBackground(Color.pink);
					cell.setForeground(Color.white);
				} else {
					cell.setBackground(Color.white);
					cell.setForeground(Color.black);
				}
				return cell;
			}
		});
		JScrollPane scroller = new JScrollPane(listDevices);
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		listPanel.add(scroller);
		listPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		lbDiscovering = new JLabel(new ImageIcon(ClassLoader.getSystemResource("images/discovering.gif")));
		lbDiscovering.setText("Discovering...");
		lbDiscovering.setVisible(false);
		listPanel.add(lbDiscovering);
		
		listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		getContentPane().add(listPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACTION_DISCOVER)) {
			discoverBluetoothDevices();
		}
	}

	private void discoverBluetoothDevices() {
		if (discovering)
			return;
		
		devicesDiscovered.clear();
		try {
			discovering = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
			
			if (discovering) {
				lbDiscovering.setVisible(true);
				System.out.println("inquiry started");
			}
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		if (devicesDiscovered.add(btDevice))
			listDevices.setListData(devicesDiscovered);
	}

	@Override
	public void inquiryCompleted(int discType) {
		System.out.println("inquiry completed");
		discovering = false;
		lbDiscovering.setVisible(false);
		String errMsg;
		switch (discType) {
		case INQUIRY_TERMINATED:
			errMsg = "device discovery has been canceled.";
			break;
		case INQUIRY_ERROR:
			errMsg = "the inquiry request failed.";
			break;
		case INQUIRY_COMPLETED:
		default:
			errMsg = "ok";
			break;
		}
		System.out.println(errMsg);
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		System.out.println(String.format("discovering services completed (transID=%d)", transID));
		String errMsg;
		switch (respCode) {
		case SERVICE_SEARCH_NO_RECORDS:
			errMsg = "no service records found on the device.";
			break;
		case SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
			errMsg = "the remote device provided could not be reached.";
			break;
		case SERVICE_SEARCH_ERROR:
			errMsg = "the service search terminated with an error.";
			break;
		case SERVICE_SEARCH_TERMINATED:
			errMsg = "the service search has been canceled.";
			break;
		case SERVICE_SEARCH_COMPLETED:
		default:
			errMsg = "ok";
			break;
		}
		System.out.println(errMsg);
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		System.out.println(String.format("services discovered (transID=%d)", transID));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
	}
}
