package com.lbconsulting.splits.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.AthletesTable;

public class AllRacesAthletesSpinnerAdapter extends CursorAdapter {

	public AllRacesAthletesSpinnerAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);

		MyLog.i("AllRacesAthletesSpinnerAdapter", "constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {
			int id = view.getId();
			if (id == R.id.llRow) {
				TextView tvRow = (TextView) view.findViewById(R.id.tvRow);
				if (tvRow != null) {
					tvRow.setText(cursor.getString(cursor.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME)));
				}
			} else if (id == R.id.llRowDropdown) {
				TextView tvDropdownRow = (TextView) view.findViewById(R.id.tvDropdownRow);
				if (tvDropdownRow != null) {
					tvDropdownRow.setText(cursor.getString(cursor
							.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME)));
				}
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_spinner_genoa, parent, false);
		return view;
	}

	@Override
	public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.row_spinner_dropdown, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
