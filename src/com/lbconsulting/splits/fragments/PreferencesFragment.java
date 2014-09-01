package com.lbconsulting.splits.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.lbconsulting.splits.R;
import com.lbconsulting.splits.classes.DateTimeUtils;

public class PreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private String MEET_TYPE_KEY = "";
	private String MIN_SPLIT_DURATION_KEY = "";
	private Resources res = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		res = getActivity().getResources();
		MEET_TYPE_KEY = res.getString(R.string.settings_meet_type_key);
		MIN_SPLIT_DURATION_KEY = res.getString(R.string.settings_split_min_duration_key);

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePrefSummary(findPreference(key));
	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}

	}

	private void updatePrefSummary(Preference p) {

		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			if (p.getKey().equals(MEET_TYPE_KEY)) {
				String summaryText = res.getString(R.string.settings_current_selection_text)
						+ "  " + listPref.getEntry();
				p.setSummary(summaryText);
			} else if (p.getKey().equals(MIN_SPLIT_DURATION_KEY)) {

				String valueString = DateTimeUtils.formatDuration(Integer.parseInt(listPref.getValue()), 1);
				valueString = valueString.substring(valueString.length() - 3, valueString.length());
				String units = res.getString(R.string.settings_split_min_duration_units);
				String currentSelectionText = res.getString(
						R.string.settings_current_selection_text);

				StringBuilder sb = new StringBuilder();
				sb.append(currentSelectionText).append(" ")
						.append(valueString).append(" ")
						.append(units);

				p.setSummary(sb.toString());
			}

		}
		if (p instanceof EditTextPreference) {
			/*EditTextPreference editTextPref = (EditTextPreference) p;
			if (p.getKey().equalsIgnoreCase("editKey")) {
				p.setSummary("I am not going to display a password!");
			} else {
				p.setSummary(editTextPref.getText());
			}*/
		}

	}
}
