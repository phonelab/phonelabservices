/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */

package edu.buffalo.cse.phonelab.phonelabservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PeriodicCheckReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent periodicServiceIntent = new Intent(context, PeriodicCheckService.class);
		context.startService(periodicServiceIntent);
	}
}
