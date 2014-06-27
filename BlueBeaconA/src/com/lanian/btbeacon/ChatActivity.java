package com.lanian.btbeacon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ChatActivity extends Activity {
	static final String TAG = "BlueBeacon";
	
	public static final String EXTRA_ADDRESS = "address";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, ChatFragment.newInstance(getIntent().getStringExtra(EXTRA_ADDRESS))).commit();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
	}

}
