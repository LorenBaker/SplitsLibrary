package com.lbconsulting.splits.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.RelaysTable;
import com.lbconsulting.splits.database.SplitsTable;

public class Number_Picker_DialogFragment extends DialogFragment {

	private NumberPicker npHours;
	private NumberPicker npMinutes;
	private NumberPicker npSeconds;
	private NumberPicker npMilliSeconds;
	private TextView tvMilliSeconds;
	private RelativeLayout rlMilliSeconds;

	private Button btnApply;
	private Button btnCancel;

	private String mDialogTitle;
	private long mRaceID;
	private boolean mIsRelay;
	private long mOrignialTime;
	private int mNumberFormat;

	private String mEventShortTitle;
	private long mRaceAthleteID = -1;

	private static final String DIALOG_TITLE = "dialogTitle";
	private static final String DIALOG_RACE_ID = "dialogRaceID";
	private static final String DIALOG_IS_RELAY = "dialogIsRelay";
	private static final String DIALOG_NUMBER_FORMAT = "dialogNumberFormat";

	private static final int HOURS = 0;
	private static final int MINUTES = 1;
	private static final int SECONDS = 2;
	private static final int MILLIS = 3;

	private ArrayList<Integer> mOriginalTime_HrsMinSec = null;

	public Number_Picker_DialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Create a new instance of Number_Picker_DialogFragment
	 * 
	 * @param itemID
	 * @return Number_Picker_DialogFragment
	 */
	public static Number_Picker_DialogFragment newInstance(String dialogTitle, long raceID, boolean isRelay,
			int numberFormat) {
		Number_Picker_DialogFragment fragment = new Number_Picker_DialogFragment();
		Bundle args = new Bundle();
		args.putString(DIALOG_TITLE, dialogTitle);
		args.putLong(DIALOG_RACE_ID, raceID);
		args.putBoolean(DIALOG_IS_RELAY, isRelay);
		args.putInt(DIALOG_NUMBER_FORMAT, numberFormat);
		fragment.setArguments(args);
		return fragment;
	}

	@SuppressWarnings("resource")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Number_Picker_DialogFragment", "onCreateView");

		Bundle args = getArguments();
		if (args != null) {
			mDialogTitle = args.getString(DIALOG_TITLE);
			mRaceID = args.getLong(DIALOG_RACE_ID, -1);
			mIsRelay = args.getBoolean(DIALOG_IS_RELAY);
			mNumberFormat = args.getInt(DIALOG_NUMBER_FORMAT, 1);

			Cursor raceCursor = null;
			if (mIsRelay) {
				raceCursor = RelaysTable.getRelayRaceCursor(getActivity(), mRaceID);
				if (raceCursor != null && raceCursor.getCount() > 0) {
					raceCursor.moveToFirst();
					mEventShortTitle = raceCursor.getString(raceCursor
							.getColumnIndexOrThrow(RelaysTable.COL_EVENT_SHORT_TITLE));
				}
			} else {
				// have an individual race
				raceCursor = RacesTable.getRaceCursor(getActivity(), mRaceID);
				if (raceCursor != null && raceCursor.getCount() > 0) {
					raceCursor.moveToFirst();
					mEventShortTitle = raceCursor.getString(raceCursor
							.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
					mRaceAthleteID = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_ATHLETE_ID));
				}
			}
			if (raceCursor != null) {
				raceCursor.close();
			}
		}

		Cursor cursor = null;
		if (mIsRelay) {
			cursor = RelaysTable.getRelayRaceCursor(getActivity(), mRaceID);
			if (cursor != null && cursor.getColumnCount() > 0) {
				cursor.moveToFirst();
				mOrignialTime = cursor.getLong(cursor.getColumnIndexOrThrow(RelaysTable.COL_RELAY_TIME));
			}
		} else {
			cursor = RacesTable.getRaceCursor(getActivity(), mRaceID);
			if (cursor != null && cursor.getColumnCount() > 0) {
				cursor.moveToFirst();
				mOrignialTime = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME));
			}
		}

		if (cursor != null) {
			cursor.close();
		}

		mOriginalTime_HrsMinSec = DateTimeUtils.getHrsMinSec(mOrignialTime);

		View view = inflater.inflate(R.layout.dialog_number_picker, container);

		npHours = (NumberPicker) view.findViewById(R.id.npHours);
		if (npHours != null) {
			npHours.setMinValue(0);
			npHours.setMaxValue(100);
			npHours.setValue(mOriginalTime_HrsMinSec.get(HOURS));
		}

		npMinutes = (NumberPicker) view.findViewById(R.id.npMinutes);
		if (npMinutes != null) {
			npMinutes.setMinValue(0);
			npMinutes.setMaxValue(59);
			npMinutes.setValue(mOriginalTime_HrsMinSec.get(MINUTES));
		}

		npSeconds = (NumberPicker) view.findViewById(R.id.npSeconds);
		if (npSeconds != null) {
			npSeconds.setMinValue(0);
			npSeconds.setMaxValue(59);
			npSeconds.setFormatter(new NumberPicker.Formatter() {

				@Override
				public String format(int value) {
					return String.format(Locale.getDefault(), "%02d", value);
				}
			});
			npSeconds.setValue(mOriginalTime_HrsMinSec.get(SECONDS));
		}

		npMilliSeconds = (NumberPicker) view.findViewById(R.id.npMilliSeconds);
		if (npMilliSeconds != null) {
			npMilliSeconds.setMinValue(0);
			npMilliSeconds.setMaxValue(9);
		}

		rlMilliSeconds = (RelativeLayout) view.findViewById(R.id.rlMilliSeconds);
		tvMilliSeconds = (TextView) view.findViewById(R.id.tvMilliSeconds);
		if (tvMilliSeconds != null) {
			switch (mNumberFormat) {
				case 0:
					if (rlMilliSeconds != null) {
						rlMilliSeconds.setVisibility(View.GONE);
					}
					break;

				case 1:
					npMilliSeconds.setValue(mOriginalTime_HrsMinSec.get(MILLIS) / 100);
					break;

				case 2:
					tvMilliSeconds.setText(R.string.hunds_text);
					npMilliSeconds.setMaxValue(99);
					npMilliSeconds.setFormatter(new NumberPicker.Formatter() {

						@Override
						public String format(int value) {
							return String.format(Locale.getDefault(), "%02d", value);
						}
					});
					npMilliSeconds.setValue(mOriginalTime_HrsMinSec.get(MILLIS) / 10);
					break;

				default:
					break;

			}

		}

		btnApply = (Button) view.findViewById(R.id.btnApply);
		if (btnApply != null) {
			btnApply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// send event to Flurry
					Map<String, String> timeAdjustmentParams = new HashMap<String, String>();

					// calculate the time adjustment amount
					ArrayList<Integer> finalTime_HrsMinSec = new ArrayList<Integer>();
					finalTime_HrsMinSec.add(npHours.getValue());
					finalTime_HrsMinSec.add(npMinutes.getValue());
					finalTime_HrsMinSec.add(npSeconds.getValue());
					switch (mNumberFormat) {
						case DateTimeUtils.FORMAT_SECONDS:
							finalTime_HrsMinSec.add(0);
							break;
						case DateTimeUtils.FORMAT_HUNDREDTHS:
							finalTime_HrsMinSec.add(npMilliSeconds.getValue() * 10);
							break;
						default:
							finalTime_HrsMinSec.add(npMilliSeconds.getValue() * 100);
							break;
					}

					long finalTime = DateTimeUtils.getTime(finalTime_HrsMinSec);
					long totalDelta = finalTime - mOrignialTime;
					if (mIsRelay) {
						timeAdjustmentParams.put("RelayTimeAdjustment", String.valueOf(totalDelta));
						UpdateRaceSplits(totalDelta);
						// update the race's time
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(RelaysTable.COL_RELAY_TIME, finalTime);
						RelaysTable.UpdateRelayRaceFieldValues(getActivity(), mRaceID, newFieldValues);
						RelaysTable.setRelayBestTime(getActivity(), mEventShortTitle);

					} else {
						UpdateRaceSplits(totalDelta);
						timeAdjustmentParams.put("RaceTimeAdjustment", String.valueOf(totalDelta));
						// update the race's time
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(RacesTable.COL_RACE_TIME, finalTime);
						RacesTable.UpdateRaceFieldValues(getActivity(), mRaceID, newFieldValues);
						RacesTable.setAthleteEventBestTime(getActivity(), mEventShortTitle, mRaceAthleteID, false, 0);
					}

					FlurryAgent.logEvent("RaceTimeChanged", timeAdjustmentParams);
					getDialog().dismiss();
				}

				private void UpdateRaceSplits(long totalDeltaMills) {
					if (totalDeltaMills != 0) {
						boolean isNegative = false;
						if (totalDeltaMills < 0) {
							isNegative = true;
							totalDeltaMills = totalDeltaMills * -1;
						}
						int totalDeletaTenths = (int) (totalDeltaMills / 100);
						int adjustmetToAllSplits = 0;
						int numberOfAditionalAdjustments = 0;

						Cursor splitsCursor = SplitsTable.getAllSplitsCursor(getActivity(), mRaceID, mIsRelay,
								SplitsTable.SORT_ORDER_SPLIT_ID_ASC);
						if (splitsCursor != null && splitsCursor.getCount() > 0) {
							long oneTenth = 100;
							adjustmetToAllSplits = totalDeletaTenths / splitsCursor.getCount();
							adjustmetToAllSplits = adjustmetToAllSplits * 100;
							if (isNegative) {
								adjustmetToAllSplits = adjustmetToAllSplits * -1;
								oneTenth = oneTenth * -1;
							}
							numberOfAditionalAdjustments = totalDeletaTenths % splitsCursor.getCount();

							int count = 0;
							long newSplitTime = 0;
							long previousSplitCumulativeTime = 0;
							long newCumulativeTime = 0;
							long splitID = -1;
							int relayLeg = 1;
							while (splitsCursor.moveToNext()) {

								newSplitTime = splitsCursor.getLong(splitsCursor
										.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_TIME)) + adjustmetToAllSplits;
								if (count < numberOfAditionalAdjustments) {
									newSplitTime += oneTenth;
								}
								newCumulativeTime = previousSplitCumulativeTime + newSplitTime;

								splitID = splitsCursor.getLong(splitsCursor
										.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_ID));

								ContentValues newFieldValues = new ContentValues();
								newFieldValues.put(SplitsTable.COL_SPLIT_TIME, newSplitTime);
								newFieldValues.put(SplitsTable.COL_CUMULATIVE_TIME, newCumulativeTime);
								SplitsTable.UpdateSplitFieldValues(getActivity(), splitID, newFieldValues);

								if (mIsRelay) {
									long athleteID = splitsCursor.getLong(splitsCursor
											.getColumnIndexOrThrow(SplitsTable.COL_ATHLETE_ID));
									long relayID = splitsCursor.getLong(splitsCursor
											.getColumnIndexOrThrow(SplitsTable.COL_RACE_ID));

									RacesTable.UpdateRelayRaceTime(getActivity(), athleteID, relayID, newSplitTime);
									RacesTable.setAthleteEventBestTime(getActivity(), mEventShortTitle, athleteID,
											true, relayLeg);
									relayLeg++;
								}
								previousSplitCumulativeTime = newCumulativeTime;
								count++;
							}

						}
						if (splitsCursor != null) {
							splitsCursor.close();
						}
					}
				}
			});
		}

		btnCancel = (Button) view.findViewById(R.id.btnCancel);
		if (btnCancel != null) {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Do Nothing
					getDialog().dismiss();
				}
			});
		}
		getDialog().setTitle(mDialogTitle);

		return view;
	}

	@Override
	public void onDestroy() {
		MyLog.i("Number_Picker_DialogFragment", "onDestroy");
		super.onDestroy();
	}
}
