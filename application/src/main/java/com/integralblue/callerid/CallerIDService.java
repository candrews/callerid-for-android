package com.integralblue.callerid;

import roboguice.service.RoboService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.google.inject.Inject;
import com.integralblue.callerid.contacts.ContactsHelper;

public class CallerIDService extends RoboService {
	@Inject
	ContactsHelper contactsHelper;

	@Inject
	WindowManager windowManager;
	
	@Inject
	LayoutInflater layoutInflater;

	//@InjectView(R.layout.toast)
	View toastLayout;
	
	@Inject
	CallerIDLookup callerIDLookup;
	
	@Inject
	SharedPreferences sharedPreferences;
	
	//@InjectResource(R.integer.default_popup_horizontal_gravity)
	int defaultPopupHorizontalGravity;
	
	//@InjectResource(R.integer.default_popup_vertical_gravity)
	int defaultPopupVerticalGravity;
	
	class ToastLookupAsyncTask extends LookupAsyncTask {
		public ToastLookupAsyncTask(CharSequence phoneNumber) {
			super(phoneNumber,toastLayout);
		}
		@Override
		protected void onSuccess(CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
			toastLayout.setVisibility(View.VISIBLE);
		}
		@Override
		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			toastLayout.setVisibility(View.GONE);
		}
		@Override
		protected void onException(Exception e) throws RuntimeException {
			super.onException(e);
			toastLayout.setVisibility(View.VISIBLE);
		}
		@Override
		protected void onInterrupted(Exception e) {
			super.onInterrupted(e);
			toastLayout.setVisibility(View.GONE);
		}
	}

	LookupAsyncTask currentLookupAsyncTask = null;
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent, startId);
		return START_NOT_STICKY;
	}
	
	protected void handleCommand(Intent intent, int startId){
		final String phone_state = intent
				.getStringExtra(TelephonyManager.EXTRA_STATE);
		final String phoneNumber = intent
				.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

		// since we're about to start a new lookup or the phone stopped ringing,
		// we want to cancel any lookups in progress
		if (currentLookupAsyncTask != null)
			currentLookupAsyncTask.cancel(true);

		if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING)
				&& !contactsHelper.haveContactWithPhoneNumber(phoneNumber)) {
			currentLookupAsyncTask = new ToastLookupAsyncTask(phoneNumber);
			currentLookupAsyncTask.execute();
		} else {
			toastLayout.setVisibility(View.GONE);
			stopSelf(startId);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		toastLayout = layoutInflater.inflate(R.layout.toast, null);
		
		defaultPopupHorizontalGravity = getResources().getInteger(R.integer.default_popup_horizontal_gravity);
		defaultPopupVerticalGravity = getResources().getInteger(R.integer.default_popup_vertical_gravity);
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		//params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		params.gravity = 0;
		String popupVerticalGravity = sharedPreferences.getString("popup_vertical_gravity", null);
		if(popupVerticalGravity == null)
			params.gravity |= defaultPopupVerticalGravity;
		else
			params.gravity |= Integer.parseInt(popupVerticalGravity);
		String popupHorizontalGravity = sharedPreferences.getString("popup_horizontal_gravity", null);
		if(popupHorizontalGravity == null)
			params.gravity |= defaultPopupHorizontalGravity;
		else
			params.gravity |= Integer.parseInt(popupHorizontalGravity);
		toastLayout.setVisibility(View.GONE);
		windowManager.addView(toastLayout, params);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Does not support binding
		return null;
	}

	@Override
	public void onDestroy() {
		windowManager.removeView(toastLayout);
	}
}
