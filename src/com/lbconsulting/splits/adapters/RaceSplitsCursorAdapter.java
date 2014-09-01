package com.lbconsulting.splits.adapters;

import java.text.NumberFormat;

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
import com.lbconsulting.splits.database.SplitsTable;

public class RaceSplitsCursorAdapter extends CursorAdapter {

	private int mNumberFormat;
	private NumberFormat mLocalNumberFormater;

	public RaceSplitsCursorAdapter(Context context, Cursor c, int flags, int numberFormat) {
		super(context, c, flags);
		this.mNumberFormat = numberFormat;
		this.mLocalNumberFormater = NumberFormat.getInstance();
		MyLog.i("RaceSplitsCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			TextView tvLapNumber = (TextView) view.findViewById(R.id.tvLapNumber);
			TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
			TextView tvSplitTime = (TextView) view.findViewById(R.id.tvSplitTime);
			TextView tvCumulativeTime = (TextView) view.findViewById(R.id.tvCumulativeTime);

			if (tvLapNumber != null) {
				tvLapNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow(SplitsTable.COL_LAP_NUMBER)));
			}

			if (tvDistance != null) {
				tvDistance.setText(mLocalNumberFormater.format(cursor.getInt(cursor
						.getColumnIndexOrThrow(SplitsTable.COL_DISTANCE))));
			}

			if (tvSplitTime != null) {
				long splitDuration = cursor.getLong(cursor.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_TIME));
				String splitTime = DateTimeUtils.formatDuration(splitDuration, mNumberFormat);
				tvSplitTime.setText(splitTime);
			}

			if (tvCumulativeTime != null) {
				long cumulativeDuration = cursor.getLong(cursor.getColumnIndexOrThrow(SplitsTable.COL_CUMULATIVE_TIME));
				String cumulativeTime = DateTimeUtils.formatDuration(cumulativeDuration, mNumberFormat);
				tvCumulativeTime.setText(cumulativeTime);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_race_split_times, parent, false);
		return view;
	}

	public void setNumberFormat(int numberFormat) {
		mNumberFormat = numberFormat;
	}
}
