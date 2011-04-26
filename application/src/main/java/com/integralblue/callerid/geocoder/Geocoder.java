package com.integralblue.callerid.geocoder;

/** Unfortunately, android.location.Geocoder is final, so we can't extend it.
 * This interface is a copy of that of android.location.Geocoder
 * @author candrews
 *
 */
public interface Geocoder {
	public java.util.List<android.location.Address> getFromLocation(
			double latitude, double longitude, int maxResults)
			throws java.io.IOException;

	public java.util.List<android.location.Address> getFromLocationName(
			java.lang.String locationName, int maxResults)
			throws java.io.IOException;

	public java.util.List<android.location.Address> getFromLocationName(
			java.lang.String locationName, int maxResults,
			double lowerLeftLatitude, double lowerLeftLongitude,
			double upperRightLatitude, double upperRightLongitude)
			throws java.io.IOException;
}
