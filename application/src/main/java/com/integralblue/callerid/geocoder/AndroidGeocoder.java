package com.integralblue.callerid.geocoder;

import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;

import android.content.Context;
import android.location.Address;

/** Delegate to the android.location.Geocoder
 * @author candrews
 *
 */
public class AndroidGeocoder implements Geocoder {
	android.location.Geocoder geocoder;
	
	@Inject NominatimGeocoder nominatimGeocoder;
	
	public AndroidGeocoder(Context context){
		geocoder = new android.location.Geocoder(context);
	}

	public List<Address> getFromLocation(double latitude, double longitude,
			int maxResults) throws IOException {
		try{
			return geocoder.getFromLocation(latitude, longitude, maxResults);
		}catch(IOException e){
			//probably service not available - fall back to the backup geocoder
			return nominatimGeocoder.getFromLocation(latitude, longitude, maxResults);
		}
	}

	public List<Address> getFromLocationName(String locationName, int maxResults)
			throws IOException {
		try{
			return geocoder.getFromLocationName(locationName, maxResults);
		}catch(IOException e){
			//probably service not available - fall back to the backup geocoder
			return nominatimGeocoder.getFromLocationName(locationName, maxResults);
		}
	}

	public List<Address> getFromLocationName(String locationName,
			int maxResults, double lowerLeftLatitude,
			double lowerLeftLongitude, double upperRightLatitude,
			double upperRightLongitude) throws IOException {
		try{
			return geocoder.getFromLocationName(locationName, maxResults, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
		}catch(IOException e){
			//probably service not available - fall back to the backup geocoder
			return nominatimGeocoder.getFromLocationName(locationName, maxResults, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
		}
	}
}
