/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.statusmonitor;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Locks;

public class StatusMonitorBattery extends Service {

	Timer timer;
	BroadcastReceiver myBatteryReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Locks.acquireWakeLock(this);
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				StatusMonitorBattery.this.unregisterReceiver(myBatteryReceiver);
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Couldn't learn location");
				Locks.releaseWakeLock();
				StatusMonitorBattery.this.stopSelf();
			}
		}, 60000*1);
		
		myBatteryReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				try {
					timer.cancel();
					StatusMonitorBattery.this.unregisterReceiver(this);
					
					int bLevel = arg1.getIntExtra("level", 0);
					String batteryLevel = String.valueOf(bLevel);
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Battery_level: " + batteryLevel);
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(),e.getMessage());
				}

				Locks.releaseWakeLock();
				StatusMonitorBattery.this.stopSelf();
			}
		};
		this.registerReceiver(myBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		return START_STICKY;
	}
}
