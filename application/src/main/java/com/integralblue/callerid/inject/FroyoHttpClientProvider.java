package com.integralblue.callerid.inject;

import org.apache.http.client.HttpClient;

import roboguice.inject.InjectResource;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.integralblue.callerid.R;

public class FroyoHttpClientProvider implements Provider<HttpClient> {
	@Inject
	Context context;
	@InjectResource(R.string.app_name)
	String appName;

	public HttpClient get() {
		return AndroidHttpClient.newInstance(appName, context);
	}

}
