package com.integralblue.callerid.inject;

import java.util.Locale;

import com.google.inject.Inject;

import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class CountryDetector {
	@Inject TelephonyManager telephonyManager;
	
	/** Get the device's current country's 2 letter ISO code
	 * @return
	 */
	public String getCountry(){
		String result;
        result = getNetworkBasedCountry();
        if (result == null) {
            result = getSimBasedCountry();
        }
        if (result == null) {
            result = getLocaleCountry();
        }
        if(result!=null) result = result.toUpperCase(); //ISO country codes are always uppercase
        return result;
	}
	
    /**
     * @return the country from the mobile network.
     */
    protected String getNetworkBasedCountry() {
        String countryIso = null;
        // TODO: The document says the result may be unreliable on CDMA networks. Shall we use
        // it on CDMA phone? We may test the Android primarily used countries.
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            countryIso = telephonyManager.getNetworkCountryIso();
            if (!TextUtils.isEmpty(countryIso)) {
                return countryIso;
            }
        }
        return null;
    }
    
    /**
     * @return the country from SIM card
     */
    protected String getSimBasedCountry() {
        String countryIso = null;
        countryIso = telephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return countryIso;
        }
        return null;
    }

    /**
     * @return the country from the system's locale.
     */
    protected String getLocaleCountry() {
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != null) {
            return defaultLocale.getCountry();
        } else {
            return null;
        }
    }

}
