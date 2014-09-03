package com.lbconsulting.splits.dialogs;

import java.util.HashMap;
import java.util.Map;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.RelaysTable;

public class DeleteRace_DialogFragment extends DialogFragment {

	private TextView tvMessage;
	private Button btnDelete;
	private Button btnCancel;

	private String mDialogTitle;
	private String mMessage;
	private long mRaceID;
	private boolean mIsRelay;

	private static final String DIALOG_TITLE = "dialogTitle";
	private static final String DIALOG_MESSAGE = "dialogMessage";
	private static final String DIALOG_RACE_ID = "dialogRaceID";
	private static final String DIALOG_IS_RELAY = "dialogIsRelay";

	public DeleteRace_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of DeleteRace_DialogFragment
	 * 
	 * @param itemID
	 * @return DeleteRace_DialogFragment
	 */
	public static DeleteRace_DialogFragment newInstance(String dialogTitle, String dialogMessage, long raceID,
			boolean isRelay) {
		DeleteRace_DialogFragment fragment = new DeleteRace_DialogFragment();
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putString(DIALOG_MESSAGE, dialogMessage);
		args.putLong(DIALOG_RACE_ID, raceID);
		args.putBoolean(DIALOG_IS_RELAY, isRelay);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("DeleteRace_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = args.getString(DIALOG_TITLE);
			mMessage = args.getString(DIALOG_MESSAGE, "");
			mRaceID = args.getLong(DIALOG_RACE_ID, -1);
			mIsRelay = args.getBoolean(DIALOG_IS_RELAY);
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
					Map<String, String> deleteRaceParams = new HashMap<String, String>();
					if (mIsRelay) {
						RelaysTable.deleteRelayRace(getActivity(), mRaceID);
						String eventShortTitle = RelaysTable.getEventShortTitle(getActivity(), mRaceID);
						deleteRaceParams.put("DeleteRelay", eventShortTitle);
					} else {
						RacesTable.deleteRace(getActivity(), mRaceID);
						String eventShortTitle = RacesTable.getEventShortTitle(getActivity(), mRaceID);
						deleteRaceParams.put("DeleteRace", eventShortTitle);
					}

					// send event to Flurry
					FlurryAgent.logEvent("RaceEventDeleted", deleteRaceParams);

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
		MyLog.i("DeleteRace_DialogFragment", "onDestroy");
		super.onDestroy();
	}

}
