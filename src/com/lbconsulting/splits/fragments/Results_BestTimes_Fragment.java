package com.lbconsulting.splits.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.activites.MainActivity;
import com.lbconsulting.splits.adapters.BestTimesAthletesSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.BestTimesCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowRaceSplits;
import com.lbconsulting.splits.classes.SplitsEvents.UpdateBestTimes;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.RacesTable;

import de.greenrobot.event.EventBus;

public class Results_BestTimes_Fragment extends Fragment implements LoaderCallbacks<Cursor> {

	private int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;

	private Spinner spinResultsAthletes;
	private ListView lvResultsTimes;
	private static RadioButton rbInRelays;
	private RadioButton rbIndividualEvents;
	// private boolean mProhibitAllRacesRadioButtonUpdate = false;

	private static long mAthleteID;

	public static long getAthleteID() {
		return mAthleteID;
	}

	private int mMeetType;

	public static boolean IsRelay() {
		return rbInRelays.isChecked();
	}

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mBestTimesFragmentCallbacks;

	private BestTimesAthletesSpinnerCursorAdapter mBestTimesAthletesSpinnerAdapter;
	private boolean mFirstTimeLoadingBestTimesAthletes = false;

	private BestTimesCursorAdapter mBestTimesCursorAdapter;

	public Results_BestTimes_Fragment() {
		// Empty constructor
	}

	public static Results_BestTimes_Fragment newInstance() {
		MyLog.i("Results_BestTimes_Fragment", "newInstance()");

		Results_BestTimes_Fragment fragment = new Results_BestTimes_Fragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Results_BestTimes_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_results, container, false);

		spinResultsAthletes = (Spinner) view.findViewById(R.id.spinResultsAthletes);
		if (spinResultsAthletes != null) {
			mBestTimesAthletesSpinnerAdapter = new BestTimesAthletesSpinnerCursorAdapter(getActivity(), null, 0);
			spinResultsAthletes.setAdapter(mBestTimesAthletesSpinnerAdapter);

			spinResultsAthletes.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view1, int pos, long id) {
					mAthleteID = id;
					mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES, null,
							mBestTimesFragmentCallbacks);

					Bundle bestTimesBundle = new Bundle();
					bestTimesBundle.putLong(MySettings.STATE_BEST_TIMES_ATHLETE_ID, mAthleteID);
					MySettings.set("", bestTimesBundle);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// Do nothing
				}
			});
		}

		lvResultsTimes = (ListView) view.findViewById(R.id.lvResultsTimes);
		if (lvResultsTimes != null) {
			mBestTimesCursorAdapter = new BestTimesCursorAdapter(getActivity(), mNumberFormat, null, 0);
			lvResultsTimes.setAdapter(mBestTimesCursorAdapter);
			lvResultsTimes.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int postion, long id) {
					int splitsResultsFragment = MainActivity.FRAG_RESULTS_RACE_SPLITS;
					if (rbInRelays.isChecked()) {
						splitsResultsFragment = MainActivity.FRAG_RESULTS_RELAY_SPLITS;
						long relayID = RacesTable.getRelayID(getActivity(), id);
						EventBus.getDefault().post(new ShowRaceSplits(relayID, splitsResultsFragment));
					} else {
						EventBus.getDefault().post(new ShowRaceSplits(id, splitsResultsFragment));
					}
				}
			});

		}

		rbInRelays = (RadioButton) view.findViewById(R.id.rbInRelays);
		if (rbInRelays != null) {
			rbInRelays.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES, null,
							mBestTimesFragmentCallbacks);
				}
			});
		}

		rbIndividualEvents = (RadioButton) view.findViewById(R.id.rbIndividualEvents);

		mBestTimesFragmentCallbacks = this;
		return view;
	}

	private void setRadioButtons(boolean inRelays) {
		if (inRelays) {
			rbInRelays.setChecked(true);
		} else {
			rbIndividualEvents.setChecked(true);
		}
	}

	public void onEvent(UpdateBestTimes event) {
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES, null, mBestTimesFragmentCallbacks);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Results_BestTimes_Fragment", "onActivityCreated()");
		mLoaderManager = getLoaderManager();
		mLoaderManager
				.initLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES_ATHLETES, null, mBestTimesFragmentCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES, null, mBestTimesFragmentCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Results_BestTimes_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));
		boolean inRelays = MySettings.getBestTimeInRelays();
		setRadioButtons(inRelays);
		mFirstTimeLoadingBestTimesAthletes = true;
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RESULTS_BEST_TIMES, null, mBestTimesFragmentCallbacks);
		EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Results_BestTimes_Fragment", "onPause()");

		Bundle bestTimesBundle = new Bundle();
		bestTimesBundle.putLong(MySettings.STATE_BEST_TIMES_ATHLETE_ID, mAthleteID);
		bestTimesBundle.putBoolean(MySettings.STATE_BEST_TIMES_IN_RELAYS, rbInRelays.isChecked());
		MySettings.set("", bestTimesBundle);

		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Results_BestTimes_Fragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Results_BestTimes_Fragment", "onCreateLoader. LoaderId = " + id);
		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES_ATHLETES:
				cursorLoader = AthletesTable.getAllAthletesExcludingDefault(getActivity(),
						MySettings.SELECTED_ATHLETES_ONLY, AthletesTable.SORT_ORDER_ATHLETE_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES:
				cursorLoader = RacesTable.getAllBestTimeRaces(getActivity(), mAthleteID, mMeetType,
						rbInRelays.isChecked());
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Results_BestTimes_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (id) {
			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES_ATHLETES:
				mBestTimesAthletesSpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingBestTimesAthletes) {
					spinResultsAthletes.setSelection(MySettings.getBestTimeAthletesPosition(spinResultsAthletes));
					mFirstTimeLoadingBestTimesAthletes = false;
				}
				break;

			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES:
				mBestTimesCursorAdapter.swapCursor(newCursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Meet_Maker_Fragment", "onLoaderReset. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES_ATHLETES:
				mBestTimesAthletesSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RESULTS_BEST_TIMES:
				mBestTimesCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
