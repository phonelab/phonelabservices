/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */
package edu.buffalo.cse.phonelab.phonelabservices;

// TESTing for update status

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

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
import edu.buffalo.cse.phonelab.utilities.InformServer;
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
		if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, false)) { //Checks for wifi Logs Setting
			ConnectivityManager myConnManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(myConnManager != null){
				if(myConnManager.getActiveNetworkInfo() != null){
					if(myConnManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {//wifi is connected ON, upload
						if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false)) {
							if (settings.getBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false)) {//power is plugged in ON,upload
								//Start uploading logs
								Locks.acquireWakeLock(this);
								Intent uploadIntent = new Intent(this, UploadLogs.class);
								startService(uploadIntent);
							}
						} else { //Power is plugged in OFF, wifi ON, upload
							//Start uploading logs
							Locks.acquireWakeLock(this);
							Intent uploadIntent = new Intent(this, UploadLogs.class);
							startService(uploadIntent);
						}
					} //Wifi Setting ON, not available - No upload
				}
			}
		} else { //Checked for power Setting
			if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false)) {
				if (settings.getBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false)) {//power is plugged in
					//Start uploading logs
					Locks.acquireWakeLock(this);
					Intent uploadIntent = new Intent(this, UploadLogs.class);
					startService(uploadIntent);
				} // Power Setting ON, not available - No upload
			} else { // Both Settings are OFF. Upload in 3G.
				//Start uploading logs
				Locks.acquireWakeLock(this);
				Intent uploadIntent = new Intent(this, UploadLogs.class);
				startService(uploadIntent);
			}
		}
		
		
		//Randomizing the transmission of heartbeat messages 
		//The msg will be transmitted at a random time in the next 5 mins
		
		int fivemins = 5*60*1000;
		
		int randomsleep = ((int)(Math.random() * (fivemins + 1)));
		Log.v("PhoneLab-" + getClass().getSimpleName(), "Sleeping for  " + randomsleep);
		
		try
		{
			Thread.sleep(randomsleep);
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		
		
		
		
		//sending heartbeat messages here
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Sending Heartbeat message ");
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(Util.DEVICE_STATUS_UPLOAD_URL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			String response = "";
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			nameValuePairs.add(new BasicNameValuePair("device_id", Util.getDeviceId(getApplicationContext())));
			nameValuePairs.add(new BasicNameValuePair("status_type", "H"));
			nameValuePairs.add(new BasicNameValuePair("status_value", "1"));
			//nameValuePairs.add(new BasicNameValuePair(, ));
			
			
			httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Post URI is:: "+httpost.getURI().toString());
			response = httpclient.execute(httpost,responseHandler);
			
			JSONObject responseJ = new JSONObject(response);
			
			if (responseJ.getString("error").equals("")) {//success
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Heartbeat message successfully sent : ");
			} else {//error
				Log.e("PhoneLab-" + getClass().getSimpleName(), "Oops!, some problem, heart beat transmission failed : ");
				Log.v("PhoneLab-" + getClass().getSimpleName(), responseJ.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
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
