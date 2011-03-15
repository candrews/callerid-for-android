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
	
	public CallerIDResult(String phoneNumber, String name){
		this.phoneNumber=phoneNumber;
		this.name=name;
	}
}
