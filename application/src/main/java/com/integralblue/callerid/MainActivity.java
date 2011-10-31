package com.integralblue.callerid;

import roboguice.activity.RoboTabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends RoboTabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final TabHost tabHost = getTabHost();
		
		final Intent lookupTabIntent = new Intent();
		lookupTabIntent.setClass(this, LookupActivity.class);
		lookupTabIntent.putExtra("phoneNumber", this.getIntent().getStringExtra("phoneNumber"));
		tabHost.addTab(tabHost.newTabSpec("lookup").setContent(lookupTabIntent).setIndicator("Lookup",getResources().getDrawable(R.drawable.ic_tab_dialer)));
		
		final Intent recentCallsTabIntent = new Intent();
		recentCallsTabIntent.setClass(this, RecentCallsActivity.class);
		tabHost.addTab(tabHost.newTabSpec("recentCalls").setContent(recentCallsTabIntent).setIndicator("Recent Calls",getResources().getDrawable(R.drawable.ic_tab_recent)));
		
		final Intent settingsTabIntent = new Intent();
		settingsTabIntent.setClass(this, PreferencesActivity.class);
		tabHost.addTab(tabHost.newTabSpec("settings").setContent(settingsTabIntent).setIndicator("Settings",getResources().getDrawable(R.drawable.ic_tab_settings)));
		
		tabHost.setCurrentTabByTag("lookup");
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		
		final TabHost tabHost = getTabHost();
		
		tabHost.setCurrentTabByTag("lookup");
		
		((LookupActivity) getLocalActivityManager().getActivity("lookup")).lookup(intent.getStringExtra("phoneNumber"));
	}
}
