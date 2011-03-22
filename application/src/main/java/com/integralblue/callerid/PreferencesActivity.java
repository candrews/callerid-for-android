package com.integralblue.callerid;

import android.os.Bundle;
import roboguice.activity.RoboPreferenceActivity;

public class PreferencesActivity extends RoboPreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
