package com.lanian.btbeacon;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ChatFragment extends Fragment implements LoaderCallbacks<Cursor> {

	String remoteAddress;
	BeaconServiceManager beaconService;
	SimpleCursorAdapter messageAdapter;
	ContentObserver observer;
	
	public ChatFragment setRemoteAddress(String address) {
		remoteAddress = address;
		return this;
	}
	
	public Fragment setBeaconServiceManager(BeaconServiceManager serviceManager) {
		beaconService = serviceManager;
		return this;
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
				android.R.layout.simple_list_item_1, 
				null, 
				new String[] {MessageDBHelper.MessageEntry.COLUMN_NAME_MESSAGE}, 
				new int[] { android.R.id.text1 }, 
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		ListView listView = (ListView)rootView.findViewById(R.id.listView_messages);
		listView.setAdapter(messageAdapter);
		
		
		observer = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				// TODO Auto-generated method stub
				super.onChange(selfChange);
				reload();
			}
		};
		
		getActivity().getContentResolver().registerContentObserver(MessageProvider.CONTENT_URI, true, observer);
		
		return rootView;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		reload();
		
		
	}

	private void reload() {
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
		return new CursorLoader(getActivity(), MessageProvider.CONTENT_URI, 
				new String[] { MessageDBHelper.MessageEntry._ID, MessageDBHelper.MessageEntry.COLUMN_NAME_ADDRESS, MessageDBHelper.MessageEntry.COLUMN_NAME_DIRECTION, MessageDBHelper.MessageEntry.COLUMN_NAME_MESSAGE, MessageDBHelper.MessageEntry.COLUMN_NAME_TIME }, 
				MessageDBHelper.MessageEntry.COLUMN_NAME_ADDRESS+"=?", 
				new String[] { remoteAddress }, 
				MessageDBHelper.MessageEntry.COLUMN_NAME_TIME+" ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		messageAdapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		messageAdapter.swapCursor(null);
	}

	
	
	
}
