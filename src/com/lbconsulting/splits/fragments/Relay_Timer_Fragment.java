package com.lbconsulting.splits.fragments;

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
import com.lbconsulting.splits.adapters.AthleteSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.EventsSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.MeetsSpinnerCursorAdapter;
import com.lbconsulting.splits.adapters.RaceSplitsCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.Relay;
import com.lbconsulting.splits.classes.SplitsBuild;
import com.lbconsulting.splits.classes.SplitsEvents.ChangeActionBarTitle;
import com.lbconsulting.splits.classes.SplitsEvents.ClearRace;
import com.lbconsulting.splits.classes.SplitsEvents.RaceComplete;
import com.lbconsulting.splits.classes.SplitsEvents.RelaySplit;
import com.lbconsulting.splits.classes.SplitsEvents.ShowAthletesFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowEventsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowMeetsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowSplitButton;
import com.lbconsulting.splits.classes.SplitsEvents.ShowStopButton;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.LastEventAthletesTable;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RelaysTable;
import com.lbconsulting.splits.database.SplitsTable;
import com.lbconsulting.splits.dialogs.PlayStore_DialogFragment;

import de.greenrobot.event.EventBus;

public class Relay_Timer_Fragment extends Fragment implements OnClickListener, OnLongClickListener,
		OnItemSelectedListener, LoaderCallbacks<Cursor> {

	private final int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;

	private Handler mHandler = new Handler();
	private long mClockElapsedTime;
	private final int REFRESH_RATE = 100;

	// Relay Timer Variables - states not stored in mRelay
	private Relay mRelay;
	private String mRelayEventShortTitle = "";

	private boolean areRelaySpinnersEnabled = true;
	private boolean mResetAthletes = false;
	private int mRelayRaceCount = 0;

	// Variables from Preferences
	private int mMeetType;

	// Relay Timer Flags
	private boolean mFirstTimeLoadingMeetID = false;
	private boolean mFirstTimeLoadingEventID = false;
	private boolean mFirstTimeLoadingAthlete0ID = false;
	private boolean mFirstTimeLoadingAthlete1ID = false;
	private boolean mFirstTimeLoadingAthlete2ID = false;
	private boolean mFirstTimeLoadingAthlete3ID = false;
	private boolean mProhibitSpinnerUpdates = false;

	// Language variables
	private String MEDLEY;
	private String FLY;
	private String BACK;
	private String BREAST;
	private String FREE;
	private String DASH;

	// Loader setup
	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mRelayTimerCallbacks;

	// Relay Timer Adapters
	private MeetsSpinnerCursorAdapter mMeetsSpinnerAdapter;
	private EventsSpinnerCursorAdapter mEventsSpinnerAdapter;

	// Relay Timer Views
	private Spinner spinRelayMeets;
	private Spinner spinRelayEvents;

	private Spinner spinAthlete0;
	private Spinner spinAthlete1;
	private Spinner spinAthlete2;
	private Spinner spinAthlete3;
	private Button btnActiveAthlete;

	private TextView tvRelaytime;

	private LinearLayout llRelayTable;
	private ListView lvRelaySplits;
	private Button btnStart;
	private Button btnStop;
	private Button btnSplit;
	private RaceSplitsCursorAdapter mRelaySplitsCursorAdapter;

	private Vibrator mVibrator = null;

	private AthleteSpinnerCursorAdapter mAthlete0SpinnerAdapter;
	private AthleteSpinnerCursorAdapter mAthlete1SpinnerAdapter;
	private AthleteSpinnerCursorAdapter mAthlete2SpinnerAdapter;
	private AthleteSpinnerCursorAdapter mAthlete3SpinnerAdapter;

	public Relay_Timer_Fragment() {
		// Empty constructor
	}

	public static Relay_Timer_Fragment newInstance() {
		MyLog.i("Relay_Timer_Fragment", "newInstance()");
		Relay_Timer_Fragment fragment = new Relay_Timer_Fragment();

		/*		Bundle args = new Bundle();
				args.putInt(MySettings.KEY_MEET_TYPE, meetType);
				fragment.setArguments(args);*/
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Relay_Timer_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_relay_timer, container, false);

		/*		Bundle bundle = getArguments();
				if (bundle != null) {
					mMeetType = bundle.getInt(MySettings.KEY_MEET_TYPE, MySettings.SWIM_MEET);
				}*/

		mRelayEventShortTitle = getActivity().getResources().getString(R.string.not_available_text);

		mRelay = new Relay(getActivity());

		spinRelayMeets = (Spinner) view.findViewById(R.id.spinRaceMeets);
		if (spinRelayMeets != null) {
			mMeetsSpinnerAdapter = new MeetsSpinnerCursorAdapter(getActivity(), null, 0);
			spinRelayMeets.setAdapter(mMeetsSpinnerAdapter);
			spinRelayMeets.setOnItemSelectedListener(this);
			spinRelayMeets.setOnLongClickListener(this);
		}

		spinRelayEvents = (Spinner) view.findViewById(R.id.spinRaceEvents);
		if (spinRelayEvents != null) {
			mEventsSpinnerAdapter = new EventsSpinnerCursorAdapter(getActivity(), null, 0);
			spinRelayEvents.setAdapter(mEventsSpinnerAdapter);
			spinRelayEvents.setOnItemSelectedListener(this);
			spinRelayEvents.setOnLongClickListener(this);
		}

		llRelayTable = (LinearLayout) view.findViewById(R.id.llRelayTable);
		if (llRelayTable != null) {
			lvRelaySplits = (ListView) view.findViewById(R.id.lvRaceSplits);
			mRelaySplitsCursorAdapter = new RaceSplitsCursorAdapter(getActivity(), null, 0, mNumberFormat);
			lvRelaySplits.setAdapter(mRelaySplitsCursorAdapter);
		}

		tvRelaytime = (TextView) view.findViewById(R.id.tvRacetime);

		btnStart = (Button) view.findViewById(R.id.btnStart);
		if (btnStart != null) {
			btnStart.setOnClickListener(this);
		}
		btnStop = (Button) view.findViewById(R.id.btnStop);
		if (btnStop != null) {
			btnStop.setOnClickListener(this);
		}
		btnSplit = (Button) view.findViewById(R.id.btnSplit);
		if (btnSplit != null) {
			btnSplit.setOnClickListener(this);
		}

		spinAthlete0 = (Spinner) view.findViewById(R.id.spinAthlete0);
		if (spinAthlete0 != null) {
			mAthlete0SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
			spinAthlete0.setAdapter(mAthlete0SpinnerAdapter);
			spinAthlete0.setOnItemSelectedListener(this);
			spinAthlete0.setOnLongClickListener(this);
		}

		spinAthlete1 = (Spinner) view.findViewById(R.id.spinAthlete1);
		if (spinAthlete1 != null) {
			mAthlete1SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
			spinAthlete1.setAdapter(mAthlete1SpinnerAdapter);
			spinAthlete1.setOnItemSelectedListener(this);
			spinAthlete1.setOnLongClickListener(this);
		}

		spinAthlete2 = (Spinner) view.findViewById(R.id.spinAthlete2);
		if (spinAthlete2 != null) {
			mAthlete2SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
			spinAthlete2.setAdapter(mAthlete2SpinnerAdapter);
			spinAthlete2.setOnItemSelectedListener(this);
			spinAthlete2.setOnLongClickListener(this);
		}

		spinAthlete3 = (Spinner) view.findViewById(R.id.spinAthlete3);
		if (spinAthlete3 != null) {
			mAthlete3SpinnerAdapter = new AthleteSpinnerCursorAdapter(getActivity(), null, 0);
			spinAthlete3.setAdapter(mAthlete3SpinnerAdapter);
			spinAthlete3.setOnItemSelectedListener(this);
			spinAthlete3.setOnLongClickListener(this);
		}

		btnActiveAthlete = (Button) view.findViewById(R.id.btnActiveAthlete);
		if (btnActiveAthlete != null) {
			btnActiveAthlete.setOnClickListener(this);
		}

		mRelayTimerCallbacks = this;
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Relay_Timer_Fragment", "onActivityCreated()");

		Resources res = getActivity().getResources();
		MEDLEY = res.getString(R.string.swimming_medley_text);
		FLY = res.getString(R.string.swimming_fly_text);
		BACK = res.getString(R.string.swimming_back_text);
		BREAST = res.getString(R.string.swimming_breast_text);
		FREE = res.getString(R.string.swimming_free_text);
		DASH = res.getString(R.string.track_dash_text);

		mLoaderManager = getLoaderManager();

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_MEETS, null, mRelayTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_EVENTS, null, mRelayTimerCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS, null, mRelayTimerCallbacks);

		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null, mRelayTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null, mRelayTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null, mRelayTimerCallbacks);
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null, mRelayTimerCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Relay_Timer_Fragment", "onResume()");

		EventBus.getDefault().register(this);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));
		mResetAthletes = sharedPrefs.getBoolean(MySettings.KEY_RESET_ATHLETES, false);

		mFirstTimeLoadingMeetID = true;
		mFirstTimeLoadingEventID = true;
		mFirstTimeLoadingAthlete0ID = true;
		mFirstTimeLoadingAthlete1ID = true;
		mFirstTimeLoadingAthlete2ID = true;
		mFirstTimeLoadingAthlete3ID = true;

		mRelayRaceCount = MySettings.getRelayRaceCount();

		if (mRelay == null) {
			mRelay = new Relay(getActivity());
		}
		mRelay.setState(MySettings.getRelayLongValues("Relay"),
				MySettings.getRelayIntValues("Relay"),
				MySettings.isRelayRunning("Relay"));

		mRelaySplitsCursorAdapter.setNumberFormat(mNumberFormat);

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_MEETS, null, mRelayTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_EVENTS, null, mRelayTimerCallbacks);

		if (!MySettings.isRelayStartButtonVisible()) {
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS, null, mRelayTimerCallbacks);
		}

		// check if spinRelayEvents and spinRelayMeets have a selected items
		long relayEventID = spinRelayEvents.getSelectedItemId();
		if (mRelay.getRelayEventID() != relayEventID) {
			if (relayEventID > 0) {
				mRelay.setRelayEventID(relayEventID);
			}
		}

		long relayMeetID = spinRelayMeets.getSelectedItemId();
		if (mRelay.getRelayMeetID() != relayMeetID) {
			if (relayMeetID > 0) {
				mRelay.setRelayMeetID(relayMeetID);
			}
		}

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null, mRelayTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null, mRelayTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null, mRelayTimerCallbacks);
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null, mRelayTimerCallbacks);

		if (MySettings.isRelayStartButtonVisible()) {
			btnStart.setVisibility(View.VISIBLE);
			mRelaySplitsCursorAdapter.swapCursor(null);
		} else {
			btnStart.setVisibility(View.GONE);
		}
		if (MySettings.isRelaySplitButtonVisible()) {
			btnSplit.setVisibility(View.VISIBLE);
		} else {
			btnSplit.setVisibility(View.GONE);
		}
		if (MySettings.isRelayStopButtonVisible()) {
			btnStop.setVisibility(View.VISIBLE);
		} else {
			btnStop.setVisibility(View.GONE);
		}

		if (MySettings.isRelayActiveAthleteButtonVisible()) {
			btnActiveAthlete.setVisibility(View.VISIBLE);
		} else {
			btnActiveAthlete.setVisibility(View.GONE);
		}

		if (MySettings.areRelaySpinnersEnabled()) {
			EnableSpinners();
		} else {
			DisableSpinners();
		}

		if (mRelay.isRelayRunning()) {
			// start clock
			mHandler.removeCallbacks(startTimer);
			mHandler.postDelayed(startTimer, 0);

		} else {
			ShowTime(mRelay.getRelayElapsedTime());
		}

		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Relay_Timer_Fragment", "onPause()");
		Bundle RelayTimerBundle = new Bundle();
		RelayTimerBundle.putBundle("Relay", mRelay.getState());

		RelayTimerBundle.putLong(MySettings.STATE_MEET_ID, mRelay.getRelayMeetID());

		RelayTimerBundle.putBoolean(MySettings.STATE_RELAY_RACE_ARE_SPINNERS_ENABLED, areRelaySpinnersEnabled);
		RelayTimerBundle.putInt(MySettings.STATE_RELAY_RACE_COUNT, mRelayRaceCount);

		RelayTimerBundle.putLong(MySettings.STATE_RELAY_RACE_ELAPSED_TIME, mRelay.getRelayElapsedTime());

		RelayTimerBundle.putBoolean(MySettings.STATE_RELAY_RACE_START_BUTTTON_VISIBLE,
				btnStart.getVisibility() == View.VISIBLE);
		RelayTimerBundle.putBoolean(MySettings.STATE_RELAY_RACE_SPLIT_BUTTTON_VISIBLE,
				btnSplit.getVisibility() == View.VISIBLE);
		RelayTimerBundle.putBoolean(MySettings.STATE_RELAY_RACE_STOP_BUTTTON_VISIBLE,
				btnStop.getVisibility() == View.VISIBLE);
		RelayTimerBundle.putBoolean(MySettings.STATE_RELAY_RACE_ACTIVE_ATHLETE_BUTTTON_VISIBLE,
				btnActiveAthlete.getVisibility() == View.VISIBLE);

		MySettings.set("", RelayTimerBundle);

		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Relay_Timer_Fragment", "onDestroy()");
		super.onDestroy();
	}

	public void onEvent(ClearRace event) {
		ClearRelayRace();
	}

	public void onEvent(ShowSplitButton event) {
		ShowSplitButton();
	}

	public void onEvent(ShowStopButton event) {
		ShowStopButton();
	}

	public void onEvent(RelaySplit event) {
		ShowActiveAthlete(event.getActiveAthlete());
	}

	public void onEvent(RaceComplete event) {
		// stop clock
		mHandler.removeCallbacks(startTimer);
		btnStop.setVisibility(View.GONE);
		ShowTime(event.getRaceElapsedTime());
		ShowAthleteSpinners();
	}

	private final int mNumberOfAthleteLoaders = 4;
	private boolean mSpinRaceEventChanged = false;
	private int mNumberOfLoadersCompleted = 0;

	private void AthleteLoaderFinished() {
		mNumberOfLoadersCompleted++;
		if (mNumberOfLoadersCompleted == mNumberOfAthleteLoaders) {
			mNumberOfLoadersCompleted = 0;
			mSpinRaceEventChanged = false;

			mRelay.setAthletes(LastEventAthletesTable.getEventAthletes(getActivity(), mRelay.getRelayEventID()));

			spinAthlete0.setSelection(MySettings.getIndexFromCursor(spinAthlete0, mRelay.getAthletes().get(0)));
			spinAthlete1.setSelection(MySettings.getIndexFromCursor(spinAthlete1, mRelay.getAthletes().get(1)));
			spinAthlete2.setSelection(MySettings.getIndexFromCursor(spinAthlete2, mRelay.getAthletes().get(2)));
			spinAthlete3.setSelection(MySettings.getIndexFromCursor(spinAthlete3, mRelay.getAthletes().get(3)));

			SetAthlete0(mRelay.getAthletes().get(0));
			SetAthlete1(mRelay.getAthletes().get(1));
			SetAthlete2(mRelay.getAthletes().get(2));
			SetAthlete3(mRelay.getAthletes().get(3));
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		int spinnerID = parent.getId();

		if (spinnerID == R.id.spinRaceMeets) {
			mRelay.setRelayMeetID(id);
		} else if (spinnerID == R.id.spinRaceEvents) {
			mRelay.setRelayEventID(id);
			mRelayEventShortTitle = EventsTable.getEventShortTitle(getActivity(), mRelay.getRelayEventID());

			mSpinRaceEventChanged = true;
			mRelay.SetAllAthletesToDefaultValues();
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null, mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null, mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null, mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null, mRelayTimerCallbacks);
			// wait for all loaders to finish before setting the athlete's in their spinners

		} else if (spinnerID == R.id.spinAthlete0) {
			if (spinAthlete0.getTag() == null) {
				SetAthlete0(id);
				spinAthlete0.setTag(id);
			} else {
				// the tag != null, so check it
				if ((Long) spinAthlete0.getTag() != id) {
					SetAthlete0(id);
					spinAthlete0.setTag(id);
				}
			}

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

		} else if (spinnerID == R.id.spinAthlete3) {
			if (spinAthlete3.getTag() == null) {
				SetAthlete3(id);
				spinAthlete3.setTag(id);
			} else {
				// the tag != null, so check it
				if ((Long) spinAthlete3.getTag() != id) {
					SetAthlete3(id);
					spinAthlete3.setTag(id);
				}
			}

		}

	}

	private void SetAthlete0(long id) {
		if (!mProhibitSpinnerUpdates) {
			mProhibitSpinnerUpdates = true;
			mRelay.setAthlete(id, 0);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null,
					mRelayTimerCallbacks);
			mProhibitSpinnerUpdates = false;
		}
	}

	private void SetAthlete1(long id) {
		if (!mProhibitSpinnerUpdates) {
			mProhibitSpinnerUpdates = true;
			mRelay.setAthlete(id, 1);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null,
					mRelayTimerCallbacks);
			mProhibitSpinnerUpdates = false;
		}
	}

	private void SetAthlete2(long id) {
		if (!mProhibitSpinnerUpdates) {
			mProhibitSpinnerUpdates = true;
			mRelay.setAthlete(id, 2);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3, null,
					mRelayTimerCallbacks);
			mProhibitSpinnerUpdates = false;
		}
	}

	private void SetAthlete3(long id) {
		if (!mProhibitSpinnerUpdates) {
			mProhibitSpinnerUpdates = true;
			mRelay.setAthlete(id, 3);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1, null,
					mRelayTimerCallbacks);
			mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2, null,
					mRelayTimerCallbacks);
			mProhibitSpinnerUpdates = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnStart) {
			if (!mRelay.isRelayRunning()) {
				mRelay.setRelayStartTime(System.currentTimeMillis());
				new buttonVibration().execute();
				CreateNewRelayRace();
			}
		} else if (id == R.id.btnSplit) {
			mRelay.CreateSplit(System.currentTimeMillis());
			new buttonVibration().execute();

		} else if (id == R.id.btnStop) {
			mRelay.StopRace(System.currentTimeMillis());
			new buttonVibration().execute();

		} else if (id == R.id.btnActiveAthlete) {
			long currentTime = System.currentTimeMillis();
			if (btnSplit.getVisibility() == View.VISIBLE) {
				mRelay.CreateSplit(currentTime);
			} else if (btnStop.getVisibility() == View.VISIBLE) {
				mRelay.StopRace(currentTime);
			}
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
		} else if (id == R.id.spinAthlete0 || id == R.id.spinAthlete1
				|| id == R.id.spinAthlete2 || id == R.id.spinAthlete3) {
			EventBus.getDefault().post(new ShowAthletesFragment());
			// Toast.makeText(getActivity(), "LongClick: spinAthlete", Toast.LENGTH_LONG).show();
		}
		return true;
	}

	private void ShowAthleteSpinners() {
		spinAthlete0.setVisibility(View.VISIBLE);
		spinAthlete1.setVisibility(View.VISIBLE);
		spinAthlete2.setVisibility(View.VISIBLE);
		spinAthlete3.setVisibility(View.VISIBLE);
		btnActiveAthlete.setVisibility(View.GONE);
	}

	@SuppressWarnings("resource")
	private void ShowActiveAthlete(int activeAthlete) {
		spinAthlete0.setVisibility(View.GONE);
		spinAthlete1.setVisibility(View.GONE);
		spinAthlete2.setVisibility(View.GONE);
		spinAthlete3.setVisibility(View.GONE);
		btnActiveAthlete.setVisibility(View.VISIBLE);

		Cursor athleteCursor = null;
		switch (activeAthlete) {
			case 1:
				athleteCursor = (Cursor) spinAthlete0.getSelectedItem();
				break;

			case 2:
				athleteCursor = (Cursor) spinAthlete1.getSelectedItem();
				break;

			case 3:
				athleteCursor = (Cursor) spinAthlete2.getSelectedItem();
				break;

			case 4:
				athleteCursor = (Cursor) spinAthlete3.getSelectedItem();
				break;
			default:
				break;

		}
		String athleteDisplayName = "";
		if (athleteCursor != null) {

			athleteDisplayName = athleteCursor.getString(athleteCursor
					.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME));
		}
		String relayLegTitle = getRelayLegTitle((Cursor) spinRelayEvents.getSelectedItem(), activeAthlete);
		btnActiveAthlete.setText(new StringBuilder()
				.append(relayLegTitle)
				.append(System.getProperty("line.separator"))
				.append(athleteDisplayName)
				.toString());
	}

	private String getRelayLegTitle(Cursor eventCursor, int relayLap) {
		String relayLegTitle = "";
		if (eventCursor != null) {
			String eventShortTitle = eventCursor.getString(eventCursor
					.getColumnIndexOrThrow(EventsTable.COL_EVENT_SHORT_TITLE));
			int lapDistance = eventCursor.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_LAP_DISTANCE));
			int unitsID = eventCursor.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_UNITS_ID));

			switch (mMeetType) {
				case MySettings.SWIM_MEET:
					// working with a swim meet relay
					if (eventShortTitle.contains(MEDLEY)) {
						switch (relayLap) {
							case 1:
								relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID, BACK,
										false);
								break;
							case 2:
								relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID,
										BREAST, false);
								break;
							case 3:
								relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID, FLY,
										false);
								break;
							case 4:
								relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID, FREE,
										false);
								break;

							default:
								break;
						}

					} else if (eventShortTitle.contains(FREE)) {
						relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID, FREE, false);
					}
					break;

				case MySettings.TRACK_MEET:
					relayLegTitle = EventsTable.MakeShortTitle(getActivity(), lapDistance, unitsID, DASH, false);
					break;

				default:
					break;
			}
		}
		return relayLegTitle;
	}

	private void CreateNewRelayRace() {
		Resources res = getActivity().getResources();
		boolean isFree = false;
		if (SplitsBuild.isFree(getActivity())) {
			isFree = true;
			if (mRelayRaceCount >= MySettings.MAX_NUMBER_OF_RELAYS) {
				mRelay.setRelayStartTime(-1);
				SoundError();

				// TODO: create Splits web site and revise dialog_maxRacesMessage
				String dialogTitle = res.getString(string.dialog_maxRelaysTitle);
				String dialogMessage = res.getString(string.dialog_maxRelaysMessage);

				FragmentManager fm = getFragmentManager();
				PlayStore_DialogFragment frag = PlayStore_DialogFragment.newInstance(dialogTitle, dialogMessage);
				frag.show(fm, "playStore_DialogFragment");
				return;
			}
		}

		if (mRelay.getAthletes().get(0) < 2 || mRelay.getAthletes().get(1) < 2
				|| mRelay.getAthletes().get(2) < 2 || mRelay.getAthletes().get(3) < 2) {

			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRelayRace_unable_to_start_relay))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRelayRace_no_athletes_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

			mRelay.setRelayStartTime(-1);
			SoundError();
			return;
		}

		if (mRelay.getRelayMeetID() < 1) {
			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRelayRace_unable_to_start_relay))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRace_no_meet_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

			mRelay.setRelayStartTime(-1);
			SoundError();
			return;
		}

		if (mRelay.getRelayEventID() < 1) {
			// if (mRelay.getRelayMeetID() < 1) {
			String toastMessage = new StringBuilder()
					.append(res.getString(R.string.createNewRelayRace_unable_to_start_relay))
					.append(System.getProperty("line.separator"))
					.append(res.getString(R.string.createNewRace_no_event_selected))
					.toString();
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
			mRelay.setRelayStartTime(-1);
			SoundError();
			return;
			// }
		}

		// start clock
		mHandler.removeCallbacks(startTimer);
		mHandler.postDelayed(startTimer, 0);
		EventBus.getDefault().post(new ChangeActionBarTitle(mRelayEventShortTitle));
		if (isFree) {
			mRelayRaceCount += 1;
		}

		ShowActiveAthlete(1);
		mRelay.CreateNewRelayRace();
		if (mRelay.getRelayRaceID() < 1) {
			MyLog.e("Relay_Timer_Fragment",
					"CreateNewRelayRace(). Relay not created! MeetID:" + mRelay.getRelayMeetID()
							+ "RelayEventID:" + mRelay.getRelayEventID() + "mMeetType:" + mMeetType
							+ "RelayStartTime:" + mRelay.getRelayStartTime());
		}
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS, null, mRelayTimerCallbacks);
		LastEventAthletesTable.SaveLastEventAthletes(getActivity(), mRelay.getRelayEventID(), mRelay.getAthletes());

		// send event to Flurry
		Map<String, String> raceParams = new HashMap<String, String>();
		raceParams.put("RelayEventShortTitle", mRelayEventShortTitle);
		FlurryAgent.logEvent("NewRelay", raceParams);

	}

	private void SoundError() {
		if (mVibrator.hasVibrator()) {
			long pattern[] = { 0, 100, 200, 300, 400 };
			mVibrator.vibrate(pattern, -1);
		}
	}

	private void ClearRelayRace() {
		// stop clock
		mHandler.removeCallbacks(startTimer);

		// delete the ongoing races from the database
		if (mRelay.isRelayRunning()) {
			if (mRelay.getRelayRaceID() > 0) {
				RelaysTable.deleteRelayRace(getActivity(), mRelay.getRelayRaceID());
			}
		}

		// re-initialize race variables
		mClockElapsedTime = -1;
		mRelay.resetRealyRaceID();
		ShowStartButton();
		mRelay.Clear();
		mRelaySplitsCursorAdapter.swapCursor(null);
		ShowZeroTime();

		if (mResetAthletes) {
			spinAthlete0.setSelection(MySettings.getIndexFromCursor(spinAthlete0, 1));
			spinAthlete1.setSelection(MySettings.getIndexFromCursor(spinAthlete1, 1));
			spinAthlete2.setSelection(MySettings.getIndexFromCursor(spinAthlete2, 1));
			spinAthlete3.setSelection(MySettings.getIndexFromCursor(spinAthlete3, 1));
		}
	}

	private void ShowZeroTime() {
		switch (mNumberFormat) {
			case DateTimeUtils.FORMAT_SECONDS:
				tvRelaytime.setText("0:00");
				break;

			case DateTimeUtils.FORMAT_TENTHS:
				tvRelaytime.setText("0:00.0");
				break;

			default:
				tvRelaytime.setText("0:00.00");
				break;
		}
	}

	private void ShowTime(long elapsedTime) {
		if (elapsedTime > 0) {
			tvRelaytime.setText(DateTimeUtils.formatDuration(elapsedTime, mNumberFormat));
		} else {
			ShowZeroTime();
		}
	}

	private void ShowStartButton() {
		btnStart.setVisibility(View.VISIBLE);
		btnSplit.setVisibility(View.GONE);
		btnStop.setVisibility(View.GONE);
		EventBus.getDefault().post(new ChangeActionBarTitle(""));
		EnableSpinners();
	}

	private void ShowSplitButton() {
		btnStart.setVisibility(View.GONE);
		btnSplit.setVisibility(View.VISIBLE);
		btnStop.setVisibility(View.GONE);
		DisableSpinners();
	}

	private void ShowStopButton() {
		btnStart.setVisibility(View.GONE);
		btnSplit.setVisibility(View.GONE);
		btnStop.setVisibility(View.VISIBLE);
		DisableSpinners();
	}

	private void EnableSpinners() {
		spinRelayMeets.setVisibility(View.VISIBLE);
		spinRelayEvents.setVisibility(View.VISIBLE);
		spinAthlete0.setEnabled(true);
		spinAthlete1.setEnabled(true);
		spinAthlete2.setEnabled(true);
		spinAthlete3.setEnabled(true);
		areRelaySpinnersEnabled = true;
	}

	private void DisableSpinners() {
		spinRelayMeets.setVisibility(View.GONE);
		spinRelayEvents.setVisibility(View.GONE);
		spinAthlete0.setEnabled(false);
		spinAthlete1.setEnabled(false);
		spinAthlete2.setEnabled(false);
		spinAthlete3.setEnabled(false);
		areRelaySpinnersEnabled = false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Relay_Timer_Fragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_TIMER_MEETS:
				cursorLoader = MeetsTable.getAllMeets(getActivity(), mMeetType, true, MeetsTable.SORT_ORDER_MEET_TITLE);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_EVENTS:
				cursorLoader = EventsTable.getAllEvents(getActivity(), mMeetType, true, true,
						EventsTable.SORT_ORDER_UNITS_STYLE_DISTANCE);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS:
				cursorLoader = SplitsTable.getAllSplits(getActivity(), mRelay.getRelayRaceID(), true,
						SplitsTable.SORT_ORDER_SPLIT_ID_DESC);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true,
						mRelay.getAthletes().get(1), mRelay.getAthletes().get(2), mRelay.getAthletes().get(3),
						AthletesTable.COL_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true,
						mRelay.getAthletes().get(0), mRelay.getAthletes().get(2), mRelay.getAthletes().get(3),
						AthletesTable.COL_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true,
						mRelay.getAthletes().get(0), mRelay.getAthletes().get(1), mRelay.getAthletes().get(3),
						AthletesTable.COL_DISPLAY_NAME);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3:
				cursorLoader = AthletesTable.getAllAthletes(getActivity(), true,
						mRelay.getAthletes().get(0), mRelay.getAthletes().get(1), mRelay.getAthletes().get(2),
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
		MyLog.i("Relay_Timer_RELAYment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_TIMER_MEETS:
				mMeetsSpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingMeetID) {
					spinRelayMeets.setSelection(MySettings.getMeetPosition(spinRelayMeets));
					mFirstTimeLoadingMeetID = false;
				}
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_EVENTS:
				mEventsSpinnerAdapter.swapCursor(newCursor);

				if (mFirstTimeLoadingEventID) {
					spinRelayEvents.setSelection(MySettings.getIndexFromCursor(spinRelayEvents,
							mRelay.getRelayEventID()));

					if (btnActiveAthlete.getVisibility() == View.VISIBLE) {
						ShowActiveAthlete(mRelay.getActiveAthlete());
					}
					mFirstTimeLoadingEventID = false;
				}
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS:
				mRelaySplitsCursorAdapter.swapCursor(newCursor);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0:
				mAthlete0SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete0ID) {
					mProhibitSpinnerUpdates = true;
					spinAthlete0.setSelection(MySettings.getIndexFromCursor(spinAthlete0, mRelay.getAthletes().get(0)));
					mFirstTimeLoadingAthlete0ID = false;
					mProhibitSpinnerUpdates = false;
				}

				if (mSpinRaceEventChanged) {
					AthleteLoaderFinished();
				}
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1:
				mAthlete1SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete1ID) {
					mProhibitSpinnerUpdates = true;
					spinAthlete1.setSelection(MySettings.getIndexFromCursor(spinAthlete1, mRelay.getAthletes().get(1)));
					mFirstTimeLoadingAthlete1ID = false;
					mProhibitSpinnerUpdates = false;
				}

				if (mSpinRaceEventChanged) {
					AthleteLoaderFinished();
				}
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2:
				mAthlete2SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete2ID) {
					mProhibitSpinnerUpdates = true;
					spinAthlete2.setSelection(MySettings.getIndexFromCursor(spinAthlete2, mRelay.getAthletes().get(2)));
					mFirstTimeLoadingAthlete2ID = false;
					mProhibitSpinnerUpdates = false;
				}

				if (mSpinRaceEventChanged) {
					AthleteLoaderFinished();
				}
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3:
				mAthlete3SpinnerAdapter.swapCursor(newCursor);
				if (mFirstTimeLoadingAthlete3ID) {
					mProhibitSpinnerUpdates = true;
					spinAthlete3.setSelection(MySettings.getIndexFromCursor(spinAthlete3, mRelay.getAthletes().get(3)));
					mFirstTimeLoadingAthlete3ID = false;
					mProhibitSpinnerUpdates = false;
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
		MyLog.i("Meet_Maker_RELAYment", "onLoaderReset. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_TIMER_MEETS:
				mMeetsSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_EVENTS:
				mEventsSpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_SPLITS:
				mRelaySplitsCursorAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE0:
				mAthlete0SpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE1:
				mAthlete1SpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE2:
				mAthlete2SpinnerAdapter.swapCursor(null);
				break;

			case MySettings.LOADER_FRAG_RELAY_TIMER_ATHLETE3:
				mAthlete3SpinnerAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

	private Runnable startTimer = new Runnable() {

		public void run() {
			mClockElapsedTime = System.currentTimeMillis() - mRelay.getRelayStartTime();
			ShowTime(mClockElapsedTime);
			mHandler.postDelayed(this, REFRESH_RATE);
		}
	};

}
