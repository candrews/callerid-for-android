package com.integralblue.callerid;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import com.google.inject.Inject;
import com.integralblue.callerid.geocoder.Geocoder;

import roboguice.inject.InjectorProvider;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GeocoderAsyncTask extends RoboAsyncTask<Address> {

	final String locationName;
	final ViewGroup layout;
	
	@Inject
	Geocoder geocoder;

	public GeocoderAsyncTask(final String locationName, final ViewGroup layout) {
		((InjectorProvider)context).getInjector().injectMembers(this); //work around RoboGuice bug: https://code.google.com/p/roboguice/issues/detail?id=93
		this.locationName = locationName;
		this.layout = layout;
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
		MapView mapView = (MapView) layout.findViewById(R.id.map_view);
		if(address == null){
			if(mapView!=null) mapView.setVisibility(View.GONE);
		}else{
			if(mapView == null){
				LayoutInflater.from(context).inflate(R.layout.map, layout, true);
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
		super.onInterrupted(e);
		if(layout.findViewById(R.id.map_view)!=null) layout.findViewById(R.id.map_view).setVisibility(View.GONE);
	}
}
