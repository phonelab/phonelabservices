/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import edu.buffalo.cse.phonelab.datalogger.LoggerService;
import edu.buffalo.cse.phonelab.utilities.Util;

public class PhoneLabMainView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*Start Data Logger*/
        Intent service = new Intent(this, LoggerService.class);
		this.startService(service);
		
		/*Check for unsynced info with server*/
        checkForSync();
    }

    /**
     * This method makes sure that server has the device_id and registration_id to send C2D messages
     */
	private void checkForSync() {
		SharedPreferences settings = getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		boolean isSynced = settings.getBoolean(Util.SHARED_PREFERENCES_SYNC_KEY, false);
		if (!isSynced) {
			Log.w(getClass().getSimpleName(), "User info is not synched yet");
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
	        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
	        registrationIntent.putExtra("sender", "phone.lab.buffalo@gmail.com");
	        startService(registrationIntent);
		} else {
			Log.i(getClass().getSimpleName(), "User info is synched");
		}
	}
	
	public void installedApps (View view) {
		Log.i(getClass().getSimpleName(), "See All applications");
		
		Intent intent = new Intent(this, ApplicationList.class);
		startActivity(intent);
	}
	
	public void showInfo (View view) {
		Log.i(getClass().getSimpleName(), "Info will be shown");
		
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.phone-lab.org"));
		startActivity(myIntent);
	}
}

