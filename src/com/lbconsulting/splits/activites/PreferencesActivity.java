package com.lbconsulting.splits.activites;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;
import com.lbconsulting.splits.classes.MyLog;
import com.lbconsulting.splits.classes.MySettings;
import com.lbconsulting.splits.fragments.PreferencesFragment;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("PreferencesActivity", "onCreate()");
		super.onCreate(savedInstanceState);

		if (MySettings.DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads()
					.detectDiskWrites()
					.detectNetwork() // or .detectAll() for all detectable problems
					.penaltyLog()
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects()
					.penaltyLog()
					.penaltyDeath()
					.build());
		}

		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, new PreferencesFragment());
			ft.commit();
		}
	}

	@Override
	protected void onDestroy() {
		MyLog.i("PreferencesActivity", "onDestroy()");
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home)
			finish();

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart()
	{
		MyLog.i("PreferencesActivity", "onStart()");
		super.onStart();
		FlurryAgent.onStartSession(this, MySettings.getFlurryAPIkey());
	}

	@Override
	protected void onStop()
	{
		MyLog.i("PreferencesActivity", "onStop()");
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

}
