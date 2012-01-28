/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerOnReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(getClass().getSimpleName(), "Power is plugged in!");
		
	}
}
