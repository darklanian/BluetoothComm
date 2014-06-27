package com.lanian.btbeacon;

import android.app.Activity;
import android.os.Bundle;

public class ChatActivity extends Activity {
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

}
