/**
 * @author Vnu
 *
 */
package edu.buffalo.cse.phonelab.statusmonitor;

import java.util.Timer;
import java.util.TimerTask;

import edu.buffalo.cse.phonelab.utilities.Locks;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;

public class StatusMonitorCellLocation extends Service {

	Timer timer;
	TelephonyManager mTelManager;
	int mPhoneType;
	String TAG = "PhoneLab-" + getClass().getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Learning Cell Location");

		Locks.acquireWakeLock(this);

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				Log.i(TAG, "Couldn't learn Cell location");
				Locks.releaseWakeLock();
				StatusMonitorCellLocation.this.stopSelf();
			}
		}, 60000 * 1);

		mTelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneType = mTelManager.getPhoneType(); // Retrieving CDMA or GSM PhoneType
		String country = mTelManager.getNetworkCountryIso();
		String NetworkOperator = mTelManager.getNetworkOperator();
		String Operator = mTelManager.getNetworkOperatorName();
		int NetworkType = mTelManager.getNetworkType();

		// Checking for CDMA phone type
		if (mPhoneType == TelephonyManager.PHONE_TYPE_CDMA) {
			try {
				timer.cancel();
				CdmaCellLocation CDMALocation = (CdmaCellLocation) mTelManager
						.getCellLocation();
				Log.i("PhoneLab-" + getClass().getSimpleName(),
						"Retrieving CDMA Cell Location");

				// Retrieve information Related to CDMACellLocation
				int baseStationID = CDMALocation.getBaseStationId();
				int baseStationLatitude = CDMALocation.getBaseStationLatitude();
				int baseStationLongitude = CDMALocation.getBaseStationLongitude();
				int networkID = CDMALocation.getNetworkId();
				int systemID = CDMALocation.getSystemId();

				String Network = "Country: " + country + " MCC+MNC: "+ NetworkOperator + " Operator: " + Operator+ " NetworkType: " + String.valueOf(NetworkType);
				String cellLocation = " BaseStationID: "+ String.valueOf(baseStationID)+ " BaseStation_Latitude: "+ String.valueOf(baseStationLatitude)+
				" BaseStation_Longitude: "+ String.valueOf(baseStationLongitude) + " NetworkID: "+ String.valueOf(networkID) + " SystemID: "+ String.valueOf(systemID);
				Log.i(TAG, Network + cellLocation);
				
				Locks.releaseWakeLock();
				StatusMonitorCellLocation.this.stopSelf();
			
			
			}catch (Exception e) {
				Log.i(TAG, e.toString());
				timer.cancel();
				Locks.releaseWakeLock();
				stopSelf();
			}

		}
		else{
			Log.i(TAG,"Phone type not recognized");
		}

		return START_STICKY;
	}

}
