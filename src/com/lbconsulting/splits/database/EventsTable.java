package com.lbconsulting.splits.database;

import java.text.NumberFormat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;

public class EventsTable {

	private static Context mContext;
	private static Resources mRes;

	// Version 1
	public static final String TABLE_EVENTS = "tblEvents";
	public static final String COL_EVENT_ID = "_id";
	public static final String COL_EVENT_LONG_TITLE = "eventlongTitle";
	public static final String COL_EVENT_SHORT_TITLE = "eventShortTitle";
	public static final String COL_MEET_TYPE_ID = "meetTypeID";
	public static final String COL_DISTANCE = "distance";
	public static final String COL_STYLE = "style";
	public static final String COL_UNITS_ID = "unitsID";
	public static final String COL_LAP_DISTANCE = "lapDistance";
	public static final String COL_HAS_PARTIAL_LAP = "hasPartialLap";
	public static final String COL_NUMBER_OF_LAPS = "numberOfLaps";
	public static final String COL_IS_RELAY = "isRelay";
	public static final String COL_SELECTED = "selected";
	public static final String COL_CHECKED = "checked";

	public static final String[] PROJECTION_ALL = { COL_EVENT_ID, COL_EVENT_LONG_TITLE, COL_EVENT_SHORT_TITLE,
			COL_MEET_TYPE_ID, COL_DISTANCE, COL_STYLE, COL_UNITS_ID, COL_LAP_DISTANCE, COL_HAS_PARTIAL_LAP,
			COL_NUMBER_OF_LAPS, COL_IS_RELAY, COL_SELECTED, COL_CHECKED };

	public static final String CONTENT_PATH = "events";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	public static final String SORT_ORDER_UNITS_STYLE_DISTANCE = COL_UNITS_ID + " ASC, " + COL_STYLE + " ASC, "
			+ COL_DISTANCE + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_EVENTS
					+ " ("
					+ COL_EVENT_ID + " integer primary key autoincrement, "
					+ COL_EVENT_LONG_TITLE + " text collate nocase, "
					+ COL_EVENT_SHORT_TITLE + " text collate nocase, "
					+ COL_MEET_TYPE_ID + " integer default -1, "
					+ COL_DISTANCE + " integer default 0, "
					+ COL_STYLE + " text, "
					+ COL_UNITS_ID + " integer default -1, "
					+ COL_LAP_DISTANCE + " integer default 0, "
					+ COL_HAS_PARTIAL_LAP + " integer default 0, "
					+ COL_NUMBER_OF_LAPS + " integer default 0, "
					+ COL_IS_RELAY + " integer default 0, "
					+ COL_SELECTED + " integer default 1, "
					+ COL_CHECKED + " integer default 0 "
					+ ");";

	private static String BY;
	private static String YardsAbbr;
	private static String MetersAbbr;

	public static void onCreate(Context context, SQLiteDatabase database) {
		mContext = context;
		mRes = context.getResources();
		BY = mRes.getString(R.string.event_by);
		YardsAbbr = mRes.getStringArray(R.array.units)[1];
		YardsAbbr = YardsAbbr.substring(0, 1);
		MetersAbbr = mRes.getStringArray(R.array.units)[0];
		MetersAbbr = MetersAbbr.substring(0, 1);
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("EventsTable", "onCreate: " + TABLE_EVENTS + " created.");
		CreateInitialEvents(context, database);
		MyLog.i("EventsTable", "onCreate: " + " Initial swimming events created.");
	}

	private static String insertProjection = "insert into " + TABLE_EVENTS + " (" +
			COL_EVENT_ID + ", " + COL_EVENT_LONG_TITLE + ", " + COL_EVENT_SHORT_TITLE + ", " + COL_MEET_TYPE_ID + ", " +
			COL_DISTANCE + ", " + COL_UNITS_ID + ", " + COL_STYLE + ", " + COL_LAP_DISTANCE
			+ ", " + COL_HAS_PARTIAL_LAP + ", " + COL_NUMBER_OF_LAPS + ", " + COL_IS_RELAY
			+ ") VALUES ";

	private static void CreateInitialEvents(Context context, SQLiteDatabase database) {
		Resources res = context.getResources();
		String[] swimmingEvents = res.getStringArray(R.array.swimming_events);

		for (String event : swimmingEvents) {
			CreateRaceEvent(context, database, event);
		}

		String[] trackEvents = res.getStringArray(R.array.track_events);
		for (String event : trackEvents) {
			CreateRaceEvent(context, database, event);
		}
	}

	private static void CreateRaceEvent(Context context, SQLiteDatabase database, String event) {
		try {

			String[] item = event.split(";");
			int meetTypeID = Integer.parseInt(item[0]);
			int distance = Integer.parseInt(item[1]);
			int unitsID = Integer.parseInt(item[2]);
			String style = item[3];
			int lapDistance = Integer.parseInt(item[4]);
			int isRelay = Integer.parseInt(item[5]);

			String eventLongTitle = MakeLongTitle(context, distance, unitsID, style, isRelay > 0, lapDistance);
			String eventShortTitle = MakeShortTitle(context, distance, unitsID, style, isRelay > 0);

			int numberOfLaps = 1;
			int remainder = 0;
			int hasPartialLap = 0;
			if (lapDistance > 0) {
				numberOfLaps = distance / lapDistance;
				remainder = distance % lapDistance;
			}

			if (remainder > 0) {
				numberOfLaps++;
				hasPartialLap = remainder;
			}

			String sql = insertProjection + "(null, "
					+ "'" + eventLongTitle + "', "
					+ "'" + eventShortTitle + "', "
					+ meetTypeID + ", "
					+ distance + ", "
					+ unitsID + ", "
					+ "'" + style + "', "
					+ lapDistance + ", "
					+ hasPartialLap + ", "
					+ numberOfLaps + ", "
					+ isRelay
					+ ")";
			database.execSQL(sql);

		} catch (Exception e) {
			MyLog.e("EventsTable", "An Exception error occurred in CreateRaceEvent.");
			e.printStackTrace();
		}

	}

	private static NumberFormat numberFormat = NumberFormat.getInstance();

	public static String MakeLongTitle(Context context, int distance, int unitsID, String style, boolean isRelay,
			int lapDistance) {
		String shortTitle = MakeShortTitle(context, distance, unitsID, style, isRelay);
		String longTitle = shortTitle;
		if (!isRelay && distance != lapDistance && lapDistance > 0) {
			longTitle = new StringBuilder()
					.append(shortTitle)
					.append(" [")
					.append(BY)
					.append(String.valueOf(lapDistance))
					.append("s]").toString();
		}

		return longTitle;
	}

	public static String MakeShortTitle(Context context, int distance, int unitsID, String style, boolean isRelay) {
		if (YardsAbbr == null || MetersAbbr == null) {
			Resources res = context.getResources();
			if (res != null) {
				YardsAbbr = res.getStringArray(R.array.units)[1];
				YardsAbbr = YardsAbbr.substring(0, 1);
				MetersAbbr = res.getStringArray(R.array.units)[0];
				MetersAbbr = MetersAbbr.substring(0, 1);
			}
		}

		String distanceStr = String.valueOf(distance);
		if (distance > 1999) {
			distanceStr = numberFormat.format(distance);
		}

		String unitsStr = new StringBuilder().append(" ").append(YardsAbbr).append(" ").toString();
		if (unitsID == 0) {
			unitsStr = new StringBuilder().append(" ").append(MetersAbbr).append(" ").toString();
		}

		String shortTitle = "";
		if (isRelay) {
			distance = distance / 4;
			distanceStr = String.valueOf(distance);
			shortTitle = new StringBuilder().append("4x").append(distanceStr).append(unitsStr).append(style).toString();
		} else {
			shortTitle = new StringBuilder().append(distanceStr).append(unitsStr).append(style).toString();
		}
		return shortTitle;
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_EVENTS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		onCreate(mContext, database);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreatEvent(Context context, String eventLongTitle, String eventShortTitle, int meetTypeID,
			int distance, String style,
			int unitsID, int lapDistance, boolean isRelay) {
		long newEventID = -1;
		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}

		Cursor eventCursor = null;
		if (!TextUtils.isEmpty(eventLongTitle) && !TextUtils.isEmpty(eventShortTitle) && meetTypeID > 0 && distance > 0
				&& !TextUtils.isEmpty(style) && unitsID > -1) {
			// check to see if the event is already in the database
			eventCursor = getEventCursor(context, meetTypeID, distance, style, unitsID, lapDistance);
			if (eventCursor != null && eventCursor.getCount() > 0) {
				// the Event already exists in the database
				eventCursor.moveToFirst();
				newEventID = eventCursor.getLong(eventCursor.getColumnIndexOrThrow(COL_EVENT_ID));
				eventCursor.close();
				return newEventID;
			}

			// the event is NOT in the database ... so create it.
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_EVENT_LONG_TITLE, eventLongTitle);
			cv.put(COL_EVENT_SHORT_TITLE, eventShortTitle);
			cv.put(COL_MEET_TYPE_ID, meetTypeID);
			cv.put(COL_DISTANCE, distance);
			cv.put(COL_STYLE, style);
			cv.put(COL_UNITS_ID, unitsID);
			cv.put(COL_LAP_DISTANCE, lapDistance);
			cv.put(COL_IS_RELAY, isRelayValue);

			int numberOfLaps = 1;
			int remainder = 0;
			int hasPartialLap = 0;
			if (lapDistance > 0) {
				numberOfLaps = distance / lapDistance;
				remainder = distance % lapDistance;
			}

			if (remainder > 0) {
				numberOfLaps++;
				hasPartialLap = remainder;
			}

			cv.put(COL_HAS_PARTIAL_LAP, hasPartialLap);
			cv.put(COL_NUMBER_OF_LAPS, numberOfLaps);
			Uri newMeetEventUri = cr.insert(uri, cv);
			if (newMeetEventUri != null) {
				newEventID = Long.parseLong(newMeetEventUri.getLastPathSegment());
			}
		}
		if (eventCursor != null) {
			eventCursor.close();
		}
		return newEventID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Cursor getEventCursor(Context context, int meetTypeID, int distance, String style, int unitsID,
			int lapDistance) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_MEET_TYPE_ID + " = ? AND "
				+ COL_DISTANCE + " = ? AND "
				+ COL_STYLE + " = ? AND "
				+ COL_UNITS_ID + " = ? AND "
				+ COL_LAP_DISTANCE + " = ?";
		String selectionArgs[] = new String[] {
				String.valueOf(meetTypeID),
				String.valueOf(distance),
				style,
				String.valueOf(unitsID),
				String.valueOf(lapDistance) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("EventsTable", "Exception error in getEventCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getEventCursor(Context context, long eventID) {
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
			MyLog.e("EventsTable", "Exception error in getEventCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllCheckedEventsCursor(Context context, int meetType) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_EVENT_ID, COL_EVENT_LONG_TITLE };
		String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_CHECKED + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(meetType), String.valueOf(1) };

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("EventsTable", "Exception error in getAllCheckedEventsCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static CursorLoader getAllEvents(Context context, int meetTypeID, String sortOrder) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_MEET_TYPE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(meetTypeID) };
		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("EventsTable", "Exception error in getAllEvents:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static CursorLoader getAllEvents(Context context, int meetTypeID, boolean selected, boolean isRelay,
			String sortOrder) {
		int selectedValue = 0;
		if (selected) {
			selectedValue = 1;
		}

		int isRelayValue = 0;
		if (isRelay) {
			isRelayValue = 1;
		}
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_SELECTED + " = ? AND " + COL_IS_RELAY + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(meetTypeID), String.valueOf(selectedValue),
				String.valueOf(isRelayValue) };
		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("EventsTable", "Exception error in getAllEvents:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static boolean isEventSelected(Context context, long eventID) {
		boolean result = false;
		Cursor cursor = getEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			int selectedValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SELECTED));
			result = selectedValue > 0;
		}

		if (cursor != null) {
			cursor.close();
		}
		return result;
	}

	public static boolean isEventChecked(Context context, long eventID) {
		boolean result = false;
		Cursor cursor = getEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			int checkedValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHECKED));
			result = checkedValue > 0;
		}

		if (cursor != null) {
			cursor.close();
		}
		return result;
	}

	public static int getLapDistance(Context context, long eventID) {
		int lapDistance = 0;
		Cursor cursor = getEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			lapDistance = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LAP_DISTANCE));
		}

		if (cursor != null) {
			cursor.close();
		}
		return lapDistance;
	}

	public static String getEventLongTitle(Context context, long eventID) {
		String eventTitle = "";
		Cursor cursor = getEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			eventTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_LONG_TITLE));
		}

		if (cursor != null) {
			cursor.close();
		}
		return eventTitle;
	}

	public static String getEventShortTitle(Context context, long eventID) {
		String eventTitle = "";
		Cursor cursor = getEventCursor(context, eventID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			eventTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_SHORT_TITLE));
		}

		if (cursor != null) {
			cursor.close();
		}
		return eventTitle;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateEventFieldValues(Context context, long eventID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (eventID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri meetEventUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(eventID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(meetEventUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static int CheckAllEvents(Context context, boolean checked) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_CHECKED + " = ?";

		ContentValues newFieldValues = new ContentValues();
		String selectionArgs[] = new String[] { String.valueOf(0) };
		if (checked) {
			newFieldValues.put(COL_CHECKED, 1);

		} else {
			selectionArgs = new String[] { String.valueOf(1) };
			newFieldValues.put(COL_CHECKED, 0);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int SelectAllEvents(Context context, int meetType, boolean selected) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_SELECTED + " = ?";

		ContentValues newFieldValues = new ContentValues();
		String selectionArgs[] = new String[] { String.valueOf(meetType), String.valueOf(0) };
		if (selected) {
			newFieldValues.put(COL_SELECTED, 1);

		} else {
			selectionArgs = new String[] { String.valueOf(meetType), String.valueOf(1) };
			newFieldValues.put(COL_SELECTED, 0);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int setEventCheckbox(Context context, long eventID, boolean checked) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };

		ContentValues newFieldValues = new ContentValues();

		if (checked) {
			newFieldValues.put(COL_CHECKED, 1);

		} else {
			newFieldValues.put(COL_CHECKED, 0);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int ToggleEventCheckbox(Context context, long eventID) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };

		ContentValues newFieldValues = new ContentValues();

		if (isEventChecked(context, eventID)) {
			newFieldValues.put(COL_CHECKED, 0);

		} else {
			newFieldValues.put(COL_CHECKED, 1);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int ToggleEventSelectedBox(Context context, long eventID) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_EVENT_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(eventID) };

		ContentValues newFieldValues = new ContentValues();

		if (isEventSelected(context, eventID)) {
			newFieldValues.put(COL_SELECTED, 0);

		} else {
			newFieldValues.put(COL_SELECTED, 1);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TO DO: verify all deletion scenarios
	public static int DeleteEvent(Context context, long eventID) {
		int numberOfDeletedRecords = 0;
		if (eventID > 0) {
			// Races Table: iteratively delete all Races that contain the Event
			Cursor racesCursor = RacesTable.getAllRacesCursorWithEvent(context, eventID);
			if (racesCursor != null && racesCursor.getCount() > 0) {
				long raceID = -1;
				while (racesCursor.moveToNext()) {
					raceID = racesCursor.getLong(racesCursor
							.getColumnIndexOrThrow(RacesTable.COL_RACE_ID));
					numberOfDeletedRecords += RacesTable.deleteRace(context, raceID);
				}
			}

			if (racesCursor != null) {
				racesCursor.close();
			}

			numberOfDeletedRecords += LastEventAthletesTable.DeleteEvent(context, eventID);

			ContentResolver cr = context.getContentResolver();
			Uri channelUri = CONTENT_URI;
			String where = COL_EVENT_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(eventID) };
			numberOfDeletedRecords += cr.delete(channelUri, where, selectionArgs);
		}
		return numberOfDeletedRecords;

	}

	public static int DeleteAllCheckedEvents(Context context, int meetType) {
		int numberOfDeletedRecords = 0;
		Cursor cursor = getAllCheckedEventsCursor(context, meetType);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				numberOfDeletedRecords += DeleteEvent(context,
						cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ID)));
			}
			cursor.close();
		}
		return numberOfDeletedRecords;
	}

}
