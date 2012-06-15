package com.integralblue.callerid;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import roboguice.inject.InjectResource;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.integralblue.callerid.inject.CountryDetector;

public class HttpCallerIDLookup implements CallerIDLookup {
	@Inject SharedPreferences sharedPreferences;
	@InjectResource(R.string.default_lookup_url) String defaultLookupUrl;
	@Inject RestTemplate restTemplate;
	
	@Inject CountryDetector countryDetector;

	public CallerIDResult lookup(final CharSequence phoneNumber) throws NoResultException {
		
		final String beforeSubstitutionLookupUrl = sharedPreferences.getString("lookup_url", defaultLookupUrl);
		final String url;
		if(beforeSubstitutionLookupUrl.contains("{0}")){
			// ensure backwards compatibility. The URL used to use {0} and {1}
			url = MessageFormat.format(beforeSubstitutionLookupUrl, "{number}", "{agentCountry}");
		}else{
			url = beforeSubstitutionLookupUrl;
		}
		final Map<String, String> urlVariables = new HashMap<String, String>();
		urlVariables.put("number", phoneNumber.toString());
		urlVariables.put("agentCountry", countryDetector.getCountry());
		try{
			return restTemplate.getForObject(url, CallerIDResult.class, urlVariables);
		}catch(HttpClientErrorException e){
			if(HttpStatus.NOT_FOUND.equals(e.getStatusCode())){
				throw new NoResultException();
			}else{
				throw new RuntimeException(e);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

}
