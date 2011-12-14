/**
 * 
 */
package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author stevko
 *
 */
public class StatusMonitor extends Service {

	/**
	 * 
	 */
	public StatusMonitor() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
