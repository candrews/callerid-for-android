package com.integralblue.callerid;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import roboguice.inject.InjectResource;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.inject.Inject;

public class HttpCallerIDLookup implements CallerIDLookup {
	@Inject SharedPreferences sharedPreferences;
	@InjectResource(R.string.default_lookup_url) String defaultLookupUrl;
	@Inject RestTemplate restTemplate;
	
	@Inject TelephonyManager telephonyManager;

	public CallerIDResult lookup(final CharSequence phoneNumber) throws NoResultException {
		//use the network's country if it's available (as I figure that's probably the best? I'm just guessing)
		//if the network's country isn't available, using the SIM's
		//I have no idea how or if this works on CDMA networks
		//(Android documentation warns that these function may not work as expected with CDMA)
		final String agentCountry = TextUtils.isEmpty(telephonyManager.getNetworkCountryIso())?telephonyManager.getNetworkCountryIso():telephonyManager.getSimCountryIso();
		
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
		urlVariables.put("agentCountry", agentCountry);
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
