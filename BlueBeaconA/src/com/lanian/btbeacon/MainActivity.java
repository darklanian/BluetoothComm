package com.lanian.btbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements OnBeaconSelectedListener {

	static final String TAG = "BlueBeacon";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, StoredBeaconListFragment.newInstance(false)).commit();
		}
		startService(new Intent(this, BeaconService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_request_discoverable:
			startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
			break;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.action_banned_beacons:
			getFragmentManager().beginTransaction()
				.replace(R.id.container, StoredBeaconListFragment.newInstance(true)).addToBackStack("banned_beacons").commit();
			break;
		case R.id.action_scanned_beacons:
			getFragmentManager().beginTransaction()
				.replace(R.id.container, new ScannedBeaconListFragment()).addToBackStack("scanned_beacons").commit();
			break;
		case R.id.action_conversations:
			getFragmentManager().beginTransaction()
				.replace(R.id.container, new ConversationListFragment()).addToBackStack("conversations").commit();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onBeaconSelected(String address) {
		if (address == null)
			return;
		Log.d(TAG, "onBeaconSelected "+address);
		
		startActivity(new Intent(this, ChatActivity.class).putExtra(ChatActivity.EXTRA_ADDRESS, address));
	}

}
