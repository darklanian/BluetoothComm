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
import android.view.Menu;
import android.view.MenuInflater;
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
			Log.d(TAG, "Beacon list changed");
			loadStoredBeacons();
		}
	};
	boolean observerRegistered = false;
	
	OnBeaconSelectedListener listener;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getActivity().setTitle(banned?R.string.title_fragment_banned_beacons:R.string.title_fragment_beacons);
		setHasOptionsMenu(true);
		
		registerForContextMenu(getListView());
		
		adapter = new ArrayAdapter<Beacon>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		
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
	public void onStart() {
		super.onStart();
		registerContentObserver();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		unregisterContentObserver();
	}
	
	private void registerContentObserver() {
		getActivity().getContentResolver().registerContentObserver(BlueBeaconProvider.CONTENT_URI, true, observer);
	}
	
	private void unregisterContentObserver() {
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadStoredBeacons();
	}
	
	private void loadStoredBeacons() {
		adapter.clear();
		adapter.notifyDataSetChanged();
		getLoaderManager().restartLoader(banned?1:0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		return new CursorLoader(getActivity(), BlueBeaconProvider.CONTENT_URI_BEACON, 
				new String[] {BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS, BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS}, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED+"=?", new String[] {String.valueOf(id)}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.getCount() == 0)
			return;
		
		data.moveToFirst();
		do {
			adapter.add(new Beacon(
					data.getString(data.getColumnIndexOrThrow(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS)), 
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
		getActivity().getMenuInflater().inflate(banned?R.menu.context_banned_beacons:R.menu.context_stored_beacons, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_alias:
			askAlias(adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAddress(), adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAlias());
			break;
		case R.id.action_ban_beacon:
			ban(adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAddress(), true);
			break;
		case R.id.action_forget_beacon:
			forget(adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAddress());
			break;
		case R.id.action_unban_beacon:
			ban(adapter.getItem(((AdapterContextMenuInfo)item.getMenuInfo()).position).getAddress(), false);
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
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(banned?R.menu.banned_beacon_list:R.menu.stored_beacon_list, menu);
	}
	
}
