package com.integralblue.callerid;

import java.io.Serializable;

public class CallerIDResult implements Serializable {
	private static final long serialVersionUID = 8782505400376543794L;
	
	final String phoneNumber;
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getName() {
		return name;
	}

	final String name;
	
	final String address;

	public String getAddress() {
		return address;
	}
	
	final Double latitude;
	public Double getLatitude(){
		return latitude;
	}

	final Double longitude;
	public Double getLongitude(){
		return longitude;
	}
	
	
	public CallerIDResult(String phoneNumber, String name, String address){
		this.phoneNumber=phoneNumber;
		this.name=name;
		this.address=address;
		this.latitude=null;
		this.longitude=null;
	}
	public CallerIDResult(String phoneNumber, String name, String address,Double latitude, Double longitude){
		this.phoneNumber=phoneNumber;
		this.name=name;
		this.address=address;
		this.latitude=latitude;
		this.longitude=longitude;
	}
}
