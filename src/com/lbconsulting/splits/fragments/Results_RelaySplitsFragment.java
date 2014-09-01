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
import com.lbconsulting.splits.adapters.RelaySplitsCursorAdapter;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RelaysTable;
import com.lbconsulting.splits.database.SplitsTable;

import de.greenrobot.event.EventBus;

public class Results_RelaySplitsFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private int mNumberFormat = DateTimeUtils.FORMAT_TENTHS;
	private static long mRelayID = -1;

	public static long getRelayID() {
		return mRelayID;
	}

	private long mMeetID = -1;
	private boolean mIsRelay = true;
	private String mShortRelayTitle = "";
	private long mRelayTimeValue = -1;
	private long mRelayDateValue = -1;

	private TextView tvMeetTitleAndDate;
	private TextView tvRelayNameAndTime;
	private ListView lvRelaySplits;
	private RelaySplitsCursorAdapter mRelaySplitsCursorAdapter;
	private Button btnOkFinished;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mRelaySplitsFragmentCallbacks;

	public Results_RelaySplitsFragment() {
		// Empty constructor
	}

	public static Results_RelaySplitsFragment newInstance(long relayID) {
		MyLog.i("Results_RelaySplitsFragment", "newInstance()");

		Results_RelaySplitsFragment fragment = new Results_RelaySplitsFragment();
		Bundle args = new Bundle();
		args.putLong(MySettings.STATE_RACE_SPLITS_RACE_ID, relayID);
		// args.putBoolean(MySettings.STATE_RACE_SPLITS_IS_RELAY, isRelay);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Results_RelaySplitsFragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_relay_splits, container, false);

		Bundle args = getArguments();
		if (args != null) {
			mRelayID = args.getLong(MySettings.STATE_RACE_SPLITS_RACE_ID, -1);
			// mIsRelay = args.getBoolean(MySettings.STATE_RACE_SPLITS_IS_RELAY);
		}

		Cursor relayRaceCursor = RelaysTable.getRelayRaceCursor(getActivity(), mRelayID);

		java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(getActivity());

		if (relayRaceCursor != null && relayRaceCursor.getCount() > 0) {
			relayRaceCursor.moveToFirst();
			mMeetID = relayRaceCursor.getLong(relayRaceCursor.getColumnIndexOrThrow(RelaysTable.COL_MEET_ID));

			mRelayDateValue = relayRaceCursor.getLong(relayRaceCursor
					.getColumnIndexOrThrow(RelaysTable.COL_RELAY_START_DATE_TIME));
			mRelayTimeValue = relayRaceCursor
					.getLong(relayRaceCursor.getColumnIndexOrThrow(RelaysTable.COL_RELAY_TIME));
			mShortRelayTitle = relayRaceCursor.getString(relayRaceCursor
					.getColumnIndexOrThrow(RelaysTable.COL_EVENT_SHORT_TITLE));
		}

		tvMeetTitleAndDate = (TextView) view.findViewById(R.id.tvMeetTitleAndDate);
		if (tvMeetTitleAndDate != null) {
			tvMeetTitleAndDate.setText(
					new StringBuilder().append(MeetsTable.getMeetTitle(getActivity(), mMeetID))
							.append(System.getProperty("line.separator"))
							.append(dateFormat.format(mRelayDateValue))
					);
		}

		tvRelayNameAndTime = (TextView) view.findViewById(R.id.tvRelayNameAndTime);
		if (tvRelayNameAndTime != null) {
			tvRelayNameAndTime.setText(getRelayNameAndTime(mRelayTimeValue));
		}

		if (relayRaceCursor != null) {
			relayRaceCursor.close();
		}

		lvRelaySplits = (ListView) view.findViewById(R.id.lvRelaySplits);
		mRelaySplitsCursorAdapter = new RelaySplitsCursorAdapter(getActivity(), null, 0, mNumberFormat);
		lvRelaySplits.setAdapter(mRelaySplitsCursorAdapter);

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new ShowPreviousFragment());
				}
			});
		}

		mRelaySplitsFragmentCallbacks = this;
		return view;
	}

	private String getRelayNameAndTime(long relayTimeValue) {
		StringBuilder sb = new StringBuilder();
		sb.append(mShortRelayTitle).append(": ")
				.append(DateTimeUtils.formatDuration(relayTimeValue, mNumberFormat));
		return sb.toString();
	}

	/*	public void onEvent(RaceFinalTime event) {
			tvRelayNameAndTime.setText(getRelayNameAndTime(event.getRaceFinalTime()));
			RelaysTable.setRelayBestTime(getActivity(), mShortRelayTitle);
		}*/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Results_RelaySplitsFragment", "onActivityCreated()");

		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_RELAY_SPLITS, null, mRelaySplitsFragmentCallbacks);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Results_RelaySplitsFragment", "onResume()");
		// EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Results_RelaySplitsFragment", "onPause()");
		// EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Results_RelaySplitsFragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Results_RelaySplitsFragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_SPLITS:
				cursorLoader = SplitsTable.getAllSplits(getActivity(), mRelayID, mIsRelay,
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
		MyLog.i("Results_RelaySplitsFragment", "onLoadFinished. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_SPLITS:
				mRelaySplitsCursorAdapter.swapCursor(newCursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Results_RelaySplitsFragment", "onLoadFinished. LoaderID = " + id);

		switch (id) {
			case MySettings.LOADER_FRAG_RELAY_SPLITS:
				mRelaySplitsCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
