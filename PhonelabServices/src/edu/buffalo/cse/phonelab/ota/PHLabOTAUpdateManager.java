package edu.buffalo.cse.phonelab.ota;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class PHLabOTAUpdateManager extends Activity
{
	private int					battery_level	= -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d("PhoneLab-"+ getClass().getSimpleName(), "Displaying the update Dialog");

		registerReceiver(this.batteryInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("A new system update from PhoneLab is available, What do you want to do?");
		builder.setCancelable(false);
		builder.setPositiveButton("Install Now", new InstallNowListener());
		builder.setNegativeButton("Later", new LaterListener());
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private BroadcastReceiver	batteryInfoReceiver	= new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0,	Intent intent)
		{
			battery_level = intent.getIntExtra("level",	0);
			arg0.unregisterReceiver(this);
		}
	};

	
	private final class LaterListener implements
			DialogInterface.OnClickListener
	{
		public void onClick(DialogInterface dialog, int which)
		{
			Toast.makeText(getApplicationContext(),
					"Postponing the update by 4 hours.", Toast.LENGTH_LONG)
					.show();
			Log.d("PhoneLab-"+ getClass().getSimpleName(), "Update rescheduled to a later time by user");
			
			rescheduleOTANotification();
			PHLabOTAUpdateManager.this.finish();
		}
	}

	private final class InstallNowListener implements
			DialogInterface.OnClickListener
	{
		

		public void onClick(DialogInterface dialog, int which)
		{
			// Check for battery status, if more than 60% or plugged in, go
			// ahead with the OTA

			Toast.makeText(getApplicationContext(),
					"Your battery level is " + battery_level, Toast.LENGTH_LONG)
					.show();

			if (battery_level > 59) //should be configurable
			{
				// Install the update
				Toast.makeText(getApplicationContext(), "Installing update ",
						Toast.LENGTH_LONG).show();
				
				File packageFile = new File(
						Environment.getDownloadCacheDirectory()
								+ "/ota.zip");
				Log.d("PhoneLab-"+ getClass().getSimpleName(), "The OTA file path is "+ packageFile.getAbsolutePath());
				
				if(packageFile.exists())
				{
				
					try
					{
						RecoverySystem.installPackage(
								getApplicationContext(), packageFile);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				else// file does not exist, try to download the file to complete the update
				{
					Log.wtf("PhoneLab-"+ getClass().getSimpleName(), "OTA file not found");
					//download now if wifi plugged in and battery > 70%, also implement progress bar 
									
					//check for wifi connection, if not connected prompt user, else reschedule the update

					// Use this to download the file again
//					DownloadFile downloadFile = new DownloadFile();
//					downloadFile.execute(Util.OTA_DOWNLOAD_URL + "ota.zip");					
					
				}

			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PHLabOTAUpdateManager.this);
				builder.setMessage(
						"Your battery level is too low to complete the update. Please charge your phone.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,
											int id)
									{
										
										rescheduleOTANotification();
										PHLabOTAUpdateManager.this.finish();										
									}
								});
				AlertDialog alert = builder.create();
		    	alert.show();
			}

		}
	}
	
	
	
	private void rescheduleOTANotification(){
		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent newIntent = new Intent(getApplicationContext(), OTANotifier.class);
		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, 0);
		long scheduletime = 1000*60*4*60;//4 hours
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + scheduletime, pending);
	}
	
	
	
	

}
