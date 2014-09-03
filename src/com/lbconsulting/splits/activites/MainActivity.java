/*
 * Copyright 2014 Loren A. Baker
 * All rights reserved.
 */

package com.lbconsulting.splits.activites;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.about.About;
import com.lbconsulting.splits.R;
import com.lbconsulting.splits.R.string;
import com.lbconsulting.splits.classes.DateTimeUtils;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.classes.SplitsBuild;
import com.lbconsulting.splits.classes.SplitsEvents.AddAthletetNameToContacts;
import com.lbconsulting.splits.classes.SplitsEvents.AddThumbnailToMemoryCache;
import com.lbconsulting.splits.classes.SplitsEvents.ChangeActionBarTitle;
import com.lbconsulting.splits.classes.SplitsEvents.ClearRace;
import com.lbconsulting.splits.classes.SplitsEvents.DuplicateAthleteSelected;
import com.lbconsulting.splits.classes.SplitsEvents.ShowAthletesFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowEventsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowMeetsFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowPreviousFragment;
import com.lbconsulting.splits.classes.SplitsEvents.ShowRaceSplits;
import com.lbconsulting.splits.database.AthletesTable;
import com.lbconsulting.splits.database.EventsTable;
import com.lbconsulting.splits.database.MeetsTable;
import com.lbconsulting.splits.database.RacesTable;
import com.lbconsulting.splits.database.RelaysTable;
import com.lbconsulting.splits.database.SplitsTable;
import com.lbconsulting.splits.database.Splits_ContentProvider;
import com.lbconsulting.splits.dialogs.ContactAdder_DialogFragment;
import com.lbconsulting.splits.dialogs.DeleteRace_DialogFragment;
import com.lbconsulting.splits.dialogs.EditText_DialogFragment;
import com.lbconsulting.splits.dialogs.Help_DialogFragment;
import com.lbconsulting.splits.dialogs.Number_Picker_DialogFragment;
import com.lbconsulting.splits.dialogs.PlayStore_DialogFragment;
import com.lbconsulting.splits.fragments.Athletes_Fragment;
import com.lbconsulting.splits.fragments.Create_Event_Fragment;
import com.lbconsulting.splits.fragments.Events_Fragment;
import com.lbconsulting.splits.fragments.Meets_Fragment;
import com.lbconsulting.splits.fragments.Race_Timer_Fragment;
import com.lbconsulting.splits.fragments.Relay_Timer_Fragment;
import com.lbconsulting.splits.fragments.Results_AllRaces_Fragment;
import com.lbconsulting.splits.fragments.Results_BestTimes_Fragment;
import com.lbconsulting.splits.fragments.Results_RaceSplitsFragment;
import com.lbconsulting.splits.fragments.Results_RelaySplitsFragment;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {

	private int mActiveFragment = 0;
	private int mPreviousFragment = 0;
	public static final int FRAG_INDIVIDUAL_RACES = 0;
	public static final int FRAG_RELAY_RACES = 1;
	public static final int FRAG_RESULTS_BEST_TIMES = 2;
	public static final int FRAG_RESULTS_ALL_RACES = 3;
	public static final int FRAG_ATHLETES = 4;
	public static final int FRAG_MEETS = 5;
	public static final int FRAG_EVENTS = 6;
	public static final int FRAG_CREATE_EVENTS = 7;
	public static final int FRAG_RESULTS_RACE_SPLITS = 100;
	public static final int FRAG_RESULTS_RELAY_SPLITS = 101;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private CharSequence mPreviousTitle;
	private String[] mFragmentTitles;

	private int mAthleteCount = 0;
	private int mMeetType;
	private long mSelectedRaceID;
	private boolean mIsRelay = false;
	// private boolean mIsFAQ = false;

	private static final int CONTACTS_URI_REQUEST = 333;
	private static LruCache<String, Bitmap> mMemoryCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i("MainActivity", "onCreate()");
		setContentView(R.layout.activity_main);

		MySettings.setContext(this);

		// long now = System.currentTimeMillis(); // current time
		if (MySettings.IS_BETA) {
			Calendar now = Calendar.getInstance();

			if (now.get(Calendar.YEAR) > MySettings.BETA_EXPIRATION_YEAR) {
				ShowBetaExpirationDialog();
			} else if (now.get(Calendar.YEAR) == MySettings.BETA_EXPIRATION_YEAR) {
				if (now.get(Calendar.MONTH) + 1 > MySettings.BETA_EXPIRATION_MONTH) {
					ShowBetaExpirationDialog();
				} else if (now.get(Calendar.MONTH) + 1 == MySettings.BETA_EXPIRATION_MONTH) {
					if (now.get(Calendar.DAY_OF_MONTH) >= MySettings.BETA_EXPIRATION_DAY) {
						ShowBetaExpirationDialog();
					}
				}
			}
		}

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));

		mMemoryCache = Splits_ContentProvider.getAthleteThumbnailMemoryCache();
		mDrawerTitle = getResources().getString(R.string.app_name);
		mTitle = mDrawerTitle;
		// mTitle = mDrawerTitle = getTitle();
		mFragmentTitles = getResources().getStringArray(R.array.navDrawerTitles);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mFragmentTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this, /* host Activity */
				mDrawerLayout, /* DrawerLayout object */
				R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open, /* "open drawer" description for accessibility */
				R.string.drawer_close /* "close drawer" description for accessibility */
				) {

					public void onDrawerClosed(View view) {
						getActionBar().setTitle(mTitle);
						invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
					}

					public void onDrawerOpened(View drawerView) {
						getActionBar().setTitle(mDrawerTitle);
						invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
					}
				};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			SelectFragment(0);
		}

		if (MySettings.isFirstTimeMainActivityShown()) {
			Bundle MainActivityBundle = new Bundle();
			MainActivityBundle.putBoolean(MySettings.STATE_MAIN_ACTIVITY_FIRST_TIME_SHOWN, false);
			MySettings.set("", MainActivityBundle);

			ShowHelpQuickStart();
		}
	}

	private void ShowBetaExpirationDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set title
		alertDialogBuilder.setTitle("Splits Beta Has Expired");

		// set dialog message
		alertDialogBuilder
				.setMessage(MySettings.BETA_EXPIRATION_MESSAGE)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						MainActivity.this.finish();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("MainActivity", "onCreateOptionsMenu()");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MyLog.i("MainActivity", "onPrepareOptionsMenu()");
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		if (drawerOpen) {

			menu.findItem(R.id.action_accept).setVisible(false);
			menu.findItem(R.id.action_add_person).setVisible(false);
			menu.findItem(R.id.action_add_athlete_name).setVisible(false);
			menu.findItem(R.id.action_select_all_events).setVisible(false);
			menu.findItem(R.id.action_deselect_all_events).setVisible(false);

			menu.findItem(R.id.action_email_times).setVisible(false);
			menu.findItem(R.id.action_email_race_results).setVisible(false);
			// menu.findItem(R.id.action_email_splits_csv_file).setVisible(false);

			menu.findItem(R.id.action_preferences).setVisible(false);
			menu.findItem(R.id.action_help).setVisible(false);
			menu.findItem(R.id.action_help_faq).setVisible(false);
			menu.findItem(R.id.action_help_quick_start).setVisible(false);
			menu.findItem(R.id.action_about).setVisible(true);

		} else {
			switch (mActiveFragment) {
				case FRAG_INDIVIDUAL_RACES:
				case FRAG_RELAY_RACES:
					menu.findItem(R.id.action_accept).setVisible(true);
					menu.findItem(R.id.action_add_person).setVisible(false);
					menu.findItem(R.id.action_add_athlete_name).setVisible(false);
					menu.findItem(R.id.action_select_all_events).setVisible(false);
					menu.findItem(R.id.action_deselect_all_events).setVisible(false);
					menu.findItem(R.id.action_race_splits_edit).setVisible(false);
					menu.findItem(R.id.action_race_splits_discard).setVisible(false);

					menu.findItem(R.id.action_email_times).setVisible(false);
					menu.findItem(R.id.action_email_race_results).setVisible(false);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(false);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);

					menu.findItem(R.id.action_about).setVisible(true);

					break;
				case FRAG_ATHLETES:
					menu.findItem(R.id.action_accept).setVisible(false);
					menu.findItem(R.id.action_add_person).setVisible(true);
					menu.findItem(R.id.action_add_athlete_name).setVisible(true);
					menu.findItem(R.id.action_select_all_events).setVisible(false);
					menu.findItem(R.id.action_deselect_all_events).setVisible(false);
					menu.findItem(R.id.action_race_splits_edit).setVisible(false);
					menu.findItem(R.id.action_race_splits_discard).setVisible(false);

					menu.findItem(R.id.action_email_times).setVisible(false);
					menu.findItem(R.id.action_email_race_results).setVisible(false);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(false);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);
					menu.findItem(R.id.action_about).setVisible(true);

					break;

				case FRAG_EVENTS:
					menu.findItem(R.id.action_accept).setVisible(false);
					menu.findItem(R.id.action_add_person).setVisible(false);
					menu.findItem(R.id.action_add_athlete_name).setVisible(false);
					menu.findItem(R.id.action_select_all_events).setVisible(true);
					menu.findItem(R.id.action_deselect_all_events).setVisible(true);
					menu.findItem(R.id.action_race_splits_edit).setVisible(false);
					menu.findItem(R.id.action_race_splits_discard).setVisible(false);

					menu.findItem(R.id.action_email_times).setVisible(false);
					menu.findItem(R.id.action_email_race_results).setVisible(false);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(false);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);
					menu.findItem(R.id.action_about).setVisible(true);
					break;

				case FRAG_RESULTS_RACE_SPLITS:
				case FRAG_RESULTS_RELAY_SPLITS:
					menu.findItem(R.id.action_accept).setVisible(false);
					menu.findItem(R.id.action_add_person).setVisible(false);
					menu.findItem(R.id.action_add_athlete_name).setVisible(false);
					menu.findItem(R.id.action_select_all_events).setVisible(false);
					menu.findItem(R.id.action_deselect_all_events).setVisible(false);
					menu.findItem(R.id.action_race_splits_edit).setVisible(true);
					menu.findItem(R.id.action_race_splits_discard).setVisible(true);

					menu.findItem(R.id.action_email_times).setVisible(false);
					menu.findItem(R.id.action_email_race_results).setVisible(true);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(true);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);
					menu.findItem(R.id.action_about).setVisible(true);
					break;

				case FRAG_RESULTS_BEST_TIMES:
				case FRAG_RESULTS_ALL_RACES:
					menu.findItem(R.id.action_accept).setVisible(false);
					menu.findItem(R.id.action_add_person).setVisible(false);
					menu.findItem(R.id.action_add_athlete_name).setVisible(false);
					menu.findItem(R.id.action_select_all_events).setVisible(false);
					menu.findItem(R.id.action_deselect_all_events).setVisible(false);
					menu.findItem(R.id.action_race_splits_edit).setVisible(false);
					menu.findItem(R.id.action_race_splits_discard).setVisible(false);

					menu.findItem(R.id.action_email_times).setVisible(true);
					menu.findItem(R.id.action_email_race_results).setVisible(false);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(true);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);
					menu.findItem(R.id.action_about).setVisible(true);
					break;

				case FRAG_MEETS:
				case FRAG_CREATE_EVENTS:
				default:
					menu.findItem(R.id.action_accept).setVisible(false);
					menu.findItem(R.id.action_add_person).setVisible(false);
					menu.findItem(R.id.action_add_athlete_name).setVisible(false);
					menu.findItem(R.id.action_select_all_events).setVisible(false);
					menu.findItem(R.id.action_deselect_all_events).setVisible(false);
					menu.findItem(R.id.action_race_splits_edit).setVisible(false);
					menu.findItem(R.id.action_race_splits_discard).setVisible(false);

					menu.findItem(R.id.action_email_times).setVisible(false);
					menu.findItem(R.id.action_email_race_results).setVisible(false);
					// menu.findItem(R.id.action_email_splits_csv_file).setVisible(false);

					menu.findItem(R.id.action_preferences).setVisible(true);
					menu.findItem(R.id.action_help).setVisible(true);
					menu.findItem(R.id.action_help_faq).setVisible(true);
					menu.findItem(R.id.action_help_quick_start).setVisible(true);
					menu.findItem(R.id.action_about).setVisible(true);
					break;

			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int itemId = item.getItemId();
		if (itemId == R.id.action_accept) {
			ClearRace();
			return true;

		} else if (itemId == R.id.action_add_person) {
			if (OkToAddAthlete()) {
				Intent addPersonIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(addPersonIntent, CONTACTS_URI_REQUEST);
			} else {
				return true;
			}

		} else if (itemId == R.id.action_add_athlete_name) {
			if (OkToAddAthlete()) {
				String dialogTitle = "Enter Athlete Name";
				FragmentManager fm = getFragmentManager();
				EditText_DialogFragment editAthleteDisplayName = EditText_DialogFragment
						.newInstance(dialogTitle, -1, null, MySettings.DIALOG_EDIT_TEXT_ADD_ATHLETE_NAME);
				editAthleteDisplayName.show(fm, "fragment_add_athlete_name");

				if (SplitsBuild.isFree(this)) {
					// increment and save the athlete count
					mAthleteCount = mAthleteCount + 1;
					Bundle SetupActivityBundle = new Bundle();
					SetupActivityBundle.putInt(MySettings.STATE_MAIN_ACTIVITY_ATHLETE_COUNT, mAthleteCount);
					MySettings.set("", SetupActivityBundle);
				}
			} else {
				return true;
			}

		} else if (itemId == R.id.action_select_all_events) {
			EventsTable.SelectAllEvents(this, mMeetType, true);
			// Toast.makeText(this, "action_select_all_events", Toast.LENGTH_SHORT).show();
			return true;

		} else if (itemId == R.id.action_deselect_all_events) {
			EventsTable.SelectAllEvents(this, mMeetType, false);
			// Toast.makeText(this, "action_deselect_all_events", Toast.LENGTH_SHORT).show();
			return true;

		} else if (itemId == R.id.action_race_splits_edit) {
			String dialogTitle = getResources().getString(R.string.race_splits_edit_dialog_title);
			Number_Picker_DialogFragment finalTimeNumberPicker = Number_Picker_DialogFragment
					.newInstance(dialogTitle, mSelectedRaceID, mIsRelay, DateTimeUtils.FORMAT_TENTHS);
			FragmentManager fm = getFragmentManager();
			finalTimeNumberPicker.show(fm, "fragment_final_number_picker");
			// Toast.makeText(this, "action_edit", Toast.LENGTH_SHORT).show();
			return true;

		} else if (itemId == R.id.action_race_splits_discard) {
			String message = getResources().getString(R.string.race_splits_discard_dialog_message);
			String dialogTitle = getResources().getString(R.string.race_splits_discard_dialog_title);
			DeleteRace_DialogFragment deleteAthletes = DeleteRace_DialogFragment
					.newInstance(dialogTitle, message, mSelectedRaceID, mIsRelay);
			FragmentManager fm = getFragmentManager();
			deleteAthletes.show(fm, "fragment_delete_race");
			// Toast.makeText(this, "action_discard", Toast.LENGTH_SHORT).show();
			return true;

		} else if (itemId == R.id.action_email_times) {
			EmailTimes();
			// Toast.makeText(this, "action_email_times", Toast.LENGTH_SHORT).show();
			return true;

		} else if (itemId == R.id.action_email_race_results) {
			EmailRaceResults();
			// Toast.makeText(this, "action_email_race_results", Toast.LENGTH_SHORT).show();
			return true;

			/*} else if (itemId == R.id.action_email_splits_csv_file) {
				Toast.makeText(this, "action_email_splits_csv_file", Toast.LENGTH_SHORT).show();
				return true;*/

		} else if (itemId == R.id.action_preferences) {
			ClearRace();
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;

		} else if (itemId == R.id.action_help) {
			String dialogMessage = "";
			Map<String, String> helpParams = new HashMap<String, String>();

			switch (mActiveFragment) {

				case FRAG_INDIVIDUAL_RACES:
					dialogMessage = getResources().getString(R.string.help_individual_races);
					helpParams.put("HelpRequest", "help_individual_races");
					break;

				case FRAG_RELAY_RACES:
					dialogMessage = getResources().getString(R.string.help_relay_races);
					helpParams.put("HelpRequest", "help_relay_races");
					break;

				case FRAG_RESULTS_BEST_TIMES:
					dialogMessage = getResources().getString(R.string.help_results_best_times);
					helpParams.put("HelpRequest", "help_results_best_times");
					break;

				case FRAG_RESULTS_ALL_RACES:
					dialogMessage = getResources().getString(R.string.help_results_all_races);
					helpParams.put("HelpRequest", "help_results_all_races");
					break;

				case FRAG_ATHLETES:
					dialogMessage = getResources().getString(R.string.help_athletes);
					helpParams.put("HelpRequest", "help_athletes");
					break;

				case FRAG_MEETS:
					dialogMessage = getResources().getString(R.string.help_meets);
					helpParams.put("HelpRequest", "help_meets");
					break;

				case FRAG_EVENTS:
					dialogMessage = getResources().getString(R.string.help_events);
					helpParams.put("HelpRequest", "help_events");
					break;

				case FRAG_CREATE_EVENTS:
					dialogMessage = getResources().getString(R.string.help_creat_events);
					helpParams.put("HelpRequest", "help_creat_events");
					break;

				case FRAG_RESULTS_RACE_SPLITS:
				case FRAG_RESULTS_RELAY_SPLITS:
					dialogMessage = getResources().getString(R.string.help_race_and_relay_splits);
					helpParams.put("HelpRequest", "help_race_and_relay_splits");
					break;
				default:
					break;

			}
			if (!dialogMessage.isEmpty()) {
				// send event to Flurry
				FlurryAgent.logEvent("SplitsHelp", helpParams);

				FragmentManager fm = getFragmentManager();
				Help_DialogFragment frag = Help_DialogFragment.newInstance(dialogMessage);
				frag.show(fm, "help_DialogFragment");
			}
			return true;

		} else if (itemId == R.id.action_help_faq) {
			String dialogMessage = getResources().getString(R.string.help_FAQ);
			if (!dialogMessage.isEmpty()) {
				// send event to Flurry
				Map<String, String> helpParams = new HashMap<String, String>();
				helpParams.put("HelpRequest", "help_FAQ");
				FlurryAgent.logEvent("SplitsHelp", helpParams);

				FragmentManager fm = getFragmentManager();
				Help_DialogFragment frag = Help_DialogFragment.newInstance(dialogMessage);
				frag.show(fm, "help_DialogFragment");
			}
			return true;

		} else if (itemId == R.id.action_help_quick_start) {
			ShowHelpQuickStart();
			// send event to Flurry
			Map<String, String> helpParams = new HashMap<String, String>();
			helpParams.put("HelpRequest", "help_quick_start");
			FlurryAgent.logEvent("SplitsHelp", helpParams);
			return true;

		} else if (itemId == R.id.action_about) {

			// send event to Flurry
			Map<String, String> helpParams = new HashMap<String, String>();
			helpParams.put("HelpRequest", "action_about");
			FlurryAgent.logEvent("SplitsHelp", helpParams);

			Resources res = getResources();
			String aboutText = res.getString(string.dialogAbout_aboutText);
			String copyrightText = res.getString(string.copyright_text);
			String okButtonText = res.getString(string.btnOK_text);
			About.show(this, aboutText, copyrightText, okButtonText);
			// Toast.makeText(this, "action_about", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;

	}

	private void ShowHelpQuickStart() {
		String dialogMessage = getResources().getString(R.string.help_quick_start);
		if (!dialogMessage.isEmpty()) {

			FragmentManager fm = getFragmentManager();
			Help_DialogFragment frag = Help_DialogFragment.newInstance(dialogMessage);
			frag.show(fm, "help_DialogFragment");
		}
	}

	@SuppressWarnings("resource")
	private void EmailRaceResults() {

		Cursor relayRaceCursor = null;
		Cursor raceCursor = null;
		long raceID = -1;
		long relayID = -1;
		long meetID = -1;
		long athleteID = -1;
		String athleteName = "";
		long raceDateValue = -1;
		long raceTimeValue = 0;
		String eventShortTitle = "";
		String raceHeader = "";

		Cursor splitsCursor = null;
		ArrayList<String> athleteList = new ArrayList<String>();
		ArrayList<String> lapList = new ArrayList<String>();
		ArrayList<String> distanceList = new ArrayList<String>();
		ArrayList<String> eventShortTitleList = new ArrayList<String>();
		ArrayList<String> splitList = new ArrayList<String>();
		ArrayList<String> timeList = new ArrayList<String>();

		String lapNumber = "";
		String distance = "";
		long splitDuration = -1;
		String splitTime = "";
		long cumulativeDuration = -1;
		String cumulativeTime = "";

		String emailSubject = "";

		switch (mActiveFragment) {

			case FRAG_RESULTS_RACE_SPLITS:
				raceID = Results_RaceSplitsFragment.getRaceID();

				// create race header for email
				raceCursor = RacesTable.getRaceCursor(this, raceID);
				if (raceCursor != null && raceCursor.getCount() > 0) {
					raceCursor.moveToFirst();
					meetID = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_MEET_ID));
					athleteID = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_ATHLETE_ID));
					athleteName = AthletesTable.getDisplayName(this, athleteID);
					raceDateValue = raceCursor.getLong(raceCursor
							.getColumnIndexOrThrow(RacesTable.COL_RACE_START_DATE_TIME));
					raceTimeValue = raceCursor.getLong(raceCursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME));
					eventShortTitle = raceCursor.getString(raceCursor
							.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
					raceHeader = makeRaceHeader(athleteName, meetID, raceDateValue, raceTimeValue, eventShortTitle);
					emailSubject = athleteName + " " + eventShortTitle + " " + "Results";
				}

				// records: Lap Distance Split Time
				splitsCursor = SplitsTable.getAllSplitsCursor(this, raceID, false, SplitsTable.SORT_ORDER_SPLIT_ID_ASC);
				if (splitsCursor != null && splitsCursor.getCount() > 0) {
					// add column headers into their column arrays
					lapList.add("Lap");
					distanceList.add("Dist");
					splitList.add("Split");
					timeList.add("Time");

					NumberFormat localNumberFormater = NumberFormat.getInstance();
					// add split data into their column arrays
					while (splitsCursor.moveToNext()) {
						lapNumber = splitsCursor.getString(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_LAP_NUMBER));
						distance = localNumberFormater.format(
								splitsCursor.getInt(splitsCursor.getColumnIndexOrThrow(SplitsTable.COL_DISTANCE)));

						splitDuration = splitsCursor.getLong(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_TIME));
						splitTime = DateTimeUtils.formatDuration(splitDuration, DateTimeUtils.FORMAT_TENTHS);

						cumulativeDuration = splitsCursor.getLong(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_CUMULATIVE_TIME));
						cumulativeTime = DateTimeUtils.formatDuration(cumulativeDuration, DateTimeUtils.FORMAT_TENTHS);

						lapList.add(lapNumber);
						distanceList.add(distance);
						splitList.add(splitTime);
						timeList.add(cumulativeTime);
					}

				}

				StringBuilder emailBody = new StringBuilder();
				emailBody.append(raceHeader);
				emailBody = FillEmailBody(emailBody, null, lapList, distanceList, splitList, timeList, 4);
				EmailResults(emailSubject, emailBody);
				break;

			case FRAG_RESULTS_RELAY_SPLITS:

				// create relay header for email
				relayID = Results_RelaySplitsFragment.getRelayID();
				relayRaceCursor = RelaysTable.getRelayRaceCursor(this, relayID);
				if (relayRaceCursor != null && relayRaceCursor.getCount() > 0) {
					relayRaceCursor.moveToFirst();
					meetID = relayRaceCursor.getLong(relayRaceCursor.getColumnIndexOrThrow(RelaysTable.COL_MEET_ID));
					raceDateValue = relayRaceCursor.getLong(relayRaceCursor
							.getColumnIndexOrThrow(RelaysTable.COL_RELAY_START_DATE_TIME));
					raceTimeValue = relayRaceCursor
							.getLong(relayRaceCursor.getColumnIndexOrThrow(RelaysTable.COL_RELAY_TIME));
					eventShortTitle = relayRaceCursor.getString(relayRaceCursor
							.getColumnIndexOrThrow(RelaysTable.COL_EVENT_SHORT_TITLE));
					raceHeader = makeRaceHeader(null, meetID, raceDateValue, raceTimeValue, eventShortTitle);
					emailSubject = athleteName + " " + eventShortTitle + " " + "Results";
				}

				// records: Leg Name Event Split Time
				splitsCursor = SplitsTable.getAllSplitsCursor(this, relayID, true, SplitsTable.SORT_ORDER_SPLIT_ID_ASC);
				if (splitsCursor != null && splitsCursor.getCount() > 0) {
					// add column headers into their column arrays
					lapList.add("Leg");
					athleteList.add("Athlete Name");
					eventShortTitleList.add("Event");
					splitList.add("Split");
					// timeList.add("Time");

					// NumberFormat localNumberFormater = NumberFormat.getInstance();
					// add split data into their column arrays
					while (splitsCursor.moveToNext()) {
						lapNumber = splitsCursor.getString(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_LAP_NUMBER));

						athleteID = splitsCursor
								.getLong(splitsCursor.getColumnIndexOrThrow(SplitsTable.COL_ATHLETE_ID));
						athleteName = AthletesTable.getDisplayName(this, athleteID);

						eventShortTitle = splitsCursor.getString(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_EVENT_SHORT_TITLE));

						/*distance = localNumberFormater.format(
								splitsCursor.getInt(splitsCursor.getColumnIndexOrThrow(SplitsTable.COL_DISTANCE)));*/

						splitDuration = splitsCursor.getLong(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_SPLIT_TIME));
						splitTime = DateTimeUtils.formatDuration(splitDuration, DateTimeUtils.FORMAT_TENTHS);

						/*cumulativeDuration = splitsCursor.getLong(splitsCursor
								.getColumnIndexOrThrow(SplitsTable.COL_CUMULATIVE_TIME));
						cumulativeTime = DateTimeUtils.formatDuration(cumulativeDuration, DateTimeUtils.FORMAT_TENTHS);*/

						athleteList.add(athleteName);
						lapList.add(lapNumber);
						eventShortTitleList.add(eventShortTitle);
						splitList.add(splitTime);
						// timeList.add(cumulativeTime);
					}

				}
				emailBody = new StringBuilder();
				emailBody.append(raceHeader);
				emailBody = FillEmailBody(emailBody, athleteList, lapList, eventShortTitleList, splitList, null, 4);
				EmailResults(emailSubject, emailBody);
				break;
			default:
				break;

		}

		if (splitsCursor != null) {
			splitsCursor.close();
		}

		if (raceCursor != null) {
			raceCursor.close();
		}

		if (relayRaceCursor != null) {
			relayRaceCursor.close();
		}
	}

	private String makeRaceHeader(String athleteName, long meetID, long raceDateValue, long raceTimeValue,
			String eventShortTitle) {
		// header line 1: Athlete Name

		// header line 2: Meet & Date
		String meetTitle = (String) MeetsTable.getMeetTitle(this, meetID);
		java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(this);
		String dateString = dateFormat.format(raceDateValue);

		// header line 3: Event & time
		String raceTime = DateTimeUtils.formatDuration(raceTimeValue, DateTimeUtils.FORMAT_TENTHS);

		StringBuilder sb = new StringBuilder();

		if (athleteName != null) {
			sb.append(athleteName).append(System.getProperty("line.separator"))
					.append(meetTitle).append(": ").append(dateString).append(System.getProperty("line.separator"))
					.append(eventShortTitle).append(": ").append(raceTime).append(System.getProperty("line.separator"));
		} else {
			sb.append(meetTitle).append(": ").append(dateString).append(System.getProperty("line.separator"))
					.append(eventShortTitle).append(": ").append(raceTime).append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}

	@SuppressWarnings("resource")
	private void EmailTimes() {
		Cursor cursor = null;
		long athleteID = -1;
		boolean isRelay = false;
		String athleteName = "";
		String emailSubject = "";
		switch (mActiveFragment) {
			case FRAG_RESULTS_BEST_TIMES:
				athleteID = Results_BestTimes_Fragment.getAthleteID();
				isRelay = Results_BestTimes_Fragment.IsRelay();
				athleteName = AthletesTable.getDisplayName(this, athleteID);
				if (isRelay) {
					emailSubject = athleteName + " " + "Relay Best Times";
				} else {
					emailSubject = athleteName + " " + "Best Times";
				}
				cursor = RacesTable.getAllBestTimeRacesCursor(this, athleteID, mMeetType, isRelay);
				break;

			case FRAG_RESULTS_ALL_RACES:
				athleteID = Results_AllRaces_Fragment.getAthleteID();
				isRelay = Results_AllRaces_Fragment.IsRelay();
				athleteName = AthletesTable.getDisplayName(this, athleteID);
				if (isRelay) {
					emailSubject = athleteName + " " + "Relay Times";
				} else {
					emailSubject = athleteName + " " + "Times";
				}
				cursor = RacesTable.getAllRacesCursor(this, athleteID, mMeetType, isRelay);
				break;

			default:
				break;

		}

		if (cursor != null && cursor.getCount() > 0) {

			long meetID = -1;
			String meetTitle = "";
			String eventTitle = "";
			long dateValue = 0;
			java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(this);
			String dateString = "";
			long raceTimeValue = 0;
			String raceTime = "";
			int relayLeg = 0;
			StringBuilder emailBody = new StringBuilder();

			emailBody.append(emailSubject).append(System.getProperty("line.separator"));

			ArrayList<String> eventTitleList = new ArrayList<String>();
			ArrayList<String> meetTitleList = new ArrayList<String>();
			ArrayList<String> dateStringList = new ArrayList<String>();
			ArrayList<String> raceTimeList = new ArrayList<String>();

			while (cursor.moveToNext()) {
				meetID = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_MEET_ID));
				meetTitle = (String) MeetsTable.getMeetTitle(this, meetID);
				eventTitle = cursor.getString(cursor.getColumnIndexOrThrow(RacesTable.COL_EVENT_SHORT_TITLE));
				dateValue = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_START_DATE_TIME));
				dateString = dateFormat.format(dateValue);
				raceTimeValue = cursor.getLong(cursor.getColumnIndexOrThrow(RacesTable.COL_RACE_TIME));
				raceTime = DateTimeUtils.formatDuration(raceTimeValue, DateTimeUtils.FORMAT_TENTHS);

				if (isRelay) {
					relayLeg = cursor.getInt(cursor.getColumnIndexOrThrow(RacesTable.COL_RELAY_LEG));
					StringBuilder ssb = new StringBuilder();
					ssb.append(eventTitle);
					ssb
							.append(" (")
							.append(MySettings.string_relay_leg_text)
							.append(" ")
							.append(Integer.toString(relayLeg))
							.append(")");

					eventTitle = ssb.toString();
				}

				eventTitleList.add(eventTitle);
				meetTitleList.add(meetTitle);
				dateStringList.add(dateString);
				raceTimeList.add(raceTime);

			}

			emailBody = FillEmailBody(emailBody, null, eventTitleList, meetTitleList,
					dateStringList, raceTimeList, 4);

			EmailResults(emailSubject, emailBody);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	private StringBuilder FillEmailBody(StringBuilder emailBody, ArrayList<String> athleteList,
			ArrayList<String> eventTitleList, ArrayList<String> meetTitleList, ArrayList<String> dateStringList,
			ArrayList<String> raceTimeList, int padding) {

		int athleteMaxLength = 0;
		if (athleteList != null) {
			athleteMaxLength = getMaxLength(athleteList);
		}
		int eventTitleMaxLength = getMaxLength(eventTitleList);
		int meetTitleMaxLength = getMaxLength(meetTitleList);
		int dateStringMaxLength = getMaxLength(dateStringList);
		int raceTimeMaxLength = 0;
		if (raceTimeList != null) {
			raceTimeMaxLength = getMaxLength(raceTimeList);
		}
		for (int index = 0; index < eventTitleList.size(); index++) {
			String athleteName = "";
			if (athleteList != null) {
				athleteName = pad(athleteList.get(index), athleteMaxLength, padding);
			}
			String eventTitle = pad(eventTitleList.get(index), eventTitleMaxLength, padding);
			String meetTitle = pad(meetTitleList.get(index), meetTitleMaxLength, padding);
			String dateString = pad(dateStringList.get(index), dateStringMaxLength, padding);
			String raceTime = "";
			if (raceTimeList != null) {
				raceTime = pad(raceTimeList.get(index), raceTimeMaxLength, padding);
			}
			String timeLine = "";
			if (athleteList != null) {
				// records: Leg Name Event Split Time
				// emailBody, athleteList, lapList, eventShortTitleList, splitList, timeList
				timeLine = MakeRelayTimeLine(athleteName, eventTitle, meetTitle, dateString, "    ");
			} else {
				timeLine = MakeBestTimesLine(eventTitle, meetTitle, dateString, raceTime, "    ");
			}
			emailBody.append(timeLine).append(System.getProperty("line.separator"));
		}

		return emailBody;
	}

	private String pad(String item, int maxLength, int padding) {
		int itemStartingLength = item.length();
		int spacesToAdd = maxLength + padding - itemStartingLength;
		StringBuilder paddedItem = new StringBuilder();
		// paddedItem.append(item);
		if (spacesToAdd > 0) {
			for (int index = 0; index < spacesToAdd; index++) {
				paddedItem.append(" ");
			}
		}

		paddedItem.append(item);

		return paddedItem.toString();
	}

	private int getMaxLength(ArrayList<String> arrayList) {
		int maxLenght = 0;

		for (String s : arrayList) {
			if (s.length() > maxLenght) {
				maxLenght = s.length();
			}
		}
		return maxLenght;
	}

	private void EmailResults(String emailSubject, StringBuilder emailBody) {

		// send event to Flurry
		Map<String, String> raceParams = new HashMap<String, String>();
		raceParams.put("EmailSubject", emailSubject);
		FlurryAgent.logEvent("EmailResults", raceParams);

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody.toString());
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_your_email_using_text)));
	}

	private String MakeRelayTimeLine(String athleteName, String leg, String eventShortTitle, String split,
			String padding) {
		StringBuilder sb = new StringBuilder();
		// records: Leg Name Event Split Time
		sb.append(padding);
		sb.append(leg);
		sb.append(athleteName);
		sb.append(eventShortTitle);
		sb.append(split);
		// sb.append(raceTime);

		return sb.toString();
	}

	private String MakeBestTimesLine(String eventTitle, String meetTitle, String dateString,
			String raceTime, String padding) {
		StringBuilder sb = new StringBuilder();

		sb.append(padding);
		sb.append(eventTitle);
		sb.append(meetTitle);
		sb.append(dateString);
		sb.append(raceTime);

		return sb.toString();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request it is that we're responding to
		if (requestCode == CONTACTS_URI_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				if (SplitsBuild.isFree(this)) {
					// increment and save the athlete count
					mAthleteCount = mAthleteCount + 1;
					Bundle SetupActivityBundle = new Bundle();
					SetupActivityBundle.putInt(MySettings.STATE_MAIN_ACTIVITY_ATHLETE_COUNT, mAthleteCount);
					MySettings.set("", SetupActivityBundle);
				}
				// Get the URI that points to the selected contact
				Uri contactUri = data.getData();
				new ImageLoadTask().execute(contactUri);
			}
		}
	}

	private boolean OkToAddAthlete() {
		boolean result = true;
		if (SplitsBuild.isFree(this)) {
			if (mAthleteCount >= MySettings.MAX_NUMBER_OF_ATHLETES) {
				result = false;
				// TODO: create Splits web site and revise dialog_maxAthletesgMessage
				Resources res = getResources();
				String dialogTitle = res.getString(string.dialog_maxAthletesTitle);
				String dialogMessage = res.getString(string.dialog_maxAthletesgMessage);

				FragmentManager fm = getFragmentManager();
				PlayStore_DialogFragment frag = PlayStore_DialogFragment.newInstance(dialogTitle, dialogMessage);
				frag.show(fm, "playStore_DialogFragment");

			}
		}
		return result;
	}

	public void onEvent(DuplicateAthleteSelected event) {
		// decrement and save the athlete count
		mAthleteCount--;
		Bundle SetupActivityBundle = new Bundle();
		SetupActivityBundle.putInt(MySettings.STATE_MAIN_ACTIVITY_ATHLETE_COUNT, mAthleteCount);
		MySettings.set("", SetupActivityBundle);
	}

	public void onEvent(AddAthletetNameToContacts event) {
		FragmentManager fm = getFragmentManager();
		ContactAdder_DialogFragment contactAdderDialog = ContactAdder_DialogFragment.newInstance(event.getAthleteID(),
				event.getAthleteName());
		contactAdderDialog.show(fm, "fragment_contact_adder");
	}

	public void onEvent(AddThumbnailToMemoryCache event) {
		if (event.getPhotoThumbnail() != null && !event.getPhotoThumbnail().isEmpty()) {
			Uri photoThumbnailUri = Uri.parse(event.getPhotoThumbnail());
			Bitmap thumbnail = getContactBitmapFromURI(MainActivity.this, photoThumbnailUri);
			if (thumbnail != null) {
				mMemoryCache.put(String.valueOf(event.getAthleteID()), thumbnail);
			}
		}
	}

	public void onEvent(ShowPreviousFragment event) {
		SelectFragment(mPreviousFragment);
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	public void onEvent(ShowEventsFragment event) {
		SelectFragment(FRAG_EVENTS);
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	public void onEvent(ShowMeetsFragment event) {
		SelectFragment(FRAG_MEETS);
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	public void onEvent(ShowAthletesFragment event) {
		SelectFragment(FRAG_ATHLETES);
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	public void onEvent(ShowRaceSplits event) {
		mSelectedRaceID = event.getRaceID();
		switch (event.getSplitsResultsFragment()) {
			case FRAG_RESULTS_RACE_SPLITS:
				mIsRelay = false;
				SelectFragment(FRAG_RESULTS_RACE_SPLITS);
				break;

			case FRAG_RESULTS_RELAY_SPLITS:
				mIsRelay = true;
				SelectFragment(FRAG_RESULTS_RELAY_SPLITS);
				break;
			default:
				break;
		}
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	}

	public void onEvent(ChangeActionBarTitle event) {
		if (event.getEventShortTitle().isEmpty()) {
			getActionBar().setTitle(mPreviousTitle);
		} else {
			mPreviousTitle = getActionBar().getTitle();
			getActionBar().setTitle(event.getEventShortTitle());
		}
	}

	private class ImageLoadTask extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {
			Uri contactUri = params[0];

			// Perform the query on the contact to get its information
			// We don't need a selection or sort order (there's only one result for the given URI)

			String[] projection = { ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY,
					ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
					ContactsContract.Contacts.PHOTO_URI };

			Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				String lookupKey = cursor.getString(1);
				String displayName = cursor.getString(2);
				String photoThumbnail = cursor.getString(3);
				String photo = cursor.getString(4);

				long athleteID = AthletesTable.CreatAthlete(MainActivity.this, contactUri.toString(), lookupKey,
						displayName, photo, photoThumbnail);

				if (photoThumbnail != null && !photoThumbnail.isEmpty()) {
					Uri photoThumbnailUri = Uri.parse(photoThumbnail);
					Bitmap thumbnail = getContactBitmapFromURI(MainActivity.this, photoThumbnailUri);
					if (thumbnail != null) {
						mMemoryCache.put(String.valueOf(athleteID), thumbnail);
					}
				}
				cursor.close();
			}
			return null;
		}
	}

	private Bitmap getContactBitmapFromURI(Context context, Uri uri) {
		Bitmap bitmap = null;
		InputStream input = null;

		try {
			input = context.getContentResolver().openInputStream(uri);
			if (input != null) {
				bitmap = BitmapFactory.decodeStream(input);
				input.close();
			}
		} catch (FileNotFoundException e) {
			MyLog.e("SetupActivity", "FileNotFoundException in getContactBitmapFromURI()");
			e.printStackTrace();
		} catch (IOException e) {
			MyLog.e("SetupActivity", "IOException in getContactBitmapFromURI()");
			e.printStackTrace();
		}

		return bitmap;
	}

	private void ClearRace() {
		EventBus.getDefault().post(new ClearRace());
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

			mDrawerLayout.closeDrawer(mDrawerList);
			// postDelayed Runnable used to allow the Navigation Drawer to close smoothly
			mDrawerLayout.postDelayed(new Runnable() {

				@Override
				public void run() {
					SelectFragment(position);
				}
			}, 300);
		}
	}

	private void SelectFragment(int position) {

		Fragment fragment = null;

		mPreviousFragment = mActiveFragment;
		mActiveFragment = position;

		// start getting new title
		String fragmentTitle = "";
		if (position < mFragmentTitles.length) {
			fragmentTitle = mFragmentTitles[position];
		}

		// create the new fragment
		switch (position) {
			case FRAG_INDIVIDUAL_RACES:
				fragmentTitle = "Race";
				fragment = Race_Timer_Fragment.newInstance();
				break;
			case FRAG_RELAY_RACES:
				fragmentTitle = "Relay";
				fragment = Relay_Timer_Fragment.newInstance();
				break;
			case FRAG_RESULTS_BEST_TIMES:
				fragmentTitle = ": Best Times";
				fragment = Results_BestTimes_Fragment.newInstance();
				break;
			case FRAG_RESULTS_ALL_RACES:
				fragmentTitle = ": All Races";
				fragment = Results_AllRaces_Fragment.newInstance();
				break;
			case FRAG_ATHLETES:
				fragment = Athletes_Fragment.newInstance();
				break;
			case FRAG_MEETS:
				fragment = Meets_Fragment.newInstance();
				break;
			case FRAG_EVENTS:
				fragment = Events_Fragment.newInstance();
				break;
			case FRAG_CREATE_EVENTS:
				fragment = Create_Event_Fragment.newInstance();
				break;
			case FRAG_RESULTS_RACE_SPLITS:
				fragmentTitle = "Race Splits";
				fragment = Results_RaceSplitsFragment.newInstance(mSelectedRaceID);
				break;
			case FRAG_RESULTS_RELAY_SPLITS:
				fragmentTitle = "Relay Splits";
				fragment = Results_RelaySplitsFragment.newInstance(mSelectedRaceID);
				break;

			default:
				break;
		}

		// replace the old fragment
		if (fragment != null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
			ft.replace(R.id.content_frame, fragment);
			ft.commit();
		}

		// finish new title and then set it in the action bar
		mDrawerList.setItemChecked(position, true);

		String meetTypeString = "";
		switch (mMeetType) {
			case MySettings.SWIM_MEET:
				meetTypeString = getResources().getString(R.string.meet_type_swimming);
				break;

			case MySettings.TRACK_MEET:
				meetTypeString = getResources().getString(R.string.meet_type_track);
				break;
			default:
				break;
		}

		if (position != FRAG_ATHLETES) {
			fragmentTitle = meetTypeString + " " + fragmentTitle;
		}
		setTitle(fragmentTitle);

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		MyLog.i("MainActivity", "onPostCreate()");
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		MyLog.i("MainActivity", "onConfigurationChanged()");
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		MyLog.i("MainActivity", "onResume()");
		EventBus.getDefault().register(this);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mMeetType = Integer.valueOf(sharedPrefs.getString(MySettings.KEY_MEET_TYPE,
				String.valueOf(MySettings.SWIM_MEET)));

		// temporarily set the mActiveFragment as the Previous Fragment
		// The mActiveFragment will be replaced as part of the SelectFragment() method called below;
		mActiveFragment = MySettings.getMainActivityPreviousFragment();
		mDrawerTitle = MySettings.getMainActivityDrawerTitle();
		mTitle = MySettings.getMainActivityTitle();
		mPreviousTitle = MySettings.getMainActivityPreviousTitle();
		mSelectedRaceID = MySettings.getMainActivitySelectedRaceID();

		SelectFragment(MySettings.getMainActivityActiveFragment());
		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("MainActivity", "onPause()");

		EventBus.getDefault().unregister(this);
		// save activity state
		Bundle MainActivityBundle = new Bundle();
		MainActivityBundle.putInt(MySettings.STATE_MAIN_ACTIVITY_ACTIVE_FRAGMENT, mActiveFragment);
		MainActivityBundle.putInt(MySettings.STATE_MAIN_ACTIVITY_PREVIOUS_FRAGMENT, mPreviousFragment);
		MainActivityBundle.putCharSequence(MySettings.STATE_MAIN_ACTIVITY_DRAWER_TITLE, mDrawerTitle);
		MainActivityBundle.putCharSequence(MySettings.STATE_MAIN_ACTIVITY_TITLE, mTitle);
		MainActivityBundle.putCharSequence(MySettings.STATE_MAIN_ACTIVITY_PREVIOUS_TITLE, mPreviousTitle);
		MainActivityBundle.putLong(MySettings.STATE_MAIN_ACTIVITY_SELECTED_RACE_ID, mSelectedRaceID);
		MySettings.set("", MainActivityBundle);

		super.onPause();
	}

	@Override
	protected void onStart()
	{
		MyLog.i("MainActivity", "onStart()");
		super.onStart();
		FlurryAgent.onStartSession(this, MySettings.getFlurryAPIkey());
	}

	@Override
	protected void onStop()
	{
		MyLog.i("MainActivity", "onStop()");
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
}
