package com.lbconsulting.splits.classes;

public class SplitsEvents {

	public static class ClearRace {

		public ClearRace() {
		}

	}

	public static class ChangeActionBarTitle {

		String mEventShortTitle;

		public ChangeActionBarTitle(String eventShortTitle) {
			mEventShortTitle = eventShortTitle;
		}

		public String getEventShortTitle() {
			return mEventShortTitle;
		}
	}

	public static class ShowStopButton {

		int mRaceTablePosition;

		public ShowStopButton(int raceTablePosition) {
			mRaceTablePosition = raceTablePosition;
		}

		public int getRaceTablePosition() {
			return mRaceTablePosition;
		}
	}

	public static class ShowSplitButton {

		int mRaceTablePosition;

		public ShowSplitButton(int raceTablePosition) {
			mRaceTablePosition = raceTablePosition;
		}

		public int getRaceTablePosition() {
			return mRaceTablePosition;
		}
	}

	public static class RaceComplete {

		long mRaceElapsedTime;
		int mRaceTablePosition;

		public RaceComplete(long raceElapsedTime, int raceTablePosition) {
			mRaceTablePosition = raceTablePosition;
			mRaceElapsedTime = raceElapsedTime;
		}

		public long getRaceElapsedTime() {
			return mRaceElapsedTime;
		}

		public int getRaceTablePosition() {
			return mRaceTablePosition;
		}
	}

	public static class RelaySplit {

		int mActiveAthlete;

		public RelaySplit(int activeAthlete) {
			mActiveAthlete = activeAthlete;
		}

		public int getActiveAthlete() {
			return mActiveAthlete;
		}
	}

	public static class RaceSplitsResults {

		long mRaceID, mAthleteID;

		public RaceSplitsResults(long raceID, long athleteID) {
			mRaceID = raceID;
			mAthleteID = athleteID;
		}

		public long getRaceID() {
			return mRaceID;
		}

		public long getAthleteID() {
			return mAthleteID;
		}
	}

	public static class UpdateBestTimes {

		public UpdateBestTimes() {
		}

	}

	/*	public static class DuplicateAthleteSelected {

			public DuplicateAthleteSelected() {
			}

		}*/

	public static class AddAthletetNameToContacts {

		long mAthleteID;
		String mAthleteName;

		public AddAthletetNameToContacts(long athleteID, String athleteName) {
			mAthleteID = athleteID;
			mAthleteName = athleteName;
		}

		public long getAthleteID() {
			return mAthleteID;
		}

		public String getAthleteName() {
			return mAthleteName;
		}
	}

	public static class AddThumbnailToMemoryCache {

		long mAthleteID;
		String mPhotoThumbnail;

		public AddThumbnailToMemoryCache(long athleteID, String photoThumbnail) {
			mAthleteID = athleteID;
			mPhotoThumbnail = photoThumbnail;
		}

		public long getAthleteID() {
			return mAthleteID;
		}

		public String getPhotoThumbnail() {
			return mPhotoThumbnail;
		}

	}

	public static class ShowPreviousFragment {

		public ShowPreviousFragment() {
		}
	}

	public static class ShowMeetsFragment {

		public ShowMeetsFragment() {
		}
	}

	public static class ShowEventsFragment {

		public ShowEventsFragment() {
		}
	}

	public static class ShowAthletesFragment {

		public ShowAthletesFragment() {
		}
	}

	public static class ShowRaceSplits {

		long mRaceID;
		int mSplitsResultsFragment;

		public ShowRaceSplits(long raceID, int splitsResultsFragment) {
			mRaceID = raceID;
			mSplitsResultsFragment = splitsResultsFragment;
		}

		public long getRaceID() {
			return mRaceID;
		}

		public int getSplitsResultsFragment() {
			return mSplitsResultsFragment;
		}
	}

	public static class UpdateRaceTime {

		long mRaceTime;

		public UpdateRaceTime(long raceTime) {
			mRaceTime = raceTime;
		}

		public long getRaceTime() {
			return mRaceTime;
		}
	}

	public static class SplitFragmentOnResume {

		int mActiveFragment;
		CharSequence mActiveFragmentTitle;

		public SplitFragmentOnResume(int activeFragment, CharSequence activeFragmentTitle) {
			mActiveFragment = activeFragment;
			mActiveFragmentTitle = activeFragmentTitle;
		}

		public int getActiveFragment() {
			return mActiveFragment;
		}

		public CharSequence getActiveFragmentTitle() {
			return mActiveFragmentTitle;
		}

	}

}
