package com.lbconsulting.splits.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.LruCache;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;

public class LoadAthleteThumbnailsTask extends AsyncTask<LruCache<String, Bitmap>, Void, Void> {

	private Context mContext;

	public LoadAthleteThumbnailsTask(Context context) {
		this.mContext = context;
	}

	@Override
	protected Void doInBackground(LruCache<String, Bitmap>... params) {
		LruCache<String, Bitmap> mMemoryCache = params[0];

		// add default thumb nail image to the memory cache
		Bitmap thumbNailBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.ic_contact_picture_180_holo_light);
		if (thumbNailBitmap != null) {
			mMemoryCache.put(MySettings.DEFAULT_THUMB_NAIL_IMAGE_KEY, thumbNailBitmap);
		}

		// search for athletes that are in contacts, but do not have a thumb nail in the database
		Cursor athletesCursorNoThumbnail = AthletesTable.getAthletesWithOutThumbnails(mContext);
		if (athletesCursorNoThumbnail != null && athletesCursorNoThumbnail.getCount() > 0) {
			long athleteID;
			Uri contactUri;
			Cursor contactCursor = null;
			int idx;
			String photoThumbnailUri;

			while (athletesCursorNoThumbnail.moveToNext()) {
				String contactUriString = athletesCursorNoThumbnail.getString(
						athletesCursorNoThumbnail.getColumnIndexOrThrow(AthletesTable.COL_CONTACT_URI));
				if (contactUriString != null && !contactUriString.isEmpty()) {
					contactUri = Uri.parse(contactUriString);
					if (contactUri != null) {
						// get the contact cursor
						contactCursor = mContext.getContentResolver().query(contactUri, null, null, null, null);
						if (contactCursor != null && contactCursor.getCount() > 0) {
							contactCursor.moveToFirst();
							idx = contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
							photoThumbnailUri = contactCursor.getString(idx);
							if (photoThumbnailUri != null && !photoThumbnailUri.isEmpty()) {
								// the contact now has a thumb nail ... but it is not in our database
								// so add it.
								athleteID = athletesCursorNoThumbnail.getLong(
										athletesCursorNoThumbnail.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));
								ContentValues newFieldValues = new ContentValues();
								newFieldValues.put(AthletesTable.COL_PHOTO_THUMBNAIL_URI, photoThumbnailUri);
								AthletesTable.UpdateAthleteFieldValues(mContext, athleteID, newFieldValues);
							}
						}
						if (contactCursor != null) {
							contactCursor.close();
						}
					}
				}
			}
		}

		if (athletesCursorNoThumbnail != null) {
			athletesCursorNoThumbnail.close();
		}

		// verify that athletes with contact uri exists in contacts
		Cursor athletesCursorWithContactUri = AthletesTable.getAthletesWithContactUri(mContext);
		String contactUriString;
		Uri contactUri;
		Cursor contactCursor;
		while (athletesCursorWithContactUri.moveToNext()) {
			contactUriString = athletesCursorWithContactUri.getString(
					athletesCursorWithContactUri.getColumnIndexOrThrow(AthletesTable.COL_CONTACT_URI));
			if (contactUriString != null && !contactUriString.isEmpty()) {
				contactUri = Uri.parse(contactUriString);
				if (contactUri != null) {
					contactCursor = mContext.getContentResolver().query(contactUri, null, null, null, null);
					if (contactCursor != null && contactCursor.getCount() == 0) {
						// no contact exists ... so remove it from our database
						long athleteID = athletesCursorWithContactUri.getLong(
								athletesCursorWithContactUri.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.putNull(AthletesTable.COL_CONTACT_URI);
						newFieldValues.putNull(AthletesTable.COL_PHOTO_THUMBNAIL_URI);
						AthletesTable.UpdateAthleteFieldValues(mContext, athleteID, newFieldValues);
					}
					if (contactCursor != null) {
						contactCursor.close();
					}
				}
			}
		}
		athletesCursorWithContactUri.close();

		// add thumb nails to the memory cache
		Cursor athletesCursorWithThumbnail2 = AthletesTable.getAthletesWithThumbnails(mContext);
		if (athletesCursorWithThumbnail2 != null && athletesCursorWithThumbnail2.getCount() > 0) {
			String thumbnailKey = "";
			String photoThumbnail = "";
			Uri photoThumbnailUri = null;

			while (athletesCursorWithThumbnail2.moveToNext()) {
				thumbnailKey = athletesCursorWithThumbnail2.getString(athletesCursorWithThumbnail2
						.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));
				photoThumbnail = athletesCursorWithThumbnail2.getString(athletesCursorWithThumbnail2
						.getColumnIndexOrThrow(AthletesTable.COL_PHOTO_THUMBNAIL_URI));
				photoThumbnailUri = Uri.parse(photoThumbnail);
				thumbNailBitmap = getContactBitmapFromURI(mContext, photoThumbnailUri);
				if (thumbNailBitmap != null) {
					mMemoryCache.put(thumbnailKey, thumbNailBitmap);
				}

			}
		}
		if (athletesCursorWithThumbnail2 != null) {
			athletesCursorWithThumbnail2.close();
		}
		return null;
	}

	private Bitmap getContactBitmapFromURI(Context context, Uri photoThumbnailUri) {
		Bitmap bitmap = null;
		InputStream input = null;

		try {
			input = context.getContentResolver().openInputStream(photoThumbnailUri);
			if (input != null) {
				bitmap = BitmapFactory.decodeStream(input);
				input.close();
			}
		} catch (FileNotFoundException e) {
			MyLog.e("LoadAthleteThumbnailsTask", "FileNotFoundException in getContactBitmapFromURI");
			e.printStackTrace();
		} catch (IOException e) {
			MyLog.e("LoadAthleteThumbnailsTask", "IOException in getContactBitmapFromURI");
			e.printStackTrace();
		}

		return bitmap;
	}

}
