package com.integralblue.callerid;

import java.io.File;
import java.lang.reflect.Method;

import roboguice.util.Ln;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.os.Build;

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
	        	Ln.d(e, "Strict mode not available");
	        }
		}
		
        disableConnectionReuseIfNecessary();
        
        //enable the http response cache in a thread to avoid a strict mode violation
        new Thread(){
			@Override
			public void run() {
				enableHttpResponseCache();
			}
        }.start();
	}
	
    private void enableHttpResponseCache() {
    	// see http://android-developers.blogspot.com/2011/09/androids-http-clients.html
        final long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
        final File httpCacheDir = new File(getCacheDir(), "http");
        try {
            Class.forName("android.net.http.HttpResponseCache")
                .getMethod("install", File.class, long.class)
                .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Ln.d(httpResponseCacheNotAvailable, "android.net.http.HttpResponseCache not available, probably because we're running on a pre-ICS version of Android. Using com.integralblue.httpresponsecache.HttpResponseCache.");
            try{
            	com.integralblue.httpresponsecache.HttpResponseCache.install(httpCacheDir, httpCacheSize);
            }catch(Exception e){
            	Ln.e(e, "Failed to set up com.integralblue.httpresponsecache.HttpResponseCache");
            }
        }
    }

    private void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
    // see http://android-developers.blogspot.com/2011/09/androids-http-clients.html
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
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
