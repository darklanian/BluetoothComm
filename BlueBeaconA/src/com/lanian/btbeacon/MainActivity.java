package com.lanian.btbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity implements BeaconListFragment.OnBeaconClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new BeaconListFragment()).commit();
		}
		
		startService(new Intent(this, BeaconService.class));
	}

	@Override
	public void onBeaconClick(BluetoothDevice dev) {
		getFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment()).addToBackStack("ChatFragment").commit();
	}


}
