package com.lbconsulting.splits.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.R.string;
import com.lbconsulting.splits.activites.MainActivity;
import com.lbconsulting.splits.adapters.AthleteSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.EventsSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.MeetsSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.RaceSplitsCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.Race;
import com.lbconsulting.splits.classes.SplitsBuild;
import com.lbconsulting.splits.classes.SplitsEvents.ChangeActionBarTitle;
import com.lbconsulting.splits.classes.SplitsEvents.ClearRace;
import com.lbconsulting.splits.classes.SplitsEvents.RaceComplete;
import com.lbconsulting.splits.classes.SplitsEvents.ShowAthletesFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowEventsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowMeetsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowSplitButton;
import com.lbconsulting.splits.classes.SplitsEvents.ShowStopButton;
import com.lbconsulting.splits.classes.SplitsEvents.SplitFragmentOnResume;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.LastEventAthletesTable;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.SplitsTable;
import com.lbconsulting.splits.dialogs.PlayStore_DialogFragment;

import de.greenrobot.event.EventBus;

public class Race_Timer_Fragment extends Fragment implements OnClickListener, OnItemSelectedListener,
		OnLongClickListener, LoaderCallbacks<Cursor> {

	private int mMeetType;
	private boolean mResetAthletes = false;
	private int mRaceCount = 0;
	private long mRaceStartTime = -1;
	private CharSequence mActiveFragmentTitle;

	private final int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;

	private boolean isRaceRunning() {
		return mAthlete1Race.isRaceRunning() || mAthlete2Race.isRaceRunning();
	}

	private boolean areSpinnersEnabled = true;

	private Handler mHandler = new Handler();
	private long mClockElapsedTime;
	private final int REFRESH_RATE = 100;

	private Race mAthlete1Race;
	private long mAthlete1StartingBestTime;
	private Race mAthlete2Race;
	private long mAthlete2StartingBestTime;;
	private String mEventShortTitle = "";

	private boolean mFirstTimeLoadingMeetID = false;
	private boolean mFirstTimeLoadingEventID = false;
	private boolean mFirstTimeLoadingAthlete1ID = false;
	private boolean mFirstTimeLoadingAthlete2ID = false;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mRaceStartTimerCallbacks;

	private MeetsSpinnerCursorAdapter mMeetsSpinnerAdapter;
	private EventsSpinnerCursorAdapter mEventsSpinnerAdapter;

	private Spinner spinRaceMeets;
	private Spinner spinRaceEvents;
	private Spinner spinAthlete1;
	private Spinner spinAthlete2;

	private TextView tvRacetime;

	private LinearLayout llAthlete1RaceTable;
	private ListView lvAthlete1RaceSplits;
	private TextView tvAthlete1BestTime;
	private Button btnAthlete1Start;
	private Button btnAthlete1Stop;
	private Button btnAthlete1Split;
	private RaceSplitsCursorAdapter mAthlete1SplitsCursorAdapter;
	private AthleteSpinnerCursorAdapter mAthlete1SpinnerAdapter;

	private Vibrator mVibrator = null;

	private LinearLayout llAthlete2RaceTable;
	private ListView lvAthlete2RaceSplits;
	private TextView tvAthlete2BestTime;
	private Button btnAthlete2Start;
	private Button btnAthlete2Stop;
	private Button btnAthlete2Split;
	private RaceSplitsCursorAdapter mAthlete2SplitsCursorAdapter;
	private AthleteSpinnerCursorAdapter mAthlete2SpinnerAdapter;

	public Race_Timer_Fragment() {
		// Empty constructor
	}

	public static Race_Timer_Fragment newInstance() {
		MyLog.i("Race_Timer_Fragment", "newInstance()");
		Race_Timer_Fragment fragment = new Race_Timer_Fragment();
		return fragment;
	}

	public static int getFragmentID() {
		return MainActivity.FRAG_RACE_TIMER;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Race_Timer_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_race_timer, container, false);

		mEventShortTitle = getActivity().getResources().getString(R.string.not_available_text);

		spinRaceMeets = (Spinner) view.findViewById(R.id.spinRaceMeets);
		spinRaceMeets.setOnLongClickListener(this);
		mMeetsSpinnerAdapter = new MeetsSpinnerCursorAdapter(getActivity(), null, 0);
		spinRaceMeets.setAdapter(mMeetsSpinnerAdapter);
		spinRaceMeets.setOnItemSelectedListener(this);

		spinRaceEvents = (Spinner) view.findViewById(R.id.spinRaceEvents);
		spinRaceEvents.setOnLongClickListener(this);
		mEventsSpinnerAdapter = new EventsSpinnerCursorAdapter(getActivity(), null, 0);
		spinRaceEvents.setAdapter(mEventsSpinnerAdapter);
		spinRaceEvents.setOnItemSelectedListener(this);

		llAthlete1RaceTable = (LinearLayout) view.findViewById(R.id.llAthlete1RaceTable);
		lvAthlete1RaceSplits = (ListView) view.findViewById(R.id.lvAthlete1RaceSplits);
		mAthlete1SplitsCursorAdapter = new RaceSplitsCursorAdapter(getActivity(), null, 0, mNumberFormat);
		lvAthlete1RaceSplits.setAdapter(mAthlete1SplitsCursorAdapter);

		llAthlete2RaceTable = (LinearLayout) view.findViewById(R.id.llAthlete2RaceTable);
		lvAthlete2RaceSplits = (ListView) view.findViewById(R.id.lvAthlete2RaceSplits);
		mAthlete2SplitsCursorAdapter = new RaceSplitsCursorAdapter(getActivity(), null, 0, mNumberFormat);
		lvAthlete2RaceSplits.setAdapter(mAthlete2SplitsCursorAdapter);

		tvRacetime = (TextView) view.findViewById(R.id.tvRacetime);

		tvAthlete1BestTime = (TextView) view.findViewById(R.id.tvAthlete1BestTime);
		tvAthlete2BestTime = (TextView) view.findViewById(R.id.tvAthlete2BestTime);

		btnAthlete1Start = (Button) view.findViewById(R.id.btnAthlete1Start);
		btnAthlete1Stop = (Button) view.findViewById(R.id.btnAthlete1Stop);
		btnAthlete1Split = (Button) view.findViewById(R.id.btnAthlete1Split);
		spinAthlete1 = (Spinner) view.findViewById(R.id.spinAthlete1);
		mAthlete1SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
		spinAthlete1.setAdapter(mAthlete1SpinnerAdapter);
		spinAthlete1.setOnLongClickListener(this);
		btnAthlete1Start.setOnClickListener(this);
		btnAthlete1Stop.setOnClickListener(this);
		btnAthlete1Split.setOnClickListener(this);
		spinAthlete1.setOnItemSelectedListener(this);

		btnAthlete2Start = (Button) view.findViewById(R.id.btnAthlete2Start);
		btnAthlete2Stop = (Button) view.findViewById(R.id.btnAthlete2Stop);
		btnAthlete2Split = (Button) view.findViewById(R.id.btnAthlete2Split);
		spinAthlete2 = (Spinner) view.findViewById(R.id.spinAthlete2);
		mAthlete2SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
		spinAthlete2.setAdapter(mAthlete2SpinnerAdapter);
		spinAthlete2.setOnLongClickListener(this);
		btnAthlete2Start.setOnClickListener(this);
		btnAthlete2Stop.setOnClickListener(this);
		btnAthlete2Split.setOnClickListener(this);
		spinAthlete2.setOnItemSelectedListener(this);

		mAthlete1Race = new Race(getActivity());
		mAthlete1Race.setSplitTablePosition(1);
		mAthlete2Race = new Race(getActivity());
		mAthlete2Race.setSplitTablePosition(2);

		mRaceStartTimerCallbacks = this;
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Race_Timer_Fragment", "onActivityCreated()");

		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_MEETS, null, mRaceStartTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_EVENTS, null, mRaceStartTimerCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS, null, mRaceStartTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS, null, mRaceStartTimerCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1, null, mRaceStartTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2, null, mRaceStartTimerCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Race_Timer_Fragment", "onResume()");

		EventBus.getDefault().register(this);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer
				.parseInt(sharedPrefs.getString(MySettings.KEY_MEET_TYPE, String.valueOf(MySettings.SWIM_MEET)));

		mResetAthletes = sharedPrefs.getBoolean(MySettings.KEY_RESET_ATHLETES, false);

		mFirstTimeLoadingMeetID = true;
		mFirstTimeLoadingEventID = true;
		mFirstTimeLoadingAthlete1ID = true;
		mFirstTimeLoadingAthlete2ID = true;

		mRaceCount = MySettings.getRaceCount();

		mAthlete1Race = new Race(getActivity());
		mAthlete2Race = new Race(getActivity());

		mAthlete1Race.setState(MySettings.getRaceLongValues("Athlete1"),
				MySettings.getRaceIntValues("Athlete1"),
				MySettings.getAthleteBestTimeText("Athlete1"));
		mAthlete1Race.setSplitTablePosition(1);

		mAthlete2Race.setState(MySettings.getRaceLongValues("Athlete2"),
				MySettings.getRaceIntValues("Athlete2"),
				MySettings.getAthleteBestTimeText("Athlete2"));
		mAthlete2Race.setSplitTablePosition(2);

		long meetID = MySettings.getMeetID();
		mAthlete1Race.setMeetID(meetID);
		mAthlete2Race.setMeetID(meetID);

		if (mAthlete1Race.getStartTime() > 0) {
			mRaceStartTime = mAthlete1Race.getStartTime();
		} else {
			mRaceStartTime = mAthlete2Race.getStartTime();
		}

		boolean isAthlete1StartButtonVisible = MySettings.isAthlete1StartButtonVisible();
		boolean isAthlete1SplitButtonVisible = MySettings.isAthlete1SplitButtonVisible();
		boolean isAthlete1StopButtonVisible = MySettings.isAthlete1StopButtonVisible();
		boolean isAthlete1RaceTableVisible = MySettings.isAthlete1RaceTableVisible();
		if (isAthlete1StartButtonVisible) {
			btnAthlete1Start.setVisibility(View.VISIBLE);
		} else {
			btnAthlete1Start.setVisibility(View.GONE);
		}
		if (isAthlete1SplitButtonVisible) {
			btnAthlete1Split.setVisibility(View.VISIBLE);
		} else {
			btnAthlete1Split.setVisibility(View.GONE);
		}

		if (isAthlete1StopButtonVisible) {
			btnAthlete1Stop.setVisibility(View.VISIBLE);
		} else {
			btnAthlete1Stop.setVisibility(View.GONE);
		}
		if (isAthlete1RaceTableVisible) {
			llAthlete1RaceTable.setVisibility(View.VISIBLE);
			if (mAthlete1Race.isNewBestTime()) {
				tvAthlete1BestTime.setBackgroundResource(R.drawable.rec_bkgrd_green_stroke_red);
				tvAthlete1BestTime.setTextColor(getResources().getColor(R.color.white));
			} else {
				tvAthlete1BestTime.setBackgroundResource(R.drawable.rec_bkgrd_smoke_white_no_stroke);
				tvAthlete1BestTime.setTextColor(getResources().getColor(R.color.black));
			}
			tvAthlete1BestTime.setText(mAthlete1Race.getBestTimeText());
			if (!isAthlete1StartButtonVisible) {
				tvAthlete1BestTime.setVisibility(View.VISIBLE);
			}
		} else {
			llAthlete1RaceTable.setVisibility(View.GONE);
		}

		if (MySettings.areSpinnersEnabled()) {
			EnableSpinners();
		} else {
			DisableSpinners();
		}

		boolean isAthlete2StartButtonVisible = MySettings.isAthlete2StartButtonVisible();
		boolean isAthlete2SplitButtonVisible = MySettings.isAthlete2SplitButtonVisible();
		boolean isAthlete2StopButtonVisible = MySettings.isAthlete2StopButtonVisible();
		boolean isAthlete2RaceTableVisible = MySettings.isAthlete2RaceTableVisible();
		if (isAthlete2StartButtonVisible) {
			btnAthlete2Start.setVisibility(View.VISIBLE);
		} else {
			btnAthlete2Start.setVisibility(View.GONE);
		}
		if (isAthlete2SplitButtonVisible) {
			btnAthlete2Split.setVisibility(View.VISIBLE);
		} else {
			btnAthlete2Split.setVisibility(View.GONE);
		}
		if (isAthlete2StopButtonVisible) {
			btnAthlete2Stop.setVisibility(View.VISIBLE);
		} else {
			btnAthlete2Stop.setVisibility(View.GONE);
		}
		if (isAthlete2RaceTableVisible) {
			llAthlete2RaceTable.setVisibility(View.VISIBLE);
			if (mAthlete2Race.isNewBestTime()) {
				tvAthlete2BestTime.setBackgroundResource(R.drawable.rec_bkgrd_green_stroke_red);
				tvAthlete2BestTime.setTextColor(getResources().getColor(R.color.white));
			} else {
				tvAthlete2BestTime.setBackgroundResource(R.drawable.rec_bkgrd_smoke_white_no_stroke);
				tvAthlete2BestTime.setTextColor(getResources().getColor(R.color.black));
			}
			tvAthlete2BestTime.setText(mAthlete2Race.getBestTimeText());
			if (!isAthlete2StartButtonVisible) {
				tvAthlete2BestTime.setVisibility(View.VISIBLE);
			}
		} else {
			llAthlete2RaceTable.setVisibility(View.GONE);
		}

		mEventShortTitle = EventsTable.getEventShortTitle(getActivity(), mAthlete1Race.getEventID());
		mActiveFragmentTitle = getString(R.string.race_text);
		EventBus.getDefault().post(new SplitFragmentOnResume(MainActivity.FRAG_RACE_TIMER, mActiveFragmentTitle));
		if (isAthlete1StartButtonVisible || isAthlete2StartButtonVisible) {
			// EventBus.getDefault().post(new ChangeActionBarTitle(""));
		} else {
			EventBus.getDefault().post(new ChangeActionBarTitle(mEventShortTitle));
		}

		mAthlete1SplitsCursorAdapter.setNumberFormat(mNumberFormat);
		mAthlete2SplitsCursorAdapter.setNumberFormat(mNumberFormat);

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_MEETS, null, mRaceStartTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_EVENTS, null, mRaceStartTimerCallbacks);

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1, null, mRaceStartTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2, null, mRaceStartTimerCallbacks);

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS, null, mRaceStartTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS, null, mRaceStartTimerCallbacks);

		if (isRaceRunning()) {
			// start clock
			mHandler.removeCallbacks(startTimer);
			mHandler.postDelayed(startTimer, 0);

		} else {
			ShowElapsedTime();
		}

		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Race_Timer_Fragment", "onPause()");
		Bundle raceTimerBundle = new Bundle();
		raceTimerBundle.putBundle("Athlete1", mAthlete1Race.getState());
		raceTimerBundle.putBundle("Athlete2", mAthlete2Race.getState());

		raceTimerBundle.putLong(MySettings.STATE_MEET_ID, mAthlete1Race.getMeetID());

		raceTimerBundle.putBoolean(MySettings.STATE_RT_ARE_SPINNERS_ENABLED, areSpinnersEnabled);
		raceTimerBundle.putInt(MySettings.STATE_RT_RACE_COUNT, mRaceCount);

		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE1_START_BUTTTON_VISIBLE,
				btnAthlete1Start.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE1_SPLIT_BUTTTON_VISIBLE,
				btnAthlete1Split.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE1_STOP_BUTTTON_VISIBLE,
				btnAthlete1Stop.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE1_RACE_TABLE_VISIBLE,
				llAthlete1RaceTable.getVisibility() == View.VISIBLE);

		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE2_START_BUTTTON_VISIBLE,
				btnAthlete2Start.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE2_SPLIT_BUTTTON_VISIBLE,
				btnAthlete2Split.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE2_STOP_BUTTTON_VISIBLE,
				btnAthlete2Stop.getVisibility() == View.VISIBLE);
		raceTimerBundle.putBoolean(MySettings.STATE_RT_ATHLETE2_RACE_TABLE_VISIBLE,
				llAthlete2RaceTable.getVisibility() == View.VISIBLE);

		// RelayTimerBundle.putLong(MySettings.STATE_MEET_ID, mRelay.getRelayMeetID());

		MySettings.set("", raceTimerBundle);

		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Race_Timer_Fragment", "onDestroy()");
		super.onDestroy();
	}

	public void onEvent(ClearRace event) {
		// stop clock
		mHandler.removeCallbacks(startTimer);

		// delete the ongoing races from the database
		if (isRaceRunning()) {
			if (mAthlete1Race.getAthleteID() > 0) {
				RacesTable.deleteRace(getActivity(), mAthlete1Race.getAthleteID());
			}

			if (mAthlete2Race.getAthleteID() > 0) {
				RacesTable.deleteRace(getActivity(), mAthlete2Race.getAthleteID());
			}
		}

		// re-initialize race variables
		resetRaceStartTime();
		mAthlete1Race.setElapsedTime(-1);
		mAthlete2Race.setElapsedTime(-1);
		mClockElapsedTime = -1;
		mAthlete1Race.setAthleteRaceID(-1);
		mAthlete2Race.setAthleteRaceID(-1);
		ShowStartButtons();
		mAthlete1Race.Clear();
		mAthlete2Race.Clear();
		mAthlete1SplitsCursorAdapter.swapCursor(null);
		mAthlete2SplitsCursorAdapter.swapCursor(null);
		ShowZeroTime();

		if (mResetAthletes) {
			spinAthlete1.setSelection(MySettings.getIndexFromCursor(spinAthlete1, 1));
			spinAthlete2.setSelection(MySettings.getIndexFromCursor(spinAthlete2, 1));
		}
	}

	public void onEvent(ShowSplitButton event) {
		switch (event.getRaceTablePosition()) {
			case 1:
				ShowAthlete1SplitButton();
				break;

			case 2:
				ShowAthlete2SplitButton();
				break;

			default:
				MyLog.e("Race_Timer_Fragment",
						"onEvent(ShowSplitButton) Unknown RaceTablePosition:" + event.getRaceTablePosition());
				break;
		}
	}

	public void onEvent(ShowStopButton event) {
		switch (event.getRaceTablePosition()) {
			case 1:
				ShowAthlete1StopButton();
				break;

			case 2:
				ShowAthlete2StopButton();
				break;

			default:
				MyLog.e("Race_Timer_Fragment",
						"onEvent(ShowStopButton) Unknown RaceTablePosition:" + event.getRaceTablePosition());
				break;
		}
	}

	public void onEvent(RaceComplete event) {
		switch (event.getRaceTablePosition()) {
			case 1:
				btnAthlete1Stop.setVisibility(View.GONE);
				if (mAthlete1Race.getElapsedTime() < mAthlete1StartingBestTime || mAthlete1StartingBestTime < 0) {
					tvAthlete1BestTime.setText(getActivity().getResources().getString(R.string.best_time_text) + " "
							+ DateTimeUtils.formatDuration(mAthlete1Race.getElapsedTime(), mNumberFormat));
					tvAthlete1BestTime.setBackgroundResource(R.drawable.rec_bkgrd_green_stroke_red);
					tvAthlete1BestTime.setTextColor(getResources().getColor(R.color.white));
					mAthlete1Race.setIsNewBestTime(true);
				}
				break;

			case 2:
				btnAthlete2Stop.setVisibility(View.GONE);
				if (mAthlete2Race.getElapsedTime() < mAthlete2StartingBestTime || mAthlete2StartingBestTime < 0) {
					tvAthlete2BestTime.setText(getActivity().getResources().getString(R.string.best_time_text) + " "
							+ DateTimeUtils.formatDuration(mAthlete2Race.getElapsedTime(), mNumberFormat));
					tvAthlete2BestTime.setBackgroundResource(R.drawable.rec_bkgrd_green_stroke_red);
					tvAthlete2BestTime.setTextColor(getResources().getColor(R.color.white));
					mAthlete2Race.setIsNewBestTime(true);
				}
				break;

			default:
				MyLog.e("Race_Timer_Fragment",
						"onEvent(RaceElapsedTime) Unknown RaceTablePosition:" + event.getRaceTablePosition());
				break;
		}

		if (!isRaceRunning()) {
			ShowElapsedTime();
		}
	}

	private final int mNumberOfAthleteLoaders = 2;
	private boolean mSpinRaceEventChanged = false;
	private int mNumberOfLoadersCompleted = 0;

	private void AthleteLoaderFinished() {
		mNumberOfLoadersCompleted++;
		if (mNumberOfLoadersCompleted == mNumberOfAthleteLoaders) {
			mNumberOfLoadersCompleted = 0;
			mSpinRaceEventChanged = false;

			ArrayList<Long> athletes = LastEventAthletesTable.getEventAthletes(getActivity(),
					mAthlete1Race.getEventID());
			mAthlete1Race.setAthleteID(athletes.get(0));
			mAthlete2Race.setAthleteID(athletes.get(1));

			spinAthlete1.setSelection(MySettings.getIndexFromCursor(spinAthlete1, athletes.get(0)));
			spinAthlete2.setSelection(MySettings.getIndexFromCursor(spinAthlete2, athletes.get(1)));

			SetAthlete1(athletes.get(0));
			SetAthlete2(athletes.get(1));

		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		int spinnerID = parent.getId();

		if (spinnerID == R.id.spinRaceMeets) {
			mAthlete1Race.setMeetID(id);
			mAthlete2Race.setMeetID(id);

		} else if (spinnerID == R.id.spinRaceEvents) {
			mAthlete1Race.setEventID(id);
			mAthlete2Race.setEventID(id);

			mEventShortTitle = EventsTable.getEventShortTitle(getActivity(), id);

			mSpinRaceEventChanged = true;
			mAthlete1Race.setAthleteID(1);
			mAthlete2Race.setAthleteID(1);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1, null, mRaceStartTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2, null, mRaceStartTimerCallbacks);

		} else if (spinnerID == R.id.spinAthlete1) {
			if (spinAthlete1.getTag() == null) {
				SetAthlete1(id);
				spinAthlete1.setTag(id);
			} else {
				// the tag != null, so check it
				if ((Long) spinAthlete1.getTag() != id) {
					SetAthlete1(id);
					spinAthlete1.setTag(id);
				}
			}

		} else if (spinnerID == R.id.spinAthlete2) {
			if (spinAthlete2.getTag() == null) {
				SetAthlete2(id);
				spinAthlete2.setTag(id);
			} else {
				// the tag != null, so check it
				if ((Long) spinAthlete2.getTag() != id) {
					SetAthlete2(id);
					spinAthlete2.setTag(id);
				}
			}
		}

	}

	private void SetAthlete1(long id) {
		mAthlete1Race.setAthleteID(id);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2, null, mRaceStartTimerCallbacks);
	}

	private void SetAthlete2(long id) {
		mAthlete2Race.setAthleteID(id);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1, null, mRaceStartTimerCallbacks);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnAthlete1Start) {
			if (!isRaceRunning()) {
				long raceStartTime = System.currentTimeMillis(); // current time
				mAthlete1Race.setStartTime(raceStartTime);
				mAthlete2Race.setStartTime(raceStartTime);
				mRaceStartTime = raceStartTime;
				new buttonVibration().execute();
				CreateNewRace();
			}
		} else if (id == R.id.btnAthlete1Split) {
			mAthlete1Race.CreateSplit(System.currentTimeMillis());
			new buttonVibration().execute();

		} else if (id == R.id.btnAthlete1Stop) {
			mAthlete1Race.StopRace(System.currentTimeMillis());
			new buttonVibration().execute();

		} else if (id == R.id.btnAthlete2Start) {
			if (!isRaceRunning()) {
				long raceStartTime = System.currentTimeMillis(); // current time
				mAthlete1Race.setStartTime(raceStartTime);
				mAthlete2Race.setStartTime(raceStartTime);
				mRaceStartTime = raceStartTime;
				new buttonVibration().execute();
				CreateNewRace();
			}
		} else if (id == R.id.btnAthlete2Split) {
			mAthlete2Race.CreateSplit(System.currentTimeMillis());
			new buttonVibration().execute();
		} else if (id == R.id.btnAthlete2Stop) {
			mAthlete2Race.StopRace(System.currentTimeMillis());
			new buttonVibration().execute();

		}
	}

	private class buttonVibration extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mVibrator.hasVibrator()) {
				mVibrator.vibrate(MySettings.BUTTON_CLICK_VIBRATION_DURATION);
			}
			return null;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if (id == R.id.spinRaceEvents) {
			EventBus.getDefault().post(new ShowEventsFragment());
			// Toast.makeText(getActivity(), "LongClick: spinRaceEvents", Toast.LENGTH_LONG).show();
		} else if (id == R.id.spinRaceMeets) {
			EventBus.getDefault().post(new ShowMeetsFragment());
			// Toast.makeText(getActivity(), "LongClick: spinRaceMeets", Toast.LENGTH_LONG).show();
		} else if (id == R.id.spinAthlete1 || id == R.id.spinAthlete2) {
			EventBus.getDefault().post(new ShowAthletesFragment());
			// Toast.makeText(getActivity(), "LongClick: spinAthlete", Toast.LENGTH_LONG).show();
		}
		return true;
	}

	private void CreateNewRace() {
		Resources res = getActivity().getResources();
		boolean isFree = false;
		if (SplitsBuild.isFree(getActivity())) {
			isFree = true;
			if (mRaceCount >= MySettings.MAX_NUMBER_OF_RACES) {
				resetRaceStartTime();
				SoundError();

				String dialogTitle = res.getString(string.dialog_maxRacesTitle);
				String dialogMessage = res.getString(string.dialog_maxRacesMessage);

				FragmentManager fm = getFragmentManager();
				PlayStore_DialogFragment frag = PlayStore_DialogFragment.newInstance(dialogTitle, dialogMessage);
				frag.show(fm, "playStore_DialogFragment");
				return;
			}
		}

		if (mAthlete1Race.getAthleteID() < 2 && mAthlete2Race.getAthleteID() < 2) {
			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRace_unable_to_start_race))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRace_no_athletes_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
			resetRaceStartTime();
			SoundError();
			return;
		}

		if (mAthlete1Race.getMeetID() < 2) {
			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRace_unable_to_start_race))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRace_no_meet_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

			resetRaceStartTime();
			SoundError();
			return;
		}

		if (mAthlete1Race.getEventID() < 2) {
			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRace_unable_to_start_race))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRace_no_event_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

			resetRaceStartTime();
			SoundError();
			return;
		}

		// start clock
		mHandler.removeCallbacks(startTimer);
		mHandler.postDelayed(startTimer, 0);
		EventBus.getDefault().post(new ChangeActionBarTitle(mEventShortTitle));
		if (isFree) {
			mRaceCount += 1;
		}

		ArrayList<Long> athletes = new ArrayList<Long>();
		athletes.add((long) 1);
		athletes.add((long) 1);

		if (mAthlete1Race.getAthleteID() > 1) {
			mAthlete1Race.StartNewRace();
			String shortRaceTitle = EventsTable.getEventShortTitle(getActivity(), mAthlete1Race.getEventID());
			mAthlete1StartingBestTime = RacesTable
					.getAthleteEventBestTimeValue(getActivity(), shortRaceTitle, mAthlete1Race.getAthleteID(), false, 0);
			if (tvAthlete1BestTime != null) {
				if (mAthlete1StartingBestTime > 0) {
					tvAthlete1BestTime.setText(getActivity().getResources().getString(R.string.best_time_text) + " "
							+ DateTimeUtils.formatDuration(mAthlete1StartingBestTime, mNumberFormat));
				} else {
					tvAthlete1BestTime.setText(res.getString(R.string.best_time_text) + " "
							+ res.getString(R.string.not_available_text));
				}

				tvAthlete1BestTime.setBackgroundResource(R.drawable.rec_bkgrd_smoke_white_no_stroke);
				tvAthlete1BestTime.setTextColor(getResources().getColor(R.color.black));
				mAthlete1Race.setIsNewBestTime(false);
				mAthlete1Race.setBestTimeText(tvAthlete1BestTime.getText().toString());
			}
			athletes.remove(0);
			athletes.add(0, mAthlete1Race.getAthleteID());
		} else {
			mAthlete1Race.setAthleteRaceID(-1);
			btnAthlete1Start.setVisibility(View.GONE);
			btnAthlete1Split.setVisibility(View.GONE);
			btnAthlete1Stop.setVisibility(View.GONE);
			llAthlete1RaceTable.setVisibility(View.GONE);
		}

		if (mAthlete2Race.getAthleteID() > 1) {
			mAthlete2Race.StartNewRace();
			String shortRaceTitle = EventsTable.getEventShortTitle(getActivity(), mAthlete2Race.getEventID());
			mAthlete2StartingBestTime = RacesTable.getAthleteEventBestTimeValue(getActivity(), shortRaceTitle,
					mAthlete2Race.getAthleteID(), false, 0);
			if (tvAthlete2BestTime != null) {
				if (mAthlete2StartingBestTime > 0) {
					tvAthlete2BestTime.setText(getActivity().getResources().getString(R.string.best_time_text) + " "
							+ DateTimeUtils.formatDuration(mAthlete2StartingBestTime, mNumberFormat));
				} else {
					tvAthlete2BestTime.setText(res.getString(R.string.best_time_text) + " "
							+ res.getString(R.string.not_available_text));
				}
				tvAthlete2BestTime.setBackgroundResource(R.drawable.rec_bkgrd_smoke_white_no_stroke);
				tvAthlete2BestTime.setTextColor(getResources().getColor(R.color.black));
				mAthlete2Race.setIsNewBestTime(false);
				mAthlete2Race.setBestTimeText(tvAthlete2BestTime.getText().toString());
			}

			athletes.remove(1);
			athletes.add(1, mAthlete2Race.getAthleteID());
		} else {
			mAthlete2Race.setAthleteRaceID(-1);
			btnAthlete2Start.setVisibility(View.GONE);
			btnAthlete2Split.setVisibility(View.GONE);
			btnAthlete2Stop.setVisibility(View.GONE);
			llAthlete2RaceTable.setVisibility(View.GONE);
		}

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS, null,
				mRaceStartTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS, null,
				mRaceStartTimerCallbacks);

		mAthlete1Race.setElapsedTime(-1);
		mAthlete2Race.setElapsedTime(-1);

		LastEventAthletesTable.SaveLastEventAthletes(getActivity(), mAthlete1Race.getEventID(), athletes);

		// send event to Flurry
		Map<String, String> raceParams = new HashMap<String, String>();
		raceParams.put("EventShortTitle", mEventShortTitle);
		FlurryAgent.logEvent("NewRace", raceParams);

	}

	private void resetRaceStartTime() {
		mRaceStartTime = -1;
		mAthlete1Race.setStartTime(-1);
		mAthlete2Race.setStartTime(-1);
	}

	private void SoundError() {
		if (mVibrator.hasVibrator()) {
			long pattern[] = { 0, 100, 200, 300, 400 };
			mVibrator.vibrate(pattern, -1);
		}
	}

	private void ShowZeroTime() {
		switch (mNumberFormat) {
			case DateTimeUtils.FORMAT_SECONDS:
				tvRacetime.setText("0:00");
				break;

			case DateTimeUtils.FORMAT_TENTHS:
				tvRacetime.setText("0:00.0");
				break;

			default:
				tvRacetime.setText("0:00.00");
				break;
		}
	}

	private void ShowTime(long elapsedTime) {
		if (elapsedTime > 0) {
			String totalElapsedTime = DateTimeUtils.formatDuration(elapsedTime, mNumberFormat);
			tvRacetime.setText(totalElapsedTime);
		} else {
			ShowZeroTime();
		}
	}

	private void ShowElapsedTime() {
		if (llAthlete1RaceTable.getVisibility() == View.VISIBLE && llAthlete2RaceTable.getVisibility() == View.VISIBLE) {
			if (!mAthlete1Race.isRaceRunning() && !mAthlete2Race.isRaceRunning()) {
				// both athlete races have finished.
				mHandler.removeCallbacks(startTimer);
				// show the slowest time
				if (mAthlete1Race.getElapsedTime() > mAthlete2Race.getElapsedTime()) {
					ShowTime(mAthlete1Race.getElapsedTime());
				} else {
					ShowTime(mAthlete2Race.getElapsedTime());
				}
			} else if (mAthlete1Race.getElapsedTime() < 0 && mAthlete2Race.getElapsedTime() < 0) {
				ShowZeroTime();
			}

		} else if (llAthlete1RaceTable.getVisibility() == View.VISIBLE) {
			// only one racer ... show their time
			mHandler.removeCallbacks(startTimer);
			ShowTime(mAthlete1Race.getElapsedTime());

		} else if (llAthlete2RaceTable.getVisibility() == View.VISIBLE) {
			// only one racer ... show their time
			mHandler.removeCallbacks(startTimer);
			ShowTime(mAthlete2Race.getElapsedTime());
		}
	}

	private void ShowSplitTables() {
		llAthlete1RaceTable.setVisibility(View.VISIBLE);
		llAthlete2RaceTable.setVisibility(View.VISIBLE);
	}

	private void ShowStartButtons() {
		ShowSplitTables();
		btnAthlete1Start.setVisibility(View.VISIBLE);
		btnAthlete1Split.setVisibility(View.GONE);
		btnAthlete1Stop.setVisibility(View.GONE);
		btnAthlete2Start.setVisibility(View.VISIBLE);
		btnAthlete2Split.setVisibility(View.GONE);
		btnAthlete2Stop.setVisibility(View.GONE);
		EventBus.getDefault().post(new ChangeActionBarTitle(""));
		EnableSpinners();
	}

	private void EnableSpinners() {
		tvRacetime.setVisibility(View.GONE);
		spinRaceMeets.setVisibility(View.VISIBLE);
		spinRaceEvents.setVisibility(View.VISIBLE);
		spinAthlete1.setEnabled(true);
		spinAthlete2.setEnabled(true);
		tvAthlete1BestTime.setVisibility(View.GONE);
		tvAthlete2BestTime.setVisibility(View.GONE);
		areSpinnersEnabled = true;
	}

	private void DisableSpinners() {
		tvRacetime.setVisibility(View.VISIBLE);
		spinRaceMeets.setVisibility(View.GONE);
		spinRaceEvents.setVisibility(View.GONE);
		spinAthlete1.setEnabled(false);
		spinAthlete2.setEnabled(false);
		tvAthlete1BestTime.setVisibility(View.VISIBLE);
		tvAthlete2BestTime.setVisibility(View.VISIBLE);
		areSpinnersEnabled = false;
	}

	private void ShowAthlete1SplitButton() {
		if (mAthlete1Race.getAthleteID() == 1) {
			btnAthlete1Split.setVisibility(View.GONE);
			llAthlete1RaceTable.setVisibility(View.GONE);
		} else {
			btnAthlete1Split.setVisibility(View.VISIBLE);
		}

		btnAthlete1Start.setVisibility(View.GONE);
		btnAthlete1Stop.setVisibility(View.GONE);

		DisableSpinners();

	}

	private void ShowAthlete2SplitButton() {
		if (mAthlete2Race.getAthleteID() == 1) {
			btnAthlete2Split.setVisibility(View.GONE);
			llAthlete2RaceTable.setVisibility(View.GONE);
		} else {
			btnAthlete2Split.setVisibility(View.VISIBLE);
		}

		btnAthlete2Start.setVisibility(View.GONE);
		btnAthlete2Stop.setVisibility(View.GONE);

		DisableSpinners();
	}

	private void ShowAthlete1StopButton() {
		if (mAthlete1Race.getAthleteID() == 1) {
			btnAthlete1Stop.setVisibility(View.GONE);
			llAthlete2RaceTable.setVisibility(View.GONE);
		} else {
			btnAthlete1Stop.setVisibility(View.VISIBLE);
		}

		btnAthlete1Start.setVisibility(View.GONE);
		btnAthlete1Split.setVisibility(View.GONE);

		DisableSpinners();
	}

	private void ShowAthlete2StopButton() {
		if (mAthlete2Race.getAthleteID() == 1) {
			btnAthlete2Stop.setVisibility(View.GONE);
			llAthlete2RaceTable.setVisibility(View.GONE);
		} else {
			btnAthlete2Stop.setVisibility(View.VISIBLE);
		}

		btnAthlete2Start.setVisibility(View.GONE);
		btnAthlete2Split.setVisibility(View.GONE);

		DisableSpinners();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// do nothing
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Race_Timer_Fragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RACE_TIMER_MEETS:
				cursorLoader = MeetsTable.getAllMeets(getActivity(), mMeetType, true, MeetsTable.SORT_ORDER_MEET_TITLE);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_EVENTS:
				cursorLoader = EventsTable.getAllEvents(getActivity(), mMeetType, true, false,
						EventsTable.SORT_ORDER_UNITS_STYLE_DISTANCE);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS:
				MyLog.i("Race_Timer_Fragment", "onCreateLoader. LoaderId = " + id + "; raceID:"
						+ mAthlete1Race.getAthleteRaceID() + "; athleteID:" + mAthlete1Race.getAthleteID());
				cursorLoader = SplitsTable.getAllSplits(getActivity(), mAthlete1Race.getAthleteRaceID(), false,
						SplitsTable.SORT_ORDER_SPLIT_ID_DESC);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS:
				MyLog.i("Race_Timer_Fragment", "onCreateLoader. LoaderId = " + id + "; raceID:"
						+ mAthlete2Race.getAthleteRaceID() + "; athleteID:" + mAthlete2Race.getAthleteID());
				cursorLoader = SplitsTable.getAllSplits(getActivity(), mAthlete2Race.getAthleteRaceID(), false,
						SplitsTable.SORT_ORDER_SPLIT_ID_DESC);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true, mAthlete2Race.getAthleteID(),
						AthletesTable.COL_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true, mAthlete1Race.getAthleteID(),
						AthletesTable.COL_DISPLAY_NAME);
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Race_Timer_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (id) {
			case MySettings.LOADER_FRAG_RACE_TIMER_MEETS:
				mMeetsSpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingMeetID) {
					spinRaceMeets.setSelection(MySettings.getIndexFromCursor(spinRaceMeets, mAthlete1Race.getMeetID()));
					mFirstTimeLoadingMeetID = false;
				}
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_EVENTS:
				mEventsSpinnerAdapter.swapCursor(newCursor);

				if (mFirstTimeLoadingEventID) {
					spinRaceEvents.setSelection(MySettings.getIndexFromCursor(spinRaceEvents,
							mAthlete1Race.getEventID()));
					mFirstTimeLoadingEventID = false;
				}
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS:
				mAthlete1SplitsCursorAdapter.swapCursor(newCursor);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS:
				mAthlete2SplitsCursorAdapter.swapCursor(newCursor);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1:
				mAthlete1SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete1ID) {
					spinAthlete1
							.setSelection(MySettings.getIndexFromCursor(spinAthlete1, mAthlete1Race.getAthleteID()));
					mFirstTimeLoadingAthlete1ID = false;
				}
				if (mSpinRaceEventChanged) {
					AthleteLoaderFinished();
				}
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2:
				mAthlete2SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete2ID) {
					spinAthlete2
							.setSelection(MySettings.getIndexFromCursor(spinAthlete2, mAthlete2Race.getAthleteID()));
					mFirstTimeLoadingAthlete2ID = false;
				}
				if (mSpinRaceEventChanged) {
					AthleteLoaderFinished();
				}
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
			case MySettings.LOADER_FRAG_RACE_TIMER_MEETS:
				mMeetsSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_EVENTS:
				mEventsSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS:
				mAthlete1SplitsCursorAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS:
				mAthlete2SplitsCursorAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE1:
				mAthlete1SpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RACE_TIMER_ATHLETE2:
				mAthlete2SpinnerAdapter.swapCursor(null);
				break;

			default:
				break;
		}

	}

	private Runnable startTimer = new Runnable() {

		public void run() {
			mClockElapsedTime = System.currentTimeMillis() - mRaceStartTime;
			ShowTime(mClockElapsedTime);
			mHandler.postDelayed(this, REFRESH_RATE);
		}
	};

}
