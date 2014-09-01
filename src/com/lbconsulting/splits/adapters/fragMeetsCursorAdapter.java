package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.MeetsTable;

public class fragMeetsCursorAdapter extends CursorAdapter {

	private Context mAdaptorContext;
	private boolean isContextualMode = false;

	public void setContextualMode(boolean isContextualMode) {
		this.isContextualMode = isContextualMode;
	}

	public fragMeetsCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.mAdaptorContext = context;

		MyLog.i("fragMeetsCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			if (isContextualMode) {
				int isCheckedValue = cursor.getInt(cursor.getColumnIndex(MeetsTable.COL_CHECKED));
				boolean isChecked = isCheckedValue > 0;
				CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
				if (ckBox != null) {
					ckBox.setChecked(isChecked);
					ckBox.setVisibility(View.VISIBLE);
				}
				view.setBackground(mAdaptorContext.getResources()
						.getDrawable(R.drawable.rec_bkgrd_contextual_mode));
			} else {
				int isSelectedValue = cursor.getInt(cursor.getColumnIndex(MeetsTable.COL_SELECTED));
				boolean isSelected = isSelectedValue > 0;
				CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
				if (ckBox != null) {
					ckBox.setChecked(isSelected);
					ckBox.setVisibility(View.VISIBLE);
				}
				view.setBackground(null);
			}

			TextView tvMeetTitle = (TextView) view.findViewById(R.id.tvMeetTitle);
			if (tvMeetTitle != null) {
				tvMeetTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(MeetsTable.COL_TITLE)));
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_meet_titles, parent, false);
		return view;
	}

}
