package edu.buffalo.cse.phonelab.ota;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;

import edu.buffalo.cse.phonelab.utilities.Util;

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
	private String              ota_path_uri;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Intent intent= getIntent();
		ota_path_uri = intent.getStringExtra(Util.DOWNLOADED_OTA_FILE_FILEPATH_URI);
		
		
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
				
				
				
				//copy the file to download cache directory
				 FileChannel source = null;
				    FileChannel destination = null;
				    try {
				    	File downloadedpackageFile = new File(new URI(ota_path_uri));
				        source = new FileInputStream(downloadedpackageFile).getChannel();
				        destination = new FileOutputStream(packageFile).getChannel();

				        // previous code: destination.transferFrom(source, 0, source.size());
				        // to avoid infinite loops, should be:
				        long count = 0;
				        long size = source.size();              
				        while((count += destination.transferFrom(source, count, size-count))<size);
				    }
				    catch(Exception e){
				    	e.printStackTrace();
				    }
				    finally {
				        if(source != null) {
				            try
							{
								source.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
				        }
				        if(destination != null) {
				            try
							{
								destination.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
				        }
				    }
				
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
				else
				{
					Log.wtf("PhoneLab-"+ getClass().getSimpleName(), "OTA file not found");				
					
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
