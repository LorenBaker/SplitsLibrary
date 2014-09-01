package com.lbconsulting.splits.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lbconsulting.splits.classes.MyLog;

public class Splits_DatabaseHelper extends SQLiteOpenHelper {

	private static Context mContext;

	private static final String DATABASE_NAME = "Splits.db";
	private static final int DATABASE_VERSION = 1;

	private static SQLiteDatabase dBase;

	public Splits_DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Splits_DatabaseHelper.dBase = database;
		MyLog.i("Splits_DatabaseHelper", "onCreate");
		AthletesTable.onCreate(database, mContext);
		EventsTable.onCreate(mContext, database);
		MeetsTable.onCreate(database);
		RacesTable.onCreate(database);
		RelaysTable.onCreate(database);
		SplitsTable.onCreate(database);
		LastEventAthletesTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.i("Splits_DatabaseHelper", "onUpgrade");
		RacesTable.onUpgrade(database, oldVersion, newVersion);

	}

	public static SQLiteDatabase getDatabase() {
		return dBase;
	}

}
