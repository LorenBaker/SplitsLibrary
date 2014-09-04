package com.lbconsulting.splits.fragments;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.database.EventsTable;

import de.greenrobot.event.EventBus;

public class Create_Event_Fragment extends Fragment implements OnClickListener, OnItemSelectedListener, OnKeyListener {

	private TextView tvEventTitle;
	private Button btnCreateEvent;
	private EditText txtDistance;
	private EditText txtStyle;
	private Spinner spinUnits;
	private EditText txtLapDistance;
	private CheckBox ckIsRelay;
	private Button btnOkFinished;

	private int mMeetType;

	private String YardsAbbr;
	private String MetersAbbr;
	private String BY;

	public Create_Event_Fragment() {
		// Empty constructor
	}

	public static Create_Event_Fragment newInstance() {
		MyLog.i("Create_Event_Fragment", "newInstance()");
		Create_Event_Fragment fragment = new Create_Event_Fragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Create_Event_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_create_event, container, false);

		Resources res = getActivity().getResources();
		YardsAbbr = res.getStringArray(R.array.units)[1];
		YardsAbbr = YardsAbbr.substring(0, 1);
		MetersAbbr = res.getStringArray(R.array.units)[0];
		MetersAbbr = MetersAbbr.substring(0, 1);
		BY = res.getString(R.string.event_by);

		tvEventTitle = (TextView) view.findViewById(R.id.tvEventTitle);

		btnCreateEvent = (Button) view.findViewById(R.id.btnCreateEvent);
		btnCreateEvent.setOnClickListener(this);

		txtDistance = (EditText) view.findViewById(R.id.txtDistance);
		txtStyle = (EditText) view.findViewById(R.id.txtStyle);
		txtLapDistance = (EditText) view.findViewById(R.id.txtLapDistance);

		ckIsRelay = (CheckBox) view.findViewById(R.id.ckIsRelay);

		txtDistance.setOnKeyListener(this);
		txtStyle.setOnKeyListener(this);
		txtLapDistance.setOnKeyListener(this);

		spinUnits = (Spinner) view.findViewById(R.id.spinUnits);
		spinUnits.setOnItemSelectedListener(this);

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new ShowPreviousFragment());
				}
			});
		}

		return view;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		CreateEventTitle();
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		CreateEventTitle();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing
	}

	private void CreateEventTitle() {

		StringBuilder sb = new StringBuilder();

		String units = "";
		switch (spinUnits.getSelectedItemPosition()) {
			case MySettings.METERS:
				units = MetersAbbr;
				break;
			default:
				// yards
				units = YardsAbbr;
				break;
		}

		int distance = 0;
		if (!txtDistance.getText().toString().isEmpty()) {
			distance = Integer.parseInt(txtDistance.getText().toString());
		}

		int lapDistance = 0;
		if (!txtLapDistance.getText().toString().isEmpty()) {
			lapDistance = Integer.parseInt(txtLapDistance.getText().toString());
		}

		if (distance > 0) {
			if (lapDistance > 0) {
				if (distance != lapDistance) {

					sb.append(txtDistance.getText().toString()).append(" ")
							.append(units).append(" ")
							.append(txtStyle.getText().toString())
							.append(" [").append(BY)
							.append(txtLapDistance.getText().toString())
							.append(R.string.s_close_right_brack_text);
				} else {
					// lapDistance == distance
					sb.append(txtDistance.getText().toString()).append(" ")
							.append(units).append(" ")
							.append(txtStyle.getText().toString());
				}
			} else {
				// distance>0 but lapDistance=0
				sb.append(txtDistance.getText().toString()).append(" ")
						.append(units).append(" ")
						.append(txtStyle.getText().toString());
			}
		}

		tvEventTitle.setText(sb.toString());
	}

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		int id = btn.getId();

		if (id == R.id.btnCreateEvent) {
			if (txtStyle.getText().toString().isEmpty()) {
				ShowErrorDialog(getActivity().getString(R.string.btnCreateEvent_style_not_provided));
				return;
			}
			if (txtDistance.getText().toString().isEmpty()) {
				ShowErrorDialog(getActivity().getString(R.string.btnCreateEvent_distance_not_provided));
				return;
			}
			if (txtLapDistance.getText().toString().isEmpty()) {
				ShowErrorDialog(getActivity().getString(R.string.btnCreateEvent_lap_distance_not_provided));
				return;
			}
			int distance = Integer.parseInt(txtDistance.getText().toString());
			if (distance == 0) {
				ShowErrorDialog(getActivity().getString(R.string.btnCreateEvent_distance_must_be_greater_than_0));
				return;
			}
			int lapDistance = Integer.parseInt(txtLapDistance.getText().toString());
			if (lapDistance > distance) {
				ShowErrorDialog(getActivity().getString(
						R.string.btnCreateEvent_lap_distance_may_not_exceed_event_distance));
				return;
			}
			if (lapDistance == distance) {
				// a lap distance of zero makes the Stop button appear
				// instead of the split button after the race starts
				lapDistance = 0;
			}
			String shortTitle = EventsTable.MakeShortTitle(getActivity(), distance,
					spinUnits.getSelectedItemPosition(),
					txtStyle.getText().toString(), ckIsRelay.isChecked());
			long newEventID = EventsTable.CreatEvent(getActivity(),
					tvEventTitle.getText().toString(),
					shortTitle,
					mMeetType,
					distance,
					txtStyle.getText().toString(),
					spinUnits.getSelectedItemPosition(),
					lapDistance,
					ckIsRelay.isChecked());
			if (newEventID > 0) {

				// send event to Flurry
				Map<String, String> raceParams = new HashMap<String, String>();
				raceParams.put("NewEventTitle", tvEventTitle.getText().toString());
				FlurryAgent.logEvent("NewEventCreated", raceParams);

				String toastMessage = new StringBuilder()
						.append(tvEventTitle.getText().toString())
						.append(System.getProperty("line.separator"))
						.append(getActivity().getResources().getString(R.string.successfully_created_text))
						.toString();
				Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(),
						getActivity().getResources().getString(R.string.btnCreateEvent_failed_to_create_event),
						Toast.LENGTH_SHORT).show();
			}
			txtDistance.setText("");
			txtStyle.setText("");
			txtLapDistance.setText("");
			tvEventTitle.setText("");
		}

	}

	private void ShowErrorDialog(String message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(getActivity().getResources().getString(R.string.showErrorDialog_unable_to_create_event));
		alert.setMessage(message);
		alert.setIcon(R.drawable.ic_action_error);

		String ok = getActivity().getResources().getString(R.string.btnOK_text);
		alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Create_Event_Fragment", "onActivityCreated()");
		Resources res = getActivity().getResources();
		String[] arrayUnits = res.getStringArray(R.array.units);

		ArrayAdapter<String> adapterUnits = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, arrayUnits);

		spinUnits.setAdapter(adapterUnits);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Create_Event_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));

		spinUnits.setSelection(MySettings.getCreateEventUnitsPosition(spinUnits));

		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Create_Event_Fragment", "onPause()");
		// save activity state
		Bundle createEventBundle = new Bundle();
		createEventBundle.putLong(MySettings.STATE_CREATE_EVENT_UNITS_ID, spinUnits.getSelectedItemId());
		MySettings.set("", createEventBundle);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Create_Event_Fragment", "onDestroy()");
		super.onDestroy();
	}

}
