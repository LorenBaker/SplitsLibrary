package com.lbconsulting.splits.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.lbconsulting.splits.classes.MyLog;

public class RacesTable {

	// Version 1
	public static final String TABLE_RACES = "tblRaces";
	public static final String COL_RACE_ID = "_id";
	public static final String COL_MEET_ID = "meetID";
	public static final String COL_EVENT_ID = "eventID";
	public static final String COL_EVENT_SHORT_TITLE = "eventShortTitle";
	public static final String COL_RACE_START_DATE_TIME = "raceStartDateTime";
	public static final String COL_ATHLETE_ID = "athleteID";
	public static final String COL_RACE_TIME = "raceTime";
	public static final String COL_IS_EVENT_BEST_TIME = "isEventBestTime";
	public static final String COL_IS_RELAY = "isRelay";
	public static final String COL_RELAY_LEG = "relayLeg";
	public static final String COL_RELAY_ID = "relayID";

	public static final String COL_CHECKED = "checked";

	public static final String[] PROJECTION_ALL = { COL_RACE_ID, COL_MEET_ID, COL_EVENT_ID, COL_EVENT_SHORT_TITLE,
			COL_RACE_START_DATE_TIME, COL_ATHLETE_ID, COL_RACE_TIME, COL_IS_EVENT_BEST_TIME, COL_IS_RELAY,
			COL_RELAY_LEG, COL_RELAY_ID, COL_CHECKED };

	public static final String CONTENT_PATH = "races";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	public static final String CONTENT_PATH_RACES_WITH_EVENT_FIELDS = "racesWithEventFields";
	public static final Uri CONTENT_URI_RACESS_WITH_EVENT_FIELDS = Uri.parse("content://"
			+ Splits_ContentProvider.AUTHORITY
			+ "/" + CONTENT_PATH_RACES_WITH_EVENT_FIELDS);

	public static final String SORT_ORDER_START_TIME_ASC = COL_RACE_START_DATE_TIME + " ASC";
	public static final String SORT_ORDER_START_TIME_DESC = COL_RACE_START_DATE_TIME + " DESC";
	public static final String SORT_ORDER_RACE_TIME = COL_RACE_TIME + " ASC";
	public static final String SORT_ORDER_EVENT_ID = COL_EVENT_ID + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_RACES
					+ " ("
					+ COL_RACE_ID + " integer primary key autoincrement, "
					+ COL_MEET_ID + " integer default -1, "
					+ COL_EVENT_ID + " integer default -1, "
					+ COL_EVENT_SHORT_TITLE + " text collate nocase, "
					+ COL_RACE_START_DATE_TIME + " integer default 0, "

					+ COL_ATHLETE_ID + " integer default -1, "
					+ COL_RACE_TIME + " integer default -1, "
					+ COL_IS_EVENT_BEST_TIME + " integer default 0, "
					+ COL_IS_RELAY + " integer default 0, "
					+ COL_RELAY_LEG + " integer default -1, "
					+ COL_RELAY_ID + " integer default -1, "

					+ COL_CHECKED + " integer default 0 "
					+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("RacesTable", "onCreate: " + TABLE_RACES + " created.");

	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_RACES, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_RACES);
		onCreate(database);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateRace(Context context, long meetID, long eventID, String shortTitle, long athleteID,
			long startTimeDate, boolean isRelay) {
		long newRaceID = -1;

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		if (meetID > 0 && eventID > 0 && !shortTitle.isEmpty() && athleteID > 1 && startTimeDate > 0) {
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_MEET_ID, meetID);
			cv.put(COL_EVENT_ID, eventID);
			cv.put(COL_EVENT_SHORT_TITLE, shortTitle);
			cv.put(COL_ATHLETE_ID, athleteID);
			cv.put(COL_RACE_START_DATE_TIME, startTimeDate);
			cv.put(COL_IS_RELAY, isRelayValue);

			Uri newRaceUri = cr.insert(uri, cv);
			if (newRaceUri != null) {
				newRaceID = Long.parseLong(newRaceUri.getLastPathSegment());
			}
		} else {
			MyLog.e("RacesTable", "Failed to Create Race!");
		}
		return newRaceID;
	}

	public static long CreateRace(Context context, long meetID, long eventID, String shortTitle, long athleteID,
			long startTimeDate, long raceTime, int relayLeg, boolean isRelay, long relayID) {
		long newRaceID = -1;
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		if (meetID > 0 && eventID > 0 && !shortTitle.isEmpty() && athleteID > 1 && startTimeDate > 0 && raceTime > 0
				&& relayLeg > 0) {
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_MEET_ID, meetID);
			cv.put(COL_EVENT_ID, eventID);
			cv.put(COL_EVENT_SHORT_TITLE, shortTitle);
			cv.put(COL_ATHLETE_ID, athleteID);
			cv.put(COL_RACE_START_DATE_TIME, startTimeDate);
			cv.put(COL_RACE_TIME, raceTime);
			cv.put(COL_RELAY_LEG, relayLeg);
			cv.put(COL_IS_RELAY, isRelayValue);
			cv.put(COL_RELAY_ID, relayID);

			Uri newRaceUri = cr.insert(uri, cv);
			if (newRaceUri != null) {
				newRaceID = Long.parseLong(newRaceUri.getLastPathSegment());
			}
		} else {
			MyLog.e("RacesTable", "Failed to Create Race! meetID:" + meetID
					+ "eventID:" + eventID + "shortTitle.isEmpty():" + shortTitle.isEmpty()
					+ "athleteID:" + athleteID + "startTimeDate:" + startTimeDate + "raceTime:" + raceTime);
		}
		return newRaceID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Cursor getRaceCursor(Context context, long meetID, long eventID, long athleteID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_MEET_ID + " = ? AND " + COL_EVENT_ID + " = ? AND " + COL_ATHLETE_ID + " = ?";
		;
		String selectionArgs[] = new String[]
		{ String.valueOf(meetID), String.valueOf(eventID), String.valueOf(athleteID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getRaceCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getRaceCursor(Context context, long raceID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_RACE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(raceID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getRaceCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRacesCursorWithAthlete(Context context, long athleteID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RACE_ID };
		String selection = COL_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(athleteID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRacesCursorWithAthlete:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRacesCursorWithEvent(Context context, long eventID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RACE_ID };
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRacesCursorWithEvent:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRacesCursorWithMeet(Context context, long meetID) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_RACE_ID };
		String selection = COL_MEET_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(meetID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRacesCursorWithMeet:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllRacesCursorForAthleteEvent(Context context, String eventShortTitle, long athleteID,
			boolean isRelay, int relayLeg) {
		int isRelayValue = 0;
		String selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND " + COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { eventShortTitle, String.valueOf(athleteID),
				String.valueOf(isRelayValue) };

		if (isRelay) {
			isRelayValue = 1;
			selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
					+ COL_RELAY_LEG + " = ? AND " + COL_IS_RELAY + " = ?";
			selectionArgs = new String[] { eventShortTitle, String.valueOf(athleteID),
					String.valueOf(relayLeg), String.valueOf(isRelayValue) };
		}

		Uri uri = CONTENT_URI;
		String[] projection = { COL_RACE_ID };

		String sortOrder = SORT_ORDER_RACE_TIME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRacesCursorForAthleteEvent:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static long getAthleteEventBestTimeValue(Context context, String shortRaceTitle, long athleteID,
			boolean isRelay, int relayLeg) {

		int isRelayValue = 0;
		String selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
				+ COL_IS_EVENT_BEST_TIME + " = ? AND " + COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { shortRaceTitle, String.valueOf(athleteID), String.valueOf(1),
				String.valueOf(isRelayValue) };

		if (isRelay) {
			isRelayValue = 1;
			selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
					+ COL_IS_EVENT_BEST_TIME + " = ? AND " + COL_RELAY_LEG + " = ? AND " + COL_IS_RELAY + " = ?";
			selectionArgs = new String[] { shortRaceTitle, String.valueOf(athleteID), String.valueOf(1),
					String.valueOf(relayLeg), String.valueOf(isRelayValue) };
		}

		long bestTimeValue = -1;

		// String bestTime = "N/A";

		Uri uri = CONTENT_URI;
		String[] projection = { COL_RACE_ID, COL_RACE_TIME };

		String sortOrder = SORT_ORDER_RACE_TIME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAthleteEventBestTime:");
			e.printStackTrace();
		}

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			bestTimeValue = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RACE_TIME));
		}

		if (cursor != null) {
			cursor.close();
		}

		return bestTimeValue;
	}

	public static CursorLoader getAllBestTimeRaces(Context context, long athleteID, int meetTypeID, boolean isRelay) {

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}
		Uri uri = CONTENT_URI_RACESS_WITH_EVENT_FIELDS;

		String projection[] = {
				TABLE_RACES + "." + COL_RACE_ID,
				EventsTable.TABLE_EVENTS + "." + EventsTable.COL_EVENT_SHORT_TITLE,
				TABLE_RACES + "." + COL_EVENT_SHORT_TITLE,
				TABLE_RACES + "." + COL_RELAY_LEG,
				TABLE_RACES + "." + COL_RACE_START_DATE_TIME,
				TABLE_RACES + "." + COL_RACE_TIME
		};

		String selection =
				TABLE_RACES + "." + COL_ATHLETE_ID + " = ? AND "
						+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_MEET_TYPE_ID + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_EVENT_BEST_TIME + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_RELAY + " = ?";

		String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(meetTypeID),
				String.valueOf(1), String.valueOf(isRelayValue) };

		String sortOrder = "";
		if (isRelay) {
			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ TABLE_RACES + "." + COL_EVENT_SHORT_TITLE + " ASC, "
					+ TABLE_RACES + "." + COL_RELAY_LEG + " ASC, "
					+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		} else {

			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_STYLE + " ASC , "
					+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_DISTANCE + " ASC";
		}

		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllBestTimeRaces:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static Cursor getAllBestTimeRacesCursor(Context context, long athleteID, int meetTypeID, boolean isRelay) {
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}
		Uri uri = CONTENT_URI_RACESS_WITH_EVENT_FIELDS;

		String projection[] = {
				TABLE_RACES + "." + COL_RACE_ID,
				TABLE_RACES + "." + COL_MEET_ID,
				TABLE_RACES + "." + COL_EVENT_SHORT_TITLE,
				TABLE_RACES + "." + COL_RELAY_LEG,
				TABLE_RACES + "." + COL_RACE_START_DATE_TIME,
				TABLE_RACES + "." + COL_RACE_TIME
		};

		String selection =
				TABLE_RACES + "." + COL_ATHLETE_ID + " = ? AND "
						+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_MEET_TYPE_ID + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_EVENT_BEST_TIME + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_RELAY + " = ?";

		String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(meetTypeID),
				String.valueOf(1), String.valueOf(isRelayValue) };

		String sortOrder = "";
		if (isRelay) {
			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ TABLE_RACES + "." + COL_EVENT_SHORT_TITLE + " ASC, "
					+ TABLE_RACES + "." + COL_RELAY_LEG + " ASC, "
					+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		} else {

			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_STYLE + " ASC , "
					+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_DISTANCE + " ASC";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllBestTimeRacesCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static CursorLoader getAllRaces(Context context, long athleteID, int meetTypeID, boolean isRelay) {

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Uri uri = CONTENT_URI_RACESS_WITH_EVENT_FIELDS;

		String projection[] = {
				TABLE_RACES + "." + COL_RACE_ID,
				TABLE_RACES + "." + COL_MEET_ID,
				TABLE_RACES + "." + COL_EVENT_SHORT_TITLE,
				TABLE_RACES + "." + COL_RELAY_LEG,
				TABLE_RACES + "." + COL_RACE_START_DATE_TIME,
				TABLE_RACES + "." + COL_RACE_TIME
		};

		String selection =
				TABLE_RACES + "." + COL_ATHLETE_ID + " = ? AND "
						+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_MEET_TYPE_ID + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_RELAY + " = ?";

		String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(meetTypeID),
				String.valueOf(isRelayValue) };

		String sortOrder = "";
		if (isRelay) {
			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ TABLE_RACES + "." + COL_EVENT_SHORT_TITLE + " ASC, "
					+ TABLE_RACES + "." + COL_RELAY_LEG + " ASC, "
					+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		} else {
			sortOrder =
					EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
							+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_STYLE + " ASC, "
							+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_DISTANCE + " ASC, "
							+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		}

		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRaces:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static Cursor getAllRacesCursor(Context context, long athleteID, int meetTypeID, boolean isRelay) {
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Uri uri = CONTENT_URI_RACESS_WITH_EVENT_FIELDS;

		String projection[] = {
				TABLE_RACES + "." + COL_RACE_ID,
				TABLE_RACES + "." + COL_MEET_ID,
				TABLE_RACES + "." + COL_EVENT_SHORT_TITLE,
				TABLE_RACES + "." + COL_RELAY_LEG,
				TABLE_RACES + "." + COL_RACE_START_DATE_TIME,
				TABLE_RACES + "." + COL_RACE_TIME
		};

		String selection =
				TABLE_RACES + "." + COL_ATHLETE_ID + " = ? AND "
						+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_MEET_TYPE_ID + " = ? AND "
						+ TABLE_RACES + "." + COL_IS_RELAY + " = ?";

		String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(meetTypeID),
				String.valueOf(isRelayValue) };

		String sortOrder = "";
		if (isRelay) {
			sortOrder = EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
					+ TABLE_RACES + "." + COL_EVENT_SHORT_TITLE + " ASC, "
					+ TABLE_RACES + "." + COL_RELAY_LEG + " ASC, "
					+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		} else {
			sortOrder =
					EventsTable.TABLE_EVENTS + "." + EventsTable.COL_UNITS_ID + " ASC, "
							+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_STYLE + " ASC, "
							+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_DISTANCE + " ASC, "
							+ TABLE_RACES + "." + COL_RACE_TIME + " ASC";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getAllRacesCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static long getRelayID(Context context, long raceID) {
		long relayID = -1;

		Cursor cursor = getRaceCursor(context, raceID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			relayID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RELAY_ID));
		}

		if (cursor != null) {
			cursor.close();
		}

		return relayID;
	}

	public static String getEventShortTitle(Context context, long raceID) {
		String eventShortTitle = "";

		Cursor cursor = getRaceCursor(context, raceID);
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
		String[] projection = { COL_RACE_ID };
		String selection = null;
		String selectionArgs[] = null;
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("RacesTable", "Exception error in getRaceCount:");
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

	public static int UpdateRaceFieldValues(Context context, long raceID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (raceID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(raceID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static int UpdateRelayRaceTime(Context context, long athleteID, long relayID, long relayRaceTime) {
		int numberOfUpdatedRecords = -1;

		if (relayID > 0 && athleteID > 1) {
			Uri uri = CONTENT_URI;
			String selection = COL_ATHLETE_ID + " = ? AND " + COL_RELAY_ID + " = ? AND " + COL_IS_RELAY + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(athleteID), String.valueOf(relayID),
					String.valueOf(1) };
			ContentValues newFieldValues = new ContentValues();
			newFieldValues.put(COL_RACE_TIME, relayRaceTime);
			ContentResolver cr = context.getContentResolver();
			numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void setAthleteEventBestTime(Context context, String eventShortTitle, long athleteID,
			boolean isRelay, int relayLeg) {

		int isRelayValue = 0;
		String selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
				+ COL_IS_RELAY + " = ?";
		String selectionArgs[] = new String[] { eventShortTitle, String.valueOf(athleteID),
				String.valueOf(isRelayValue) };

		if (isRelay) {
			isRelayValue = 1;
			selection = COL_EVENT_SHORT_TITLE + " = ? AND " + COL_ATHLETE_ID + " = ? AND "
					+ COL_RELAY_LEG + " = ? AND " + COL_IS_RELAY + " = ?";
			selectionArgs = new String[] { eventShortTitle, String.valueOf(athleteID),
					String.valueOf(relayLeg), String.valueOf(isRelayValue) };
		}

		// first set all the races' best time boolean to false.
		Uri uri = CONTENT_URI;

		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(COL_IS_EVENT_BEST_TIME, 0);

		ContentResolver cr = context.getContentResolver();
		cr.update(uri, newFieldValues, selection, selectionArgs);

		// Get all the races sorted by race time
		// The first race is the fastest (best time)
		Cursor cursor = getAllRacesCursorForAthleteEvent(context, eventShortTitle, athleteID, isRelay, relayLeg);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			long raceID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RACE_ID));
			newFieldValues = new ContentValues();
			newFieldValues.put(COL_IS_EVENT_BEST_TIME, 1);
			UpdateRaceFieldValues(context, raceID, newFieldValues);
		}
		if (cursor != null) {
			cursor.close();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int deleteRace(Context context, long raceID) {
		int numberOfDeletedRecords = 0;
		if (raceID > 0) {

			numberOfDeletedRecords = SplitsTable.DeleteAllSplits(context, raceID, false);

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_RACE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(raceID) };
			numberOfDeletedRecords += cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int deleteRelayRecords(Context context, long relayRaceID) {
		int numberOfDeletedRecords = 0;
		if (relayRaceID > 0) {

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_RELAY_ID + " = ? AND " + COL_IS_RELAY + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(relayRaceID), String.valueOf(1) };
			numberOfDeletedRecords += cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

}
