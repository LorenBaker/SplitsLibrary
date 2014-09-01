package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RacesTable;

public class AllRacesCursorAdapter extends CursorAdapter {

	private Context mContext1;
	private int mNumberFormat;

	java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(mContext1);

	public AllRacesCursorAdapter(Context context, int numberFormat, Cursor c, int flags) {
		super(context, c, flags);
		this.mContext1 = context;
		this.mNumberFormat = numberFormat;

		MyLog.i("AllRacesCursorAdapter", "constructor.");
	}

	private boolean ShowSeparator(TextView tv, Cursor listCursor, String currentTitle) {
		boolean result = false;
		String previousTitle = "";
		if (listCursor.moveToPrevious()) {
			previousTitle = listCursor.getString(listCursor.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
			listCursor.moveToNext();
			if (currentTitle.equals(previousTitle)) {
				tv.setVisibility(View.GONE);
				result = false;
			} else {
				tv.setVisibility(View.VISIBLE);
				result = true;
			}
		} else {
			tv.setVisibility(View.VISIBLE);
			listCursor.moveToFirst();
			result = true;
		}
		return result;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {
			TextView tvListItemSeparator = (TextView) view.findViewById(R.id.tvListItemSeparator);
			TextView tvRaceTime = (TextView) view.findViewById(R.id.tvRaceTime);
			TextView tvRaceLeg = (TextView) view.findViewById(R.id.tvRaceLeg);
			TextView tvRaceDate = (TextView) view.findViewById(R.id.tvRaceDate);
			TextView tvMeetTitle = (TextView) view.findViewById(R.id.tvMeetTitle);

			if (tvListItemSeparator != null) {
				String eventShortTitle =
						cursor.getString(cursor.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
				if (ShowSeparator(tvListItemSeparator, cursor, eventShortTitle)) {
					tvListItemSeparator.setText(eventShortTitle);
				}
			}

			if (tvRaceTime != null) {
				tvRaceTime.setText(DateTimeUtils.formatDuration(
						cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME)), mNumberFormat));
			}

			if (tvRaceLeg != null) {
				int relayLeg = cursor.getInt(cursor.getColumnIndexOrThrow(RacesTable.COL_RELAY_LEG));

				if (relayLeg > 0) {
					tvRaceLeg.setText(
							new StringBuilder()
									.append(" (")
									.append(MySettings.string_relay_leg_text)
									.append(" ")
									.append(Integer.toString(relayLeg))
									.append(")")
							);

					tvRaceLeg.setVisibility(View.VISIBLE);
				} else {
					tvRaceLeg.setVisibility(View.GONE);
				}

			}

			if (tvRaceDate != null) {
				long date = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_START_DATE_TIME));
				String formattedDate = dateFormat.format(date);
				tvRaceDate.setText(formattedDate);
			}

			if (tvMeetTitle != null) {
				long meetID = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_MEET_ID));
				tvMeetTitle.setText(MeetsTable.getMeetTitle(mContext1, meetID));
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_all_races, parent, false);
		return view;
	}

}
