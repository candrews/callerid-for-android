package com.integralblue.callerid;

import roboguice.fragment.RoboListFragment;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.integralblue.callerid.CallerIDLookup.NoResultException;
import com.integralblue.callerid.contacts.ContactsHelper;

public class RecentCallsFragment extends RoboListFragment {
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
	
	private static final int CALL_LOG_LOADER = 1;
	
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
		public CallerIDResult call() throws Exception {
			CallerIDResult result;
			try{
				result = contactsHelper.getContact(phoneNumber.toString());
			}catch(NoResultException e){
				// not a phone number in the local contacts database, so time to query the web service
				
				result = callerIDLookup.lookup(phoneNumber);
			}
			
			return result;
		}
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        
        final ResourceCursorAdapter adapter = new ResourceCursorAdapter(getActivity().getApplicationContext(),R.layout.recent_calls_list_item, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				new RecentCallsLookupAsyncTask(
						getActivity(),
						view,
						cursor.getString(NUMBER_COLUMN_INDEX),
						cursor.getLong(DATE_COLUMN_INDEX),
						cursor.getInt(CALL_TYPE_COLUMN_INDEX)).execute();
			}
		};
		
		setListAdapter(adapter);
        
        getLoaderManager().initLoader(CALL_LOG_LOADER, null, new LoaderManager.LoaderCallbacks<Cursor>(){

			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new CursorLoader(getActivity(),
						android.provider.CallLog.Calls.CONTENT_URI, CALL_LOG_PROJECTION, null, null, ORDER);
			}

			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				adapter.swapCursor(data);
			}

			public void onLoaderReset(Loader<Cursor> loader) {
				adapter.swapCursor(null);
			}
        	
        });
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		final Cursor cursor = (Cursor) getListAdapter().getItem(position);
		final Intent lookupIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
		lookupIntent.putExtra("phoneNumber", cursor.getString(NUMBER_COLUMN_INDEX));
		startActivity(lookupIntent);
	}
    
    
}
