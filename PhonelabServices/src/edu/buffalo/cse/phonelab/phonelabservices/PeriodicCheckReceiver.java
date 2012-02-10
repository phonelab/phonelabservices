/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */

package edu.buffalo.cse.phonelab.phonelabservices;

import edu.buffalo.cse.phonelab.utilities.Locks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PeriodicCheckReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Locks.acquireWakeLock(context);
		
		Intent periodicServiceIntent = new Intent(context, PeriodicCheckService.class);
		context.startService(periodicServiceIntent);
	}
}
