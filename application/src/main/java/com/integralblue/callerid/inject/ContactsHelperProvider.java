package com.integralblue.callerid.inject;

import android.os.Build;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.integralblue.callerid.contacts.ContactsHelper;

public class ContactsHelperProvider implements Provider<ContactsHelper> {
	@Inject Injector injector;

	public ContactsHelper get() {
		final String className;
        final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            className = "OldContactsHelper";
        } else {
            className = "NewContactsHelper";
        }
        try {
            final Class<? extends ContactsHelper> clazz =
                    Class.forName(ContactsHelper.class.getPackage().getName() + "." + className)
                            .asSubclass(ContactsHelper.class);
            final ContactsHelper ret = clazz.newInstance();
            injector.injectMembers(ret);
            return ret;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
	}

}
