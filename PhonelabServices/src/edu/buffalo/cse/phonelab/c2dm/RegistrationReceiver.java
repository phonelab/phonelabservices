/**
 * @author fatih
 */

package edu.buffalo.cse.phonelab.c2dm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * Handles registration information
 */
public class RegistrationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {		
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Registration Receiver is fired");
			handleRegistration(context, intent);
		}
	}

	/**
	 * Method to handle registration call back details
	 * @param context
	 * @param intent
	 */
	private void handleRegistration(Context context, Intent intent) {
		SharedPreferences settings = context.getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		Editor editor = settings.edit();
		
		String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {// Registration failed, should try again later.
			editor.putString(Util.SHARED_PREFERENCES_REG_ID_KEY, null);
		} else if (intent.getStringExtra("unregistered") != null) {// unregistration done, new messages from the authorized sender will be rejected
			editor.putString(Util.SHARED_PREFERENCES_REG_ID_KEY, null);
		} else if (registration != null) {// Send the registration ID to the 3rd party site that is sending the messages.
			editor.putString(Util.SHARED_PREFERENCES_REG_ID_KEY, registration);
			
			Locks.acquireWakeLock(context);
			
			Intent regService = new Intent(context, RegistrationService.class);
			regService.putExtra("device_id", Util.getDeviceId(context));
			regService.putExtra("reg_id", registration);
			context.startService(regService);
		}
		
		if (editor.commit()) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Shared Preferences Settings updated successfully");
		} else {
			Log.e("PhoneLab-" + getClass().getSimpleName(), "Shared Preferences Settings couldn't be updated");
		}
	}
}
