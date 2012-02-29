/**
 * @author fatih
 */

package edu.buffalo.cse.phonelab.statusmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import edu.buffalo.cse.phonelab.utilities.Locks;

public class StatusMonitorReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Locks.acquireWakeLock(context);
		
		Intent newIntent = new Intent(context, StatusMonitor.class);
		newIntent.putExtras(intent.getExtras());
		context.startService(newIntent);
	}
}
