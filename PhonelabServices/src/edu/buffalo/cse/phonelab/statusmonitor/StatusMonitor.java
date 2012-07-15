/**
 * @author fatih
 * 
 */

package edu.buffalo.cse.phonelab.statusmonitor;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.Util;

public class StatusMonitor extends Service {

	private long runInterval = 0;//this will be in miliseconds

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Status Monitoring is started");

		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
		if (manifest.getManifest()) {
			try {
				HashMap<String, String> constraintMap = new HashMap<String, String>();
				constraintMap.put("name", "runInterval");
				ArrayList<PhoneLabParameter> parameters = manifest.getStatParamaterByConstraints(constraintMap);
				if (parameters.size() == 1) {
					PhoneLabParameter param = parameters.get(0);

					if (param.getUnits() != null && param.getValue() != null) {
						long value = Long.parseLong(param.getValue());
						String units = param.getUnits();
						

						Log.i("PhoneLab-" + getClass().getSimpleName(), "Value: " + value + " - unit: " + units);

						if (units.equals("hour")) {
							runInterval = value *  60 * 60 * 1000;
						} else if (units.equals("min")) {
							runInterval = value * 60 * 1000;
						} else if (units.equals("sec")) {
							runInterval = value * 1000;
						} else if (units.equals("milisec")) {
							runInterval = value;
						}
						Log.i("PhoneLab-" + getClass().getSimpleName(), "Running interval: " + runInterval);

						Intent locationIntent = new Intent(getApplicationContext(), StatusMonitorLocation.class);
						this.startService(locationIntent);
						Intent batteryIntent = new Intent(getApplicationContext(), StatusMonitorBattery.class);
						this.startService(batteryIntent);
						Intent signalIntent = new Intent(getApplicationContext(), StatusMonitorSignal.class);
						this.startService(signalIntent);
						Intent cellLocationIntent = new Intent(getApplicationContext(), StatusMonitorCellLocation.class);
						this.startService(cellLocationIntent);
						
						rescheduleMonitoring();
					}
				}
			} catch (XPathExpressionException e) {
				Log.e(getClass().getSimpleName(),e.toString());
			}
		}
		
		Locks.releaseWakeLock();
		
		this.stopSelf();
		
		return START_STICKY;
	}

	/**
	 * Internal method for setting an alarm to wake the service up after runInterval amount 
	 * If there exist an already set up alarm, it will first cancel it 
	 */
	private void rescheduleMonitoring() {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Rescheduling status monitoring...");
		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent newIntent = new Intent(getApplicationContext(), StatusMonitorReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + runInterval, pending);
	} 
}
