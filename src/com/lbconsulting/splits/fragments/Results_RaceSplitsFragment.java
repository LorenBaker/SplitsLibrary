package com.lbconsulting.splits.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.adapters.RaceSplitsCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.SplitsTable;

import de.greenrobot.event.EventBus;

public class Results_RaceSplitsFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;

	private static long mRaceID = -1;

	public static long getRaceID() {
		return mRaceID;
	}

	private String mShortRaceTitle = "";
	private long mAthleteID = -1;

	private TextView tvMeetTitleAndDate;
	private TextView tvAthleteName_RaceAndTime;
	private ListView lvAthleteRaceSplits;
	private RaceSplitsCursorAdapter mRaceSplitsCursorAdapter;
	private Button btnOkFinished;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mRaceSplitsFragmentCallbacks;

	public Results_RaceSplitsFragment() {
		// Empty constructor
	}

	public static Results_RaceSplitsFragment newInstance(long raceID) {
		MyLog.i("Results_RaceSplitsFragment", "newInstance()");

		Results_RaceSplitsFragment fragment = new Results_RaceSplitsFragment();
		// Supply listID input as an argument.
		Bundle args = new Bundle();
		args.putLong(MySettings.STATE_RACE_SPLITS_RACE_ID, raceID);
		// args.putBoolean(MySettings.STATE_RACE_SPLITS_IS_RELAY, isRelay);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Results_RaceSplitsFragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_race_splits, container, false);

		Bundle args = getArguments();
		if (args != null) {
			mRaceID = args.getLong(MySettings.STATE_RACE_SPLITS_RACE_ID, -1);
		}

		Cursor raceCursor = RacesTable.getRaceCursor(getActivity(), mRaceID);
		long meetID = -1;
		long raceDateValue = -1;
		long raceTimeValue = -1;

		java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(getActivity());

		if (raceCursor != null && raceCursor.getCount() > 0) {
			raceCursor.moveToFirst();
			meetID = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_MEET_ID));
			mAthleteID = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_ATHLETE_ID));
			raceDateValue = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_RACE_START_DATE_TIME));
			raceTimeValue = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME));
			mShortRaceTitle = raceCursor.getString(raceCursor.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
		}

		tvMeetTitleAndDate = (TextView) view.findViewById(R.id.tvMeetTitleAndDate);
		if (tvMeetTitleAndDate != null) {
			tvMeetTitleAndDate.setText(
					new StringBuilder().append(MeetsTable.getMeetTitle(getActivity(), meetID))
							.append(System.getProperty("line.separator"))
							.append(dateFormat.format(raceDateValue))
					);
		}

		tvAthleteName_RaceAndTime = (TextView) view.findViewById(R.id.tvAthleteName_RaceAndTime);
		if (tvAthleteName_RaceAndTime != null) {
			tvAthleteName_RaceAndTime.setText(getAthleteName_Race_Time(mAthleteID, mShortRaceTitle, raceTimeValue,
					mNumberFormat));
		}

		if (raceCursor != null) {
			raceCursor.close();
		}

		lvAthleteRaceSplits = (ListView) view.findViewById(R.id.lvAthleteRaceSplits);
		mRaceSplitsCursorAdapter = new RaceSplitsCursorAdapter(getActivity(), null, 0, mNumberFormat);
		lvAthleteRaceSplits.setAdapter(mRaceSplitsCursorAdapter);

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new ShowPreviousFragment());
				}
			});
		}

		mRaceSplitsFragmentCallbacks = this;
		return view;
	}

	private CharSequence getAthleteName_Race_Time(long athleteID, String shortRaceTitle, long raceTimeValue,
			int numberFormat) {

		StringBuilder sb = new StringBuilder().append(AthletesTable.getDisplayName(getActivity(), athleteID))
				.append(System.getProperty("line.separator"))
				.append(shortRaceTitle).append(": ")
				.append(DateTimeUtils.formatDuration(raceTimeValue, mNumberFormat));
		return sb.toString();

	}

	/*	public void onEvent(RaceFinalTime event) {
			tvAthleteName_RaceAndTime.setText(getAthleteName_Race_Time(mAthleteID, mShortRaceTitle,
					event.getRaceFinalTime(), mNumberFormat));
			RacesTable.setAthleteEventBestTime(getActivity(), mShortRaceTitle, mAthleteID, false, 0);
		}*/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Results_RaceSplitsFragment", "onActivityCreated()");

		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_SPLITS, null, mRaceSplitsFragmentCallbacks);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Results_RaceSplitsFragment", "onResume()");
		// EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Results_RaceSplitsFragment", "onPause()");
		// EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Results_RaceSplitsFragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Results_RaceSplitsFragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RACE_SPLITS:
				cursorLoader = SplitsTable.getAllSplits(getActivity(), mRaceID, false,
						SplitsTable.SORT_ORDER_SPLIT_ID_ASC);
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Results_RaceSplitsFragment", "onLoadFinished. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RACE_SPLITS:
				mRaceSplitsCursorAdapter.swapCursor(newCursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Results_RaceSplitsFragment", "onLoadFinished. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RACE_SPLITS:
				mRaceSplitsCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
