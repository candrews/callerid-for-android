package com.integralblue.callerid.inject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import roboguice.inject.SharedPreferencesName;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.integralblue.callerid.CallerIDLookup;
import com.integralblue.callerid.HttpCallerIDLookup;
import com.integralblue.callerid.contacts.ContactsHelper;
import com.integralblue.callerid.geocoder.Geocoder;
import com.integralblue.callerid.geocoder.NominatimGeocoder;

public class CallerIDModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(SharedPreferencesName.class).toProvider(PreferencesNameProvider.class).in(Scopes.SINGLETON);
		bind(ContactsHelper.class).toProvider(ContactsHelperProvider.class).in(Scopes.SINGLETON);
		bind(CallerIDLookup.class).to(HttpCallerIDLookup.class).in(Scopes.SINGLETON);
		bind(Geocoder.class).toProvider(GeocoderHelperProvider.class).in(Scopes.SINGLETON);
		bind(NominatimGeocoder.class).in(Scopes.SINGLETON);
		bind(VersionInformationHelper.class).in(Scopes.SINGLETON);
		bind(TextToSpeechHelper.class).in(Scopes.SINGLETON);
		bind(CountryDetector.class).in(Scopes.SINGLETON);
		
		final ObjectMapper jsonObjectMapper = new ObjectMapper();
		jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		bind(ObjectMapper.class).annotatedWith(Names.named(("jsonObjectMapper"))).toInstance(jsonObjectMapper);
		bind(RestTemplate.class).toProvider(RestTemplateProvider.class).in(Scopes.SINGLETON);
		
	}

}
