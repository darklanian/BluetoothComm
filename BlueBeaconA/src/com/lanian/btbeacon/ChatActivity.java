package com.lanian.btbeacon;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

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
