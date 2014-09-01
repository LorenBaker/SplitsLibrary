package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.SplitsEvents.AddAthletetNameToContacts;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.Splits_ContentProvider;

import de.greenrobot.event.EventBus;

public class fragAthletesCursorAdapter extends CursorAdapter {

	private Context mAdaptorContext;
	private LruCache<String, Bitmap> mMemoryCache;
	private boolean isContextualMode = false;

	public void setContextualMode(boolean isContextualMode) {
		this.isContextualMode = isContextualMode;
	}

	public fragAthletesCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.mAdaptorContext = context;
		this.mMemoryCache = Splits_ContentProvider.getAthleteThumbnailMemoryCache();
		MyLog.i("fragAthletesCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			if (isContextualMode) {
				int isCheckedValue = cursor.getInt(cursor.getColumnIndex(AthletesTable.COL_CHECKED));
				boolean isChecked = isCheckedValue > 0;
				CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
				if (ckBox != null) {
					ckBox.setChecked(isChecked);
					ckBox.setVisibility(View.VISIBLE);
				}

				view.setBackground(mAdaptorContext.getResources()
						.getDrawable(R.drawable.rec_bkgrd_contextual_mode));
			} else {
				int isSelectedValue = cursor.getInt(cursor.getColumnIndex(AthletesTable.COL_SELECTED));
				boolean isSelected = isSelectedValue > 0;
				CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
				if (ckBox != null) {
					ckBox.setChecked(isSelected);
					ckBox.setVisibility(View.VISIBLE);
				}
				long athleteID = cursor.getLong(cursor.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));
				String athleteDisplayName = cursor.getString(cursor
						.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME));
				AthleteHolder holder = new AthleteHolder();
				holder.athleteID = athleteID;
				holder.athleteDisplayName = athleteDisplayName;

				view.setTag(holder);
				view.setBackground(null);
			}

			TextView tvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
			if (tvDisplayName != null) {
				tvDisplayName.setText(cursor.getString(cursor.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME)));
			}

			QuickContactBadge qcbAthlete = (QuickContactBadge) view.findViewById(R.id.qcbAthlete);
			Button btnAddAthleteToContacts = (Button) view.findViewById(R.id.btnAddAthleteToContacts);

			if (cursor.getString(cursor.getColumnIndexOrThrow(AthletesTable.COL_CONTACT_URI)) != null) {

				if (qcbAthlete != null) {
					setQuickContactBadgeVisible(qcbAthlete, btnAddAthleteToContacts);
					qcbAthlete.setVisibility(View.VISIBLE);

					Uri athleteUri = Uri
							.parse(cursor.getString(cursor.getColumnIndexOrThrow(AthletesTable.COL_CONTACT_URI)));
					qcbAthlete.assignContactUri(athleteUri);
					String thumbnailKey = cursor.getString(cursor.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));

					Bitmap thumbnailBitmap = mMemoryCache.get(thumbnailKey);
					if (thumbnailBitmap != null) {
						qcbAthlete.setImageBitmap(thumbnailBitmap);

					} else {
						thumbnailBitmap = mMemoryCache.get(MySettings.DEFAULT_THUMB_NAIL_IMAGE_KEY);
						if (thumbnailBitmap != null) {
							qcbAthlete.setImageBitmap(thumbnailBitmap);
						}
					}
				}
			} else {
				setBtnAthleteVisible(qcbAthlete, btnAddAthleteToContacts);
				// there is no contact URL ... athlete was added manually
			}

		}
	}

	private void setQuickContactBadgeVisible(QuickContactBadge qcbAthlete, Button btnAthlete) {
		qcbAthlete.setVisibility(View.VISIBLE);
		if (btnAthlete != null) {
			btnAthlete.setVisibility(View.GONE);
		}
	}

	private void setBtnAthleteVisible(QuickContactBadge qcbAthlete, Button btnAthlete) {
		if (qcbAthlete != null) {
			qcbAthlete.setVisibility(View.GONE);
		}

		if (btnAthlete != null) {
			btnAthlete.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_athletes, parent, false);
		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView != null) {
			final AthleteHolder holder = (AthleteHolder) convertView.getTag();
			Button btnAddAthleteToContacts = (Button) convertView.findViewById(R.id.btnAddAthleteToContacts);
			btnAddAthleteToContacts.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					/*Toast.makeText(mAdaptorContext, "Add Athlete to Contcts: ID=" + String.valueOf(athleteID),
							Toast.LENGTH_SHORT).show();*/
					EventBus.getDefault().post(
							new AddAthletetNameToContacts(holder.athleteID, holder.athleteDisplayName));
				}
			});
		}
		return super.getView(position, convertView, parent);
	}

	static class AthleteHolder {

		long athleteID;
		String athleteDisplayName;
	}
}
