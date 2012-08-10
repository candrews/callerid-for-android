package com.integralblue.callerid;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import com.google.inject.Inject;
import com.integralblue.callerid.geocoder.Geocoder;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GeocoderAsyncTask extends RoboAsyncTask<Address> {

	final String locationName;
	final ViewGroup layout;
	
	@Inject
	Geocoder geocoder;

	public GeocoderAsyncTask(Context context, final String locationName, final ViewGroup layout) {
		super(context);
		this.locationName = locationName;
		this.layout = layout;
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

	public Address call() throws Exception {
		List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
		if(addresses.size()==1){
			return addresses.get(0);
		}else{
			return null;
		}
	}

	@Override
	protected void onSuccess(final Address address)
			throws Exception {
		if(isCancelled()) return; //don't do any UI things if the task was cancelled
		MapView mapView = (MapView) layout.findViewById(R.id.map_view);
		if(address == null){
			if(mapView!=null) mapView.setVisibility(View.GONE);
		}else{
			if(mapView == null){
				LayoutInflater.from(getContext()).inflate(R.layout.map, layout, true);
				mapView = (MapView) layout.findViewById(R.id.map_view);
				mapView.setBuiltInZoomControls(true);
			}
	        mapView.getController().setZoom(16);
			mapView.getController().setCenter(new GeoPoint(address.getLatitude(),address.getLongitude()));
			mapView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onException(final Exception e) throws RuntimeException {
		Ln.e(e);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
	}

	@Override
	protected void onInterrupted(final Exception e) {
		// intentionally not calling the super, as that calls onException(e), and that's not what we want
		// super.onInterrupted(e);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
	}
}
