/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */
package edu.buffalo.cse.phonelab.phonelabservices;

// TESTing for update status

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;
import edu.buffalo.cse.phonelab.c2dm.RegistrationService;
import edu.buffalo.cse.phonelab.datalogger.LoggerService;
import edu.buffalo.cse.phonelab.statusmonitor.StatusMonitorReceiver;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.UploadLogs;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * starts periodic check service. 
 */
public class PeriodicCheckService extends IntentService {

	public PeriodicCheckService() {
		super("PeriodicCheckService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		//Check for reg id is syced or not
		SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		if (!settings.getBoolean(Util.SHARED_PREFERENCES_SYNC_KEY, false)) {
			Log.w("PhoneLab-" + getClass().getSimpleName(), "User info is not synched yet");
			String regId = settings.getString(Util.SHARED_PREFERENCES_REG_ID_KEY, null);
			if (regId == null) {
				Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
				registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
				registrationIntent.putExtra("sender", Util.C2DM_EMAIL);
				startService(registrationIntent);
			} else {

				Locks.acquireWakeLock(this);

				Intent regService = new Intent(this, RegistrationService.class);
				regService.putExtra("device_id", Util.getDeviceId(this));
				regService.putExtra("reg_id", regId);
				startService(regService);
			}
		} else {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "User info is synched");
		}

		//Check Data If Data Logger is running
		if (!isMyServiceRunning("edu.buffalo.cse.phonelab.datalogger.LoggerService")) {
			Log.w("PhoneLab-" + getClass().getSimpleName(), "Logger Service is not running starting now...");
			Intent service = new Intent(this, LoggerService.class);
			this.startService(service);
		} else {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Logger Service is running");
		}

		//Check Status Monitoring
		Intent newIntent = new Intent(getApplicationContext(), StatusMonitorReceiver.class);
		PendingIntent pendingTest = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_NO_CREATE);
		if (pendingTest == null) {
			AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pending);
		}
		
		
		//Check connection and power for uploading log files
		if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, false)) {
			ConnectivityManager myConnManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(myConnManager != null){
				if(myConnManager.getActiveNetworkInfo() != null){
					if(myConnManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {//wifi is connected
						if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false)) {
							if (settings.getBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false)) {//power is plugged in
								//Start uploading logs
								Locks.acquireWakeLock(this);
								Intent uploadIntent = new Intent(this, UploadLogs.class);
								startService(uploadIntent);
							}
						} else {
							//Start uploading logs
							Locks.acquireWakeLock(this);
							Intent uploadIntent = new Intent(this, UploadLogs.class);
							startService(uploadIntent);
						}
					}
				}
			}
		} else {
			if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false)) {
				if (settings.getBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false)) {//power is plugged in
					//Start uploading logs
					Locks.acquireWakeLock(this);
					Intent uploadIntent = new Intent(this, UploadLogs.class);
					startService(uploadIntent);
				}
			} else {
				//Start uploading logs
				Locks.acquireWakeLock(this);
				Intent uploadIntent = new Intent(this, UploadLogs.class);
				startService(uploadIntent);
			}
		}
		
		//Reschedule
		reschedulePeriodicChecking();

		Locks.releaseWakeLock();
	}

	/**
	 * Internal method to check if a Service is running or not
	 * @param fullName
	 * @return
	 */
	private boolean isMyServiceRunning(String fullName) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (fullName.equals(service.service.getClassName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Internal method for setting an alarm to wake the service up after Util.PERIODIC_CHECK_INTERVAL amount 
	 * If there exist an already set up alarm, it will first cancel it 
	 */
	private void reschedulePeriodicChecking() {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Rescheduling periodic checking...");
		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent newIntent = new Intent(getApplicationContext(), PeriodicCheckReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Util.PERIODIC_CHECK_INTERVAL, pending);
	}
}
