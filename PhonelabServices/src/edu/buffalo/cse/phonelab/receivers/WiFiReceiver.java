/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.UploadLogs;
import edu.buffalo.cse.phonelab.utilities.Util;

public class WiFiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()) {
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Wi-Fi is connected!");
				
				//Get connected wi-fi information
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "SSID: " + wifiInfo.getSSID() +
							" BSSID: " + wifiInfo.getBSSID() + " RSSI: " + wifiInfo.getRssi() + 
							" LinkSpeed: " + wifiInfo.getLinkSpeed());
				}
				
				SharedPreferences settings = context.getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
				if (settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false)) {
					if (settings.getBoolean(Util.SHARED_PREFERENCES_POWER_CONNECTED, false)) {//power is plugged in
						//Start uploading logs
						Locks.acquireWakeLock(context);
						Intent uploadIntent = new Intent(context, UploadLogs.class);
						context.startService(uploadIntent);
					}
				} else {//no power requirements
					//Start uploading logs
					Locks.acquireWakeLock(context);
					Intent uploadIntent = new Intent(context, UploadLogs.class);
					context.startService(uploadIntent);
				}
			}
		} else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected()) {
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Wi-Fi is disconnected!");
			}
		}
	}
}

