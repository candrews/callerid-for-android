package com.integralblue.callerid;

import java.text.MessageFormat;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import roboguice.inject.InjectResource;

import android.content.SharedPreferences;

import com.google.inject.Inject;

public class HttpCallerIDLookup implements CallerIDLookup {
	
	@Inject HttpClient httpClient;
	@Inject SharedPreferences sharedPreferences;
	@InjectResource(R.string.default_lookup_url) String defaultLookupUrl;

	public CallerIDResult lookup(CharSequence phoneNumber) throws NoResultException {
		HttpClient client = new DefaultHttpClient();
		final String url = MessageFormat.format(sharedPreferences.getString("lookup_url", defaultLookupUrl), phoneNumber);
		
		try{
			final HttpGet get = new HttpGet(url);
			final HttpResponse response = client.execute(get);
			final StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() >= 300) {
				if(statusLine.getStatusCode() == 404)
					throw new NoResultException();
	            throw new HttpResponseException(statusLine.getStatusCode(),
	                    statusLine.getReasonPhrase());
	        }
			final JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
			return new CallerIDResult(jsonObject.getString("phoneNumber"), jsonObject.getString("name"));
		}catch(Exception e){
			if(e instanceof NoResultException)
				throw (NoResultException)e;
			else
				throw new RuntimeException(e);
		}
	}

}
