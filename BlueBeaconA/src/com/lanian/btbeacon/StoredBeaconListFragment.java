package com.lanian.btbeacon;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;

public class StoredBeaconListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	ArrayAdapter<Beacon> adapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		adapter = new ArrayAdapter<Beacon>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		loadStoredBeacons();
		
		setHasOptionsMenu(true);
	}
	
	private void loadStoredBeacons() {
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		return new CursorLoader(getActivity(), BlueBeaconProvider.CONTENT_URI_BEACON, 
				new String[] {BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS, BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS}, 
				BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED+"=?", new String[] {"0"}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.stored_beacons, menu);
	}
}
