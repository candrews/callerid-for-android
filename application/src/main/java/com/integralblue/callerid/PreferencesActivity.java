package com.integralblue.callerid;

import com.google.inject.Inject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import roboguice.activity.RoboPreferenceActivity;

public class PreferencesActivity extends RoboPreferenceActivity implements OnSharedPreferenceChangeListener {
	final static int CHECK_TTS_DATA = 0;
	
	@Inject SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if("tts_enabled".equals(key) && sharedPreferences.getBoolean("tts_enabled", true)){
			//make sure the TTS data has been installed
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, CHECK_TTS_DATA);
		}
	}
	
	@Override
	protected void onActivityResult(
	        int requestCode, int resultCode, Intent data) {
	    if (requestCode == CHECK_TTS_DATA) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	        	// no problem - all is good
	        } else {
	            // missing data, install it
	            Intent installIntent = new Intent();
	            installIntent.setAction(
	                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	            
	            //since TTS isn't available, set the preference back to disabled
	        	Editor editor = sharedPreferences.edit();
	        	editor.putBoolean("tts_enabled", false);
	        	editor.commit();
	        }
	    }
	}
}
