/**
 * @author fatih
 */

package edu.buffalo.cse.phonelab.c2dm;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import edu.buffalo.cse.phonelab.database.DatabaseAdapter;
import edu.buffalo.cse.phonelab.utilities.Util;

public class RegistrationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {		
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			Log.i(getClass().getSimpleName(), "RegistrationReceiver is fired");
			handleRegistration(context, intent);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
		dbAdapter.open(1);
		
		ContentValues values = new ContentValues();
		String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {// Registration failed, should try again later.
			values.putNull("reg_id");
			dbAdapter.update(values, 0, "device_id=" + Util.getDeviceId(context) + "'");
		} else if (intent.getStringExtra("unregistered") != null) {// unregistration done, new messages from the authorized sender will be rejected
			values.putNull("reg_id");
			dbAdapter.update(values, 0, "device_id='" + Util.getDeviceId(context) + "'");
		} else if (registration != null) {// Send the registration ID to the 3rd party site that is sending the messages.
			String deviceId = Util.getDeviceId(context);
			values.put("reg_id", registration);
			dbAdapter.update(values, 0, "device_id='" + deviceId + "'");
			
			Intent regService = new Intent(context, RegistrationService.class);
			regService.putExtra("device_id", deviceId);
			regService.putExtra("reg_id", registration);
			context.startService(regService);
		}
		
		dbAdapter.close();
	}
}
