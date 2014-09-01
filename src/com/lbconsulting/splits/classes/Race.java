package com.lbconsulting.splits.classes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.SplitsEvents.RaceComplete;
import com.lbconsulting.splits.classes.SplitsEvents.ShowSplitButton;
import com.lbconsulting.splits.classes.SplitsEvents.ShowStopButton;
import com.lbconsulting.splits.classes.SplitsEvents.UpdateBestTimes;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.SplitsTable;

import de.greenrobot.event.EventBus;

public class Race {

	public static final String STATE_START_TIME = "mStartTime";
	public static final String STATE_ELAPSED_TIME = "mElapsedTime";
	public static final String STATE_PREVIOUS_ELAPSED_TIME = "mPreviousElapsedTime";

	public static final String STATE_MEET_ID = "mMeetID";
	public static final String STATE_EVENT_ID = "mEventID";
	public static final String STATE_ATHLETE_ID = "mAthleteID";

	public static final String STATE_LAP = "mLap";
	public static final String STATE_BALANCE_LAP = "mBalanceOfLap";
	public static final String STATE_ATHLETE_RACE_ID = "mAthleteRaceID";
	public static final String STATE_IS_RACE_RUNNING = "mIsRaceRunning";

	public static final String STATE_IS_NEW_BEST_TIME = "mIsNewBestTime";
	public static final String STATE_BEST_TIME_TEXT = "mBestTimeText";

	private Context mContext;

	// Variables
	private long mStartTime = -1;
	private long mElapsedTime = -1;
	private long mPreviousElapsedTime = -1;

	private long mMeetID = -1;
	private long mEventID = -1;
	private long mAthleteID = -1;
	private int mSplitTablePosition = -1;

	private int mLap = -1;
	private int mBalanceOfLap = -1;
	private long mAthleteRaceID = -1;
	private boolean mIsRaceRunning = false;

	private boolean mIsNewBestTime = false;
	private String mBestTimeText = "";

	// Variables derived from mEventID
	private int mRaceDistance = -1;
	private int mNumberOfLaps = -1;
	private int mRaceLapDistance = -1;
	private int mHasPartialLap = -1;
	private String mEventShortTitle = "";

	// Variables derived from Application Preferences
	private long MIN_SPLIT_DURATION = 2000;

	public Race(Context context) {
		mContext = context;
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String minSplitDurationDefault = context.getResources().getString(R.string.settings_split_min_duration_default);
		MIN_SPLIT_DURATION = Long.parseLong(sharedPrefs.getString(MySettings.KEY_MIN_SPLIT_DURATION,
				minSplitDurationDefault));
	}

	public void Clear() {
		mStartTime = -1;
		mElapsedTime = -1;
		mPreviousElapsedTime = -1;

		// mEventID = -1;
		mAthleteRaceID = -1;
		mIsRaceRunning = false;
	}

	public long StartNewRace() {

		mEventShortTitle = EventsTable.getEventShortTitle(mContext, mEventID);

		mLap = 1;
		mBalanceOfLap = 0;
		setEventCurosrItems(mEventID);

		mElapsedTime = 0;
		mPreviousElapsedTime = 0;
		mAthleteRaceID = RacesTable.CreateRace(mContext, mMeetID, mEventID, mEventShortTitle, mAthleteID, mStartTime,
				false);
		if (mAthleteRaceID > 0) {
			mIsRaceRunning = true;
		} else {
			mIsRaceRunning = false;
		}

		if (mLap >= mNumberOfLaps) {
			EventBus.getDefault().post(new ShowStopButton(mSplitTablePosition));
		} else {
			EventBus.getDefault().post(new ShowSplitButton(mSplitTablePosition));
		}

		return mAthleteRaceID;
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
			mRaceLapDistance = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_LAP_DISTANCE));
			mHasPartialLap = eventCursor
					.getInt(eventCursor.getColumnIndexOrThrow(EventsTable.COL_HAS_PARTIAL_LAP));
		} else {
			mRaceDistance = -1;
			mNumberOfLaps = -1;
			mRaceLapDistance = -1;
			mHasPartialLap = -1;
		}

		if (eventCursor != null) {
			eventCursor.close();
		}

	}

	public void CreateSplit(long currentTime) {
		new Split().execute(currentTime, (long) 0);
	}

	public void StopRace(long currentTime) {
		new Split().execute(currentTime, (long) 1);
	}

	private class Split extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			return createSplit(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(Boolean isGoodSplit) {
			if (isGoodSplit) {
				if (mIsRaceRunning) {
					if (mLap >= mNumberOfLaps) {
						EventBus.getDefault().post(new ShowStopButton(mSplitTablePosition));
					}

				} else {
					mStartTime = -1;
					EventBus.getDefault().post(new RaceComplete(mElapsedTime, mSplitTablePosition));
					EventBus.getDefault().post(new UpdateBestTimes());
				}
			}
		}

	}

	private boolean createSplit(long currentTime, long isStopped) {
		long tempElapsedTime = currentTime - mStartTime;
		tempElapsedTime = DateTimeUtils.RoundMills(tempElapsedTime, DateTimeUtils.FORMAT_TENTHS);
		boolean isGoodSplit = false;

		long splitDuration = tempElapsedTime - mPreviousElapsedTime;
		if (splitDuration > MIN_SPLIT_DURATION) {
			isGoodSplit = true;
			mElapsedTime = tempElapsedTime;
			mPreviousElapsedTime = mElapsedTime;

			if (mRaceLapDistance == 0) {
				mRaceLapDistance = mRaceDistance;
			}

			int distance = (mRaceLapDistance * mLap) - mBalanceOfLap;
			if (mHasPartialLap > 0 && mLap == 1) {
				distance = mHasPartialLap;
				mBalanceOfLap = mRaceLapDistance - mHasPartialLap;
			}

			SplitsTable.CreateSplit(mContext, mAthleteRaceID, mAthleteID, mLap, null,
					distance, splitDuration, mElapsedTime, false);
			mLap++;

			if (isStopped == 1) {
				mIsRaceRunning = false;
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(RacesTable.COL_RACE_TIME, mElapsedTime);
				RacesTable.UpdateRaceFieldValues(mContext, mAthleteRaceID, newFieldValues);
				RacesTable.setAthleteEventBestTime(mContext, mEventShortTitle, mAthleteID, false, 0);
			}
		}
		return isGoodSplit;
	}

	public Bundle getState() {
		Bundle outState = new Bundle();
		outState.putLong(STATE_START_TIME, mStartTime);
		outState.putLong(STATE_ELAPSED_TIME, mElapsedTime);
		outState.putLong(STATE_PREVIOUS_ELAPSED_TIME, mPreviousElapsedTime);

		outState.putLong(STATE_MEET_ID, mMeetID);
		outState.putLong(STATE_EVENT_ID, mEventID);
		outState.putLong(STATE_ATHLETE_ID, mAthleteID);

		outState.putInt(STATE_LAP, mLap);
		outState.putInt(STATE_BALANCE_LAP, mBalanceOfLap);
		outState.putLong(STATE_ATHLETE_RACE_ID, mAthleteRaceID);

		int isRaceRunningValue = 0;
		if (mIsRaceRunning) {
			isRaceRunningValue = 1;
		}
		outState.putInt(STATE_IS_RACE_RUNNING, isRaceRunningValue);

		int newBestTime = 0;
		if (mIsNewBestTime) {
			newBestTime = 1;
		}
		outState.putInt(STATE_IS_NEW_BEST_TIME, newBestTime);
		outState.putString(STATE_BEST_TIME_TEXT, mBestTimeText);

		return outState;
	}

	public void setState(HashMap<String, Long> raceLongValues, HashMap<String, Integer> raceIntValues,
			String bestTimeText) {

		Iterator<Map.Entry<String, Long>> itLongValues = raceLongValues.entrySet().iterator();
		while (itLongValues.hasNext()) {
			Map.Entry<String, Long> pairs = itLongValues.next();

			if (pairs.getKey().toString().endsWith(STATE_START_TIME)) {
				mStartTime = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_ELAPSED_TIME)) {
				mElapsedTime = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_PREVIOUS_ELAPSED_TIME)) {
				mPreviousElapsedTime = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_MEET_ID)) {
				mMeetID = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_EVENT_ID)) {
				mEventID = pairs.getValue();
				mEventShortTitle = EventsTable.getEventShortTitle(mContext, mEventID);
			} else if (pairs.getKey().toString().equals(STATE_ATHLETE_ID)) {
				mAthleteID = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_ATHLETE_RACE_ID)) {
				mAthleteRaceID = pairs.getValue();
			} else {
				MyLog.e("class Race", "Unknown long value in setState():" + pairs.getKey().toString());
			}

			itLongValues.remove(); // avoids a ConcurrentModificationException
		}

		Iterator<Map.Entry<String, Integer>> itIntValues = raceIntValues.entrySet().iterator();
		while (itIntValues.hasNext()) {
			Map.Entry<String, Integer> pairs = itIntValues.next();

			if (pairs.getKey().toString().equals(STATE_LAP)) {
				mLap = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_BALANCE_LAP)) {
				mBalanceOfLap = pairs.getValue();
			} else if (pairs.getKey().toString().equals(STATE_IS_RACE_RUNNING)) {
				mIsRaceRunning = pairs.getValue() == 1;
			} else if (pairs.getKey().toString().equals(STATE_IS_NEW_BEST_TIME)) {
				mIsNewBestTime = pairs.getValue() == 1;
			} else {
				MyLog.e("class Race", "Unknown Integer value in setState():" + pairs.getKey().toString());
			}

			itIntValues.remove(); // avoids a ConcurrentModificationException
		}

		mBestTimeText = bestTimeText;
		setEventCurosrItems(mEventID);
	}

	public long getStartTime() {
		return mStartTime;
	}

	public void setStartTime(long startTime) {
		this.mStartTime = startTime;
	}

	public long getElapsedTime() {
		return mElapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.mElapsedTime = elapsedTime;
	}

	public long getMeetID() {
		return mMeetID;
	}

	public void setMeetID(long meetID) {
		this.mMeetID = meetID;
	}

	public long getEventID() {
		return mEventID;
	}

	public void setEventID(long eventID) {
		this.mEventID = eventID;
	}

	public long getAthleteID() {
		return mAthleteID;
	}

	public void setAthleteID(long athleteID) {
		this.mAthleteID = athleteID;
	}

	public int getLap() {
		return mLap;
	}

	public long getAthleteRaceID() {
		return mAthleteRaceID;
	}

	public void setAthleteRaceID(long athleteRaceID) {
		this.mAthleteRaceID = athleteRaceID;
	}

	public boolean isRaceRunning() {
		return mIsRaceRunning;
	}

	public int getSplitTablePosition() {
		return mSplitTablePosition;
	}

	public void setSplitTablePosition(int splitTablePosition) {
		this.mSplitTablePosition = splitTablePosition;
	}

	public boolean isNewBestTime() {
		return mIsNewBestTime;
	}

	public void setIsNewBestTime(boolean isNewBestTime) {
		this.mIsNewBestTime = isNewBestTime;
	}

	public String getBestTimeText() {
		return mBestTimeText;
	}

	public void setBestTimeText(String bestTimeText) {
		this.mBestTimeText = bestTimeText;
	}

}
