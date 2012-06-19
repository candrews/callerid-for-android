package com.integralblue.callerid;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Random;

import roboguice.service.RoboService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.inject.Inject;
import com.integralblue.callerid.CallerIDLookup.NoResultException;
import com.integralblue.callerid.contacts.ContactsHelper;
import com.integralblue.callerid.inject.TextToSpeechHelper;
import com.integralblue.callerid.inject.VersionInformationHelper;

public class CallerIDService extends RoboService {
	@Inject
	ContactsHelper contactsHelper;

	@Inject
	WindowManager windowManager;
	
	@Inject
	NotificationManager notificationManager;
	
	@Inject
	LayoutInflater layoutInflater;

	//@InjectView(R.layout.toast)
	ViewGroup toastLayout;
	
	@Inject
	CallerIDLookup callerIDLookup;
	
	@Inject
	VersionInformationHelper versionInformationHelper;
	
	@Inject
	SharedPreferences sharedPreferences;
	
	@Inject TextToSpeechHelper textToSpeechHelper;
	
	//@InjectResource(R.integer.default_popup_horizontal_gravity)
	int defaultPopupHorizontalGravity;
	
	//@InjectResource(R.integer.default_popup_vertical_gravity)
	int defaultPopupVerticalGravity;
	
	boolean defaultPopupMap;
	
	String previousPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
	String previousPhoneNumber = null;
	String previousCallerID = null;
	
	boolean ttsEnabled;
	
	static final HashMap<String, String> ttsParametersMap;
	static {
		ttsParametersMap = new HashMap<String, String>();
		ttsParametersMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_RING));
	}
	
	class ToastLookupAsyncTask extends LookupAsyncTask {
		
		public ToastLookupAsyncTask(Context context, CharSequence phoneNumber) {
			super(context, phoneNumber,toastLayout,sharedPreferences.getBoolean("popup_map", defaultPopupMap));
		}

		@Override
		protected void onSuccess(final CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
			toastLayout.setVisibility(View.VISIBLE);
			previousCallerID = result.getName();
			
			if(versionInformationHelper.shouldPromptForNewVersion()){
				Toast.makeText(CallerIDService.this, R.string.new_version_dialog_title, Toast.LENGTH_LONG).show();
			}
			
			if(ttsEnabled){
				textToSpeechHelper.speak(getString(R.string.incoming_call_tts, result.getName()), TextToSpeech.QUEUE_FLUSH, ttsParametersMap);
			}
		}
		@Override
		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			toastLayout.setVisibility(View.VISIBLE);
			previousCallerID = offlineGeocoderResult;
		}
		@Override
		protected void onException(Exception e) throws RuntimeException {
			super.onException(e);
			toastLayout.setVisibility(View.VISIBLE);
			previousCallerID = offlineGeocoderResult;
			if (e instanceof CallerIDLookup.NoResultException) {
				if(offlineGeocoderResult == null)
					textToSpeechHelper.speak(getString(R.string.incoming_call_tts_unknown), TextToSpeech.QUEUE_FLUSH, ttsParametersMap);
				else
					textToSpeechHelper.speak(getString(R.string.incoming_call_tts, offlineGeocoderResult), TextToSpeech.QUEUE_FLUSH, ttsParametersMap);
			}
		}
		@Override
		protected void onInterrupted(Exception e) {
			super.onInterrupted(e);
			toastLayout.setVisibility(View.GONE);
			previousCallerID = null;
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
		final String phoneState = intent
				.getStringExtra(TelephonyManager.EXTRA_STATE);
		final String phoneNumber = intent
				.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

		// since we're about to start a new lookup or the phone stopped ringing,
		// we want to cancel any lookups in progress
		if (currentLookupAsyncTask != null)
			currentLookupAsyncTask.cancel(true);

		if (TelephonyManager.EXTRA_STATE_RINGING.equals(phoneState)) {
			try{
				CallerIDResult result = contactsHelper.getContact(phoneNumber);
				toastLayout.setVisibility(View.GONE);
				//speak the contact's name even when we don't need to use the CallerID service to get information
				
				if(ttsEnabled && result.getName()!=null && result.getName()!=""){
					textToSpeechHelper.speak(getString(R.string.incoming_call_tts, result.getName()), TextToSpeech.QUEUE_FLUSH, ttsParametersMap);
				}
			}catch(NoResultException e){
				currentLookupAsyncTask = new ToastLookupAsyncTask(this, phoneNumber);
				currentLookupAsyncTask.execute();
			}
		} else {
			toastLayout.setVisibility(View.GONE);
			stopSelf(startId);
		}
		
		if (TelephonyManager.EXTRA_STATE_IDLE.equals(phoneState)
				&& TelephonyManager.EXTRA_STATE_RINGING.equals(previousPhoneState)
				&& previousPhoneNumber!=null
				&& !contactsHelper.haveContactWithPhoneNumber(previousPhoneNumber)){
			//add missed call notification
			final Notification notification = new Notification(
					android.R.drawable.sym_call_missed,
					previousCallerID==null?getString(R.string.missed_call_from_unknown):MessageFormat.format(getString(R.string.missed_call_from_known),previousCallerID),
					System.currentTimeMillis());
			final Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
			notificationIntent.putExtra("phoneNumber", previousPhoneNumber);
			final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), getString(R.string.missed_call), previousCallerID==null?getString(R.string.perform_lookup_label):previousCallerID, contentIntent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(new Random().nextInt(), notification);
			
			if(ttsEnabled){
				textToSpeechHelper.stop();
			}
		}
		previousPhoneNumber = phoneNumber;
		previousPhoneState = phoneState;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		toastLayout = (ViewGroup) layoutInflater.inflate(R.layout.toast, null);
		
		defaultPopupHorizontalGravity = getResources().getInteger(R.integer.default_popup_horizontal_gravity);
		defaultPopupVerticalGravity = getResources().getInteger(R.integer.default_popup_vertical_gravity);
		defaultPopupMap = Boolean.parseBoolean(getResources().getString(R.string.default_popup_map));
		
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
		ttsEnabled = sharedPreferences.getBoolean("tts_enabled", true);
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
		textToSpeechHelper.shutdown();
	}
}
