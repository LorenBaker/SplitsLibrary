package com.lbconsulting.splits.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.SplitsEvents.RaceComplete;
import com.lbconsulting.splits.classes.SplitsEvents.RelaySplit;
import com.lbconsulting.splits.classes.SplitsEvents.ShowSplitButton;
import com.lbconsulting.splits.classes.SplitsEvents.ShowStopButton;
import com.lbconsulting.splits.classes.SplitsEvents.UpdateBestTimes;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.RelaysTable;
import com.lbconsulting.splits.database.SplitsTable;

import de.greenrobot.event.EventBus;

public class Relay {

	public static final String STATE_RELAY_START_TIME = "mRelayStartTime";
	public static final String STATE_RELAY_ELAPSED_TIME = "mRelayElapsedTime";
	public static final String STATE_RELAY_PREVIOUS_ELAPSED_TIME = "mRelayPreviousElapsedTime";

	// public static final String STATE_RELAY_MEET_ID = "mRelayMeetID";
	public static final String STATE_RELAY_EVENT_ID = "mRelayEventID";
	public static final String STATE_RELAY_RACE_ID = "mRelayRaceID";

	public static final String STATE_RELAY_LEG1_ATHLETE_ID = "mRelayLeg1AthleteID";
	public static final String STATE_RELAY_LEG2_ATHLETE_ID = "mRelayLeg2AthleteID";
	public static final String STATE_RELAY_LEG3_ATHLETE_ID = "mRelayLeg3AthleteID";
	public static final String STATE_RELAY_LEG4_ATHLETE_ID = "mRelayLeg4AthleteID";

	public static final String STATE_RELAY_LAP = "mRelayLap";
	public static final String STATE_RELAY_BALANCE_OF_LAP = "mRelayBalanceOfLap";

	public static final String STATE_IS_RELAY_RACE_RUNNING = "mIsRelayRunning";

	private Context mContext;

	private long mRelayStartTime = -1;
	private long mRelayElapsedTime = -1;
	private long mRelayPreviousElapsedTime = -1;

	private long mRelayMeetID = -1;
	private long mRelayEventID = -1;
	private long mRelayRaceID = -1;

	private ArrayList<Long> mAthletes = null;

	private int mMeetType;
	private int mRelayLap = 1;
	private int mRelayBalanceOfLap = -1;

	private boolean mIsRelayRunning = false;

	// Variables derived from mRelayEventID
	private int mRaceDistance = -1;
	private int mNumberOfLaps = -1;
	private int mRelayLapDistance = -1;
	private int mHasPartialLap = -1;
	private int mRelayEventUnitsID = -1;
	private String mEventShortTitle = "";

	// Variables derived from Application Preferences
	private long MIN_SPLIT_DURATION = 2000;

	// Language variables
	private String MEDLEY;
	private String FLY;
	private String BACK;
	private String BREAST;
	private String FREE;
	private String DASH;

	// private String RUN;

	public Relay(Context context) {
		mContext = context;
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String minSplitDurationDefault = context.getResources().getString(R.string.settings_split_min_duration_default);
		MIN_SPLIT_DURATION = Long.parseLong(sharedPrefs.getString(MySettings.KEY_MIN_SPLIT_DURATION,
				minSplitDurationDefault));

		Resources res = context.getResources();
		MEDLEY = res.getString(R.string.swimming_medley_text);
		FLY = res.getString(R.string.swimming_fly_text);
		BACK = res.getString(R.string.swimming_back_text);
		BREAST = res.getString(R.string.swimming_breast_text);
		FREE = res.getString(R.string.swimming_free_text);
		DASH = res.getString(R.string.track_dash_text);
		// RUN = res.getString(R.string.track_run_text);

		Clear();
		SetAllAthletesToDefaultValues();
	}

	public void Clear() {
		mRelayStartTime = -1;
		mRelayElapsedTime = -1;
		mRelayPreviousElapsedTime = -1;
		mIsRelayRunning = false;
	}

	public void SetAllAthletesToDefaultValues() {
		mAthletes = new ArrayList<Long>();
		mAthletes.add((long) 1);
		mAthletes.add((long) 1);
		mAthletes.add((long) 1);
		mAthletes.add((long) 1);
	}

	public long CreateNewRelayRace() {

		mRelayLap = 1;
		mRelayBalanceOfLap = 0;
		setEventCurosrItems(mRelayEventID);

		mRelayElapsedTime = 0;
		mRelayPreviousElapsedTime = 0;
		mRelayRaceID = RelaysTable.CreateRace(mContext, mRelayMeetID, mRelayEventID, mEventShortTitle, mAthletes,
				mRelayStartTime);
		if (mRelayRaceID > 0) {
			mIsRelayRunning = true;
		} else {
			mIsRelayRunning = false;
			MyLog.e("class Relay", "Relay race did not start! mRelayMeetID:" + mRelayMeetID
					+ "relayEventID:" + mRelayEventID + "meetType:" + mMeetType + "startTime:" + mRelayStartTime
					+ "athlete0:" + mAthletes.get(0)
					+ "athlete1:" + mAthletes.get(1)
					+ "athlete2:" + mAthletes.get(2)
					+ "athlete3:" + mAthletes.get(3));
		}

		if (mIsRelayRunning) {
			if (mRelayLap >= mNumberOfLaps) {
				EventBus.getDefault().post(new ShowStopButton(1));
			} else {
				EventBus.getDefault().post(new ShowSplitButton(1));
			}
		}

		return mRelayRaceID;
	}

	private void setEventCurosrItems(long eventID) {
		Cursor eventCursor = EventsTable.getEventCursor(mContext, eventID);
		if (eventCursor != null && eventCursor.getCount() > 0) {
			eventCursor.moveToFirst();

			mRaceDistance = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_DISTANCE));
			mNumberOfLaps = eventCursor.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_NUMBER_OF_LAPS));
			if (mNumberOfLaps < 1) {
				mNumberOfLaps = 1;
			}
			mRelayLapDistance = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_LAP_DISTANCE));
			mHasPartialLap = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_HAS_PARTIAL_LAP));

			mRelayEventUnitsID = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_UNITS_ID));

			mEventShortTitle = eventCursor
					.getString(eventCursor.getColumnIndexOrThrow(EventsTable.COL_EVENT_SHORT_TITLE));
		} else {
			mRaceDistance = -1;
			mNumberOfLaps = -1;
			mRelayLapDistance = -1;
			mHasPartialLap = -1;
			mRelayEventUnitsID = 1;
			mEventShortTitle = "";
		}

		if (eventCursor != null) {
			eventCursor.close();
		}

	}

	public void CreateSplit(long currentTime) {
		new Split().execute(currentTime, (long) 0, mAthletes.get(mRelayLap - 1));
	}

	public void StopRace(long currentTime) {
		new Split().execute(currentTime, (long) 1, mAthletes.get(mRelayLap - 1));
	}

	private class Split extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			return createSplit(params[0], params[1], params[2]);
		}

		@Override
		protected void onPostExecute(Boolean isGoodSplit) {
			if (isGoodSplit) {
				if (mIsRelayRunning) {
					if (mRelayLap >= mNumberOfLaps) {
						EventBus.getDefault().post(new ShowStopButton(1));
					}
					// show the athlete that is now being timed
					if (mRelayLap < 5) {
						EventBus.getDefault().post(new RelaySplit(mRelayLap));
					}

				} else {
					mRelayStartTime = -1;
					EventBus.getDefault().post(new RaceComplete(mRelayElapsedTime, 1));
					EventBus.getDefault().post(new UpdateBestTimes());
				}
			}
		}
	}

	private boolean createSplit(long currentTime, long isStopped, long athleteID) {
		long tempElapsedTime = currentTime - mRelayStartTime;
		tempElapsedTime = DateTimeUtils.RoundMills(tempElapsedTime, DateTimeUtils.FORMAT_TENTHS);
		boolean isGoodSplit = false;

		long splitDuration = tempElapsedTime - mRelayPreviousElapsedTime;
		if (splitDuration > MIN_SPLIT_DURATION) {
			isGoodSplit = true;
			mRelayElapsedTime = tempElapsedTime;
			mRelayPreviousElapsedTime = mRelayElapsedTime;

			if (mRelayLapDistance == 0) {
				mRelayLapDistance = mRaceDistance;
			}

			int distance = (mRelayLapDistance * mRelayLap) - mRelayBalanceOfLap;
			if (mHasPartialLap > 0 && mRelayLap == 1) {
				distance = mHasPartialLap;
				mRelayBalanceOfLap = mRelayLapDistance - mHasPartialLap;
			}

			// set the eventShortTitle for the relay leg
			String eventShortTitle = "";

			switch (mMeetType) {
				case MySettings.SWIM_MEET:
					// working with a swim meet relay
					if (mEventShortTitle.contains(MEDLEY)) {
						switch (mRelayLap) {
							case 1:
								eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance,
										mRelayEventUnitsID, BACK, false);
								break;
							case 2:
								eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance,
										mRelayEventUnitsID, BREAST, false);
								break;
							case 3:
								eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance,
										mRelayEventUnitsID, FLY, false);
								break;
							case 4:
								eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance,
										mRelayEventUnitsID, FREE, false);
								break;

							default:
								break;
						}

					} else if (mEventShortTitle.contains(FREE)) {
						eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance, mRelayEventUnitsID,
								FREE, false);
					}
					break;

				case MySettings.TRACK_MEET:
					eventShortTitle = EventsTable.MakeShortTitle(mContext, mRelayLapDistance, mRelayEventUnitsID, DASH,
							false);
					break;

				default:
					break;
			}

			SplitsTable.CreateSplit(mContext, mRelayRaceID, athleteID, mRelayLap, eventShortTitle, distance,
					splitDuration, mRelayElapsedTime, true);

			// create a "Race" for the athlete's split so as to be able to display its results athlete's relay leg
			RacesTable.CreateRace(mContext, mRelayMeetID, mRelayEventID, eventShortTitle, athleteID,
					mRelayStartTime, splitDuration, mRelayLap, true, mRelayRaceID);
			RacesTable.setAthleteEventBestTime(mContext, eventShortTitle, athleteID, true, mRelayLap);

			mRelayLap++;

			if (isStopped == 1) {
				// set the relay's time
				mIsRelayRunning = false;
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(RelaysTable.COL_RELAY_TIME, mRelayElapsedTime);
				RelaysTable.UpdateRelayRaceFieldValues(mContext, mRelayRaceID, newFieldValues);
				mRelayLap = 1;
			}
		}
		return isGoodSplit;

	}

	public Bundle getState() {
		Bundle outState = new Bundle();
		outState.putLong(STATE_RELAY_START_TIME, mRelayStartTime);
		outState.putLong(STATE_RELAY_ELAPSED_TIME, mRelayElapsedTime);
		outState.putLong(STATE_RELAY_PREVIOUS_ELAPSED_TIME, mRelayPreviousElapsedTime);

		// outState.putLong(STATE_RELAY_MEET_ID, mRelayMeetID);
		outState.putLong(STATE_RELAY_EVENT_ID, mRelayEventID);
		outState.putLong(STATE_RELAY_RACE_ID, mRelayRaceID);

		outState.putLong(STATE_RELAY_LEG1_ATHLETE_ID, mAthletes.get(0));
		outState.putLong(STATE_RELAY_LEG2_ATHLETE_ID, mAthletes.get(1));
		outState.putLong(STATE_RELAY_LEG3_ATHLETE_ID, mAthletes.get(2));
		outState.putLong(STATE_RELAY_LEG4_ATHLETE_ID, mAthletes.get(3));

		outState.putInt(STATE_RELAY_LAP, mRelayLap);
		outState.putInt(STATE_RELAY_BALANCE_OF_LAP, mRelayBalanceOfLap);

		outState.putBoolean(STATE_IS_RELAY_RACE_RUNNING, mIsRelayRunning);

		return outState;
	}

	public void setState(HashMap<String, Long> raceLongValues, HashMap<String, Integer> raceIntValues,
			boolean isRaceRunning) {

		SetAllAthletesToDefaultValues();

		Iterator<Map.Entry<String, Long>> itLongValues = raceLongValues.entrySet().iterator();
		while (itLongValues.hasNext()) {
			Map.Entry<String, Long> pairs = itLongValues.next();

			if (pairs.getKey().toString().endsWith(STATE_RELAY_START_TIME)) {
				mRelayStartTime = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_RELAY_ELAPSED_TIME)) {
				mRelayElapsedTime = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_RELAY_PREVIOUS_ELAPSED_TIME)) {
				mRelayPreviousElapsedTime = pairs.getValue();
				/*} else if (pairs.getKey().toString().equals(STATE_RELAY_MEET_ID)) {
					mRelayMeetID = pairs.getValue();*/
			} else if (pairs.getKey().toString().equals(STATE_RELAY_EVENT_ID)) {
				mRelayEventID = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_RELAY_RACE_ID)) {
				mRelayRaceID = pairs.getValue();

			} else if (pairs.getKey().toString().equals(STATE_RELAY_LEG1_ATHLETE_ID)) {
				mAthletes.remove(0);
				mAthletes.add(0, pairs.getValue());
			} else if (pairs.getKey().toString().equals(STATE_RELAY_LEG2_ATHLETE_ID)) {
				mAthletes.remove(1);
				mAthletes.add(1, pairs.getValue());
			} else if (pairs.getKey().toString().equals(STATE_RELAY_LEG3_ATHLETE_ID)) {
				mAthletes.remove(2);
				mAthletes.add(2, pairs.getValue());
			} else if (pairs.getKey().toString().equals(STATE_RELAY_LEG4_ATHLETE_ID)) {
				mAthletes.remove(3);
				mAthletes.add(3, pairs.getValue());
			} else {
				MyLog.e("class Relay", "Unknown long value in setState():" + pairs.getKey().toString());
			}

			itLongValues.remove(); // avoids a ConcurrentModificationException
		}

		Iterator<Map.Entry<String, Integer>> itIntValues = raceIntValues.entrySet().iterator();
		while (itIntValues.hasNext()) {
			Map.Entry<String, Integer> pairs = itIntValues.next();

			if (pairs.getKey().toString().equals(STATE_RELAY_LAP)) {
				mRelayLap = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_RELAY_BALANCE_OF_LAP)) {
				mRelayBalanceOfLap = pairs.getValue();
			} else {
				MyLog.e("class Relay", "Unknown Integer value in setState():" + pairs.getKey().toString());
			}

			itIntValues.remove(); // avoids a ConcurrentModificationException
		}

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));

		mIsRelayRunning = isRaceRunning;

		setEventCurosrItems(mRelayEventID);
	}

	public ArrayList<Long> getAthletes() {
		return mAthletes;
	}

	public void setAthletes(ArrayList<Long> athletes) {
		mAthletes = athletes;
	}

	public void setAthlete(long athleteID, int athletePosition) {
		if (athletePosition > -1 && athletePosition < 4 && athleteID > 0) {
			mAthletes.remove(athletePosition);
			mAthletes.add(athletePosition, athleteID);
		}
	}

	public int getActiveAthlete() {
		return mRelayLap;
	}

	public long getRelayStartTime() {
		return mRelayStartTime;
	}

	public void setRelayStartTime(long currentTimeMillis) {
		mRelayStartTime = currentTimeMillis;
	}

	public boolean isRelayRunning() {
		return mIsRelayRunning;
	}

	public long getRelayMeetID() {
		return mRelayMeetID;
	}

	public void setRelayMeetID(long relayMeetID) {
		this.mRelayMeetID = relayMeetID;
	}

	public long getRelayEventID() {
		return mRelayEventID;
	}

	public void setRelayEventID(long relayEventID) {
		this.mRelayEventID = relayEventID;
	}

	public long getRelayRaceID() {
		return mRelayRaceID;
	}

	public void resetRealyRaceID() {
		mRelayRaceID = -1;
	}

	public long getRelayElapsedTime() {
		return mRelayElapsedTime;
	}

}
