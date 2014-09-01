package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.RacesTable;

public class BestTimesCursorAdapter extends CursorAdapter {

	private Context mContext1;
	private int mNumberFormat;

	java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(mContext1);

	public BestTimesCursorAdapter(Context context, int numberFormat, Cursor c, int flags) {
		super(context, c, flags);
		this.mContext1 = context;
		this.mNumberFormat = numberFormat;

		MyLog.i("BestTimesCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {
			TextView tvEvent = (TextView) view.findViewById(R.id.tvEvent);
			TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
			TextView tvDate = (TextView) view.findViewById(R.id.tvDate);

			if (tvEvent != null) {
				int relayLeg = cursor.getInt(cursor.getColumnIndexOrThrow(RacesTable.COL_RELAY_LEG));

				if (relayLeg > 0) {
					SpannableStringBuilder ssb = new SpannableStringBuilder();
					ssb.append(cursor.getString(cursor.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE)));
					int startOfLeg = ssb.length();
					ssb
							.append(" (")
							.append(MySettings.string_relay_leg_text)
							.append(" ")
							.append(Integer.toString(relayLeg))
							.append(")");

					ssb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), startOfLeg, ssb.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ssb.setSpan(new RelativeSizeSpan(0.75f), startOfLeg, ssb.length(), 0);

					tvEvent.setText(ssb);

				} else {
					tvEvent.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventsTable.COL_EVENT_SHORT_TITLE)));
				}
			}

			if (tvTime != null) {
				tvTime.setText(DateTimeUtils.formatDuration(
						cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME)), mNumberFormat));
			}

			if (tvDate != null) {
				long date = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_START_DATE_TIME));
				String formattedDate = dateFormat.format(date);
				tvDate.setText(formattedDate);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_best_time, parent, false);
		return view;
	}

}
