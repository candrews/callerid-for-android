package com.integralblue.callerid.contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.inject.Inject;

public class NewContactsHelper implements ContactsHelper {
	@Inject	ContentResolver contentResolver;
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

}
