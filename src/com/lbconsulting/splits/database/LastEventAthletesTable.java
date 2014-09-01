package com.lbconsulting.splits.database;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.lbconsulting.splits.classes.MyLog;

public class LastEventAthletesTable {

	// Version 1
	public static final String TABLE_LAST_EVENT_ATHLETES = "tblLastEventAthletes";
	public static final String COL_ID = "_id";
	public static final String COL_EVENT_ID = "eventID";

	public static final String COL_ATHLETE0_ID = "athlete0ID";
	public static final String COL_ATHLETE1_ID = "athlete1ID";
	public static final String COL_ATHLETE2_ID = "athlete2ID";
	public static final String COL_ATHLETE3_ID = "athlete3ID";

	public static final String[] PROJECTION_ALL = { COL_ID, COL_EVENT_ID, COL_ATHLETE0_ID, COL_ATHLETE1_ID,
			COL_ATHLETE2_ID, COL_ATHLETE3_ID };

	public static final String CONTENT_PATH = "lastEventAthletes";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_LAST_EVENT_ATHLETES
					+ " ("
					+ COL_ID + " integer primary key autoincrement, "
					+ COL_EVENT_ID + " integer default -1, "

					+ COL_ATHLETE0_ID + " integer default 1, "
					+ COL_ATHLETE1_ID + " integer default 1, "
					+ COL_ATHLETE2_ID + " integer default 1, "
					+ COL_ATHLETE3_ID + " integer default 1 "

					+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("LastEventAthletesTable", "onCreate: " + TABLE_LAST_EVENT_ATHLETES + " created.");

	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_LAST_EVENT_ATHLETES, "Upgrading database from version " + oldVersion + " to version "
				+ newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_EVENT_ATHLETES);
		onCreate(database);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void SaveLastEventAthletes(Context context, long eventID, ArrayList<Long> athletes) {

		if (eventID > 0) {
			ContentValues newFieldValues = new ContentValues();

			if (athletes.size() == 0) {
				newFieldValues.put(COL_ATHLETE0_ID, 1);
				newFieldValues.put(COL_ATHLETE1_ID, 1);
				newFieldValues.put(COL_ATHLETE2_ID, 1);
				newFieldValues.put(COL_ATHLETE3_ID, 1);
			} else if (athletes.size() == 1) {
				newFieldValues.put(COL_ATHLETE0_ID, athletes.get(0));
				newFieldValues.put(COL_ATHLETE1_ID, 1);
				newFieldValues.put(COL_ATHLETE2_ID, 1);
				newFieldValues.put(COL_ATHLETE3_ID, 1);
			} else if (athletes.size() == 2) {
				newFieldValues.put(COL_ATHLETE0_ID, athletes.get(0));
				newFieldValues.put(COL_ATHLETE1_ID, athletes.get(1));
				newFieldValues.put(COL_ATHLETE2_ID, 1);
				newFieldValues.put(COL_ATHLETE3_ID, 1);
			} else if (athletes.size() == 3) {
				newFieldValues.put(COL_ATHLETE0_ID, athletes.get(0));
				newFieldValues.put(COL_ATHLETE1_ID, athletes.get(1));
				newFieldValues.put(COL_ATHLETE2_ID, athletes.get(2));
				newFieldValues.put(COL_ATHLETE3_ID, 1);
			} else if (athletes.size() > 3) {
				newFieldValues.put(COL_ATHLETE0_ID, athletes.get(0));
				newFieldValues.put(COL_ATHLETE1_ID, athletes.get(1));
				newFieldValues.put(COL_ATHLETE2_ID, athletes.get(2));
				newFieldValues.put(COL_ATHLETE3_ID, athletes.get(3));
			}

			// look for an existing eventID record
			Cursor cursor = getLastEventCursor(context, eventID);
			if (cursor != null && cursor.getCount() > 0) {
				// the event is in the table
				// so update the athletes
				cursor.moveToFirst();
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
				UpdateLastEventFieldValues(context, id, newFieldValues);

			} else {
				// the event is not in the table
				// so create it
				Uri uri = CONTENT_URI;
				ContentResolver cr = context.getContentResolver();
				newFieldValues.put(COL_EVENT_ID, eventID);
				cr.insert(uri, newFieldValues);
			}

			if (cursor != null) {
				cursor.close();
			}
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static Cursor getLastEventCursor(Context context, long eventID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("LastEventAthletesTable", "Exception error in getLastEventCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static ArrayList<Long> getEventAthletes(Context context, long eventID) {
		ArrayList<Long> athletes = new ArrayList<Long>();

		Cursor cursor = getLastEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			athletes.add(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ATHLETE0_ID)));
			athletes.add(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ATHLETE1_ID)));
			athletes.add(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ATHLETE2_ID)));
			athletes.add(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ATHLETE3_ID)));

		} else {
			athletes.add((long) 1);
			athletes.add((long) 1);
			athletes.add((long) 1);
			athletes.add((long) 1);
		}

		if (cursor != null) {
			cursor.close();
		}
		return athletes;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static int UpdateLastEventFieldValues(Context context, long id, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (id > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteEvent(Context context, long eventID) {
		int numberOfDeletedRecords = 0;
		if (eventID > 0) {

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_EVENT_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(eventID) };
			numberOfDeletedRecords += cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;

	}

	public static int DeleteEventsWithAthlete(Context context, long athleteID) {
		int numberOfDeletedRecords = 0;
		if (athleteID > 1) {

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_ATHLETE0_ID + " = ? OR "
					+ COL_ATHLETE1_ID + " = ? OR "
					+ COL_ATHLETE2_ID + " = ? OR "
					+ COL_ATHLETE3_ID + " = ? ";
			String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(athleteID),
					String.valueOf(athleteID), String.valueOf(athleteID) };
			numberOfDeletedRecords += cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;

	}

}
