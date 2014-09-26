/*
 * Copyright 2014 Loren A. Baker
 * All rights reserved.
 */

package com.lbconsulting.splits.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Spinner;

import com.lbconsulting.splits.R;

public class MySettings {

	private static Context mContext;
	private static SplitsApplication mSplitsApplication;

	public static final boolean DEVELOPER_MODE = false;
	public static final boolean IS_BETA = false;
	public static final int BETA_EXPIRATION_MONTH = 10;
	public static final int BETA_EXPIRATION_DAY = 21;
	public static final int BETA_EXPIRATION_YEAR = 2014;
	public static String BETA_EXPIRATION_MESSAGE = "";

	// TODO: revise max number of races, relays, and athletes for free version
	// Each individual event has 1 or 2 races. Each relay has 4 races.
	public static final int MAX_NUMBER_OF_RACES = 40;
	public static final int MAX_NUMBER_OF_RELAYS = 3;
	public static final int MAX_NUMBER_OF_ATHLETES = 5;

	public static final String SPLITS_SHARED_PREFERENCES = "SplitsSharedPreferences";

	public static final int SWIM_MEET = 1;
	public static final int TRACK_MEET = 2;

	public static final int METERS = 0;
	public static final int YARDS = 1;

	public static final int BUTTON_CLICK_VIBRATION_DURATION = 25;

	public static final int UNSELECTED_ATHLETES_ONLY = 0;
	public static final int SELECTED_ATHLETES_ONLY = 1;
	public static final int BOTH_SELECTED_AND_UNSELECTED_ATHLETES = 2;

	public static final int LOADER_FRAG_ATHLETES = 100;
	public static final int LOADER_FRAG_MEETS = 101;
	public static final int LOADER_FRAG_EVENTS = 102;

	public static final int LOADER_FRAG_RACE_TIMER_MEETS = 201;
	public static final int LOADER_FRAG_RACE_TIMER_EVENTS = 202;
	public static final int LOADER_FRAG_RACE_TIMER_ATHLETE1_SPLITS = 203;
	public static final int LOADER_FRAG_RACE_TIMER_ATHLETE2_SPLITS = 204;
	public static final int LOADER_FRAG_RACE_TIMER_ATHLETE1 = 205;
	public static final int LOADER_FRAG_RACE_TIMER_ATHLETE2 = 206;

	public static final int LOADER_FRAG_RESULTS_BEST_TIMES_ATHLETES = 301;
	public static final int LOADER_FRAG_RESULTS_BEST_TIMES = 302;

	public static final int LOADER_FRAG_RESULTS_ALL_RACES_ATHLETES = 303;
	public static final int LOADER_FRAG_RESULTS_ALL_RACES = 304;

	public static final int LOADER_FRAG_RACE_SPLITS = 401;
	public static final int LOADER_FRAG_RELAY_SPLITS = 402;

	public static final int LOADER_FRAG_RELAY_TIMER_MEETS = 501;
	public static final int LOADER_FRAG_RELAY_TIMER_EVENTS = 502;
	public static final int LOADER_FRAG_RELAY_TIMER_SPLITS = 503;

	public static final int LOADER_FRAG_RELAY_TIMER_ATHLETE0 = 504;
	public static final int LOADER_FRAG_RELAY_TIMER_ATHLETE1 = 505;
	public static final int LOADER_FRAG_RELAY_TIMER_ATHLETE2 = 506;
	public static final int LOADER_FRAG_RELAY_TIMER_ATHLETE3 = 507;

	public static String KEY_MEET_TYPE = ""; // set from string resource
	public static String KEY_MIN_SPLIT_DURATION = ""; // set from string resource
	public static String KEY_RESET_ATHLETES = ""; // set from string resource
	public static String string_relay_leg_text = ""; // set from string resource

	public static final String KEY_BEST_TIMES_SHOWN = "BestTimesShown";

	public static final String STATE_MAIN_ACTIVITY_FIRST_TIME_SHOWN = "MainActivityFirstTimeShown";
	// public static final String STATE_MAIN_ACTIVITY_ATHLETE_COUNT = "MainActivityFreeAppAthleteCount";

	public static final String STATE_MAIN_ACTIVITY_ACTIVE_FRAGMENT = "MainActivityActiveFragment";
	public static final String STATE_MAIN_ACTIVITY_PREVIOUS_FRAGMENT = "MainActivityPreviousFragment";
	public static final String STATE_MAIN_ACTIVITY_DRAWER_TITLE = "MainActivityDrawerTitle";
	public static final String STATE_MAIN_ACTIVITY_ACTIVE_FRAG_TITLE = "MainActivityActiveFragmentTitle";
	public static final String STATE_MAIN_ACTIVITY_RACE_TITLE = "MainActivityRaceTitle";
	public static final String STATE_MAIN_ACTIVITY_SELECTED_RACE_ID = "MainActivitySelectedRaceID";

	public static final String STATE_MEET_ID = "stateMeetID";

	public static final String STATE_RT_ARE_SPINNERS_ENABLED = "raceTimerAreSpinnersEnabled";
	// public static final String STATE_RT_RACE_COUNT = "freeAppRaceCount";

	public static final String STATE_RT_ATHLETE1_START_BUTTTON_VISIBLE = "raceTimerAhtlete1StartButtonVisible";
	public static final String STATE_RT_ATHLETE1_SPLIT_BUTTTON_VISIBLE = "raceTimerAhtlete1SplitButtonVisible";
	public static final String STATE_RT_ATHLETE1_STOP_BUTTTON_VISIBLE = "raceTimerAhtlete1StopButtonVisible";
	public static final String STATE_RT_ATHLETE1_RACE_TABLE_VISIBLE = "raceTimerAhtlete1RaceTableVisible";

	public static final String STATE_RT_ATHLETE2_START_BUTTTON_VISIBLE = "raceTimerAhtlete2StartButtonVisible";
	public static final String STATE_RT_ATHLETE2_SPLIT_BUTTTON_VISIBLE = "raceTimerAhtlete2SplitButtonVisible";
	public static final String STATE_RT_ATHLETE2_STOP_BUTTTON_VISIBLE = "raceTimerAhtlete2StopButtonVisible";
	public static final String STATE_RT_ATHLETE2_RACE_TABLE_VISIBLE = "raceTimerAhtlete2RaceTableVisible";

	public static final String STATE_BEST_TIMES_ATHLETE_ID = "bestTimesAthleteID";
	public static final String STATE_BEST_TIMES_IN_RELAYS = "bestTimesInRelays";

	public static final String STATE_RACE_SPLITS_RACE_ID = "raceSplitsRaceID";
	public static final String STATE_RACE_SPLITS_IS_RELAY = "raceSplitsIsRelay";

	// public static final String STATE_RELAY_RACE_COUNT = "freeAppRelayRaceCount";
	public static final String STATE_RELAY_RACE_ARE_SPINNERS_ENABLED = "relayRaceAreSpinnersEnabled";
	public static final String STATE_RELAY_RACE_START_BUTTTON_VISIBLE = "relayRaceStartButtonVisible";
	public static final String STATE_RELAY_RACE_SPLIT_BUTTTON_VISIBLE = "relayRaceSplitButtonVisible";
	public static final String STATE_RELAY_RACE_STOP_BUTTTON_VISIBLE = "relayRaceStopButtonVisible";
	public static final String STATE_RELAY_RACE_ACTIVE_ATHLETE_BUTTTON_VISIBLE = "relayRaceActiveAthleteButtonVisible";
	public static final String STATE_RELAY_RACE_ACTIVE_ATHLETE_TEXT = "relayRaceActiveAthleteButtonText";

	public static final String STATE_RELAY_RACE_ELAPSED_TIME = "relayRaceElapsedTime";

	public static final String STATE_CREATE_EVENT_UNITS_ID = "createEventUnitsID";

	public static final String DEFAULT_THUMB_NAIL_IMAGE_KEY = "default_thumb_nail";

	public static final int DIALOG_EDIT_TEXT_ADD_ATHLETE_NAME = 900;
	public static final int DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME = 901;
	public static final int DIALOG_EDIT_TEXT_MEETS = 902;
	public static final int DIALOG_EDIT_TEXT_EVENTS = 903;

	public static void setContext(Context context) {
		MySettings.mContext = context;
		mSplitsApplication = new SplitsApplication(mContext);
		Resources res = context.getResources();
		KEY_MEET_TYPE = res.getString(R.string.settings_meet_type_key);
		KEY_MIN_SPLIT_DURATION = res.getString(R.string.settings_split_min_duration_key);
		KEY_RESET_ATHLETES = res.getString(R.string.settings_reset_athletes_key);

		string_relay_leg_text = res.getString(R.string.relay_leg_text);
		BETA_EXPIRATION_MESSAGE = "This Splits beta version expired on "
				+ String.valueOf(BETA_EXPIRATION_MONTH) + "/"
				+ String.valueOf(BETA_EXPIRATION_DAY) + "/"
				+ String.valueOf(BETA_EXPIRATION_YEAR) + ".";
		/*
				Calendar expirationDate = Calendar.getInstance();
				// expirationDate.set(2014, 9, 1);
				expirationDate.set(2014, 9, 1, 0, 0, 0);
				BETA_EXPIRATION_DATE = expirationDate.getTimeInMillis();*/
	}

	public static final String HELP_MESSAGE = "helpMessage";

	public static void set(String header, Bundle bundle) {
		SharedPreferences outStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = outStates.edit();

		Set<String> keySet = bundle.keySet();
		Iterator<String> it = keySet.iterator();
		String key;
		Object o;

		while (it.hasNext()) {
			key = it.next();
			o = bundle.get(key);
			if (o == null) {
				ed.remove(key);

			} else if (o instanceof Integer) {
				ed.putInt(header + key, (Integer) o);

			} else if (o instanceof Long) {
				ed.putLong(header + key, (Long) o);

			} else if (o instanceof Boolean) {
				ed.putBoolean(header + key, (Boolean) o);

			} else if (o instanceof CharSequence) {
				ed.putString(header + key, ((CharSequence) o).toString());

			} else if (o instanceof String) {
				ed.putString(header + key, (String) o);

			} else if (o instanceof Float) {
				ed.putFloat(header + key, (Float) o);

			} else if (o instanceof Bundle) {
				set(key, (Bundle) o);
			} else {
				MyLog.e("MySettings", "set(): Unknown value type; key=" + key);
			}
		}
		ed.commit();
	}

	// ACTIVIES

	public static boolean isFirstTimeMainActivityShown() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_MAIN_ACTIVITY_FIRST_TIME_SHOWN, true);
	}

	public static int getMainActivityActiveFragment() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getInt(STATE_MAIN_ACTIVITY_ACTIVE_FRAGMENT, 0);
	}

	public static int getMainActivityPreviousFragment() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getInt(STATE_MAIN_ACTIVITY_PREVIOUS_FRAGMENT, 0);
	}

	public static CharSequence getMainActivityDrawerTitle() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getString(STATE_MAIN_ACTIVITY_DRAWER_TITLE,
				mContext.getResources().getString(R.string.app_name));
	}

	public static CharSequence getActiveFragmentTitle() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getString(STATE_MAIN_ACTIVITY_ACTIVE_FRAG_TITLE, "");
	}

	public static CharSequence getMainActivityRaceTitle() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getString(STATE_MAIN_ACTIVITY_RACE_TITLE, "");
	}

	public static long getMainActivitySelectedRaceID() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return storedStates.getLong(STATE_MAIN_ACTIVITY_SELECTED_RACE_ID, 0);
	}

	// RACE TIMER FRAGMENT
	public static String getAthleteBestTimeText(String athlete) {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getString(athlete + Race.STATE_BEST_TIME_TEXT, "Best Time: N/A");
	}

	public static boolean areSpinnersEnabled() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ARE_SPINNERS_ENABLED, true);
	}

	public static HashMap<String, Long> getRaceLongValues(String athlete) {
		HashMap<String, Long> longValues = new HashMap<String, Long>();
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);

		long StartTime = storedStates.getLong(athlete + Race.STATE_START_TIME, -1);
		long ElapsedTime = storedStates.getLong(athlete + Race.STATE_ELAPSED_TIME, -1);
		long PreviousElapsedTime = storedStates.getLong(athlete + Race.STATE_PREVIOUS_ELAPSED_TIME, -1);
		// long MeetID = storedStates.getLong(athlete + Race.STATE_MEET_ID, -1);
		long EventID = storedStates.getLong(athlete + Race.STATE_EVENT_ID, -1);
		long AthleteID = storedStates.getLong(athlete + Race.STATE_ATHLETE_ID, -1);
		long AthleteRaceID = storedStates.getLong(athlete + Race.STATE_ATHLETE_RACE_ID, -1);

		longValues.put(Race.STATE_START_TIME, StartTime);
		longValues.put(Race.STATE_ELAPSED_TIME, ElapsedTime);
		longValues.put(Race.STATE_PREVIOUS_ELAPSED_TIME, PreviousElapsedTime);
		// longValues.put(Race.STATE_MEET_ID, MeetID);
		longValues.put(Race.STATE_EVENT_ID, EventID);
		longValues.put(Race.STATE_ATHLETE_ID, AthleteID);
		longValues.put(Race.STATE_ATHLETE_RACE_ID, AthleteRaceID);
		return longValues;
	}

	public static HashMap<String, Integer> getRaceIntValues(String athlete) {
		HashMap<String, Integer> intValues = new HashMap<String, Integer>();
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);

		int Lap = storedStates.getInt(athlete + Race.STATE_LAP, -1);
		int BalanceOfLap = storedStates.getInt(athlete + Race.STATE_BALANCE_LAP, -1);
		int isRaceRunningValue = storedStates.getInt(athlete + Race.STATE_IS_RACE_RUNNING, 0);
		int newBestTimeValue = storedStates.getInt(athlete + Race.STATE_IS_NEW_BEST_TIME, 0);
		intValues.put(Race.STATE_LAP, Lap);
		intValues.put(Race.STATE_BALANCE_LAP, BalanceOfLap);
		intValues.put(Race.STATE_IS_RACE_RUNNING, isRaceRunningValue);
		intValues.put(Race.STATE_IS_NEW_BEST_TIME, newBestTimeValue);
		return intValues;
	}

	/*	public static int getRaceCount() {
			SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
					Context.MODE_PRIVATE);
			return storedStates.getInt(STATE_RT_RACE_COUNT, 0);
		}*/

	public static long getMeetID() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getLong(STATE_MEET_ID, -1);
	}

	public static boolean isAthlete1StartButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE1_START_BUTTTON_VISIBLE, true);
	}

	public static boolean isAthlete1SplitButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE1_SPLIT_BUTTTON_VISIBLE, false);
	}

	public static boolean isAthlete1StopButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE1_STOP_BUTTTON_VISIBLE, false);
	}

	public static boolean isAthlete1RaceTableVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE1_RACE_TABLE_VISIBLE, true);
	}

	public static boolean isAthlete2StartButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE2_START_BUTTTON_VISIBLE, true);
	}

	public static boolean isAthlete2SplitButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE2_SPLIT_BUTTTON_VISIBLE, false);
	}

	public static boolean isAthlete2StopButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE2_STOP_BUTTTON_VISIBLE, false);
	}

	public static boolean isAthlete2RaceTableVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RT_ATHLETE2_RACE_TABLE_VISIBLE, true);
	}

	// RESULTS BEST TIMES FRAGMENT

	public static int getBestTimeAthletesPosition(Spinner spinner) {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		long itemID = storedStates.getLong(STATE_BEST_TIMES_ATHLETE_ID, -1);
		return getIndexFromCursor(spinner, itemID);
	}

	public static boolean getBestTimeInRelays() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_BEST_TIMES_IN_RELAYS, false);
	}

	// CREATE EVENT FRAGMENT
	public static long getCreateEventUnitsID() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getLong(STATE_CREATE_EVENT_UNITS_ID, 1);
	}

	public static int getCreateEventUnitsPosition(Spinner spinner) {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		long itemID = storedStates.getLong(STATE_CREATE_EVENT_UNITS_ID, 1);
		return getIndex(spinner, itemID);
	}

	private static int getIndex(Spinner spinner, long itemID) {
		long spinnerItemID = -1;
		int spinnerIndex = -1;

		for (int i = 0; i < spinner.getCount(); i++) {
			spinnerItemID = spinner.getItemIdAtPosition(i);
			if (spinnerItemID == itemID) {
				spinnerIndex = i;
				break;
			}
		}
		return spinnerIndex;
	}

	// COMMON METHODS
	@SuppressWarnings("resource")
	public static int getIndexFromCursor(Spinner spinner, long itemID) {
		Cursor spinnerItem = null;
		long spinnerItemID = -1;
		int spinnerIndex = -1;

		for (int i = 0; i < spinner.getCount(); i++) {
			spinnerItem = (Cursor) spinner.getItemAtPosition(i);
			spinnerItemID = spinnerItem.getLong(spinnerItem.getColumnIndexOrThrow("_id"));
			if (spinnerItemID == itemID) {
				spinnerIndex = i;
				break;
			}
		}

		// Cannot close the spinnerItem. It is used in the spinner
		/*if (spinnerItem != null) {
			spinnerItem.close();
		}*/
		return spinnerIndex;
	}

	public static void ExecMultipleSQL(SQLiteDatabase db,
			ArrayList<String> sqlStatements) {
		for (String statement : sqlStatements) {
			if (statement.trim().length() > 0) {
				db.execSQL(statement);
			}
		}
	}

	// RELAY TIMER FRAGMENT
	public static boolean areRelaySpinnersEnabled() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RELAY_RACE_ARE_SPINNERS_ENABLED, true);
	}

	/*	public static int getRelayRaceCount() {
			SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
					Context.MODE_PRIVATE);
			return storedStates.getInt(STATE_RELAY_RACE_COUNT, 0);
		}*/

	public static boolean isRelayStartButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RELAY_RACE_START_BUTTTON_VISIBLE, true);
	}

	public static boolean isRelayStopButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RELAY_RACE_STOP_BUTTTON_VISIBLE, false);
	}

	public static boolean isRelaySplitButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RELAY_RACE_SPLIT_BUTTTON_VISIBLE, false);
	}

	public static boolean isRelayActiveAthleteButtonVisible() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_RELAY_RACE_ACTIVE_ATHLETE_BUTTTON_VISIBLE, false);
	}

	public static String getActiveAthleteButtonText() {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getString(STATE_RELAY_RACE_ACTIVE_ATHLETE_TEXT, "");
	}

	public static boolean isRelayRunning(String header) {
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(header + Relay.STATE_IS_RELAY_RACE_RUNNING, false);
	}

	public static HashMap<String, Integer> getRelayIntValues(String header) {
		HashMap<String, Integer> intValues = new HashMap<String, Integer>();
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);

		int Lap = storedStates.getInt(header + Relay.STATE_RELAY_LAP, -1);
		int BalanceOfLap = storedStates.getInt(header + Relay.STATE_RELAY_BALANCE_OF_LAP, -1);
		intValues.put(Relay.STATE_RELAY_LAP, Lap);
		intValues.put(Relay.STATE_RELAY_BALANCE_OF_LAP, BalanceOfLap);
		return intValues;
	}

	public static HashMap<String, Long> getRelayLongValues(String header) {
		HashMap<String, Long> longValues = new HashMap<String, Long>();
		SharedPreferences storedStates = mContext.getSharedPreferences(SPLITS_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);

		long StartTime = storedStates.getLong(header + Relay.STATE_RELAY_START_TIME, -1);
		long ElapsedTime = storedStates.getLong(header + Relay.STATE_RELAY_ELAPSED_TIME, -1);
		long PreviousElapsedTime = storedStates.getLong(header + Relay.STATE_RELAY_PREVIOUS_ELAPSED_TIME, -1);

		// long MeetID = storedStates.getLong(header + Relay.STATE_RELAY_MEET_ID, -1);
		long EventID = storedStates.getLong(header + Relay.STATE_RELAY_EVENT_ID, -1);
		long RelayRaceID = storedStates.getLong(header + Relay.STATE_RELAY_RACE_ID, -1);

		long Athlete0ID = storedStates.getLong(header + Relay.STATE_RELAY_LEG1_ATHLETE_ID, 1);
		long Athlete1ID = storedStates.getLong(header + Relay.STATE_RELAY_LEG2_ATHLETE_ID, 1);
		long Athlete2ID = storedStates.getLong(header + Relay.STATE_RELAY_LEG3_ATHLETE_ID, 1);
		long Athlete3ID = storedStates.getLong(header + Relay.STATE_RELAY_LEG4_ATHLETE_ID, 1);

		longValues.put(Relay.STATE_RELAY_START_TIME, StartTime);
		longValues.put(Relay.STATE_RELAY_ELAPSED_TIME, ElapsedTime);
		longValues.put(Relay.STATE_RELAY_PREVIOUS_ELAPSED_TIME, PreviousElapsedTime);

		// longValues.put(Relay.STATE_RELAY_MEET_ID, MeetID);
		longValues.put(Relay.STATE_RELAY_EVENT_ID, EventID);
		longValues.put(Relay.STATE_RELAY_RACE_ID, RelayRaceID);

		longValues.put(Relay.STATE_RELAY_LEG1_ATHLETE_ID, Athlete0ID);
		longValues.put(Relay.STATE_RELAY_LEG2_ATHLETE_ID, Athlete1ID);
		longValues.put(Relay.STATE_RELAY_LEG3_ATHLETE_ID, Athlete2ID);
		longValues.put(Relay.STATE_RELAY_LEG4_ATHLETE_ID, Athlete3ID);

		return longValues;
	}

	// FLURRY
	public static String getFlurryAPIkey() {
		// this method allows for both paid and free version to use different Flurry API keys.
		return mSplitsApplication.getFlurryApiKey();
	}

	public static boolean isFreeVersion() {
		return mSplitsApplication.isFreeVersion();
	}

	public static boolean isPaidVersion() {
		return mSplitsApplication.isPaidVersion();
	}

	/*	public static int getBuild() {
			return mSplitsApplication.getBuild();
		}*/

	public static String getPaidPackageName() {
		return "com.lbconsulting.splits.app.paid";
	}
}
