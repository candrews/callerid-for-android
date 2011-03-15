package com.integralblue.callerid;

import roboguice.receiver.RoboBroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class CallerIDBroadcastReceiver extends RoboBroadcastReceiver {
	@Override
	public void handleReceive(Context context, Intent intent) {
		super.handleReceive(context, intent);
		
		final Intent myIntent=new Intent(context,CallerIDService.class);
		myIntent.putExtras(intent);
		context.startService(myIntent);
	}
}
