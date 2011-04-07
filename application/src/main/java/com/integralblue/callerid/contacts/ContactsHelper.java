package com.integralblue.callerid.contacts;

import com.integralblue.callerid.CallerIDResult;

public interface ContactsHelper {
	boolean haveContactWithPhoneNumber(String phoneNumber);
	void createContactEditor(CallerIDResult result);
}
