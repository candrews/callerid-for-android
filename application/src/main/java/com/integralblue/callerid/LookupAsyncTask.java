package com.integralblue.callerid;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blundell.tut.LoaderImageView;
import com.google.inject.Inject;
import com.integralblue.callerid.CallerIDLookup;
import com.integralblue.callerid.CallerIDResult;
import com.integralblue.callerid.GeocoderAsyncTask;
import com.integralblue.callerid.R;

public class LookupAsyncTask extends RoboAsyncTask<CallerIDResult> {
	
	@Inject CallerIDApplication callerIDApplication;

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
		return callerIDLookup.lookup(phoneNumber);
	}

	@Override
	protected void onPreExecute() throws Exception {
		super.onPreExecute();
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setVisibility(View.VISIBLE);
		text.setVisibility(View.VISIBLE);
		text.setText(lookupInProgress);
		image.spin();
	}

	@Override
	protected void onSuccess(CallerIDResult result)
			throws Exception {
		super.onSuccess(result);
		
		if(result.getAddress()==null){
			address.setVisibility(View.GONE);
		}else{
			address.setText(result.getAddress());
			if(result.getName().equals(result.getAddress()))
				//when the name and address are the same, there's no reason to say the same thing twice
				address.setVisibility(View.GONE);
			else
				address.setVisibility(View.VISIBLE);
			// since we're about to start a new lookup,
			// we want to cancel any lookups in progress
			if (geocoderAsyncTask != null)
				geocoderAsyncTask.cancel(true);
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
		if (e instanceof CallerIDLookup.NoResultException) {
			text.setText(lookupNoResult);
		} else {
			Ln.e(e);
			text.setText(lookupError);
		}
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setImageDrawable(null);
	}

	@Override
	protected void onInterrupted(Exception e) {
		super.onInterrupted(e);
		address.setVisibility(View.GONE);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
		image.setVisibility(View.GONE);
		text.setVisibility(View.GONE);
	}
};