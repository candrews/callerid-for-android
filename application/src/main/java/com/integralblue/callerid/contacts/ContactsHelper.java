package com.integralblue.callerid.contacts;

import android.content.Intent;

import com.integralblue.callerid.CallerIDResult;
import com.integralblue.callerid.CallerIDLookup.NoResultException;

public interface ContactsHelper {
	boolean haveContactWithPhoneNumber(String phoneNumber);
	Intent createContactEditor(CallerIDResult result);
	CallerIDResult getContact(String phoneNumber) throws NoResultException;
}
