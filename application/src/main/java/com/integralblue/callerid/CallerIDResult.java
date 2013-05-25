package com.integralblue.callerid;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallerIDResult implements Serializable {
	private static final long serialVersionUID = 3737128577227643432L;

	@JsonProperty("phoneNumber")
	String phoneNumber;
	
	@JsonProperty("latestAndroidVersionCode")
	Integer latestAndroidVersionCode = -1;

	public Integer getLatestAndroidVersionCode() {
		return latestAndroidVersionCode;
	}

	public void setLatestAndroidVersionCode(Integer latestAndroidVersionCode) {
		this.latestAndroidVersionCode = latestAndroidVersionCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@JsonProperty("name")
	String name;
	@JsonProperty("address")
	String address;
	@JsonProperty("latitude")
	Double latitude;
	@JsonProperty("longitude")
	Double longitude;

	public CallerIDResult() {
		super();
	}
}
