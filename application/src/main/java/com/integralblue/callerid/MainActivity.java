package com.integralblue.callerid;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class MainActivity extends RoboActivity {
	private static final int NEWER_VERSION_AVAILABLE_DIALOG = 1;
	
	@InjectView(R.id.phone_number)
	EditText phoneNumber;
	
	@InjectView(R.id.perform_lookup)
	Button performLookup;
	
	@InjectView(R.id.create_contact)
	Button createContact;
	
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
        setContentView(R.layout.main);
        
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

		if (!promptedForNewVersion && versionInformationHelper.shouldPromptForNewVersion()) {
			promptedForNewVersion = true;
			showDialog(NEWER_VERSION_AVAILABLE_DIALOG);
		}
        
        //if a phone number was passed in, look it up
        final String initialPhoneNumber = this.getIntent().getStringExtra("phoneNumber");
        if(! TextUtils.isEmpty(initialPhoneNumber)){
        	phoneNumber.setText(initialPhoneNumber);
        	performLookup.performClick();
        }
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
		currentLookupAsyncTask = new MainLookupAsyncTask(this, phoneNumber.getText());
		currentLookupAsyncTask.execute();
    }
    
    public void createContactClick(View button){
    	contactsHelper.createContactEditor(callerIDResult);
    }
    

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case NEWER_VERSION_AVAILABLE_DIALOG:
				return (new AlertDialog.Builder(this)
					.setTitle(R.string.new_version_dialog_title)
					.setPositiveButton(R.string.new_version_dialog_upgrade_button_text,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									versionInformationHelper.showNewVersionInformation();
									dialog.dismiss();
								}
							})
					.setNeutralButton(R.string.new_version_dialog_not_now_button_text,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
					.setNegativeButton(R.string.new_version_dialog_never_button_text,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									versionInformationHelper.setAllowPromptForNewVersion(false);
									dialog.cancel();
								}
							}).create());
			default:
				return super.onCreateDialog(id);
		}
	}
}
