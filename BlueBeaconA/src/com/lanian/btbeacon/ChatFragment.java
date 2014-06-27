package com.lanian.btbeacon;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ChatFragment extends Fragment implements LoaderCallbacks<Cursor> {
	static final String TAG = "BlueBeacon";
	
	public static ChatFragment newInstance(String address) {
		Log.d(TAG, "ChatFragment.newInstance() "+address);
		ChatFragment fragment = new ChatFragment();
		fragment.remoteAddress = address;
		return fragment;
	}
	
	String remoteAddress;
	SimpleCursorAdapter messageAdapter;
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			reload();
		}
	};
	BeaconServiceProxy beaconService = new BeaconServiceProxy();
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		beaconService.bind(getActivity());
		getActivity().getContentResolver().registerContentObserver(BlueBeaconProvider.CONTENT_URI_MESSAGE, false, observer);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		beaconService.unbind(getActivity());
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
		
		((Button)rootView.findViewById(R.id.button_send)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		
		messageAdapter = new SimpleCursorAdapter(getActivity(), 
				R.layout.message_list_item, 
				null, 
				new String[] {BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_MESSAGE}, 
				new int[] { R.id.textView1 }, 
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
			@Override
			public void bindView(View view, Context context,
					Cursor cursor) {
				// TODO Auto-generated method stub
				super.bindView(view, context, cursor);
				
				TextView tv = (TextView)view.findViewById(R.id.textView1);
				
				tv.setText(cursor.getString(cursor.getColumnIndex(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_MESSAGE)));
				int direction = cursor.getInt(cursor.getColumnIndex(BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_DIRECTION));
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tv.getLayoutParams());
				if (direction == BlueBeaconDBHelper.MessageEntry.COLUMN_VALUE_DIRECTION_RECEIVE) {
					lp.gravity = Gravity.LEFT;
					tv.setLayoutParams(lp);
					tv.setBackgroundColor(getResources().getColor(R.color.background_received_message));
				} else if (direction == BlueBeaconDBHelper.MessageEntry.COLUMN_VALUE_DIRECTION_SEND) {
					lp.gravity = Gravity.RIGHT;
					tv.setLayoutParams(lp);
					tv.setBackgroundColor(getResources().getColor(R.color.background_sent_message));
				}
			}
		};
		
		
		ListView listView = (ListView)rootView.findViewById(R.id.listView_messages);
		listView.setDivider(null);
		listView.setAdapter(messageAdapter);
		
		reload();
		
		return rootView;
	}
	
	

	private void reload() {
		if (remoteAddress != null && !remoteAddress.isEmpty())
			getLoaderManager().restartLoader(0, null, this);
	}
		
	protected boolean sendMessage() {
		EditText t = (EditText)getView().findViewById(R.id.editText_message);
		String message = t.getText().toString();
		t.setText("");
		if (message.isEmpty())
			return true;
		
		return beaconService.sendMessageTo(remoteAddress, message);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), BlueBeaconProvider.CONTENT_URI_MESSAGE, 
				new String[] { BlueBeaconDBHelper.MessageEntry._ID, BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS, BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_DIRECTION, BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_MESSAGE, BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_TIME }, 
				BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS+"=?", 
				new String[] { remoteAddress }, 
				BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_TIME+" ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		messageAdapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		messageAdapter.swapCursor(null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.chat, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_store_beacon:
			if (remoteAddress != null || !remoteAddress.isEmpty())
				BlueBeaconProvider.storeBeacon(getActivity(), remoteAddress);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		reload();
	}
}
