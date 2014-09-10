package com.lbconsulting.splits.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.adapters.fragEventsCursorAdapter;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ChangeActionBarTitle;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.dialogs.Deletion_Alert_DialogFragment;
import com.lbconsulting.splits.dialogs.EditText_DialogFragment;

import de.greenrobot.event.EventBus;

public class Events_Fragment extends Fragment implements LoaderCallbacks<Cursor> {

	private ListView lvEvents;
	private Button btnOkFinished;
	private int mMeetType;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mEventsCallbacks;
	private fragEventsCursorAdapter mEventsCursorAdapter;

	public Events_Fragment() {
		// Empty constructor
	}

	public static Events_Fragment newInstance() {
		MyLog.i("Events_Fragment", "newInstance()");
		Events_Fragment fragment = new Events_Fragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Events_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_events, container, false);

		lvEvents = (ListView) view.findViewById(R.id.lvEvents);
		if (lvEvents != null) {
			mEventsCursorAdapter = new fragEventsCursorAdapter(getActivity(), null, 0);
			lvEvents.setAdapter(mEventsCursorAdapter);

			// set the list view's contextual mode
			lvEvents.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

			lvEvents.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				private int nr = 0;

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Do nothing
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mEventsCursorAdapter.setContextualMode(false);
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					nr = 0;
					MenuInflater contextMenueInflater = getActivity().getMenuInflater();
					contextMenueInflater.inflate(R.menu.setup_frags_contextual_menu, menu);
					mEventsCursorAdapter.setContextualMode(true);
					return true;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					int itemId = item.getItemId();
					if (itemId == R.id.item_edit) {
						nr = 0;
						Cursor cursor = EventsTable.getAllCheckedEventsCursor(getActivity(), mMeetType);
						if (cursor != null && cursor.getCount() > 0) {
							String dialogTitle = getActivity().getString(R.string.events_item_edit_dialog_title);
							cursor.moveToFirst();
							long eventID = cursor.getLong(cursor.getColumnIndexOrThrow(EventsTable.COL_EVENT_ID));
							String eventLongTitle = cursor.getString(cursor
									.getColumnIndexOrThrow(EventsTable.COL_EVENT_LONG_TITLE));

							FragmentManager fm = getFragmentManager();
							EditText_DialogFragment editMeetTitle = EditText_DialogFragment
									.newInstance(dialogTitle, eventID, eventLongTitle,
											MySettings.DIALOG_EDIT_TEXT_EVENTS);
							editMeetTitle.show(fm, "fragment_edit_event_title");
						}
						if (cursor != null) {
							cursor.close();
						}
						mode.finish();
					} else if (itemId == R.id.item_delete) {
						Resources res = getResources();
						String quantityString = getResources().getQuantityString(R.plurals.events, nr, nr);

						StringBuilder message = new StringBuilder();

						// Note: this delete dialog uses the same text as the Athletes's delete dialog
						// but the plurals points to events.
						String athletes_item_delete_message1 = res.getString(R.string.athletes_item_delete_message1);
						String athletes_item_delete_message2 = res.getString(R.string.athletes_item_delete_message2);
						String athletes_item_delete_message3 = res.getString(R.string.athletes_item_delete_message3);
						String athletes_item_delete_text = res.getString(R.string.delete_text);
						String athletes_item_delete_question_mark = res
								.getString(R.string.athletes_item_delete_question_mark);

						message.append(athletes_item_delete_message1)
								.append(" ")
								.append(quantityString)
								.append(" ")
								.append(athletes_item_delete_message2)
								.append(System.getProperty("line.separator"))
								.append(System.getProperty("line.separator"))
								.append(athletes_item_delete_message3)
								.append(" ")
								.append(quantityString)
								.append(athletes_item_delete_question_mark);
						String dialogTitle = athletes_item_delete_text + " " + quantityString
								+ athletes_item_delete_question_mark;

						FragmentManager fm = getFragmentManager();
						Deletion_Alert_DialogFragment deleteAthletes = Deletion_Alert_DialogFragment
								.newInstance(dialogTitle, message.toString(), mMeetType,
										MySettings.DIALOG_EDIT_TEXT_EVENTS);
						deleteAthletes.show(fm, "fragment_delete_events");
						nr = 0;
						mode.finish();
					}
					return false;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long EventID, boolean checked) {
					if (checked) {
						nr++;
					} else {
						nr--;
					}
					EventsTable.setEventCheckbox(getActivity(), EventID, checked);
					mode.setTitle(nr + getActivity().getString(R.string.onItemCheckedStateChanged_selected_text));
				}
			});

			lvEvents.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long EventID) {
					// A meet title has been selected ...
					EventsTable.ToggleEventSelectedBox(getActivity(), EventID);
				}
			});

			lvEvents.setOnItemLongClickListener(new OnItemLongClickListener() {

				// Contextual action mode
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long EventID) {
					lvEvents.setItemChecked(position, !EventsTable.isEventChecked(getActivity(), EventID));
					return false;
				}
			});
		}

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new ShowPreviousFragment());
				}
			});
		}

		mEventsCallbacks = this;
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Events_Fragment", "onActivityCreated()");
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_EVENTS, null, mEventsCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Events_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_EVENTS, null, mEventsCallbacks);
		// show the Active Fragment Title
		EventBus.getDefault().post(new ChangeActionBarTitle(""));
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Events_Fragment", "onPause()");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Events_Fragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Events_Fragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_EVENTS:
				cursorLoader = EventsTable.getAllEvents(getActivity(), mMeetType,
						EventsTable.SORT_ORDER_UNITS_STYLE_DISTANCE);
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Events_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_EVENTS:
				mEventsCursorAdapter.swapCursor(newCursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Events_Fragment", "onLoaderReset. LoaderID = " + id);

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_EVENTS:
				mEventsCursorAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
