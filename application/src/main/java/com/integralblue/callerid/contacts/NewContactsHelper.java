package com.integralblue.callerid.contacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.integralblue.callerid.CallerIDResult;

public class NewContactsHelper implements ContactsHelper {
	@Inject	ContentResolver contentResolver;
	@Inject Context context;
	@Inject
	Provider<Activity> activityProvider;
	final static String[] projection = new String[] { ContactsContract.PhoneLookup.NUMBER };

	public boolean haveContactWithPhoneNumber(String phoneNumber) {
		final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final Cursor cursor = contentResolver.query(uri,projection,null,null,null);
		try{
			return cursor.moveToNext();
		}finally{
			cursor.close();
		}
	}

	public void createContactEditor(CallerIDResult result) {
		final Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
		intent.putExtra(ContactsContract.Intents.Insert.NAME, result.getName());
		intent.putExtra(ContactsContract.Intents.Insert.PHONE, result.getPhoneNumber());
		if(result.getAddress()!=null) intent.putExtra(ContactsContract.Intents.Insert.POSTAL, result.getAddress());
		activityProvider.get().startActivity(intent);
	}

}
