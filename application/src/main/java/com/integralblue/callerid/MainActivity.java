package com.integralblue.callerid;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import com.integralblue.callerid.contacts.ContactsHelper;

public class MainActivity extends RoboActivity {
	@InjectView(R.id.phone_number)
	EditText phoneNumber;
	
	@InjectView(R.id.perform_lookup)
	Button performLookup;
	
	@InjectView(R.id.create_contact)
	Button createContact;
	
	@Inject
	ContactsHelper contactsHelper;
	
	LookupAsyncTask currentLookupAsyncTask = null;
	
	class MainLookupAsyncTask extends LookupAsyncTask {
		public MainLookupAsyncTask(CharSequence phoneNumber) {
			super(phoneNumber,findViewById(R.id.main_layout_root));
		}
		@Override
		protected void onSuccess(CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
			callerIDResult = result;
			if(contactsHelper.haveContactWithPhoneNumber(result.getPhoneNumber())){
				createContact.setVisibility(View.GONE);
			}else{
				createContact.setVisibility(View.VISIBLE);
			}
		}
		@Override
		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			createContact.setVisibility(View.GONE);
		}
		@Override
		protected void onException(Exception e) throws RuntimeException {
			super.onException(e);
			createContact.setVisibility(View.GONE);
		}
		@Override
		protected void onInterrupted(Exception e) {
			super.onInterrupted(e);
			createContact.setVisibility(View.GONE);
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
        Ln.d("onCreate");
        
     // Injection doesn't happen until you call setContentView()
        
        // Because we want compatibility with Android 1.5, we can't use the new xml-style android:onClick method of binding click listeners :-(
        performLookup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	performLookupClick(v);
            }
        });
        createContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	createContactClick(v);
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, PreferencesActivity.class));
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
		currentLookupAsyncTask = new MainLookupAsyncTask(phoneNumber.getText());
		currentLookupAsyncTask.execute();
    }
    
    public void createContactClick(View button){
    	contactsHelper.createContactEditor(callerIDResult);
    }
}
