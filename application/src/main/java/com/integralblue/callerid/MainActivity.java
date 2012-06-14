package com.integralblue.callerid;

import javax.inject.Inject;

import com.integralblue.callerid.inject.VersionInformationHelper;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class MainActivity extends RoboFragmentActivity {
	private static final int NEWER_VERSION_AVAILABLE_DIALOG = 1;

	@InjectView(android.R.id.tabhost)
	TabHost tabHost;
	
	@InjectView(R.id.pager)
	ViewPager viewPager;
	
	@InjectResource(R.drawable.ic_tab_dialer)
    Drawable drawableTabDialer;
	
	@InjectResource(R.drawable.ic_tab_recent)
    Drawable drawableTabRecent;
	
	@InjectResource(R.drawable.ic_tab_settings)
    Drawable drawableTabSettings;
	
	@Inject
	VersionInformationHelper versionInformationHelper;
	
	TabsAdapter tabsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fragment_tabs_pager);
		
        tabHost.setup();

        tabsAdapter = new TabsAdapter(this, tabHost, viewPager);

        tabsAdapter.addTab(tabHost.newTabSpec("lookup").setIndicator("Lookup",drawableTabDialer),
                LookupFragment.class, getIntent().getExtras());
        tabsAdapter.addTab(tabHost.newTabSpec("recentCalls").setIndicator("Recent Calls",drawableTabRecent),
                RecentCallsFragment.class, null);

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
	}
	
    @Override
	protected void onResume() {
		super.onResume();
		if(versionInformationHelper.shouldPromptForNewVersion()){
			showDialog(NEWER_VERSION_AVAILABLE_DIALOG);
		}
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", tabHost.getCurrentTabTag());
    }

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		
		tabHost.setCurrentTabByTag("lookup");
		
		// TODO I don't like how the fragment is retrieved - but I don't know of a better way.
		LookupFragment lookupFragment = (LookupFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 0);
		
		lookupFragment.lookup(intent.getStringExtra("phoneNumber"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case R.id.help:
		        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.integralblue.com/callerid-for-android"));
		        startActivity(viewIntent);  
		        return true;
		    case R.id.settings:
		        startActivity(new Intent(this, PreferencesActivity.class));  
		        return true;
	        default:
	        	return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NEWER_VERSION_AVAILABLE_DIALOG:
			return versionInformationHelper.createNewVersionDialog(this);
		default:
			return super.onCreateDialog(id);
		}
	}
}
