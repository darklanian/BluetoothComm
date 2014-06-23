package com.lanian.btbeacon;

import java.util.Vector;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BeaconListFragment extends ListFragment {
	static final String TAG = "BlueBeacon";
	
	Vector<BluetoothDevice> foundDevices = new Vector<BluetoothDevice>();
	BluetoothDevice currentDevice;
	ProgressDialog progressDialog;
	ArrayAdapter<BluetoothDevice> adapter;
	OnBeaconClickListener listener;
	
	public static interface OnBeaconClickListener {
		public void onBeaconClick(BluetoothDevice dev); 
	}
	
	BroadcastReceiver btScaningReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				Log.d(TAG, BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			} else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d(TAG, BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				fetchUuids();
			} else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
				Log.d(TAG, BluetoothDevice.ACTION_FOUND);
				BluetoothDevice dev = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (dev != null) {
					String deviceName = dev.getName();
					if (deviceName == null)
						Log.e(TAG, "getName() failed");
					else
						Log.d(TAG, dev.getName());
					foundDevices.add(dev);
				} else {
					Log.e(TAG, "couldn't get BluetoothDevice");
				}
			}  else if (intent.getAction().equals(BluetoothDevice.ACTION_UUID)) {
				Log.d(TAG, BluetoothDevice.ACTION_UUID);
				BluetoothDevice dev = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (dev != null) {
					Log.d(TAG, dev.getName());
					if (currentDevice != null && dev.getAddress().equals(currentDevice.getAddress())) {
						Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
						if (uuids != null) {
							
							for (Parcelable uuid : uuids) {
								if (uuid.toString().compareTo(BeaconService.SERVICE_UUID.toString()) == 0) {
									adapter.add(currentDevice);
									adapter.notifyDataSetChanged();
									break;
								}
							}
						
						} else {
							Log.e(TAG, "couldn't get UUIDs");
						}
						
						fetchUuids();
					}
				} else {
					Log.e(TAG, "couldn't get BluetoothDevice");
				}
			}
		}
		
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		adapter = new ArrayAdapter<BluetoothDevice>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			listener = (OnBeaconClickListener)activity;
		} catch (ClassCastException e) {
			Log.w(TAG, "OnBeaconClickListener is not set");
		}
	}
		
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.beacon_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_scan:
			startScan();		
			break;
		case R.id.action_request_discoverable:
			startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onPause() {
		try {
			getActivity().unregisterReceiver(btScaningReceiver);
		} catch (IllegalArgumentException e) {
			;
		}
		super.onPause();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		if (listener != null) {
			listener.onBeaconClick(adapter.getItem(position));
		}
	}
	
	private void startScan() {
		if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
			return;
		
		adapter.clear();
		adapter.notifyDataSetChanged();
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_UUID);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		getActivity().registerReceiver(btScaningReceiver, filter);
		if (BluetoothAdapter.getDefaultAdapter().startDiscovery()) {
			progressDialog = ProgressDialog.show(getActivity(), getActivity().getText(R.string.title_scanning), getActivity().getText(R.string.message_scanning));
		} else {
			Log.e(TAG, "startDiscovery() failed");
		}
	}
	
	private void fetchUuids() {
		if (!foundDevices.isEmpty()) {
			currentDevice = foundDevices.get(0);
			foundDevices.removeElementAt(0);
			Log.d(TAG, currentDevice.getName()+" fetchUuidsWithSdp()");
			currentDevice.fetchUuidsWithSdp();
		} else {
			Log.d(TAG, "Scanning finished");
			progressDialog.dismiss();
			getActivity().unregisterReceiver(btScaningReceiver);
		}
	}
	
}
