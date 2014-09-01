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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.activites.MainActivity;
import com.lbconsulting.splits.adapters.AllRacesAthletesSpinnerAdapter;
import com.lbconsulting.splits.adapters.AllRacesCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowRaceSplits;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.RacesTable;

import de.greenrobot.event.EventBus;

public class Results_AllRaces_Fragment extends Fragment implements LoaderCallbacks<Cursor> {

	private int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;

	private LinearLayout llBestTimeHeader;
	private Spinner spinResultsAthletes;
	private ListView lvResultsTimes;
	private static RadioButton rbInRelays;
	private RadioButton rbIndividualEvents;
	// private boolean mProhibitBestTimesRadioButtonUpdate = false;

	private static long mAthleteID;

	public static long getAthleteID() {
		return mAthleteID;
	}

	private int mMeetType;

	public static boolean IsRelay() {
		return rbInRelays.isChecked();
	}

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mAllRacesFragmentCallbacks;

	private AllRacesAthletesSpinnerAdapter mAllRacesAthletesSpinnerAdapter;
	private boolean mFirstTimeLoadingAllRacesAthletes = false;

	private AllRacesCursorAdapter mAllRacesCursorAdapter;

	public Results_AllRaces_Fragment() {
		// Empty constructor
	}

	public static Results_AllRaces_Fragment newInstance() {
		MyLog.i("Results_AllRaces_Fragment", "newInstance()");

		Results_AllRaces_Fragment fragment = new Results_AllRaces_Fragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Results_AllRaces_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_results, container, false);

		llBestTimeHeader = (LinearLayout) view.findViewById(R.id.llBestTimeHeader);
		if (llBestTimeHeader != null) {
			llBestTimeHeader.setVisibility(View.GONE);
		}

		spinResultsAthletes = (Spinner) view.findViewById(R.id.spinResultsAthletes);
		if (spinResultsAthletes != null) {
			mAllRacesAthletesSpinnerAdapter = new AllRacesAthletesSpinnerAdapter(getActivity(), null, 0);
			spinResultsAthletes.setAdapter(mAllRacesAthletesSpinnerAdapter);

			spinResultsAthletes.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view1, int pos, long id) {
					mAthleteID = id;
					mLoaderManager
							.restartLoader(MySettings.LOADER_FRAG_RESULTS_ALL_RACES, null, mAllRacesFragmentCallbacks);

					Bundle allRacesBundle = new Bundle();
					allRacesBundle.putLong(MySettings.STATE_BEST_TIMES_ATHLETE_ID, mAthleteID);
					MySettings.set("", allRacesBundle);

					// EventBus.getDefault().post(new UpdateBestTimesAthlete(mAthleteID, pos));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// Do nothing
				}
			});
		}

		lvResultsTimes = (ListView) view.findViewById(R.id.lvResultsTimes);
		if (lvResultsTimes != null) {
			mAllRacesCursorAdapter = new AllRacesCursorAdapter(getActivity(), mNumberFormat, null, 0);
			lvResultsTimes.setAdapter(mAllRacesCursorAdapter);
			lvResultsTimes.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int postion, long id) {
					int splitsResultsFragment = MainActivity.FRAG_RESULTS_RACE_SPLITS;
					if (rbInRelays.isChecked()) {
						long relayID = RacesTable.getRelayID(getActivity(), id);
						splitsResultsFragment = MainActivity.FRAG_RESULTS_RELAY_SPLITS;
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

					mLoaderManager
							.restartLoader(MySettings.LOADER_FRAG_RESULTS_ALL_RACES, null,
									mAllRacesFragmentCallbacks);
					/*if (!mProhibitBestTimesRadioButtonUpdate) {
						EventBus.getDefault().post(new UpdateBestTimesRadioButtons(isChecked));
					}*/
				}
			});
		}

		rbIndividualEvents = (RadioButton) view.findViewById(R.id.rbIndividualEvents);

		mAllRacesFragmentCallbacks = this;
		return view;
	}

	/*	public void onEvent(UpdateAllRacesAthlete event) {
			mAthleteID = event.getAthleteID();
			spinResultsAthletes.setSelection(event.getSpinnerPosition());
		}*/

	/*	public void onEvent(UpdateAllRacesRadioButtons event) {
			mProhibitBestTimesRadioButtonUpdate = true;
			if (event.isRelaysChecked()) {
				rbInRelays.setChecked(true);
			} else {
				rbIndividualEvents.setChecked(true);
			}
			mProhibitBestTimesRadioButtonUpdate = false;
		}*/

	private void setRadioButtons(boolean inRelays) {
		if (inRelays) {
			rbInRelays.setChecked(true);
		} else {
			rbIndividualEvents.setChecked(true);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Results_AllRaces_Fragment", "onActivityCreated()");
		mLoaderManager = getLoaderManager();
		mLoaderManager
				.initLoader(MySettings.LOADER_FRAG_RESULTS_ALL_RACES_ATHLETES, null, mAllRacesFragmentCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RESULTS_ALL_RACES, null, mAllRacesFragmentCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Results_AllRaces_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));
		mFirstTimeLoadingAllRacesAthletes = true;
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RESULTS_ALL_RACES, null, mAllRacesFragmentCallbacks);
		boolean inRelays = MySettings.getBestTimeInRelays();
		setRadioButtons(inRelays);
		// EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Results_AllRaces_Fragment", "onPause()");

		Bundle allRacesBundle = new Bundle();
		allRacesBundle.putLong(MySettings.STATE_BEST_TIMES_ATHLETE_ID, mAthleteID);
		allRacesBundle.putBoolean(MySettings.STATE_BEST_TIMES_IN_RELAYS, rbInRelays.isChecked());
		MySettings.set("", allRacesBundle);

		// EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Results_AllRaces_Fragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Results_AllRaces_Fragment", "onCreateLoader. LoaderId = " + id);
		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES_ATHLETES:
				cursorLoader = AthletesTable.getAllAthletesExcludingDefault(getActivity(),
						MySettings.SELECTED_ATHLETES_ONLY, AthletesTable.SORT_ORDER_ATHLETE_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES:
				cursorLoader = RacesTable.getAllRaces(getActivity(), mAthleteID, mMeetType, rbInRelays.isChecked());
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Results_AllRaces_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (id) {
			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES_ATHLETES:
				mAllRacesAthletesSpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAllRacesAthletes) {
					spinResultsAthletes.setSelection(MySettings.getBestTimeAthletesPosition(spinResultsAthletes));
					mFirstTimeLoadingAllRacesAthletes = false;
				}
				break;

			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES:
				mAllRacesCursorAdapter.swapCursor(newCursor);
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
			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES_ATHLETES:
				mAllRacesAthletesSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RESULTS_ALL_RACES:
				mAllRacesCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
