package com.integralblue.callerid.contacts;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.integralblue.callerid.CallerIDLookup.NoResultException;
import com.integralblue.callerid.CallerIDResult;

public class NewContactsHelper implements ContactsHelper {
	@Inject Application application;
	@Inject
	Provider<Activity> activityProvider;
	final static String[] HAVE_CONTACT_PROJECTION = new String[] { ContactsContract.PhoneLookup.NUMBER };
	final static String[] GET_CONTACT_PROJECTION = new String[] { ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
    static final int NUMBER_COLUMN_INDEX = 0;
    static final int DISPLAY_NAME_COLUMN_INDEX = 1;

	public boolean haveContactWithPhoneNumber(String phoneNumber) {
		final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final ContentResolver contentResolver = application.getContentResolver();
		final Cursor cursor = contentResolver.query(uri,HAVE_CONTACT_PROJECTION,null,null,null);
		try{
			return cursor.moveToNext();
		}finally{
			cursor.close();
		}
	}

	public Intent createContactEditor(CallerIDResult result) {
		final Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		intent.putExtra(ContactsContract.Intents.Insert.NAME, result.getName());
		intent.putExtra(ContactsContract.Intents.Insert.PHONE, result.getPhoneNumber());
		if(result.getAddress()!=null) intent.putExtra(ContactsContract.Intents.Insert.POSTAL, result.getAddress());
		return intent;
	}

	public CallerIDResult getContact(String phoneNumber) throws NoResultException {
		final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final ContentResolver contentResolver = application.getContentResolver();
		final Cursor cursor = contentResolver.query(uri,GET_CONTACT_PROJECTION,null,null,null);
		try{
			if(cursor.moveToNext()){
				CallerIDResult ret = new CallerIDResult();
				ret.setPhoneNumber(cursor.getString(NUMBER_COLUMN_INDEX));
				ret.setName(cursor.getString(DISPLAY_NAME_COLUMN_INDEX));
				return ret;
			}else{
				throw new NoResultException();
			}
		}finally{
			cursor.close();
		}
	}

}
