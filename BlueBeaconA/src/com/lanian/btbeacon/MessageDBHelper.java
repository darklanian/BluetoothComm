package com.lanian.btbeacon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MessageDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "messages.db";
	private static final int DB_VERSION = 1;
	
	public static abstract class MessageEntry implements BaseColumns {
		public static final String TABLE_NAME = "messages";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_DIRECTION = "direction";
		public static final String COLUMN_NAME_MESSAGE = "message";
		public static final String COLUMN_NAME_TIME = "time";
		
		public static final int COLUMN_VALUE_DIRECTION_RECEIVE = 0;
		public static final int COLUMN_VALUE_DIRECTION_SEND = 1;
	}
	
	private static final String SQL_CREATE_ENTRIES = 
			"CREATE TABLE "+MessageEntry.TABLE_NAME+" ("+
					MessageEntry._ID+" INTEGER PRIMARY KEY,"+
					MessageEntry.COLUMN_NAME_ADDRESS+" TEXT,"+
					MessageEntry.COLUMN_NAME_DIRECTION+" INTEGER,"+
					MessageEntry.COLUMN_NAME_MESSAGE+" TEXT,"+
					MessageEntry.COLUMN_NAME_TIME+" TEXT"+
			")";
	
	public MessageDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
