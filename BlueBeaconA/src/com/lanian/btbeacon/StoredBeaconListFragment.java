package com.lanian.btbeacon;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
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

public class StoredBeaconListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	static final String TAG = "BlueBeacon";
	
	public static StoredBeaconListFragment newInstance(boolean banned) {
		StoredBeaconListFragment fragment = new StoredBeaconListFragment();
		fragment.banned = banned;
		return fragment;
	}
	
	boolean banned = false;
	ArrayAdapter<Beacon> adapter;
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			
			loadStoredBeacons();
		}
	};
	OnBeaconSelectedListener listener;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		registerForContextMenu(getListView());
		
		adapter = new ArrayAdapter<Beacon>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		loadStoredBeacons();
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
	public void onResume() {
		super.onResume();
		getActivity().getContentResolver().registerContentObserver(BlueBeaconProvider.CONTENT_URI_BEACON, false, observer);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}
	
	private void loadStoredBeacons() {
		adapter.clear();
		adapter.notifyDataSetChanged();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		return new CursorLoader(getActivity(), BlueBeaconProvider.CONTENT_URI_BEACON, 
				new String[] {BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS, BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS}, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED+"=?", new String[] {banned?"1":"0"}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.getCount() == 0)
			return;
		
		data.moveToFirst();
		do {
			adapter.add(new Beacon(data.getString(
					data.getColumnIndexOrThrow(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS)), 
					data.getString(data.getColumnIndexOrThrow(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS))));
			
		} while (data.moveToNext());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		adapter.clear();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(banned?R.menu.banned_beacons:R.menu.stored_beacons, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		Beacon beacon = adapter.getItem(info.position);
		switch (item.getItemId()) {
		case R.id.action_alias:
			askAlias(beacon.getAddress(), beacon.getName());
			break;
		case R.id.action_ban_beacon:
			ban(beacon.getAddress(), true);
			break;
		case R.id.action_forget_beacon:
			forget(beacon.getAddress());
			break;
		case R.id.action_unban_beacon:
			ban(beacon.getAddress(), false);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void askAlias(String address, String alias) {
		AliasDialogFragment.newInstance(address, alias).show(getFragmentManager(), "Alias");
	}
	
	private void ban(String address, boolean ban) {
		ContentValues values = new ContentValues();
		values.put(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED, ban?1:0);
		getActivity().getContentResolver().update(BlueBeaconProvider.CONTENT_URI_BEACON, values, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS+"=?", new String[] {address});
	}
	
	private void forget(String address) {
		getActivity().getContentResolver().delete(BlueBeaconProvider.CONTENT_URI_BEACON, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS+"=?", new String[] {address});
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if (listener != null && !banned)
			listener.onBeaconSelected(adapter.getItem(position).getAddress());
		
	}
}
