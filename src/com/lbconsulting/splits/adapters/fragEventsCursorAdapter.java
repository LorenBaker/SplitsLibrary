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
import com.lbconsulting.splits.database.EventsTable;

public class fragEventsCursorAdapter extends CursorAdapter {

	private Context mAdaptorContext;
	private boolean isContextualMode = false;

	public void setContextualMode(boolean isContextualMode) {
		this.isContextualMode = isContextualMode;
	}

	public fragEventsCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.mAdaptorContext = context;

		MyLog.i("fragEventsCursorAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
			TextView tvEventTitle = (TextView) view.findViewById(R.id.tvEventTitle);

			if (isContextualMode) {
				int isCheckedValue = cursor.getInt(cursor.getColumnIndex(EventsTable.COL_CHECKED));
				boolean isChecked = isCheckedValue > 0;
				if (ckBox != null) {
					ckBox.setChecked(isChecked);
				}

				view.setBackground(mAdaptorContext.getResources()
						.getDrawable(R.drawable.rec_bkgrd_contextual_mode));

			} else {
				int isSelectedValue = cursor.getInt(cursor.getColumnIndex(EventsTable.COL_SELECTED));
				boolean isSelected = isSelectedValue > 0;

				if (ckBox != null) {
					ckBox.setChecked(isSelected);
					ckBox.setBackground(null);
				}

				view.setBackground(null);
			}

			if (tvEventTitle != null) {
				tvEventTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventsTable.COL_EVENT_LONG_TITLE)));
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_events, parent, false);
		return view;
	}

}
