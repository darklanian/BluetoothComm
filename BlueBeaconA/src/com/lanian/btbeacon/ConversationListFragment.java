package com.lanian.btbeacon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;

public class ConversationListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	static final String TAG = "BlueBeacon";
	
	ArrayAdapter<Beacon> adapter;
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			Log.d(TAG, "Beacon db changed");
			loadConversations();
		}
	};
	boolean observerRegistered = false;
	OnBeaconSelectedListener listener;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		getActivity().setTitle(R.string.title_fragment_conversation);
		registerForContextMenu(getListView());
		
		adapter = new ArrayAdapter<Beacon>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		
	}
	
	private void registerContentObserver() {
		if (!observerRegistered) {
			getActivity().getContentResolver().registerContentObserver(BlueBeaconProvider.CONTENT_URI, true, observer);
			observerRegistered = true;
		}
	}
	
	private void unregisterContentObserver() {
		if (observerRegistered) {
			getActivity().getContentResolver().unregisterContentObserver(observer);
			observerRegistered = false;
		}
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerContentObserver();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterContentObserver();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadConversations();
	}
	
	private void loadConversations() {
		adapter.clear();
		adapter.notifyDataSetChanged();
		getLoaderManager().restartLoader(2, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		return new CursorLoader(getActivity(), BlueBeaconProvider.CONTENT_URI_CONVERSATION, 
				new String[] {BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS, BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS}, 
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.getCount() == 0)
			return;
		
		data.moveToFirst();
		do {
			String address = data.getString(data.getColumnIndexOrThrow(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS));
			String alias = data.getString(data.getColumnIndexOrThrow(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS));
			adapter.add(new Beacon(address, alias));
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.clear();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try {
			listener = (OnBeaconSelectedListener)activity;
		} catch (ClassCastException e) {
			Log.w(TAG, "no listener");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if (listener != null)
			listener.onBeaconSelected(adapter.getItem(position).getAddress());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.context_conversations, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_delete_conversation:
			askDeleteConversation(adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAddress());
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void askDeleteConversation(final String address) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.title_delete_conversation)
			.setMessage(R.string.message_delete_conversation)
			.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteConversation(address);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create().show();
	}

	protected void deleteConversation(String address) {
		getActivity().getContentResolver().delete(BlueBeaconProvider.CONTENT_URI_MESSAGE, 
				BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS+"=?", new String[] {address});
	}
}
