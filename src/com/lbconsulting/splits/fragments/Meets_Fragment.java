package com.lbconsulting.splits.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.activites.MainActivity;
import com.lbconsulting.splits.adapters.fragMeetsCursorAdapter;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.classes.SplitsEvents.SplitFragmentOnResume;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.dialogs.Deletion_Alert_DialogFragment;
import com.lbconsulting.splits.dialogs.EditText_DialogFragment;

import de.greenrobot.event.EventBus;

public class Meets_Fragment extends Fragment implements LoaderCallbacks<Cursor> {

	private Button btnCreateMeetTitle;
	private Button btnOkFinished;
	private EditText txtMeetTitle;
	private ListView lvMeetTitles;
	private int mMeetType;
	private CharSequence mActiveFragmentTitle;

	private LoaderManager mLoaderManager = null;
	private LoaderManager.LoaderCallbacks<Cursor> mMeetTitlesCallbacks;
	private fragMeetsCursorAdapter mMeetsCursorAdapter;

	public Meets_Fragment() {
		// Empty constructor
	}

	public static Meets_Fragment newInstance() {
		MyLog.i("Meets_Fragment", "newInstance()");

		Meets_Fragment fragment = new Meets_Fragment();
		return fragment;

	}

	public static int getFragmentID() {
		return MainActivity.FRAG_MEETS;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("Meets_Fragment", "onCreateView()");
		View view = inflater.inflate(R.layout.frag_meets, container, false);

		btnCreateMeetTitle = (Button) view.findViewById(R.id.btnCreateMeetTitle);
		btnCreateMeetTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CreateMeetTitle(txtMeetTitle.getText().toString().trim(), false);
			}

		});

		txtMeetTitle = (EditText) view.findViewById(R.id.txtMeetTitle);

		lvMeetTitles = (ListView) view.findViewById(R.id.lvMeetTitles);
		if (lvMeetTitles != null) {
			mMeetsCursorAdapter = new fragMeetsCursorAdapter(getActivity(), null, 0);
			lvMeetTitles.setAdapter(mMeetsCursorAdapter);

			// set the list view's contextual mode
			lvMeetTitles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

			lvMeetTitles.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				private int nr = 0;

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Do nothing
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mMeetsCursorAdapter.setContextualMode(false);
					txtMeetTitle.setVisibility(View.VISIBLE);
					btnCreateMeetTitle.setVisibility(View.VISIBLE);
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					nr = 0;
					MenuInflater contextMenueInflater = getActivity().getMenuInflater();
					contextMenueInflater.inflate(R.menu.setup_frags_contextual_menu, menu);
					mMeetsCursorAdapter.setContextualMode(true);
					txtMeetTitle.setVisibility(View.GONE);
					btnCreateMeetTitle.setVisibility(View.GONE);
					return true;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					int itemId = item.getItemId();
					if (itemId == R.id.item_edit) {
						nr = 0;
						Cursor cursor = MeetsTable.getAllCheckedMeetsCursor(getActivity(), mMeetType);
						if (cursor != null && cursor.getCount() > 0) {
							String dialogTitle = getActivity().getResources().getString(
									R.string.meets_item_edit_dialog_title);
							cursor.moveToFirst();
							long meetID = cursor.getLong(cursor.getColumnIndexOrThrow(MeetsTable.COL_MEET_ID));
							String meetTitle = cursor.getString(cursor.getColumnIndexOrThrow(MeetsTable.COL_TITLE));

							FragmentManager fm = getFragmentManager();
							EditText_DialogFragment editMeetTitle = EditText_DialogFragment
									.newInstance(dialogTitle, meetID, meetTitle, MySettings.DIALOG_EDIT_TEXT_MEETS);
							editMeetTitle.show(fm, "fragment_edit_meet_title");
						}
						if (cursor != null) {
							cursor.close();
						}
						mode.finish();
					} else if (itemId == R.id.item_delete) {
						String quantityString = getResources().getQuantityString(R.plurals.meets, nr, nr);
						Resources res = getResources();
						StringBuilder message = new StringBuilder();

						// Note: this delete dialog uses the same text as the Athletes's delete dialog
						// but the plurals points to meets.
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
										MySettings.DIALOG_EDIT_TEXT_MEETS);
						deleteAthletes.show(fm, "fragment_delete_meets");
						nr = 0;
						mode.finish();
					}
					return false;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
					if (checked) {
						nr++;
					} else {
						nr--;
					}
					MeetsTable.setMeetTitleCheckbox(getActivity(), id, checked);
					mode.setTitle(nr + getActivity().getString(R.string.onItemCheckedStateChanged_selected_text));
				}
			});

			lvMeetTitles.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long meetTitleID) {
					// A meet title has been selected ...
					MeetsTable.ToggleMeetTitleSelectedBox(getActivity(), meetTitleID);
				}
			});

			lvMeetTitles.setOnItemLongClickListener(new OnItemLongClickListener() {

				// Contextual action mode
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long meetID) {
					lvMeetTitles.setItemChecked(position, !MeetsTable.isMeetTitleChecked(getActivity(), meetID));
					return false;
				}
			});
		}

		btnOkFinished = (Button) view.findViewById(R.id.btnOkFinished);
		if (btnOkFinished != null) {
			btnOkFinished.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CreateMeetTitle(txtMeetTitle.getText().toString().trim(), true);
					EventBus.getDefault().post(new ShowPreviousFragment());
				}
			});
		}

		mMeetTitlesCallbacks = this;
		return view;
	}

	protected void CreateMeetTitle(String meetTitle, boolean fromOkButton) {
		if (meetTitle.isEmpty()) {
			if (!fromOkButton) {
				ShowErrorDialog(getActivity().getString(R.string.btnCreateMeetTitle_meet_title_not_provided));
			}
			return;
		}

		long newMeetID = MeetsTable.CreateMeet(getActivity(), mMeetType, meetTitle);

		if (newMeetID > 0) {
			String toastMessage = new StringBuilder()
					.append(txtMeetTitle.getText().toString().trim())
					.append(System.getProperty("line.separator"))
					.append(getActivity().getResources()
							.getString(R.string.successfully_created_text))
					.toString();

			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(
					getActivity(),
					getActivity().getResources().getString(
							R.string.btnCreateMeetTitle_failed_to_create_meet_title),
					Toast.LENGTH_SHORT).show();
		}
		txtMeetTitle.setText("");

	}

	private void ShowErrorDialog(String message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(getActivity().getResources()
				.getString(R.string.btnCreateMeetTitle_unable_to_create_meet));
		alert.setMessage(message);
		alert.setIcon(R.drawable.ic_action_error);
		String ok = getActivity().getResources().getString(R.string.btnOK_text);
		alert.setPositiveButton(ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("Meets_Fragment", "onActivityCreated()");

		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(MySettings.LOADER_FRAG_MEETS, null, mMeetTitlesCallbacks);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		MyLog.i("Meets_Fragment", "onResume()");
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));
		mLoaderManager.restartLoader(MySettings.LOADER_FRAG_MEETS, null, mMeetTitlesCallbacks);

		mActiveFragmentTitle = getResources().getStringArray(R.array.navDrawerTitles)[MainActivity.FRAG_MEETS];
		// show the Active Fragment Title
		EventBus.getDefault().post(new SplitFragmentOnResume(MainActivity.FRAG_MEETS, mActiveFragmentTitle));

		// show the Active Fragment Title
		// EventBus.getDefault().post(new ChangeActionBarTitle(""));
		super.onResume();
	}

	@Override
	public void onPause() {
		MyLog.i("Meets_Fragment", "onPause()");

		// close the keyboard
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtMeetTitle.getWindowToken(), 0);

		super.onPause();
	}

	@Override
	public void onDestroy() {
		MyLog.i("Meets_Fragment", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		MyLog.i("Meets_Fragment", "onCreateLoader. LoaderId = " + id);

		CursorLoader cursorLoader = null;

		switch (id) {
			case MySettings.LOADER_FRAG_MEETS:
				cursorLoader = MeetsTable.getAllMeetsExcludingDefault(getActivity(), mMeetType,
						MeetsTable.SORT_ORDER_MEET_TITLE);
				break;

			default:
				break;
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		int id = loader.getId();
		MyLog.i("Meets_Fragment", "onLoadFinished. LoaderID = " + id);
		// The asynchronous load is complete and the newCursor is now available for use.

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_MEETS:
				mMeetsCursorAdapter.swapCursor(newCursor);
				break;

			default:
				break;

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		MyLog.i("Meets_Fragment", "onLoaderReset. LoaderID = " + id);

		switch (loader.getId()) {
			case MySettings.LOADER_FRAG_MEETS:
				mMeetsCursorAdapter.swapCursor(null);
				break;

			default:
				break;

		}

	}

}
