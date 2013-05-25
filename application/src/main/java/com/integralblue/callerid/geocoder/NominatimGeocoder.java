package com.integralblue.callerid.geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import roboguice.util.Ln;
import android.location.Address;

import com.google.inject.Inject;

public class NominatimGeocoder implements Geocoder
{
	protected static class Place
	{
		protected static class Address
		{
			@JsonProperty("country")
			private String country;
			@JsonProperty("country_code")
			private String countryCode;
			@JsonProperty("postcode")
			private String postalCode;
			public String getCountry() {
				return country;
			}
			public void setCountry(String country) {
				this.country = country;
			}
			public String getCountryCode() {
				return countryCode;
			}
			public void setCountryCode(String countryCode) {
				this.countryCode = countryCode;
			}
			public String getPostalCode() {
				return postalCode;
			}
			public void setPostalCode(String postalCode) {
				this.postalCode = postalCode;
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((country == null) ? 0 : country.hashCode());
				result = prime * result
						+ ((countryCode == null) ? 0 : countryCode.hashCode());
				result = prime * result
						+ ((postalCode == null) ? 0 : postalCode.hashCode());
				return result;
			}
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Address other = (Address) obj;
				if (country == null) {
					if (other.country != null)
						return false;
				} else if (!country.equals(other.country))
					return false;
				if (countryCode == null) {
					if (other.countryCode != null)
						return false;
				} else if (!countryCode.equals(other.countryCode))
					return false;
				if (postalCode == null) {
					if (other.postalCode != null)
						return false;
				} else if (!postalCode.equals(other.postalCode))
					return false;
				return true;
			}
		}
		
		@JsonProperty("lat")
		private double latitude;
		@JsonProperty("lon")
		private double longitude;
		@JsonProperty("display_name")
		private String displayName;
		@JsonProperty("address")
		private Address address;

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((address == null) ? 0 : address.hashCode());
			result = prime * result
					+ ((displayName == null) ? 0 : displayName.hashCode());
			long temp;
			temp = Double.doubleToLongBits(latitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(longitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Place other = (Place) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			if (displayName == null) {
				if (other.displayName != null)
					return false;
			} else if (!displayName.equals(other.displayName))
				return false;
			if (Double.doubleToLongBits(latitude) != Double
					.doubleToLongBits(other.latitude))
				return false;
			if (Double.doubleToLongBits(longitude) != Double
					.doubleToLongBits(other.longitude))
				return false;
			return true;
		}
	}
	
	@Inject RestTemplate restTemplate;
	
	public List<Address> getFromLocation(double latitude, double longitude,
			int maxResults) throws IOException {
		return new ArrayList<Address>(); //TODO implement this
	}

	public List<Address> getFromLocationName(String locationName, int maxResults)
			throws IOException {
		final Map<String,String> urlVariables = new HashMap<String, String>();
		urlVariables.put("location", locationName);
		urlVariables.put("maxResults", Integer.toString(maxResults));
		
		try{
			// Nominatim does not handle the HTTP request header "Accept" according to RFC.
			// If any Accept header other than */* is sent, Nominatim responses with HTTP 406 (Not Acceptable).
			// So instead of the rather simple line:
			// final Place[] places = restTemplate.getForObject("http://nominatim.openstreetmap.org/search?q={location}&format=json&addressdetails=1&limit={maxResults}", Place[].class, urlVariables);
			// we have to manually set the Accept header and make things a bit more complicated.
			
			final HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("Accept", "*/*");
			final HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
			final ResponseEntity<Place[]> responseEntity = restTemplate.exchange("http://nominatim.openstreetmap.org/search?q={location}&format=json&addressdetails=1&limit={maxResults}",HttpMethod.GET, requestEntity, Place[].class, urlVariables);
			final Place[] places = responseEntity.getBody();
        	return parseResponse(places);
		}catch(RestClientException e){
			Ln.e(e);
			return new ArrayList<Address>();
		}
	}

	public List<Address> getFromLocationName(String locationName,
			int maxResults, double lowerLeftLatitude,
			double lowerLeftLongitude, double upperRightLatitude,
			double upperRightLongitude) throws IOException {
		return new ArrayList<Address>(); //TODO implement this
	}
	
	protected List<Address> parseResponse(final Place[] places) {
		final List<Address> ret = new ArrayList<Address>(places.length);
		
		for(final Place place : places){
			final Address address = new Address(Locale.getDefault());
			ret.add(address);
			
			address.setThoroughfare(place.getDisplayName());
			address.setLatitude(place.getLatitude());
			address.setLongitude(place.getLongitude());
			final Place.Address placeAddress = place.getAddress();
			if(placeAddress!=null){
				address.setCountryCode(placeAddress.getCountryCode());
				address.setCountryName(placeAddress.getCountry());
				address.setPostalCode(placeAddress.getPostalCode());
				address.setSubAdminArea(placeAddress.getCountry());
			}
		}
		
		return ret;
	}
}
