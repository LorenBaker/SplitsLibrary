package com.lbconsulting.splits.database;

import java.lang.reflect.Field;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.LruCache;

import com.lbconsulting.splits.classes.MyLog;

public class Splits_ContentProvider extends ContentProvider {

	private Splits_DatabaseHelper database = null;

	// UriMatcher switch constants
	private static final int ATHLETES_MULTI_ROWS = 10;
	private static final int ATHLETES_SINGLE_ROW = 11;

	private static final int EVENTS_MULTI_ROWS = 20;
	private static final int EVENTS_SINGLE_ROW = 21;

	private static final int LAST_EVENT_ATHLETES_MULTI_ROWS = 30;
	private static final int LAST_EVENT_ATHLETES_SINGLE_ROW = 31;

	private static final int MEETS_MULTI_ROWS = 40;
	private static final int MEETS_SINGLE_ROW = 41;

	private static final int RACES_MULTI_ROWS = 50;
	private static final int RACES_SINGLE_ROW = 51;

	private static final int RELAYS_MULTI_ROWS = 60;
	private static final int RELAYS_SINGLE_ROW = 61;

	private static final int SPLITS_MULTI_ROWS = 70;
	private static final int SPLITS_SINGLE_ROW = 71;

	private static final String CONTENT_AUTHORITY = "CONTENT_AUTHORITY";
	private static final String AUTHORITY_CLASS = "com.lbconsulting.splits.app.data.Authority";

	private static final int RACES_WITH_EVENT_FIELDS = 80;

	public static String AUTHORITY = initAuthority();

	private static String initAuthority() {
		// this method allows for both paid and free version to use the same content provider.
		String authority = "something.went.wrong.if.this.is.used";

		try {
			// Both paid and free projects must have the same class in the same package and
			// have the same CONTENT_AUTHORITY field. However, the CONTENT_AUTHORITY field values
			// are different reflecting the paid and free versions.
			// The respective CONTENT_AUTHORITY field values must be included in the respective
			// paid and free project manifests.
			Class<?> clz = Class.forName(AUTHORITY_CLASS);
			Field declaredField = clz.getDeclaredField(CONTENT_AUTHORITY);
			authority = declaredField.get(null).toString();

		} catch (ClassNotFoundException e) {
			MyLog.e("Splits_ContentProvider", "ClassNotFoundException in initAuthority.");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			MyLog.e("Splits_ContentProvider", "NoSuchFieldException in initAuthority.");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			MyLog.e("Splits_ContentProvider", "IllegalArgumentException in initAuthority.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			MyLog.e("Splits_ContentProvider", "IllegalAccessException in initAuthority.");
			e.printStackTrace();
		}

		return authority;
	}

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, AthletesTable.CONTENT_PATH, ATHLETES_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, AthletesTable.CONTENT_PATH + "/#", ATHLETES_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, EventsTable.CONTENT_PATH, EVENTS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, EventsTable.CONTENT_PATH + "/#", EVENTS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, LastEventAthletesTable.CONTENT_PATH, LAST_EVENT_ATHLETES_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, LastEventAthletesTable.CONTENT_PATH + "/#", LAST_EVENT_ATHLETES_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, MeetsTable.CONTENT_PATH, MEETS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, MeetsTable.CONTENT_PATH + "/#", MEETS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, RacesTable.CONTENT_PATH, RACES_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, RacesTable.CONTENT_PATH + "/#", RACES_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, RelaysTable.CONTENT_PATH, RELAYS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, RelaysTable.CONTENT_PATH + "/#", RELAYS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, SplitsTable.CONTENT_PATH, SPLITS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, SplitsTable.CONTENT_PATH + "/#", SPLITS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, RacesTable.CONTENT_PATH_RACES_WITH_EVENT_FIELDS, RACES_WITH_EVENT_FIELDS);
	}

	private static LruCache<String, Bitmap> mMemoryCache;

	public static LruCache<String, Bitmap> getAthleteThumbnailMemoryCache() {
		return mMemoryCache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onCreate() {
		MyLog.i("Splits_ContentProvider", "onCreate");
		// Construct the underlying database
		// Defer opening the database until you need to perform
		// a query or other transaction.
		database = new Splits_DatabaseHelper(getContext());

		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/12th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 12;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		new LoadAthleteThumbnailsTask(getContext()).execute(mMemoryCache);

		return true;
	}

	/*	A content provider is created when its hosting process is created, and remains around for as long as the process
		does, so there is no need to close the database -- it will get closed as part of the kernel cleaning up the
		process's resources when the process is killed. 
	*/

	@SuppressWarnings("resource")
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String rowID = null;
		int deleteCount = 0;

		// Open a WritableDatabase database to support the delete transaction
		SQLiteDatabase db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

			case ATHLETES_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(AthletesTable.TABLE_ATHLETES, selection, selectionArgs);
				break;

			case ATHLETES_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = AthletesTable.COL_ATHLETE_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(AthletesTable.TABLE_ATHLETES, selection, selectionArgs);
				break;

			case EVENTS_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(EventsTable.TABLE_EVENTS, selection, selectionArgs);
				break;

			case EVENTS_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = EventsTable.COL_EVENT_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(EventsTable.TABLE_EVENTS, selection, selectionArgs);
				break;

			case LAST_EVENT_ATHLETES_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES, selection, selectionArgs);
				break;

			case LAST_EVENT_ATHLETES_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = LastEventAthletesTable.COL_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES, selection, selectionArgs);
				break;

			case MEETS_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(MeetsTable.TABLE_MEETS, selection, selectionArgs);
				break;

			case MEETS_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = MeetsTable.COL_MEET_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(MeetsTable.TABLE_MEETS, selection, selectionArgs);
				break;

			case RACES_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(RacesTable.TABLE_RACES, selection, selectionArgs);
				break;

			case RACES_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = RacesTable.COL_RACE_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(RacesTable.TABLE_RACES, selection, selectionArgs);
				break;

			case RELAYS_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(RelaysTable.TABLE_RELAYS, selection, selectionArgs);
				break;

			case RELAYS_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = RelaysTable.COL_RELAY_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(RelaysTable.TABLE_RELAYS, selection, selectionArgs);
				break;

			case SPLITS_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}
				// Perform the deletion
				deleteCount = db.delete(SplitsTable.TABLE_SPLITS, selection, selectionArgs);
				break;

			case SPLITS_SINGLE_ROW:
				// Limit deletion to a single row
				rowID = uri.getLastPathSegment();
				selection = SplitsTable.COL_SPLIT_ID + "=" + rowID;
				// Perform the deletion
				deleteCount = db.delete(SplitsTable.TABLE_SPLITS, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Method delete: Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

			case ATHLETES_MULTI_ROWS:
				return AthletesTable.CONTENT_TYPE;
			case ATHLETES_SINGLE_ROW:
				return AthletesTable.CONTENT_ITEM_TYPE;

			case EVENTS_MULTI_ROWS:
				return EventsTable.CONTENT_TYPE;
			case EVENTS_SINGLE_ROW:
				return EventsTable.CONTENT_ITEM_TYPE;

			case LAST_EVENT_ATHLETES_MULTI_ROWS:
				return LastEventAthletesTable.CONTENT_TYPE;
			case LAST_EVENT_ATHLETES_SINGLE_ROW:
				return LastEventAthletesTable.CONTENT_ITEM_TYPE;

			case MEETS_MULTI_ROWS:
				return MeetsTable.CONTENT_TYPE;
			case MEETS_SINGLE_ROW:
				return MeetsTable.CONTENT_ITEM_TYPE;

			case RACES_MULTI_ROWS:
				return RacesTable.CONTENT_TYPE;
			case RACES_SINGLE_ROW:
				return RacesTable.CONTENT_ITEM_TYPE;

			case RELAYS_MULTI_ROWS:
				return RelaysTable.CONTENT_TYPE;
			case RELAYS_SINGLE_ROW:
				return RelaysTable.CONTENT_ITEM_TYPE;

			case SPLITS_MULTI_ROWS:
				return SplitsTable.CONTENT_TYPE;
			case SPLITS_SINGLE_ROW:
				return SplitsTable.CONTENT_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Method getType. Unknown URI: " + uri);
		}
	}

	@SuppressWarnings("resource")
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = null;
		long newRowId = 0;
		String nullColumnHack = null;

		// Open a WritableDatabase database to support the insert transaction
		db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

			case ATHLETES_MULTI_ROWS:
				newRowId = db.insertOrThrow(AthletesTable.TABLE_ATHLETES, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(AthletesTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(AthletesTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case ATHLETES_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case EVENTS_MULTI_ROWS:
				newRowId = db.insertOrThrow(EventsTable.TABLE_EVENTS, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(EventsTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(EventsTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case EVENTS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case LAST_EVENT_ATHLETES_MULTI_ROWS:
				newRowId = db.insertOrThrow(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(LastEventAthletesTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(LastEventAthletesTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case LAST_EVENT_ATHLETES_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case MEETS_MULTI_ROWS:
				newRowId = db.insertOrThrow(MeetsTable.TABLE_MEETS, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(MeetsTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(MeetsTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case MEETS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case RACES_MULTI_ROWS:
				newRowId = db.insertOrThrow(RacesTable.TABLE_RACES, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(RacesTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(RacesTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case RACES_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case RELAYS_MULTI_ROWS:
				newRowId = db.insertOrThrow(RelaysTable.TABLE_RELAYS, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(RelaysTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(RelaysTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case RELAYS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			case SPLITS_MULTI_ROWS:
				newRowId = db.insertOrThrow(SplitsTable.TABLE_SPLITS, nullColumnHack, values);
				if (newRowId > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(SplitsTable.CONTENT_URI, newRowId);
					getContext().getContentResolver().notifyChange(SplitsTable.CONTENT_URI, null);
					return newRowUri;
				}
				return null;

			case SPLITS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Illegal URI: Cannot insert a new row with a single row URI. " + uri);

			default:
				throw new IllegalArgumentException("Method insert: Unknown URI:" + uri);
		}
	}

	@SuppressWarnings("resource")
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

			case ATHLETES_MULTI_ROWS:
				queryBuilder.setTables(AthletesTable.TABLE_ATHLETES);
				break;

			case ATHLETES_SINGLE_ROW:
				queryBuilder.setTables(AthletesTable.TABLE_ATHLETES);
				queryBuilder.appendWhere(AthletesTable.COL_ATHLETE_ID + "=" + uri.getLastPathSegment());
				break;

			case EVENTS_MULTI_ROWS:
				queryBuilder.setTables(EventsTable.TABLE_EVENTS);
				break;

			case EVENTS_SINGLE_ROW:
				queryBuilder.setTables(EventsTable.TABLE_EVENTS);
				queryBuilder.appendWhere(EventsTable.COL_EVENT_ID + "=" + uri.getLastPathSegment());
				break;

			case LAST_EVENT_ATHLETES_MULTI_ROWS:
				queryBuilder.setTables(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES);
				break;

			case LAST_EVENT_ATHLETES_SINGLE_ROW:
				queryBuilder.setTables(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES);
				queryBuilder.appendWhere(LastEventAthletesTable.COL_ID + "=" + uri.getLastPathSegment());
				break;

			case MEETS_MULTI_ROWS:
				queryBuilder.setTables(MeetsTable.TABLE_MEETS);
				break;

			case MEETS_SINGLE_ROW:
				queryBuilder.setTables(MeetsTable.TABLE_MEETS);
				queryBuilder.appendWhere(MeetsTable.COL_MEET_ID + "=" + uri.getLastPathSegment());
				break;

			case RACES_MULTI_ROWS:
				queryBuilder.setTables(RacesTable.TABLE_RACES);
				break;

			case RACES_SINGLE_ROW:
				queryBuilder.setTables(RacesTable.TABLE_RACES);
				queryBuilder.appendWhere(RacesTable.COL_RACE_ID + "=" + uri.getLastPathSegment());
				break;

			case RELAYS_MULTI_ROWS:
				queryBuilder.setTables(RelaysTable.TABLE_RELAYS);
				break;

			case RELAYS_SINGLE_ROW:
				queryBuilder.setTables(RelaysTable.TABLE_RELAYS);
				queryBuilder.appendWhere(RelaysTable.COL_RELAY_ID + "=" + uri.getLastPathSegment());
				break;

			case SPLITS_MULTI_ROWS:
				queryBuilder.setTables(SplitsTable.TABLE_SPLITS);
				break;

			case SPLITS_SINGLE_ROW:
				queryBuilder.setTables(SplitsTable.TABLE_SPLITS);
				queryBuilder.appendWhere(SplitsTable.COL_SPLIT_ID + "=" + uri.getLastPathSegment());
				break;

			case RACES_WITH_EVENT_FIELDS:

				String tables = RacesTable.TABLE_RACES
						+ " JOIN " + EventsTable.TABLE_EVENTS + " ON "
						+ RacesTable.TABLE_RACES + "." + RacesTable.COL_EVENT_ID + " = "
						+ EventsTable.TABLE_EVENTS + "." + EventsTable.COL_EVENT_ID;
				queryBuilder.setTables(tables);
				break;

			default:
				throw new IllegalArgumentException("Method query. Unknown URI:" + uri);
		}

		// Execute the query on the database
		SQLiteDatabase db = null;
		try {
			db = database.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = database.getReadableDatabase();
		}

		if (null != db) {
			String groupBy = null;
			String having = null;
			Cursor cursor = null;
			try {
				cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
			} catch (Exception e) {
				MyLog.e("Splits_ContentProvider", "Exception error in query.");
				e.printStackTrace();
			}

			if (null != cursor) {
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
			}
			return cursor;
		}
		return null;
	}

	@SuppressWarnings("resource")
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String rowID = null;
		int updateCount = 0;

		// Open a WritableDatabase database to support the update transaction
		SQLiteDatabase db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {

			case ATHLETES_MULTI_ROWS:
				updateCount = db.update(AthletesTable.TABLE_ATHLETES, values, selection, selectionArgs);
				break;

			case ATHLETES_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = AthletesTable.COL_ATHLETE_ID + "=" + rowID;
				updateCount = db.update(AthletesTable.TABLE_ATHLETES, values, selection, selectionArgs);
				break;

			case EVENTS_MULTI_ROWS:
				updateCount = db.update(EventsTable.TABLE_EVENTS, values, selection, selectionArgs);
				break;

			case EVENTS_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = EventsTable.COL_EVENT_ID + "=" + rowID;
				updateCount = db.update(EventsTable.TABLE_EVENTS, values, selection, selectionArgs);
				break;

			case LAST_EVENT_ATHLETES_MULTI_ROWS:
				updateCount = db.update(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES, values, selection,
						selectionArgs);
				break;

			case LAST_EVENT_ATHLETES_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = LastEventAthletesTable.COL_ID + "=" + rowID;
				updateCount = db.update(LastEventAthletesTable.TABLE_LAST_EVENT_ATHLETES, values, selection,
						selectionArgs);
				break;

			case MEETS_MULTI_ROWS:
				updateCount = db.update(MeetsTable.TABLE_MEETS, values, selection, selectionArgs);
				break;

			case MEETS_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = MeetsTable.COL_MEET_ID + "=" + rowID;
				updateCount = db.update(MeetsTable.TABLE_MEETS, values, selection, selectionArgs);
				break;

			case RACES_MULTI_ROWS:
				updateCount = db.update(RacesTable.TABLE_RACES, values, selection, selectionArgs);
				break;

			case RACES_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = RacesTable.COL_RACE_ID + "=" + rowID;
				updateCount = db.update(RacesTable.TABLE_RACES, values, selection, selectionArgs);
				break;

			case RELAYS_MULTI_ROWS:
				updateCount = db.update(RelaysTable.TABLE_RELAYS, values, selection, selectionArgs);
				break;

			case RELAYS_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = RelaysTable.COL_RELAY_ID + "=" + rowID;
				updateCount = db.update(RelaysTable.TABLE_RELAYS, values, selection, selectionArgs);
				break;

			case SPLITS_MULTI_ROWS:
				updateCount = db.update(SplitsTable.TABLE_SPLITS, values, selection, selectionArgs);
				break;

			case SPLITS_SINGLE_ROW:
				rowID = uri.getLastPathSegment();
				selection = SplitsTable.COL_SPLIT_ID + "=" + rowID;
				updateCount = db.update(SplitsTable.TABLE_SPLITS, values, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Method update: Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}

	/**
	 * A test package can call this to get a handle to the database underlying HW311ContentProvider, so it can insert
	 * test data into the database. The test case class is responsible for instantiating the provider in a test context;
	 * {@link android.test.ProviderTestCase2} does this during the call to setUp()
	 * 
	 * @return a handle to the database helper object for the provider's data.
	 */
	public Splits_DatabaseHelper getOpenHelperForTest() {
		return database;
	}
}
