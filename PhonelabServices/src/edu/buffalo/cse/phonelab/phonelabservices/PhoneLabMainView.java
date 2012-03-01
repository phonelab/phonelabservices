/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
/**
 * Main view GUI.
 */
public class PhoneLabMainView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        reschedulePeriodicMonitoring();
        System.out.println(getApplicationInfo().dataDir);
    }
	
	public void installedApps (View view) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "See All applications");
		
		Intent intent = new Intent(this, ApplicationList.class);
		startActivity(intent);
	}
	
	public void showInfo (View view) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Info will be shown");
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.phone-lab.org"));
		startActivity(myIntent);
	}
	
	public void showSettings (View view) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Settings will be shown");
		Intent intent = new Intent(this, SettingsView.class);
		startActivity(intent);
	}
	
	public void showLogs (View view) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Logs will be shown");
		Intent intent = new Intent(this, LogListView.class);
		startActivity(intent);
	}
	/**
	 * Internal method for setting an alarm to wake the service up after Util.PERIODIC_CHECK_INTERVAL amount 
	 * If there exist an already set up alarm, it will do nothing 
	 */
	private void reschedulePeriodicMonitoring() {
		
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Rescheduling periodic checking...");
		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent newIntent = new Intent(getApplicationContext(), PeriodicCheckReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pending);
	}
}

