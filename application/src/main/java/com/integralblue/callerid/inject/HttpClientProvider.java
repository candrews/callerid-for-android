package com.integralblue.callerid.inject;

import java.lang.reflect.Method;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import roboguice.util.Ln;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class HttpClientProvider implements Provider<HttpClient> {
	@Inject
	Application application;
	
    // Wait this many milliseconds max for the TCP connection to be established
    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    
    // Wait this many milliseconds max for the server to send us data once the connection has been established
    private static final int SO_TIMEOUT = 60 * 1000;
    
    private String getUserAgent(String defaultHttpClientUserAgent){
    	String versionName;
		try {
			versionName = application.getPackageManager().getPackageInfo(
					application.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		StringBuilder ret = new StringBuilder();
		ret.append(application.getPackageName());
		ret.append("/");
		ret.append(versionName);
		ret.append(" (");
		ret.append("Linux; U; Android ");
		ret.append(Build.VERSION.RELEASE);
		ret.append("; ");
		ret.append(Locale.getDefault());
		ret.append("; ");
		ret.append(Build.PRODUCT);
		ret.append(")");
		if(defaultHttpClientUserAgent!=null){
			ret.append(" ");
			ret.append(defaultHttpClientUserAgent);
		}
		return ret.toString();
    }

	@Override
	public HttpClient get() {
		AbstractHttpClient client = new DefaultHttpClient(){
		    @Override
		    protected ClientConnectionManager createClientConnectionManager() {
		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(
		                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        registry.register(
		                new Scheme("https", getHttpsSocketFactory(), 443));
		        HttpParams params = getParams();
		        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
		        HttpProtocolParams.setUserAgent(params, getUserAgent(HttpProtocolParams.getUserAgent(params)));
		        return new ThreadSafeClientConnManager(params, registry);
		    }
		    
		    /** Gets an HTTPS socket factory with SSL Session Caching if such support is available, otherwise falls back to a non-caching factory
		     * @return
		     */
		    protected SocketFactory getHttpsSocketFactory(){
				try {
					Class<?> sslSessionCacheClass = Class.forName("android.net.SSLSessionCache");
			    	Object sslSessionCache = sslSessionCacheClass.getConstructor(Context.class).newInstance(application);
			    	Method getHttpSocketFactory = Class.forName("android.net.SSLCertificateSocketFactory").getMethod("getHttpSocketFactory", new Class<?>[]{int.class, sslSessionCacheClass});
			    	return (SocketFactory) getHttpSocketFactory.invoke(null, CONNECTION_TIMEOUT, sslSessionCache);
				}catch(Exception e){
					Ln.v(e, "Unable to use android.net.SSLCertificateSocketFactory to get a SSL session caching socket factory, falling back to a non-caching socket factory");
					return SSLSocketFactory.getSocketFactory();
				}
		    	
		    }
		};
		
		return client;
	}

}