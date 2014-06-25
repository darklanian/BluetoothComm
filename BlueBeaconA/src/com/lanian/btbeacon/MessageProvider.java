package com.lanian.btbeacon;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class MessageProvider extends ContentProvider {

	final String TAG = "BlueBeacon";
	
	public static final String AUTHORITY = "MessageProviderAuthorities";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY);
	
	MessageDBHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		dbHelper = new MessageDBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		return dbHelper.getReadableDatabase().query(MessageDBHelper.MessageEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = dbHelper.getWritableDatabase().insert(MessageDBHelper.MessageEntry.TABLE_NAME, null, values);
		if (id != -1) {
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.withAppendedPath(uri, String.valueOf(id));
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rows = dbHelper.getWritableDatabase().delete(MessageDBHelper.MessageEntry.TABLE_NAME, selection, selectionArgs);
		return rows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rows = dbHelper.getWritableDatabase().update(MessageDBHelper.MessageEntry.TABLE_NAME, values, selection, selectionArgs);
		return rows;
	}

}
