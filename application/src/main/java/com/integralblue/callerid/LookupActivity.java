package com.integralblue.callerid;

import java.lang.reflect.Method;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.inject.Inject;
import com.integralblue.callerid.contacts.ContactsHelper;
import com.integralblue.callerid.inject.VersionInformationHelper;

public class LookupActivity extends RoboActivity {
	private static final int NEWER_VERSION_AVAILABLE_DIALOG = 1;
	
	@InjectView(R.id.phone_number)
	EditText phoneNumber;
	
	@InjectView(R.id.perform_lookup)
	Button performLookup;
	
	@Inject
	VersionInformationHelper versionInformationHelper;
	
	@Inject
	ContactsHelper contactsHelper;
	
	LookupAsyncTask currentLookupAsyncTask = null;
	
	//keep track of if we've prompted them already. We only want to prompt the user once per run of the application.
	boolean promptedForNewVersion = false;
	
	class MainLookupAsyncTask extends LookupAsyncTask {
		public MainLookupAsyncTask(Context context, CharSequence phoneNumber) {
			super(context, phoneNumber,(ViewGroup) findViewById(R.id.toast_layout_root),true);
		}
		@Override
		protected void onSuccess(CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
			
			if (!promptedForNewVersion && versionInformationHelper.shouldPromptForNewVersion()) {
				promptedForNewVersion = true;
				showDialog(NEWER_VERSION_AVAILABLE_DIALOG);
			}
			
			//on Android API>=11, there's an invalidateOptionsMenu method that must be called when the options menu changes
			try {
				Method m = this.getClass().getMethod("invalidateOptionsMenu");
				m.invoke(this);
			} catch (Exception ex) {
				//method doesn't exist, we must on <11, so ignore this error
			}
			
			callerIDResult = result;
		}
		@Override
		protected void onPreExecute() throws Exception {
			super.onPreExecute();
		}
		@Override
		protected void onException(Exception e) throws RuntimeException {
			super.onException(e);
		}
		@Override
		protected void onInterrupted(Exception e) {
			super.onInterrupted(e);
		}
	}

	//contains the last lookup result
	private CallerIDResult callerIDResult = null;
	
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     // Injection doesn't happen until you call setContentView()
        setContentView(R.layout.lookup);
        
        // Because we want compatibility with Android 1.5, we can't use the new xml-style android:onClick method of binding click listeners :-(
        performLookup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	performLookupClick(v);
            }
        });

		if (!promptedForNewVersion && versionInformationHelper.shouldPromptForNewVersion()) {
			promptedForNewVersion = true;
			showDialog(NEWER_VERSION_AVAILABLE_DIALOG);
		}
        
        //if a phone number was passed in, look it up
        final String initialPhoneNumber = this.getIntent().getStringExtra("phoneNumber");
    	lookup(initialPhoneNumber);
    }

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		
        final String initialPhoneNumber = intent.getStringExtra("phoneNumber");
    	lookup(initialPhoneNumber);
	}

	public void lookup(String initialPhoneNumber){
        if(! TextUtils.isEmpty(initialPhoneNumber)){
        	phoneNumber.setText(initialPhoneNumber);
        	performLookup.performClick();
        }
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.lookup_menu, menu);
        return true;
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.add_contact).setVisible(callerIDResult!=null && !contactsHelper.haveContactWithPhoneNumber(callerIDResult.getPhoneNumber()));
		menu.findItem(R.id.call).setVisible(callerIDResult!=null);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.call:
            Uri callUri;
            if (CallerIDApplication.isUriNumber(callerIDResult.getPhoneNumber())) {
                callUri = Uri.fromParts("sip", callerIDResult.getPhoneNumber(), null);
            } else {
                callUri = Uri.fromParts("tel", callerIDResult.getPhoneNumber(), null);
            }
            startActivity(new Intent(Intent.ACTION_CALL,callUri));
			return true;
		case R.id.add_contact:
	    	startActivity(contactsHelper.createContactEditor(callerIDResult));
			return true;
		case R.id.help:
			Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.integralblue.com/callerid-for-android"));
			startActivity(viewIntent);  
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    public void performLookupClick(View button){
		// since we're about to start a new lookup,
		// we want to cancel any lookups in progress
		if (currentLookupAsyncTask != null)
			currentLookupAsyncTask.cancel(true);
		currentLookupAsyncTask = new MainLookupAsyncTask(this, phoneNumber.getText());
		currentLookupAsyncTask.execute();
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
