package com.lbconsulting.splits.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.lbconsulting.splits.classes.MyLog;

public class SplitsTable {

	// Version 1
	public static final String TABLE_SPLITS = "tblSplits";
	public static final String COL_SPLIT_ID = "_id";
	public static final String COL_RACE_ID = "raceID";
	public static final String COL_ATHLETE_ID = "athleteID";
	public static final String COL_EVENT_SHORT_TITLE = "eventShortTitle";

	public static final String COL_IS_RELAY = "isRelay";
	public static final String COL_LAP_NUMBER = "lapNumber";
	public static final String COL_DISTANCE = "distance";
	public static final String COL_SPLIT_TIME = "splitDuration";
	public static final String COL_CUMULATIVE_TIME = "cumulativeTime";

	public static final String[] PROJECTION_ALL = { COL_SPLIT_ID, COL_RACE_ID, COL_ATHLETE_ID, COL_EVENT_SHORT_TITLE,
			COL_IS_RELAY, COL_LAP_NUMBER, COL_DISTANCE, COL_SPLIT_TIME, COL_CUMULATIVE_TIME };

	public static final String CONTENT_PATH = "splits";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	public static final String SORT_ORDER_SPLIT_ID_ASC = COL_SPLIT_ID + " ASC";
	public static final String SORT_ORDER_SPLIT_ID_DESC = COL_SPLIT_ID + " DESC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_SPLITS
					+ " ("
					+ COL_SPLIT_ID + " integer primary key autoincrement, "
					+ COL_RACE_ID + " integer default -1, "
					+ COL_ATHLETE_ID + " integer default -1, "
					+ COL_EVENT_SHORT_TITLE + " text, "
					+ COL_IS_RELAY + " integer default 0, "
					+ COL_LAP_NUMBER + " integer default -1, "
					+ COL_DISTANCE + " integer default -1, "
					+ COL_SPLIT_TIME + " integer default 0, "
					+ COL_CUMULATIVE_TIME + " integer default 0 "
					+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("SplitsTable", "onCreate: " + TABLE_SPLITS + " created.");

	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_SPLITS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SPLITS);
		onCreate(database);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateSplit(Context context, long raceID, long athleteID,
			int lapNumber, String eventShortTitle, int distance, long splitDuration, long cumulativeTime,
			boolean isRelay) {
		long newSplitID = -1;

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Cursor splitCursor = null;
		if (raceID > 0 && lapNumber > 0 && athleteID > 0 && distance > 0 && splitDuration > 0 && cumulativeTime > 0) {
			// check to see if the race is already in the database
			splitCursor = getSplitCursor(context, raceID, athleteID, lapNumber, isRelay);
			if (splitCursor != null && splitCursor.getCount() > 0) {
				// the split already exists in the database
				splitCursor.moveToFirst();
				newSplitID = splitCursor.getLong(splitCursor.getColumnIndexOrThrow(COL_SPLIT_ID));
				splitCursor.close();
				return newSplitID;
			}

			// the split is NOT in the database ... so create it.
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_RACE_ID, raceID);
			cv.put(COL_ATHLETE_ID, athleteID);
			cv.put(COL_LAP_NUMBER, lapNumber);
			cv.put(COL_EVENT_SHORT_TITLE, eventShortTitle);
			cv.put(COL_DISTANCE, distance);
			cv.put(COL_SPLIT_TIME, splitDuration);
			cv.put(COL_CUMULATIVE_TIME, cumulativeTime);
			cv.put(COL_IS_RELAY, isRelayValue);

			Uri newSplitUri = cr.insert(uri, cv);
			if (newSplitUri != null) {
				newSplitID = Long.parseLong(newSplitUri.getLastPathSegment());
			}
		} else {
			MyLog.e("SplitsTable", "Split not created! raceID:" + raceID
					+ "lapNumber:" + lapNumber + "athleteID:" + athleteID + "distance:" + distance
					+ "splitDuration:" + splitDuration + "cumulativeTime:" + cumulativeTime);
		}
		if (splitCursor != null) {
			splitCursor.close();
		}
		return newSplitID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Cursor getSplitCursor(Context context, long raceID, long athleteID, int lapNumber, boolean isRelay) {
		Uri uri = CONTENT_URI;
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		String[] projection = PROJECTION_ALL;

		String selection = COL_RACE_ID + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
				+ COL_LAP_NUMBER + " = ? AND " + COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(raceID), String.valueOf(athleteID),
				String.valueOf(lapNumber), String.valueOf(isRelayValue) };

		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("SplitsTable", "Exception error in getSplitCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getSplitCursor(Context context, long SplitID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_SPLIT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(SplitID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("SplitsTable", "Exception error in getSplitCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllSplitsCursor(Context context, long raceID, boolean isRelay, String sortOrder) {
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_RACE_ID + " = ? AND " + COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(raceID), String.valueOf(isRelayValue) };

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("SplitsTable", "Exception error in getAllSplitsCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static CursorLoader getAllSplits(Context context, long raceID, boolean isRelay, String sortOrder) {
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_RACE_ID + " = ? AND " + COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(raceID), String.valueOf(isRelayValue) };
		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("SplitsTable", "Exception error in getAllSplits:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateSplitFieldValues(Context context, long splitID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (splitID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri splitsUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(splitID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(splitsUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteAllSplits(Context context, long raceID, boolean isRelay) {
		int numberOfDeletedRecords = -1;

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		if (raceID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_RACE_ID + " = ? AND " + COL_IS_RELAY + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(raceID), String.valueOf(isRelayValue) };
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;

	}
}
