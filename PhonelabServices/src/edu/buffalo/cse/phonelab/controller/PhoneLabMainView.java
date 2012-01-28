/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import edu.buffalo.cse.phonelab.database.DatabaseAdapter;
import edu.buffalo.cse.phonelab.utilities.Util;

public class PhoneLabMainView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		/*try {
			InputStream in = this.getAssets().open("install");
			FileOutputStream f = new FileOutputStream(new File("/data/data/com.phonelab.controller", "install"));
			byte[] buffer = new byte[1024];
			int len = 0;
			while ( (len = in.read(buffer)) > 0 ) {
				f.write(buffer,0, len);
			}
			f.close();
			Runtime.getRuntime().exec("/system/bin/chmod 744 " + "/data/data/com.phonelab.controller/install");
			Log.i(getClass().getSimpleName(), "Install binary is set.");
		} catch (IOException e) {
			e.printStackTrace(); 
		} catch (Exception exc){
			exc.printStackTrace();
		}*/
		
        checkForSync();
    }

    /*This method makes sure that server has the device_id and registration_id to send C2D messages*/
	private void checkForSync() {
		DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext());
		dbAdapter.open(1);
		boolean isSynced = false;
		Cursor cursor = dbAdapter.selectEntry("device_id='" + Util.getDeviceId(getApplicationContext()) + "'", 0, null, null, null, null);
		if (cursor.moveToFirst()) {
			if (cursor.getInt(cursor.getColumnIndex("synced")) == 1) {
				isSynced = true;
			}
		}
		
		cursor.close();
		dbAdapter.close();
		
		if (!isSynced) {
			Log.i(getClass().getSimpleName(), "User info is not synched yet");
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
	        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
	        registrationIntent.putExtra("sender", "phone.lab.buffalo@gmail.com");
	        startService(registrationIntent);
		} else {
			Log.i(getClass().getSimpleName(), "User info is synched");
		}
	}
}

