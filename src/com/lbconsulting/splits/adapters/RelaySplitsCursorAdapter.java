package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.SplitsTable;

public class RelaySplitsCursorAdapter extends CursorAdapter {

	private Context mAdaptorContext;
	private int mNumberFormat;

	public RelaySplitsCursorAdapter(Context context, Cursor c, int flags, int numberFormat) {
		super(context, c, flags);
		this.mAdaptorContext = context;
		this.mNumberFormat = numberFormat;
		MyLog.i("RelaySplitsCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			TextView tvLeg = (TextView) view.findViewById(R.id.tvLeg);
			TextView tvAthleteDisplayName = (TextView) view.findViewById(R.id.tvAthleteDisplayName);
			TextView tvEventShortTitle = (TextView) view.findViewById(R.id.tvEventShortTitle);
			TextView tvSplit = (TextView) view.findViewById(R.id.tvSplit);

			if (tvLeg != null) {
				tvLeg.setText(cursor.getString(cursor.getColumnIndexOrThrow(SplitsTable.COL_LAP_NUMBER)));
			}

			if (tvAthleteDisplayName != null) {
				long athleteID = cursor.getLong(cursor.getColumnIndexOrThrow(SplitsTable.COL_ATHLETE_ID));
				tvAthleteDisplayName.setText(AthletesTable.getDisplayName(mAdaptorContext, athleteID));
			}

			if (tvEventShortTitle != null) {
				String eventShortTitle = cursor.getString(cursor
						.getColumnIndexOrThrow(SplitsTable.COL_EVENT_SHORT_TITLE));
				tvEventShortTitle.setText(eventShortTitle);
			}

			if (tvSplit != null) {
				long splitDuration = cursor.getLong(cursor.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_TIME));
				String splitTime = DateTimeUtils.formatDuration(splitDuration, mNumberFormat);
				tvSplit.setText(splitTime);
			}

		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_relay_split_times, parent, false);
		return view;
	}

	public void setNumberFormat(int numberFormat) {
		mNumberFormat = numberFormat;
	}
}
