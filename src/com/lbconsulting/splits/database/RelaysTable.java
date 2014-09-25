package com.lbconsulting.splits.database;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;

public class RelaysTable {

	// Version 1
	public static final String TABLE_RELAYS = "tblRelays";
	public static final String COL_RELAY_ID = "_id";
	public static final String COL_MEET_ID = "meetID";
	public static final String COL_EVENT_ID = "eventID";
	public static final String COL_EVENT_SHORT_TITLE = "eventShortTitle";
	public static final String COL_RELAY_START_DATE_TIME = "relayRaceStartDateTime";

	public static final String COL_LEG1_ATHLETE_ID = "leg1AthleteID";
	public static final String COL_LEG2_ATHLETE_ID = "leg2AthleteID";
	public static final String COL_LEG3_ATHLETE_ID = "leg3AthleteID";
	public static final String COL_LEG4_ATHLETE_ID = "leg4AthleteID";

	public static final String COL_RELAY_TIME = "relayRaceTime";
	public static final String COL_IS_EVENT_BEST_TIME = "isEventBestTime";

	public static final String COL_CHECKED = "checked";

	public static final String[] PROJECTION_ALL = { COL_RELAY_ID, COL_MEET_ID, COL_EVENT_ID, COL_EVENT_SHORT_TITLE,
			COL_RELAY_START_DATE_TIME,
			COL_LEG1_ATHLETE_ID, COL_LEG2_ATHLETE_ID, COL_LEG3_ATHLETE_ID, COL_LEG4_ATHLETE_ID,
			COL_RELAY_TIME, COL_IS_EVENT_BEST_TIME,
			COL_CHECKED };

	public static final String CONTENT_PATH = "relayRaces";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	public static final String SORT_ORDER_START_TIME_ASC = COL_RELAY_START_DATE_TIME + " ASC";
	public static final String SORT_ORDER_START_TIME_DESC = COL_RELAY_START_DATE_TIME + " DESC";
	public static final String SORT_ORDER_RELAY_TIME = COL_RELAY_TIME + " ASC";
	public static final String SORT_ORDER_EVENT_ID = COL_EVENT_ID + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_RELAYS
					+ " ("
					+ COL_RELAY_ID + " integer primary key autoincrement, "
					+ COL_MEET_ID + " integer default -1, "
					+ COL_EVENT_ID + " integer default -1, "
					+ COL_EVENT_SHORT_TITLE + " text collate nocase, "
					+ COL_RELAY_START_DATE_TIME + " integer default 0, "

					+ COL_LEG1_ATHLETE_ID + " integer default -1, "
					+ COL_LEG2_ATHLETE_ID + " integer default -1, "
					+ COL_LEG3_ATHLETE_ID + " integer default -1, "
					+ COL_LEG4_ATHLETE_ID + " integer default -1, "

					+ COL_RELAY_TIME + " integer default -1, "
					+ COL_IS_EVENT_BEST_TIME + " integer default 0, "

					+ COL_CHECKED + " integer default 0 "
					+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("RelaysTable", "onCreate: " + TABLE_RELAYS + " created.");

	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_RELAYS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_RELAYS);
		onCreate(database);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateRace(Context context, long meetID, long eventID, String shortTitle,
			ArrayList<Long> athletes, long startTimeDate) {
		long newRelayRaceID = -1;

		if (meetID > 0 && eventID > 0 && !shortTitle.isEmpty()
				&& athletes.get(0) > 1 && athletes.get(1) > 1 && athletes.get(2) > 1 && athletes.get(3) > 1
				&& startTimeDate > 0) {
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_MEET_ID, meetID);
			cv.put(COL_EVENT_ID, eventID);
			cv.put(COL_EVENT_SHORT_TITLE, shortTitle);
			cv.put(COL_LEG1_ATHLETE_ID, athletes.get(0));
			cv.put(COL_LEG2_ATHLETE_ID, athletes.get(1));
			cv.put(COL_LEG3_ATHLETE_ID, athletes.get(2));
			cv.put(COL_LEG4_ATHLETE_ID, athletes.get(3));
			cv.put(COL_RELAY_START_DATE_TIME, startTimeDate);

			Uri newRelayRaceUri = cr.insert(uri, cv);
			if (newRelayRaceUri != null) {
				newRelayRaceID = Long.parseLong(newRelayRaceUri.getLastPathSegment());
			}
		} else {
			MyLog.e("RelaysTable", "Failed to Create Relay Race!");
		}
		return newRelayRaceID;
	}

	public static Cursor getRelayRaceCursor(Context context, long relayRaceID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_RELAY_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(relayRaceID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getRelayRaceCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRelayRacesCursorWithAthlete(Context context, long athleteID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID };
		String selection = COL_LEG1_ATHLETE_ID + " = ? OR " + COL_LEG2_ATHLETE_ID + " = ? OR "
				+ COL_LEG3_ATHLETE_ID + " = ? OR " + COL_LEG4_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(athleteID),
				String.valueOf(athleteID), String.valueOf(athleteID) };
		String sortOrder = SORT_ORDER_START_TIME_DESC;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getAllRelayRacesCursorWithAthlete:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRelayRacesCursorWithEvent(Context context, long eventID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID };
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };
		String sortOrder = SORT_ORDER_START_TIME_DESC;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getAllRelayRacesCursorWithEvent:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRelayRacesCursorWithMeet(Context context, long meetID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID };
		String selection = COL_MEET_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(meetID) };
		String sortOrder = SORT_ORDER_START_TIME_DESC;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getAllRelayRacesCursorWithMeet:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRelayRacesCursorForAthleteEvent(Context context, String eventShortTitle, long athleteID) {

		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID };
		String selection = COL_EVENT_SHORT_TITLE + " = ? AND ("
				+ COL_LEG1_ATHLETE_ID + " = ? OR " + COL_LEG2_ATHLETE_ID + " = ? OR "
				+ COL_LEG3_ATHLETE_ID + " = ? OR " + COL_LEG4_ATHLETE_ID + " = ?)";
		String selectionArgs[] = new String[] { eventShortTitle,
				String.valueOf(athleteID), String.valueOf(athleteID), String.valueOf(athleteID),
				String.valueOf(athleteID) };
		String sortOrder = SORT_ORDER_RELAY_TIME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getAllRelayRacesCursorForAthleteEvent:");
			e.printStackTrace();
		}
		return cursor;
	}

	private static Cursor getAllRelayRacesCursor(Context context, String eventShortTitle) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID, COL_RELAY_TIME };
		String selection = COL_EVENT_SHORT_TITLE + " = ?";
		String selectionArgs[] = new String[] { eventShortTitle };
		String sortOrder = SORT_ORDER_RELAY_TIME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getAllRelayRacesCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static String getRelayEventBestTime(Context context, String eventShortTitle, int numberFormat) {
		String bestTime = "N/A";

		Cursor cursor = getAllRelayRacesCursor(context, eventShortTitle);

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			long bestTimeValue = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RELAY_TIME));
			bestTime = DateTimeUtils.formatDuration(bestTimeValue, numberFormat);
		}

		if (cursor != null) {
			cursor.close();
		}

		return bestTime;
	}

	public static String getEventShortTitle(Context context, long relayRaceID) {
		String eventShortTitle = "";

		Cursor cursor = getRelayRaceCursor(context, relayRaceID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			eventShortTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_SHORT_TITLE));
		}
		if (cursor != null) {
			cursor.close();
		}
		return eventShortTitle;
	}

	public static int getRaceCount(Context context) {
		int raceCount = 0;
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RELAY_ID };
		String selection = null;
		String selectionArgs[] = null;
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RelaysTable", "Exception error in getRaceCount:");
			e.printStackTrace();
		}
		if (cursor != null) {
			raceCount = cursor.getCount();
			cursor.close();
		}
		return raceCount;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateRelayRaceFieldValues(Context context, long relayRaceID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (relayRaceID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri channelUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(relayRaceID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(channelUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void setRelayBestTime(Context context, String eventShortTitle) {

		// first set all the best time booleans with the provided eventShortTitle to false.
		Uri uri = CONTENT_URI;
		String selection = COL_EVENT_SHORT_TITLE + " = ?";
		String selectionArgs[] = new String[] { eventShortTitle };
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(COL_IS_EVENT_BEST_TIME, 0);

		ContentResolver cr = context.getContentResolver();
		cr.update(uri, newFieldValues, selection, selectionArgs);

		// Get all the relayRaces with the eventShortTitle sorted by relayRace time
		// The first relayRace is the fastest (best time)
		Cursor cursor = getAllRelayRacesCursor(context, eventShortTitle);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			long relayRaceID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RELAY_ID));
			newFieldValues = new ContentValues();
			newFieldValues.put(COL_IS_EVENT_BEST_TIME, 1);
			UpdateRelayRaceFieldValues(context, relayRaceID, newFieldValues);
		}
		if (cursor != null) {
			cursor.close();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int deleteRelayRace(Context context, long relayRaceID) {
		int numberOfDeletedRecords = 0;
		if (relayRaceID > 0) {

			numberOfDeletedRecords = SplitsTable.DeleteAllSplits(context, relayRaceID, true);
			numberOfDeletedRecords += RacesTable.deleteRelayRecords(context, relayRaceID);

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_RELAY_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(relayRaceID) };
			numberOfDeletedRecords += cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

}
