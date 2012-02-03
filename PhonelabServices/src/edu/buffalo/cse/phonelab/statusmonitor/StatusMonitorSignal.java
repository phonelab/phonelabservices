/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.statusmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class StatusMonitorSignal extends Service {

	TelephonyManager mTelManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(getClass().getSimpleName(), "Learning Signal Strength");

		/*Get signal strength*/
		PhoneStateListener mSignalListener = new PhoneStateListener() {
			int mStrength;
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				try {
					mTelManager.listen(this, PhoneStateListener.LISTEN_NONE);//unregistering
					
					if (signalStrength.isGsm())
						mStrength = signalStrength.getGsmSignalStrength();
					else{
						int strength = -1;
						if (signalStrength.getEvdoDbm() < 0)
							strength = signalStrength.getEvdoDbm();
						else if (signalStrength.getCdmaDbm() < 0)
							strength = signalStrength.getCdmaDbm();

						if (strength < 0) {
							// convert to asu
							mStrength = Math.round((strength + 113f) / 2f);
						}

						Log.i(getClass().getSimpleName(), "Signal Strength: " + strength + " asu: " + mStrength);
					}  
				} catch (Exception e) {
					e.printStackTrace();
				}
				             
				StatusMonitorSignal.this.stopSelf();
			}
		};

		mTelManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		return START_STICKY;
	}

}
