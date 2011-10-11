package com.integralblue.callerid.inject;

import org.apache.http.client.HttpClient;

import roboguice.inject.SharedPreferencesName;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.integralblue.callerid.CallerIDLookup;
import com.integralblue.callerid.HttpCallerIDLookup;
import com.integralblue.callerid.contacts.ContactsHelper;
import com.integralblue.callerid.geocoder.Geocoder;

public class CallerIDModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(SharedPreferencesName.class).toProvider(PreferencesNameProvider.class).in(Scopes.SINGLETON);
		bind(ContactsHelper.class).toProvider(ContactsHelperProvider.class).in(Scopes.SINGLETON);
		bind(CallerIDLookup.class).to(HttpCallerIDLookup.class).in(Scopes.SINGLETON);
		bind(Geocoder.class).toProvider(GeocoderHelperProvider.class).in(Scopes.SINGLETON);
		bind(HttpClient.class).toProvider(HttpClientProvider.class).in(Scopes.SINGLETON);
	}

}
