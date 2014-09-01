package com.lbconsulting.splits.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.MeetsTable;

public class Deletion_Alert_DialogFragment extends DialogFragment {

	private TextView tvMessage;
	private Button btnDelete;
	private Button btnCancel;

	private String mDialogTitle;
	private String mMessage;
	private int mMeetType;
	private int mDialogType;

	private static final String DIALOG_TITLE = "dialogTitle";
	private static final String DIALOG_MESSAGE = "dialogMessage";
	private static final String DIALOG_MEET_TYPE = "dialogMeetType";
	private static final String DIALOG_TYPE = "dialogType";

	public Deletion_Alert_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of Deletion_Alert_DialogFragment
	 * 
	 * @param itemID
	 * @return Deletion_Alert_DialogFragment
	 */
	public static Deletion_Alert_DialogFragment newInstance(String dialogTitle, String dialogMessage,
			int meetType, int dialogType) {
		Deletion_Alert_DialogFragment fragment = new Deletion_Alert_DialogFragment();
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putString(DIALOG_MESSAGE, dialogMessage);
		args.putInt(DIALOG_MEET_TYPE, meetType);
		args.putInt(DIALOG_TYPE, dialogType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Deletion_Alert_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = args.getString(DIALOG_TITLE);
			mMessage = args.getString(DIALOG_MESSAGE, "");
			mMeetType = args.getInt(DIALOG_MEET_TYPE, -1);
			mDialogType = args.getInt(DIALOG_TYPE, -1);
		}

		View view = inflater.inflate(R.layout.dialog_deletion_alert, container);
		tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		if (tvMessage != null) {
			tvMessage.setText(mMessage);
		}

		btnDelete = (Button) view.findViewById(R.id.btnDelete);
		if (btnDelete != null) {
			btnDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (mDialogType) {

						case MySettings.DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME:
							AthletesTable.DeleteAllCheckedAthletes(getActivity());
							break;

						case MySettings.DIALOG_EDIT_TEXT_MEETS:
							MeetsTable.DeleteAllCheckedMeets(getActivity(), mMeetType);
							break;

						case MySettings.DIALOG_EDIT_TEXT_EVENTS:
							EventsTable.DeleteAllCheckedEvents(getActivity(), mMeetType);
							break;

						default:
							break;
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

		return view;
	}

	@Override
	public void onDestroy() {
		MyLog.i("Deletion_Alert_DialogFragment", "onDestroy");
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
