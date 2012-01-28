/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.utilities;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Util {

	public static String getDeviceId (Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
}
