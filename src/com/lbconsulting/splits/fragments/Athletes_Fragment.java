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
import com.lbconsulting.splits.activites.MainActivity;
import com.lbconsulting.splits.adapters.fragAthletesCursorAdapter;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.classes.SplitsEvents.SplitFragmentOnResume;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.dialogs.Deletion_Alert_DialogFragment;
import com.lbconsulting.splits.dialogs.EditText_DialogFragment;

import de.greenrobot.event.EventBus;

public class Athletes_Fragment extends Fragment implements LoaderCallbacks<Cursor> {

	private ListView lvAthletes;
	private Button btnOkFinished;
	private int mMeetType;
	private CharSequence mActiveFragmentTitle;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mAthletesCallbacks;
	private fragAthletesCursorAdapter mAthletesCursorAdaptor;

	private final static String ARG_BACK_STACK_TAG = "argBackStackTag";

	private String mLastBackStackTag = "";

	public Athletes_Fragment() {
		// Empty constructor
	}

	public static Athletes_Fragment newInstance(String lastBackStackTag) {
		MyLog.i("Athletes_Fragment", "newInstance()");

		Athletes_Fragment fragment = new Athletes_Fragment();
		// Supply the lastBackStackTag input as an argument.
		Bundle args = new Bundle();
		args.putString(ARG_BACK_STACK_TAG, lastBackStackTag);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Athletes_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_athletes, container, false);

		Bundle args = getArguments();
		if (args != null) {
			mLastBackStackTag = args.getString(ARG_BACK_STACK_TAG);
		}

		lvAthletes = (ListView) view.findViewById(R.id.lvAthletes);
		if (lvAthletes != null) {
			mAthletesCursorAdaptor = new fragAthletesCursorAdapter(getActivity(), null, 0);
			lvAthletes.setAdapter(mAthletesCursorAdaptor);

			// set the list view's contextual mode
			lvAthletes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

			lvAthletes.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				private int nr = 0;

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Do nothing
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mAthletesCursorAdaptor.setContextualMode(false);
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					nr = 0;
					MenuInflater contextMenueInflater = getActivity().getMenuInflater();
					contextMenueInflater.inflate(R.menu.setup_frags_contextual_menu, menu);
					mAthletesCursorAdaptor.setContextualMode(true);
					return true;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					int itemId = item.getItemId();
					if (itemId == R.id.item_edit) {
						nr = 0;
						Cursor cursor = AthletesTable.getAllCheckedAthletesCursor(getActivity());
						if (cursor != null && cursor.getCount() > 0) {
							String dialogTitle = getActivity().getString(R.string.athletes_item_edit_dialog_title);
							cursor.moveToFirst();
							long athleteID = cursor.getLong(cursor
									.getColumnIndexOrThrow(AthletesTable.COL_ATHLETE_ID));
							String athleteDisplayName = cursor.getString(cursor
									.getColumnIndexOrThrow(AthletesTable.COL_DISPLAY_NAME));

							FragmentManager fm = getFragmentManager();
							EditText_DialogFragment editAthleteDisplayName = EditText_DialogFragment
									.newInstance(dialogTitle, athleteID, athleteDisplayName,
											MySettings.DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME);
							editAthleteDisplayName.show(fm, "fragment_edit_athlete_display_name");
						}
						if (cursor != null) {
							cursor.close();
						}
						mode.finish();
					} else if (itemId == R.id.item_delete) {
						Resources res = getResources();
						String quantityString = res.getQuantityString(R.plurals.athletes, nr, nr);
						StringBuilder message = new StringBuilder();

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
										MySettings.DIALOG_EDIT_TEXT_ATHLETE_DISPLAY_NAME);
						deleteAthletes.show(fm, "fragment_delete_athlete");
						nr = 0;
						/*Toast.makeText(getActivity(), "Delete All Checked Athletes under construction.",
								Toast.LENGTH_SHORT).show();*/
						mode.finish();
					} else {
					}
					return false;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long athleteID, boolean checked) {
					if (checked) {
						nr++;
					} else {
						nr--;
					}
					AthletesTable.setAthleteCheckbox(getActivity(), athleteID, checked);
					mode.setTitle(nr + getActivity().getString(R.string.onItemCheckedStateChanged_selected_text));
				}
			});

			lvAthletes.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long athleteID) {
					// An athlete title has been selected ...
					AthletesTable.ToggleAthleteSelectedBox(getActivity(), athleteID);
				}
			});

			lvAthletes.setOnItemLongClickListener(new OnItemLongClickListener() {

				// Contextual action mode
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long athleteID) {
					lvAthletes.setItemChecked(position, !AthletesTable.isAthleteChecked(getActivity(), athleteID));
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
		mAthletesCallbacks = this;
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Athletes_Fragment", "onActivityCreated()");
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_ATHLETES, null, mAthletesCallbacks);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Athletes_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer
				.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE, String.valueOf(MySettings.SWIM_MEET)));

		mActiveFragmentTitle = getResources().getStringArray(R.array.navDrawerTitles)[MainActivity.FRAG_ATHLETES];

		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_ATHLETES, null, mAthletesCallbacks);
		// show the Active Fragment Title
		EventBus.getDefault().post(new SplitFragmentOnResume(MainActivity.FRAG_ATHLETES, mActiveFragmentTitle));

		// MainActivity.displayBackStack(getFragmentManager());
		// reset backStack
		/*		if (!mLastBackStackTag.isEmpty()) {
					try {
						String backStackTag = "BS_" + MainActivity.FRAG_ATHLETES;
						int backStackCount = getFragmentManager().getBackStackEntryCount();
						if (backStackCount > 2) {
							getFragmentManager().popBackStackImmediate(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
							MainActivity.displayBackStack(getFragmentManager());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}*/
		// MainActivity.displayBackStack(getFragmentManager());
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Athletes_Fragment", "onPause()");

		// reset backStack
		/*		try {
					String backStackTag = "BS_" + MainActivity.FRAG_ATHLETES;
					int backStackCount = getFragmentManager().getBackStackEntryCount();
					if (backStackCount > 1) {
						getFragmentManager().popBackStackImmediate(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						MainActivity.displayBackStack(getFragmentManager());
					}
				} catch (Exception e) {
					e.printStackTrace();

				}*/
		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Athletes_Fragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Athletes_Fragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_ATHLETES:
				cursorLoader = AthletesTable.getAllAthletesExcludingDefault(getActivity(),
						MySettings.BOTH_SELECTED_AND_UNSELECTED_ATHLETES,
						AthletesTable.SORT_ORDER_ATHLETE_DISPLAY_NAME);
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Athletes_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_ATHLETES:
				mAthletesCursorAdaptor.swapCursor(newCursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Athletes_Fragment", "onLoaderReset. LoaderID = " + id);

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_ATHLETES:
				mAthletesCursorAdaptor.swapCursor(null);
				break;

			default:
				break;
		}
	}

}
