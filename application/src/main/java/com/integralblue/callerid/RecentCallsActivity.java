package com.integralblue.callerid;

import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.google.inject.Inject;
import com.integralblue.callerid.CallerIDLookup.NoResultException;
import com.integralblue.callerid.contacts.ContactsHelper;
import com.integralblue.callerid.inject.VersionInformationHelper;

public class RecentCallsActivity extends RoboListActivity {
	@Inject VersionInformationHelper versionInformationHelper;
	@Inject ContactsHelper contactsHelper;
	@Inject CallerIDLookup callerIDLookup;
	
	@InjectResource(R.drawable.ic_call_log_list_incoming_call)
    Drawable drawableIncoming;
	@InjectResource(R.drawable.ic_call_log_list_outgoing_call)
    Drawable drawableOutgoing;
	@InjectResource(R.drawable.ic_call_log_list_missed_call)
    Drawable drawableMissed;
	

	@InjectResource(R.string.lookup_no_result)
	String lookupNoResult;
	@InjectResource(R.string.lookup_error)
	String lookupError;
	@InjectResource(R.string.lookup_in_progress)
	String lookupInProgress;
	
	private static final int NEWER_VERSION_AVAILABLE_DIALOG = 1;
	
    static final int ID_COLUMN_INDEX = 0;
    static final int NUMBER_COLUMN_INDEX = 1;
    static final int DATE_COLUMN_INDEX = 2;
    static final int DURATION_COLUMN_INDEX = 3;
    static final int CALL_TYPE_COLUMN_INDEX = 4;
    static final int CALLER_NAME_COLUMN_INDEX = 5;
    static final int CALLER_NUMBERTYPE_COLUMN_INDEX = 6;
    static final int CALLER_NUMBERLABEL_COLUMN_INDEX = 7;
	
    static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.DATE,
        Calls.DURATION,
        Calls.TYPE,
        Calls.CACHED_NAME,
        Calls.CACHED_NUMBER_TYPE,
        Calls.CACHED_NUMBER_LABEL
    };
    final String ORDER = android.provider.CallLog.Calls.DATE + " DESC";
	
	//keep track of if we've prompted them already. We only want to prompt the user once per run of the application.
	boolean promptedForNewVersion = false;
    
	class RecentCallsLookupAsyncTask extends RoboAsyncTask<CallerIDResult> {
		final String phoneNumber;
		final View view;
		final View callIcon;
		final ImageView callTypeIcon;
		final TextView dateView;
		final TextView labelView;
		final TextView numberView;
		final TextView line1View;
		final int callType;
		final long date;
		
		public RecentCallsLookupAsyncTask(Context context, View view, String phoneNumber, long date, int callType) {
			super(context, new Handler(context.getMainLooper()));
			this.phoneNumber = phoneNumber;
			this.view = view;
			callIcon = (View) view.findViewById(R.id.call_icon);
			callTypeIcon = (ImageView) view.findViewById(R.id.call_type_icon);
			dateView = (TextView) view.findViewById(R.id.date);
			labelView = (TextView) view.findViewById(R.id.label);
			numberView = (TextView) view.findViewById(R.id.number);
			line1View = (TextView) view.findViewById(R.id.line1);
			this.date = date;
			this.callType = callType;
		}
		@Override
		protected void onSuccess(CallerIDResult result)
				throws Exception {
			super.onSuccess(result);
			
			if (!promptedForNewVersion && versionInformationHelper.shouldPromptForNewVersion()) {
				promptedForNewVersion = true;
				showDialog(NEWER_VERSION_AVAILABLE_DIALOG);
			}
			
			line1View.setText(result.getName());
			labelView.setText("");
		}
		@Override
		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			
			// Set the date/time field by mixing relative and absolute times.
			dateView.setText(
					DateUtils.getRelativeTimeSpanString(
							date,
							System.currentTimeMillis(), 
							DateUtils.MINUTE_IN_MILLIS, 
							DateUtils.FORMAT_ABBREV_RELATIVE));
			labelView.setText(lookupInProgress);
			numberView.setText(PhoneNumberUtils.formatNumber(phoneNumber));
			line1View.setText("");
			
             // Set the icon
             switch (callType) {
                 case Calls.INCOMING_TYPE:
                	 callTypeIcon.setImageDrawable(drawableIncoming);
                     break;
                 case Calls.OUTGOING_TYPE:
                	 callTypeIcon.setImageDrawable(drawableOutgoing);
                     break;
                 case Calls.MISSED_TYPE:
                	 callTypeIcon.setImageDrawable(drawableMissed);
                     break;
             }
             
             callIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
	                 Uri callUri;
	                 if (CallerIDApplication.isUriNumber(phoneNumber)) {
	                     callUri = Uri.fromParts("sip", phoneNumber, null);
	                 } else {
	                     callUri = Uri.fromParts("tel", phoneNumber, null);
	                 }
	                 startActivity(new Intent(Intent.ACTION_CALL,callUri));
				}
			});
		}
		@Override
		protected void onException(Exception e) throws RuntimeException {
			if (e instanceof CallerIDLookup.NoResultException) {
				line1View.setText(lookupNoResult);
			} else {
				Ln.e(e);
				line1View.setText(lookupError);
			}
			labelView.setText("");
		}
		@Override
		protected void onInterrupted(Exception e) {
			super.onInterrupted(e);
		}
		@Override
		public CallerIDResult call() throws Exception {
			CallerIDResult result;
			try{
				result = contactsHelper.getContact(phoneNumber.toString());
			}catch(NoResultException e){
				// not a phone number in the local contacts database, so time to query the web service
				
				result = callerIDLookup.lookup(phoneNumber);
				
				if(result.getLatestAndroidVersionCode()!=null){
					//got version info from the server
					versionInformationHelper.setLatestVersionCode(result.getLatestAndroidVersionCode());
				}
			}
			
			return result;
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        final Cursor callLogCursor = getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI,
                CALL_LOG_PROJECTION,
                null,
                null,
                ORDER
                );
        startManagingCursor(callLogCursor);
        
        setListAdapter(new ResourceCursorAdapter(this,R.layout.recent_calls_list_item, callLogCursor) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				new RecentCallsLookupAsyncTask(
						RecentCallsActivity.this,
						view,
						cursor.getString(NUMBER_COLUMN_INDEX),
						cursor.getLong(DATE_COLUMN_INDEX),
						cursor.getInt(CALL_TYPE_COLUMN_INDEX)).execute();
			}
		});
        
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Cursor cursor = (Cursor) getListAdapter().getItem(position);
				final Intent lookupIntent = new Intent(getApplicationContext(), MainActivity.class);
				lookupIntent.putExtra("phoneNumber", cursor.getString(NUMBER_COLUMN_INDEX));
				startActivity(lookupIntent);
			}
		});
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case NEWER_VERSION_AVAILABLE_DIALOG:
				versionInformationHelper.createNewVersionDialog(this);
			default:
				return super.onCreateDialog(id);
		}
	}
}
