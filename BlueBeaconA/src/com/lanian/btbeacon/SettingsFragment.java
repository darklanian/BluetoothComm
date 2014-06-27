package com.lanian.btbeacon;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences_settings);
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences_settings, false);
	}
}
