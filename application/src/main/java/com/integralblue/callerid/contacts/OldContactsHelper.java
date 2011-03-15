package com.integralblue.callerid.contacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

import com.google.inject.Inject;


@SuppressWarnings("deprecation")
public class OldContactsHelper implements ContactsHelper {
	@Inject	Context context;

	final static String[] projection = new String[] { Contacts.Phones.NUMBER };

	public boolean haveContactWithPhoneNumber(String phoneNumber) {
		final Uri uri = Uri.withAppendedPath(
				Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(phoneNumber));

		final Cursor cursor = context.getContentResolver().query(uri,projection,null,null,null);
		try{
			return cursor.moveToNext();
		}finally{
			cursor.close();
		}
	}
}
