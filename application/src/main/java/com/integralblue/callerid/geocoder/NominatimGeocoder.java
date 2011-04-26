package com.integralblue.callerid.geocoder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import roboguice.util.Ln;
import android.location.Address;

import com.google.inject.Inject;

public class NominatimGeocoder implements Geocoder
{
	@Inject
	HttpClient httpClient;
	
	public List<Address> getFromLocation(double latitude, double longitude,
			int maxResults) throws IOException {
		return new ArrayList<Address>(); //TODO implement this
	}

	public List<Address> getFromLocationName(String locationName, int maxResults)
			throws IOException {
		final String url = "http://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(locationName) + "&format=json&addressdetails=1&limit=" + maxResults;
		List<Address> ret;
		try{
			final HttpGet get = new HttpGet(url);
			final HttpResponse response = httpClient.execute(get);
			final StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() >= 300) {
				ret = new ArrayList<Address>();
	        }else{
	        	ret = parseResponse(EntityUtils.toString(response.getEntity()),maxResults);
	        }
			
		}catch(Exception e){
			ret = new ArrayList<Address>();
			Ln.e(e);
		}
		return ret;
	}

	public List<Address> getFromLocationName(String locationName,
			int maxResults, double lowerLeftLatitude,
			double lowerLeftLongitude, double upperRightLatitude,
			double upperRightLongitude) throws IOException {
		return new ArrayList<Address>(); //TODO implement this
	}
	
	protected List<Address> parseResponse(String response, int maxResults) throws Exception {
		final List<Address> ret = new ArrayList<Address>(maxResults);
		
		final JSONArray jsonArray = new JSONArray(response);
		for(int i=0;i<jsonArray.length() && i< maxResults;i++){
			final JSONObject jsonResult = jsonArray.getJSONObject(i);
			final Address address = new Address(Locale.getDefault());
			address.setThoroughfare(jsonResult.getString("display_name"));
			address.setLatitude(jsonResult.getDouble("lat"));
			address.setLongitude(jsonResult.getDouble("lon"));
			final JSONObject jsonAddress = jsonResult.getJSONObject("address");
			if(jsonAddress.has("country")) address.setCountryName(jsonAddress.getString("country"));
			if(jsonAddress.has("country_code")) address.setCountryCode(jsonAddress.getString("country_code"));
			if(jsonAddress.has("postcode")) address.setPostalCode(jsonAddress.getString("postcode"));
			if(jsonAddress.has("county")) address.setSubAdminArea(jsonAddress.getString("county"));
			ret.add(address);
		}
		
		return ret;
	}
}
