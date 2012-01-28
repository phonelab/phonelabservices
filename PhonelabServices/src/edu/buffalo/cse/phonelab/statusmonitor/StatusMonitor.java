/**
 * @author fatih
 * 
 */

package edu.buffalo.cse.phonelab.statusmonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class StatusMonitor extends Service {

	Bundle extras;
	long runInterval = 0;//this will be in miliseconds

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(getClass().getSimpleName(), "Status Monitoring is started");
		extras = intent.getExtras();
		long value = Long.parseLong(extras.getString("value"));
		String units = extras.getString("units");
		//String setBy = extras.getString("setby");

		Log.i(getClass().getSimpleName(), "Value: " + value + " - unit: " + units);

		if (units.equals("hour")) {
			runInterval = value *  60 * 60 * 1000;
		} else if (units.equals("min")) {
			runInterval = value * 60 * 1000;
		} else if (units.equals("sec")) {
			runInterval = value * 1000;
		} else if (units.equals("milisec")) {
			runInterval = value;
		}
		Log.i(getClass().getSimpleName(), "Running interval: " + runInterval);

		Intent batteryIntent = new Intent(getApplicationContext(), StatusMonitorBattery.class);
		this.startService(batteryIntent);
		Intent signalIntent = new Intent(getApplicationContext(), StatusMonitorSignal.class);
		this.startService(signalIntent);
		
		rescheduleMonitoring();
		return START_STICKY;
	}

	/* Set an alarm to wake the service up after runInterval amount, if there exist an already set up alarm, it will first cancel it */
	private void rescheduleMonitoring() {
		Log.i(getClass().getSimpleName(), "Rescheduling status monitoring...");
		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent newIntent = new Intent(getApplicationContext(), StatusMonitorReceiver.class);
		newIntent.putExtras(extras);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + runInterval, pending);
	} 
}
