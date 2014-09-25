package com.lbconsulting.splits.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;

public class AthletesTable {

	// Version 1
	public static final String TABLE_ATHLETES = "tblAthletes";
	public static final String COL_ATHLETE_ID = "_id";
	public static final String COL_CONTACT_URI = "contactUri";
	public static final String COL_LOOKUP_KEY = "lookupKey";
	public static final String COL_DISPLAY_NAME = "displayName";
	public static final String COL_PHOTO_URI = "photoUri";
	public static final String COL_PHOTO_THUMBNAIL_URI = "photoThumbnailUri";
	public static final String COL_BIRTHDATE = "birthdate";
	public static final String COL_SELECTED = "selected";
	public static final String COL_CHECKED = "checked";

	public static final String[] PROJECTION_ALL = { COL_ATHLETE_ID, COL_CONTACT_URI, COL_LOOKUP_KEY, COL_DISPLAY_NAME,
			COL_PHOTO_URI,
			COL_PHOTO_THUMBNAIL_URI, COL_BIRTHDATE, COL_SELECTED, COL_CHECKED };

	public static final String CONTENT_PATH = "athletes";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ CONTENT_PATH;
	public static final Uri CONTENT_URI = Uri.parse("content://" + Splits_ContentProvider.AUTHORITY + "/"
			+ CONTENT_PATH);

	public static final String SORT_ORDER_ATHLETE_DISPLAY_NAME = COL_DISPLAY_NAME + " ASC ";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_ATHLETES
					+ " ("
					+ COL_ATHLETE_ID + " integer primary key autoincrement, "
					+ COL_CONTACT_URI + " text, "
					+ COL_LOOKUP_KEY + " text, "
					+ COL_DISPLAY_NAME + " text collate nocase, "
					+ COL_PHOTO_URI + " text, "
					+ COL_PHOTO_THUMBNAIL_URI + " text, "
					+ COL_BIRTHDATE + " integer default 0, "
					+ COL_SELECTED + " integer default 1, "
					+ COL_CHECKED + " integer default 0 "
					+ ");";

	public static void onCreate(SQLiteDatabase database, Context context) {
		database.execSQL(DATATABLE_CREATE);
		// Enter the default athlete: id=1
		String insertProjection = "insert into "
				+ TABLE_ATHLETES
				+ " ("
				+ COL_ATHLETE_ID + ", "
				+ COL_DISPLAY_NAME + ") VALUES ";

		String DEFAULT_ATHLETE = context.getResources().getString(R.string.default_athlete_entry);
		database.execSQL(insertProjection + "(NULL, '" + DEFAULT_ATHLETE + "');");
		MyLog.i("AthletesTable", "onCreate: " + TABLE_ATHLETES + " created.");
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
		MyLog.w(TABLE_ATHLETES, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_ATHLETES);
		onCreate(database, context);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreatAthlete(Context context, String contactUri, String lookupKey, String displayName,
			String photoUri,
			String photoThumbnailUri) {
		long newAthleteID = -1;
		Cursor athleteCursor = null;
		if (!TextUtils.isEmpty(contactUri) && !TextUtils.isEmpty(lookupKey) && !TextUtils.isEmpty(displayName)) {
			// check to see if the Athlete is already in the database
			athleteCursor = getAthleteCursor(context, contactUri);
			if (athleteCursor != null && athleteCursor.getCount() > 0) {
				// the MeetAthlete already exists in the database
				athleteCursor.moveToFirst();
				newAthleteID = athleteCursor.getLong(athleteCursor.getColumnIndexOrThrow(COL_ATHLETE_ID));
				athleteCursor.close();
				return newAthleteID;
			}

			// the meet Athlete is NOT in the database ... so create it.
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_CONTACT_URI, contactUri);
			cv.put(COL_LOOKUP_KEY, lookupKey);
			cv.put(COL_DISPLAY_NAME, displayName);
			cv.put(COL_PHOTO_URI, photoUri);
			cv.put(COL_PHOTO_THUMBNAIL_URI, photoThumbnailUri);
			Uri newMeetAthleteUri = cr.insert(uri, cv);
			if (newMeetAthleteUri != null) {
				newAthleteID = Long.parseLong(newMeetAthleteUri.getLastPathSegment());
			}
		}
		if (athleteCursor != null) {
			athleteCursor.close();
		}
		return newAthleteID;
	}

	public static long CreatAthlete(Context context, String displayName) {
		long newAthleteID = -1;
		Cursor athleteCursor = null;
		if (!TextUtils.isEmpty(displayName)) {
			// check to see if the Athlete is already in the database
			athleteCursor = getAthleteCursorByDisplayName(context, displayName);
			if (athleteCursor != null && athleteCursor.getCount() > 0) {
				// the MeetAthlete already exists in the database
				athleteCursor.moveToFirst();
				newAthleteID = athleteCursor.getLong(athleteCursor.getColumnIndexOrThrow(COL_ATHLETE_ID));
				athleteCursor.close();
				return newAthleteID;
			}

			// the Athlete is NOT in the database ... so create it.
			Uri uri = CONTENT_URI;
			ContentResolver cr = context.getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put(COL_DISPLAY_NAME, displayName);
			Uri newMeetAthleteUri = cr.insert(uri, cv);
			if (newMeetAthleteUri != null) {
				newAthleteID = Long.parseLong(newMeetAthleteUri.getLastPathSegment());
			}
		}
		if (athleteCursor != null) {
			athleteCursor.close();
		}
		return newAthleteID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Cursor getAthleteCursor(Context context, String contactUri) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_CONTACT_URI + " = ? ";
		String selectionArgs[] = new String[] { contactUri };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthleteCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAthleteCursorByDisplayName(Context context, String displayName) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_DISPLAY_NAME + " = ? ";
		String selectionArgs[] = new String[] { displayName };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthleteCursorByDisplayName:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAthleteCursor(Context context, long AthleteID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(AthleteID) };
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthleteCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllAthletesCursor(Context context, boolean selected, long excludeAthleteID, String sortOrder) {
		int selectedValue = 0;
		if (selected) {
			selectedValue = 1;
		}
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = "";
		String selectionArgs[];
		if (excludeAthleteID > 1) {
			selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue), String.valueOf(excludeAthleteID) };
		} else {
			selection = COL_SELECTED + " = ?";
			selectionArgs = new String[] { String.valueOf(selectedValue) };
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAllAthletesCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllCheckedAthletesCursor(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_ATHLETE_ID, COL_DISPLAY_NAME };
		String selection = COL_CHECKED + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(1) };

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAllCheckedAthletesCursor:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAthletesWithThumbnails(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_ATHLETE_ID, COL_CONTACT_URI, COL_PHOTO_THUMBNAIL_URI };
		String selection = COL_PHOTO_THUMBNAIL_URI + " IS NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthletesWithThumbnails:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAthletesWithContactUri(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_ATHLETE_ID, COL_CONTACT_URI, COL_PHOTO_THUMBNAIL_URI };
		String selection = COL_CONTACT_URI + " IS NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthletesWithThumbnails:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAthletesWithOutThumbnails(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = { COL_ATHLETE_ID, COL_CONTACT_URI, COL_PHOTO_THUMBNAIL_URI };
		String selection = COL_CONTACT_URI + " IS NOT NULL AND " + COL_PHOTO_THUMBNAIL_URI + " IS NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAthletesWithOutThumbnails:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static int getTotalNumberOfAthletes(Context context) {
		int numberOfAthletes = 0;

		Uri uri = CONTENT_URI;
		String[] projection = { COL_ATHLETE_ID };
		String selection = null;
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		String sortOrder = null;
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getTotalNumberOfAthletes:");
			e.printStackTrace();
		}

		if (cursor != null) {
			numberOfAthletes = cursor.getCount();
			cursor.close();
		}

		return numberOfAthletes;
	}

	public static CursorLoader getAllAthletesExcludingDefault(Context context, int selectedValue, String sortOrder) {
		// Excludes the default Athlete.

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;

		String selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " > ?";
		String selectionArgs[] = new String[] { String.valueOf(selectedValue), String.valueOf(1) };
		if (selectedValue == MySettings.BOTH_SELECTED_AND_UNSELECTED_ATHLETES) {
			selection = COL_ATHLETE_ID + " > ?";
			selectionArgs = new String[] { String.valueOf(1) };
		}

		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAllAthletesExcludingDefault:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static CursorLoader getAllAthletes(Context context, boolean selected, long excludeAthleteID, String sortOrder) {
		// Includes the default Athlete.
		int selectedValue = 0;
		if (selected) {
			selectedValue = 1;
		}
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = "";
		String selectionArgs[];
		if (excludeAthleteID > 1) {
			selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue), String.valueOf(excludeAthleteID) };
		} else {
			selection = COL_SELECTED + " = ?";
			selectionArgs = new String[] { String.valueOf(selectedValue) };
		}

		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAllAthletes:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static CursorLoader getAllAthletes(Context context, boolean selected,
			long excludeAthleteID_A, long excludeAthleteID_B, long excludeAthleteID_C,
			String sortOrder) {
		// Includes the default Athlete.
		int selectedValue = 0;
		if (selected) {
			selectedValue = 1;
		}
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = "";
		String selectionArgs[];
		if (excludeAthleteID_A > 1 && excludeAthleteID_B > 1 && excludeAthleteID_C > 1) {
			selection = COL_SELECTED + " = ? AND "
					+ COL_ATHLETE_ID + " != ? AND " + COL_ATHLETE_ID + " != ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_A), String.valueOf(excludeAthleteID_B),
					String.valueOf(excludeAthleteID_C) };
		} else if (excludeAthleteID_A > 1 && excludeAthleteID_B > 1) {
			selection = COL_SELECTED + " = ? AND "
					+ COL_ATHLETE_ID + " != ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_A), String.valueOf(excludeAthleteID_B) };

		} else if (excludeAthleteID_A > 1 && excludeAthleteID_C > 1) {
			selection = COL_SELECTED + " = ? AND "
					+ COL_ATHLETE_ID + " != ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_A), String.valueOf(excludeAthleteID_C) };

		} else if (excludeAthleteID_A > 1) {
			selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_A) };

		} else if (excludeAthleteID_B > 1 && excludeAthleteID_C > 1) {
			selection = COL_SELECTED + " = ? AND "
					+ COL_ATHLETE_ID + " != ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_B), String.valueOf(excludeAthleteID_C) };

		} else if (excludeAthleteID_B > 1) {
			selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_B) };

		} else if (excludeAthleteID_C > 1) {
			selection = COL_SELECTED + " = ? AND " + COL_ATHLETE_ID + " != ?";
			selectionArgs = new String[] { String.valueOf(selectedValue),
					String.valueOf(excludeAthleteID_C) };

		} else {
			selection = COL_SELECTED + " = ?";
			selectionArgs = new String[] { String.valueOf(selectedValue) };
		}

		CursorLoader cursorLoader = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("AthletesTable", "Exception error in getAllAthletes:");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static boolean isAthleteSelected(Context context, long athleteID) {
		boolean result = false;
		Cursor cursor = getAthleteCursor(context, athleteID);
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

	public static boolean isAthleteChecked(Context context, long athleteID) {
		boolean result = false;
		Cursor cursor = getAthleteCursor(context, athleteID);
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

	public static String getDisplayName(Context context, long athleteID) {
		String displayName = "";
		Cursor cursor = getAthleteCursor(context, athleteID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			displayName = cursor.getString(cursor.getColumnIndexOrThrow(COL_DISPLAY_NAME));
		}

		if (cursor != null) {
			cursor.close();
		}
		return displayName;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateAthleteFieldValues(Context context, long athleteID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (athleteID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri meetAthleteUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(athleteID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(meetAthleteUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static int CheckAllAthletes(Context context, boolean checked) {
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

	public static int SelectAllAthletes(Context context, boolean selected) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_SELECTED + " = ?";

		ContentValues newFieldValues = new ContentValues();
		String selectionArgs[] = new String[] { String.valueOf(0) };
		if (selected) {
			newFieldValues.put(COL_SELECTED, 1);

		} else {
			selectionArgs = new String[] { String.valueOf(1) };
			newFieldValues.put(COL_SELECTED, 0);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int setAthleteCheckbox(Context context, long athleteID, boolean checked) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(athleteID) };

		ContentValues newFieldValues = new ContentValues();

		if (checked) {
			newFieldValues.put(COL_CHECKED, 1);

		} else {
			newFieldValues.put(COL_CHECKED, 0);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int ToggleAthleteCheckbox(Context context, long athleteID) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(athleteID) };

		ContentValues newFieldValues = new ContentValues();

		if (isAthleteChecked(context, athleteID)) {
			newFieldValues.put(COL_CHECKED, 0);

		} else {
			newFieldValues.put(COL_CHECKED, 1);
		}

		numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static int ToggleAthleteSelectedBox(Context context, long athleteID) {
		int numberOfUpdatedRecords = -1;

		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String selection = COL_ATHLETE_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(athleteID) };

		ContentValues newFieldValues = new ContentValues();

		if (isAthleteSelected(context, athleteID)) {
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

	public static int DeleteAthlete(Context context, long athleteID) {
		int numberOfDeletedRecords = 0;
		if (athleteID > 1) {

			// Races Table: iteratively delete all Races of the Athlete
			Cursor racesCursor = RacesTable.getAllRacesCursorWithAthlete(context, athleteID);
			if (racesCursor != null && racesCursor.getCount() > 0) {
				while (racesCursor.moveToNext()) {
					numberOfDeletedRecords += RacesTable.deleteRace(context,
							racesCursor.getLong(racesCursor.getColumnIndexOrThrow(RacesTable.COL_RACE_ID)));
				}
			}

			if (racesCursor != null) {
				racesCursor.close();
			}

			LastEventAthletesTable.DeleteEventsWithAthlete(context, athleteID);

			ContentResolver cr = context.getContentResolver();
			Uri channelUri = CONTENT_URI;
			String where = COL_ATHLETE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(athleteID) };
			numberOfDeletedRecords += cr.delete(channelUri, where, selectionArgs);
		}
		return numberOfDeletedRecords;

	}

	public static int DeleteAllCheckedAthletes(Context context) {
		int numberOfDeletedRecords = 0;
		Cursor cursor = getAllCheckedAthletesCursor(context);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				numberOfDeletedRecords += DeleteAthlete(context,
						cursor.getLong(cursor.getColumnIndexOrThrow(COL_ATHLETE_ID)));
			}
			cursor.close();
		}
		return numberOfDeletedRecords;
	}

}
