package com.integralblue.callerid;

import java.lang.reflect.Method;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;

public class CallerIDApplication extends Application {
	public CallerIDApplication() {
		super();
	}

	/**
	 * This constructor is necessary for instrumentation testing
	 * 
	 * @param instrumentation
	 */
	public CallerIDApplication(Instrumentation instrumentation) {
		super();
		attachBaseContext(instrumentation.getTargetContext());
	}

	@Override
    public void onCreate() {
		if (isDebugMode()) {
	        try {
	            final Class<?> strictMode = Class.forName("android.os.StrictMode");
	            final Method enableDefaults = strictMode.getMethod("enableDefaults");
	            enableDefaults.invoke(null);
	        } catch (Exception e) {
                //The version of Android we're on doesn't have android.os.StrictMode
                //so ignore this exception
	        }
	        
		}
	}

	public boolean isDebugMode() {
		// check if android:debuggable is set to true
		if (getApplicationInfo() == null) {
			// getApplicationInfo() returns null in unit tests
			return true;
		} else {
			int applicationFlags = getApplicationInfo().flags;
			return ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
		}
	}

}
