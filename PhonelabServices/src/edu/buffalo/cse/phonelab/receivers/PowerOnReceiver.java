/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.receivers;


import edu.buffalo.cse.phonelab.phonelabservices.PeriodicCheckService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerOnReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Power is plugged in!");
		
		Intent periodicServiceIntent = new Intent(context, PeriodicCheckService.class);
		context.startService(periodicServiceIntent);
	}
}
