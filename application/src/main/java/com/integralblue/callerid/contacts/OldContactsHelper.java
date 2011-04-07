package com.integralblue.callerid.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.integralblue.callerid.CallerIDResult;


@SuppressWarnings("deprecation")
public class OldContactsHelper implements ContactsHelper {
	@Inject Context context;
	@Inject
	Provider<Activity> activityProvider;

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

	public void createContactEditor(CallerIDResult result) {
	    final Intent intent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);
	    intent.putExtra(Contacts.Intents.Insert.NAME, result.getName());
	    intent.putExtra(Contacts.Intents.Insert.PHONE, result.getPhoneNumber());
	    activityProvider.get().startActivity(intent);
	}
}
