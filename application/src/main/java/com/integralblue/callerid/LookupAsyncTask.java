package com.integralblue.callerid;

import java.util.Locale;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blundell.tut.LoaderImageView;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.google.inject.Inject;
import com.integralblue.callerid.inject.CountryDetector;
import com.integralblue.callerid.inject.VersionInformationHelper;

public class LookupAsyncTask extends RoboAsyncTask<CallerIDResult> {
	
	String offlineGeocoderResult = null;
	
	@Inject CallerIDApplication callerIDApplication;
	
	@Inject
	SharedPreferences preferences;

	final CharSequence phoneNumber;
	
	final ViewGroup layout;
	final TextView text;
	final TextView address;
	final LoaderImageView image;
	
	final String lookupNoResult;
	final String lookupError;
	final String lookupInProgress;
	
	final boolean showMap;
	
	@Inject
	LayoutInflater layoutInflater;
	
	protected GeocoderAsyncTask geocoderAsyncTask = null;
	
	@Inject
	CallerIDLookup callerIDLookup;
	
	@Inject
	VersionInformationHelper versionInformationHelper;
	
	@Inject
	CountryDetector countryDetector;

	public LookupAsyncTask(Context context, CharSequence phoneNumber, ViewGroup layout, boolean showMap) {
		super(context);
		this.layout = layout;
		this.phoneNumber = phoneNumber;
		this.showMap = showMap;
		text = (TextView) layout.findViewById(R.id.text);
		address = (TextView) layout.findViewById(R.id.address);
		image = (LoaderImageView) layout.findViewById(R.id.image);
		
		lookupNoResult = context.getString(R.string.lookup_no_result);
		lookupError = context.getString(R.string.lookup_error);
		lookupInProgress = context.getString(R.string.lookup_in_progress);
	}

	public CallerIDResult call() throws Exception {
		CallerIDResult result = callerIDLookup.lookup(phoneNumber);
		
		if(result.getLatestAndroidVersionCode()!=null){
			//got version info from the server
			versionInformationHelper.setLatestVersionCode(result.getLatestAndroidVersionCode());
		}
		
		return result;
	}
	
	/**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * Request to add this to Roboguice: https://code.google.com/p/roboguice/issues/detail?id=210
     *
     * @return <tt>true</tt> if task was cancelled before it completed
     *
     * @see #cancel(boolean)
     */
	public boolean isCancelled(){
		return future == null ? false : future.isCancelled();
	}

	@Override
	protected void onPreExecute() throws Exception {
		super.onPreExecute();
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setVisibility(View.VISIBLE);
		text.setVisibility(View.VISIBLE);
		
		final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
		try{
			final PhoneNumber phoneNumberPhoneNumber = phoneNumberUtil.parse(phoneNumber.toString(), countryDetector.getCountry());
			final PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance();
			offlineGeocoderResult = phoneNumberOfflineGeocoder.getDescriptionForNumber(phoneNumberPhoneNumber, Locale.getDefault());
		}catch(NumberParseException e){
			//ignore this exception
		}
		if("".equals(offlineGeocoderResult)) offlineGeocoderResult = null;
		if(offlineGeocoderResult == null)
			text.setText(lookupInProgress);
		else
			text.setText(offlineGeocoderResult);
		image.spin();
	}

	@Override
	protected void onSuccess(CallerIDResult result)
			throws Exception {
		super.onSuccess(result);
		// since we're about to start a new lookup,
		// we want to cancel any lookups in progress
		if (geocoderAsyncTask != null)
			geocoderAsyncTask.cancel(true);
		if(isCancelled()) return; //don't do any UI things if the task was cancelled
		
		if(result.getAddress()==null){
			address.setVisibility(View.GONE);
		}else{
			address.setText(result.getAddress());
			if(result.getName().equals(result.getAddress()))
				//when the name and address are the same, there's no reason to say the same thing twice
				address.setVisibility(View.GONE);
			else
				address.setVisibility(View.VISIBLE);
			MapView mapView = (MapView) layout.findViewById(R.id.map_view);
			if(showMap){
				if(result.getLatitude()!=null && result.getLongitude()!=null){
					if(mapView == null){
						LayoutInflater.from(getContext()).inflate(R.layout.map, layout, true);
						mapView = (MapView) layout.findViewById(R.id.map_view);
						mapView.setBuiltInZoomControls(true);
					}
			        mapView.getController().setZoom(16);
					mapView.getController().setCenter(new GeoPoint(result.getLatitude(),result.getLongitude()));
					mapView.setVisibility(View.VISIBLE);
				}else{
					geocoderAsyncTask = new GeocoderAsyncTask(getContext(),result.getAddress(),layout);
					geocoderAsyncTask.execute();
				}
			}
		}
		image.setImageDrawable(null);
		text.setText(result.getName());
	}

	@Override
	protected void onException(Exception e) throws RuntimeException {
		// since we're about to start a new lookup,
		// we want to cancel any lookups in progress
		if (geocoderAsyncTask != null)
			geocoderAsyncTask.cancel(true);

		if(isCancelled()) return; //don't do any UI things if the task was cancelled
		
		if (e instanceof CallerIDLookup.NoResultException) {
			if(offlineGeocoderResult == null){
				text.setText(lookupNoResult);
			}else{
				// We're already displaying the offline geolocation results... so just leave that there.
				if(showMap){
					geocoderAsyncTask = new GeocoderAsyncTask(getContext(),offlineGeocoderResult,layout);
					geocoderAsyncTask.execute();
				}
			}
		} else {
			Ln.e(e);
			if(offlineGeocoderResult == null){
				text.setText(lookupError);
			}else{
				// We're already displaying the offline geolocation results... so just leave that there.
				if(showMap){
					geocoderAsyncTask = new GeocoderAsyncTask(getContext(),offlineGeocoderResult,layout);
					geocoderAsyncTask.execute();
				}
			}
		}
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setImageDrawable(null);
	}

	@Override
	protected void onInterrupted(Exception e) {
		// intentionally not calling the super, as that calls onException(e), and that's not what we want
		// super.onInterrupted(e);

		// if there's a geocoder lookup in progress, we should cancel that, too
		if (geocoderAsyncTask != null)
			geocoderAsyncTask.cancel(true);
		
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setVisibility(View.GONE);
		text.setVisibility(View.GONE);
	}
};