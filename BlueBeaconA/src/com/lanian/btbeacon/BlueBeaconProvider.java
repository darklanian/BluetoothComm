package com.lanian.btbeacon;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class BlueBeaconProvider extends ContentProvider {

	final String TAG = "BlueBeacon";
	
	public static final String AUTHORITY = "BlueBeaconProviderAuthorities";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY);
	static final String PATH_MESSAGE = BlueBeaconDBHelper.MessageEntry.TABLE_NAME;
	static final String PATH_ADDRESS = "address";
	static final String PATH_BEACON = BlueBeaconDBHelper.BeaconEntry.TABLE_NAME;
	public static final Uri CONTENT_URI_MESSAGE = Uri.withAppendedPath(CONTENT_URI, PATH_MESSAGE);
	public static final Uri CONTENT_URI_ADDRESS = Uri.withAppendedPath(CONTENT_URI, PATH_ADDRESS);
	public static final Uri CONTENT_URI_BEACON = Uri.withAppendedPath(CONTENT_URI, PATH_BEACON);
	
	BlueBeaconDBHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		dbHelper = new BlueBeaconDBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		String tableName = getTableName(uri);
		
		if (tableName.equals(PATH_ADDRESS)) {
			return dbHelper.getReadableDatabase().query(true, BlueBeaconDBHelper.MessageEntry.TABLE_NAME, 
					new String[] {BlueBeaconDBHelper.MessageEntry.COLUMN_NAME_ADDRESS}, null, null, null, null, null, null);
		} else {
			return dbHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
		}
	}

	private String getTableName(Uri uri) {
		List<String> pathSegments = uri.getPathSegments();
		String tableName = PATH_BEACON;
		if (pathSegments.size() > 0)
			tableName = pathSegments.get(0);
		
		return tableName;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = dbHelper.getWritableDatabase().insert(getTableName(uri), null, values);
		if (id != -1) {
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.withAppendedPath(uri, String.valueOf(id));
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rows = dbHelper.getWritableDatabase().delete(getTableName(uri), selection, selectionArgs);
		if (rows > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rows = dbHelper.getWritableDatabase().update(getTableName(uri), values, selection, selectionArgs);
		if (rows > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

}
