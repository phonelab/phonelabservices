
package edu.buffalo.cse.phonelab.utilities;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

public class Locks {
	private static String TAG = "phonelab-Locks";
	private static final String LOCK_NAME_WAKE="edu.buffalo.cse.phonelab.utilites.Locks";
	private static final String LOCK_NAME_WIFI="edu.buffalo.cse.phonelab.utilites.Locks";
	private static PowerManager.WakeLock lockWake = null;
	private static WifiManager.WifiLock lockWifi = null;
	
	public static void acquireWakeLock(Context context) {
		Log.i("PhoneLab-" + TAG, "wake lock acquired");
		getWakeLock(context).acquire();
	}
	
	public static void acquireWiFiLock(Context context) {
		Log.i("PhoneLab-" + TAG, "wifi lock acquired");
		getWiFiLock(context).acquire();
	}

	synchronized private static PowerManager.WakeLock getWakeLock(Context context) {
		if (lockWake == null) {
			PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			lockWake = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_WAKE);
			lockWake.setReferenceCounted(true);
		}
		return lockWake;
	}

	synchronized public static void releaseWakeLock() {
		if (lockWake != null) {
			Log.i("PhoneLab-" + TAG, "wake lock released");
			try {
				lockWake.release();
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("PhoneLab-" + Locks.class.getClass().getSimpleName(), e.getMessage());
			}
		}
	}
	
	synchronized private static WifiManager.WifiLock getWiFiLock(Context context) {
		if (lockWifi == null) {
			WifiManager mgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			lockWifi = mgr.createWifiLock(WifiManager.WIFI_MODE_FULL, LOCK_NAME_WIFI);
			lockWifi.setReferenceCounted(true);
		}
		return lockWifi;
	}

	synchronized public static void releaseWiFiLock() {
		if (lockWifi != null) {
			Log.i("PhoneLab-" + TAG, "wifi lock released");
			try {
				lockWifi.release();
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("PhoneLab-" + Locks.class.getClass().getSimpleName(), e.getMessage());
			}
		}
	}
}
