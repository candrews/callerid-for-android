package com.integralblue.callerid.contacts;

import com.integralblue.callerid.CallerIDResult;
import com.integralblue.callerid.CallerIDLookup.NoResultException;

public interface ContactsHelper {
	boolean haveContactWithPhoneNumber(String phoneNumber);
	void createContactEditor(CallerIDResult result);
	CallerIDResult getContact(String phoneNumber) throws NoResultException;
}
