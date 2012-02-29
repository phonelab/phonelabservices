/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.receivers;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;
import edu.buffalo.cse.phonelab.phonelabservices.PeriodicCheckReceiver;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.UploadLogs;
import edu.buffalo.cse.phonelab.utilities.Util;

public class PowerOnReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Power is plugged in!");
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Rescheduling periodic checking...");
			SharedPreferences settings = context.getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
			Editor editor = settings.edit();
			editor.putBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, true);
			editor.commit();
			
			
			AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent newIntent = new Intent(context, PeriodicCheckReceiver.class);
			PendingIntent pending = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pending);

			if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, false)) {
				ConnectivityManager myConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				if(myConnManager != null){
					if(myConnManager.getActiveNetworkInfo() != null){
						if(myConnManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
							//Start uploading logs
							Locks.acquireWakeLock(context);
							Intent uploadIntent = new Intent(context, UploadLogs.class);
							context.startService(uploadIntent);
						}
					}
				}
			} else {//now wi-fi requirements
				//Start uploading logs
				Locks.acquireWakeLock(context);
				Intent uploadIntent = new Intent(context, UploadLogs.class);
				context.startService(uploadIntent);
			}
		} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Power is plugged off!");
			SharedPreferences settings = context.getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
			Editor editor = settings.edit();
			editor.putBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false);
			editor.commit();
		}
	}
}
