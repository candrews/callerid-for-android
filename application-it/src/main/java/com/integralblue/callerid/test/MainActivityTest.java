package com.integralblue.callerid.test;

import android.test.ActivityInstrumentationTestCase2;
import com.integralblue.callerid.*;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super("com.integralblue.callerid",MainActivity.class);
    }

    public void testActivity() {
    	MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}

