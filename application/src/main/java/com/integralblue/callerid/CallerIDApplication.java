package com.integralblue.callerid;

import java.lang.reflect.Method;

import roboguice.util.Ln;

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
	        	Ln.e(e);
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
	
    //copied from Android 2.3 PhoneNumberUtils.isUriNumber
    //SIP support is not available before 2.3 so this method doesn't exist
    public static boolean isUriNumber(String number) {
        // Note we allow either "@" or "%40" to indicate a URI, in case
        // the passed-in string is URI-escaped.  (Neither "@" nor "%40"
        // will ever be found in a legal PSTN number.)
        return number != null && (number.contains("@") || number.contains("%40"));
    }
}
