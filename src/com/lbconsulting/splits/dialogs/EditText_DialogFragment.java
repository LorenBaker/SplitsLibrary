package com.lbconsulting.splits.dialogs;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.AddAthletetNameToContacts;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.MeetsTable;

import de.greenrobot.event.EventBus;

public class EditText_DialogFragment extends DialogFragment {

	private EditText txtEditText;
	private Button btnApply;
	private Button btnCancel;

	private String mDialogTitle;
	private long mItemID;
	private String mItemText;
	private int mDialogType;

	private static final String DIALOG_TITLE = "dialogTitle";
	private static final String DIALOG_ITEM_ID = "dialogItemID";
	private static final String DIALOG_ITEM_TEXT = "dialogItemText";
	private static final String DIALOG_TYPE = "dialogType";

	public EditText_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of EditText_DialogFragment
	 * 
	 * @param itemID
	 * @return EditText_DialogFragment
	 */
	public static EditText_DialogFragment newInstance(String dialogTitle, long itemID, String itemText, int dialogType) {
		EditText_DialogFragment fragment = new EditText_DialogFragment();
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putLong(DIALOG_ITEM_ID, itemID);
		args.putString(DIALOG_ITEM_TEXT, itemText);
		args.putInt(DIALOG_TYPE, dialogType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("EditText_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = args.getString(DIALOG_TITLE);
			mItemID = args.getLong(DIALOG_ITEM_ID, -1);
			mItemText = args.getString(DIALOG_ITEM_TEXT);
			mDialogType = args.getInt(DIALOG_TYPE, -1);
		}

		View view = inflater.inflate(R.layout.dialog_edit_text, container);
		txtEditText = (EditText) view.findViewById(R.id.txtEditText);
		if (txtEditText != null) {
			if (mItemText != null) {
				txtEditText.setText(mItemText);
			} else {
				txtEditText.setHint(getString(R.string.athlete_name_text));
			}
		}

		btnApply = (Button) view.findViewById(R.id.btnApply);
		if (btnApply != null) {
			btnApply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!txtEditText.getText().toString().isEmpty()) {
						ContentValues newFieldValues = new ContentValues();
						switch (mDialogType) {

							case MySettings.DIALOG_EDIT_TEXT_ADD_ATHLETE_NAME:
								String athleteName = txtEditText.getText().toString().trim();
								if (!athleteName.isEmpty()) {
									long athleteID = AthletesTable.CreatAthlete(getActivity(), athleteName);
									EventBus.getDefault().post(new AddAthletetNameToContacts(athleteID, athleteName));
								}
								break;

							case MySettings.DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME:
								newFieldValues.put(AthletesTable.COL_DISPLAY_NAME, txtEditText.getText().toString()
										.trim());
								AthletesTable.UpdateAthleteFieldValues(getActivity(), mItemID, newFieldValues);
								break;

							case MySettings.DIALOG_EDIT_TEXT_MEETS:
								newFieldValues.put(MeetsTable.COL_TITLE, txtEditText.getText().toString());
								MeetsTable.UpdateMeetFieldValues(getActivity(), mItemID, newFieldValues);
								break;

							case MySettings.DIALOG_EDIT_TEXT_EVENTS:
								newFieldValues.put(EventsTable.COL_EVENT_LONG_TITLE, txtEditText.getText().toString());
								EventsTable.UpdateEventFieldValues(getActivity(), mItemID, newFieldValues);
								break;

							default:
								break;
						}
					}

					getDialog().dismiss();
				}
			});
		}

		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Do Nothing
					getDialog().dismiss();
				}
			});
		}
		getDialog().setTitle(mDialogTitle);

		// Show soft keyboard automatically
		txtEditText.requestFocus();
		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return view;
	}

	@Override
	public void onDestroy() {
		MyLog.i("EditText_DialogFragment", "onDestroy");
		switch (mDialogType) {

			case MySettings.DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME:
				AthletesTable.CheckAllAthletes(getActivity(), false);
				break;

			case MySettings.DIALOG_EDIT_TEXT_MEETS:
				MeetsTable.CheckAllMeetTitles(getActivity(), false);
				break;

			case MySettings.DIALOG_EDIT_TEXT_EVENTS:
				EventsTable.CheckAllEvents(getActivity(), false);
				break;

			default:
				break;
		}

		super.onDestroy();
	}
}
