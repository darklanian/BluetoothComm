package com.lanian.btbeacon;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluethoothReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			if (state == BluetoothAdapter.STATE_ON) {
				context.startService(new Intent(context, BeaconService.class));
			} else if (state == BluetoothAdapter.STATE_OFF) {
				context.stopService(new Intent(context, BeaconService.class));
			}
		}
	}

}
