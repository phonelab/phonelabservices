/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.statusmonitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class StatusMonitorBattery extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		BroadcastReceiver myBatteryReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				try {
					StatusMonitorBattery.this.unregisterReceiver(this);
					
					int bLevel = arg1.getIntExtra("level", 0);
					String batteryLevel = String.valueOf(bLevel);
					Log.i(getClass().getSimpleName(), "Battery level: " + batteryLevel);
				} catch (Exception e) {
					e.printStackTrace();
				}

				StatusMonitorBattery.this.stopSelf();
			}
		};
		this.registerReceiver(myBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		return START_STICKY;
	}
}
