package com.integralblue.callerid;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.inject.Inject;
import com.integralblue.callerid.contacts.ContactsHelper;

public class LookupFragment extends RoboFragment {
	
	@InjectView(R.id.phone_number)
	EditText phoneNumber;
	
	@InjectView(R.id.perform_lookup)
	View performLookup;
	
	@InjectView(R.id.create_contact)
	View createContact;
	
	@InjectView(R.id.call)
	View call;
	
	@Inject
	ContactsHelper contactsHelper;
	
	LookupAsyncTask currentLookupAsyncTask = null;
	
    //contains the last lookup result
    private CallerIDResult callerIDResult = null;
	
	class MainLookupAsyncTask extends LookupAsyncTask {
		public MainLookupAsyncTask(Context context, CharSequence phoneNumber) {
			super(context, phoneNumber,(ViewGroup) getView().findViewById(R.id.toast_layout_root),true);
		}
		@Override
		protected void onSuccess(CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
            
            callerIDResult = result;
            
            createContact.setVisibility(callerIDResult==null || contactsHelper.haveContactWithPhoneNumber(callerIDResult.getPhoneNumber())?View.GONE:View.VISIBLE);
            call.setVisibility(callerIDResult==null?View.GONE:View.VISIBLE);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		return inflater.inflate(R.layout.lookup, container, false);
	}
	
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

	public void lookup(String initialPhoneNumber){
        if(! TextUtils.isEmpty(initialPhoneNumber)){
        	phoneNumber.setText(initialPhoneNumber);
        	performLookup.performClick();
        }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        
        performLookup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		// since we're about to start a new lookup,
        		// we want to cancel any lookups in progress
        		if (currentLookupAsyncTask != null)
        			currentLookupAsyncTask.cancel(true);
        		currentLookupAsyncTask = new MainLookupAsyncTask(getActivity(), phoneNumber.getText());
        		currentLookupAsyncTask.execute();
            }
        });
        
        call.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    	        Uri callUri;
    	        if (CallerIDApplication.isUriNumber(callerIDResult.getPhoneNumber())) {
    	            callUri = Uri.fromParts("sip", callerIDResult.getPhoneNumber(), null);
    	        } else {
    	            callUri = Uri.fromParts("tel", callerIDResult.getPhoneNumber(), null);
    	        }
    	        startActivity(new Intent(Intent.ACTION_CALL,callUri));
            }
        });
        
        createContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(contactsHelper.createContactEditor(callerIDResult));
            }
        });
        
        //if a phone number was passed in, look it up
        if(savedInstanceState!=null && savedInstanceState.getString("phoneNumber")!=null){
        	lookup(savedInstanceState.getString("phoneNumber"));
        }else if(getArguments() !=null) lookup(getArguments().getString("phoneNumber"));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("phoneNumber",phoneNumber.getText().toString());
	}


}
