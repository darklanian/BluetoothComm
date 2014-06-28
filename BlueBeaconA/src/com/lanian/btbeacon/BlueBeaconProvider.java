package com.lanian.btbeacon;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class BlueBeaconProvider extends ContentProvider {

	static final String TAG = "BlueBeacon";
	
	public static final String AUTHORITY = "BlueBeaconProviderAuthorities";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY);
	static final String PATH_MESSAGE = BlueBeaconDBHelper.MessageEntry.TABLE_NAME;
	static final String PATH_CONVERSATION = "conversation";
	static final String PATH_BEACON = BlueBeaconDBHelper.BeaconEntry.TABLE_NAME;
	public static final Uri CONTENT_URI_MESSAGE = Uri.withAppendedPath(CONTENT_URI, PATH_MESSAGE);
	public static final Uri CONTENT_URI_CONVERSATION = Uri.withAppendedPath(CONTENT_URI, PATH_CONVERSATION);
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
		
		if (tableName.equals(PATH_CONVERSATION)) {
			return dbHelper.queryConversation();
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

	public static void storeBeacon(Context context, String address) {
		new AsyncTask<String, Integer, Uri>() {

			Context context;
			public AsyncTask<String, Integer, Uri> setContext(Context c) { this.context = c; return this; }
			
			@Override
			protected Uri doInBackground(String... params) {
				ContentValues values = new ContentValues();
				values.put(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS, params[0]);
				values.put(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ALIAS, "");
				values.put(BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED, 0);
				
				return context.getContentResolver().insert(BlueBeaconProvider.CONTENT_URI_BEACON, values);
			}
			
			protected void onPostExecute(Uri result) {
				if (result == null) {
					Toast.makeText(context, R.string.toast_error_could_not_store_beacon, Toast.LENGTH_SHORT).show();
				} else {
					Log.d(TAG, "beacon is stored: "+result.toString());
				}
			}
			
		}.setContext(context).execute(address);
	}
	
	public static Cursor queryBannedBeacons(ContentResolver contentResolver) {
		return contentResolver.query(CONTENT_URI_BEACON, new String[] {BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_ADDRESS}, BlueBeaconDBHelper.BeaconEntry.COLUMN_NAME_BANNED+"=1", null, null);
	}
}
