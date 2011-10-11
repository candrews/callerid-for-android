package com.integralblue.callerid.inject;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PreferencesNameProvider implements Provider<String> {
	private final String preferencesName;

	@Inject
	public PreferencesNameProvider(Application application) {
		this.preferencesName = application.getPackageName() + "_preferences";
	}

	public String get() {
		return preferencesName;
	}

}
