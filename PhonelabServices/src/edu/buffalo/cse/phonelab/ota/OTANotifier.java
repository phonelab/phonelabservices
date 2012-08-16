package edu.buffalo.cse.phonelab.ota;

import edu.buffalo.cse.phonelab.phonelabservices.R;
import edu.buffalo.cse.phonelab.utilities.Util;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OTANotifier extends BroadcastReceiver
{
	
	@Override
	public void onReceive(Context context, Intent intent1) 
	{
		String uri = intent1.getStringExtra(Util.DOWNLOADED_OTA_FILE_FILEPATH_URI);
		String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(ns);
        
        
        
		Intent intent = new Intent(context,
				PHLabOTAUpdateManager.class);
		intent.putExtra(Util.DOWNLOADED_OTA_FILE_FILEPATH_URI, uri);
       // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 0);
		
//		Notification notification = new Notification(R.drawable.ic_launcher,
//				"PhoneLab update available", System.currentTimeMillis());
		
		Notification notification = new Notification.Builder(context)
		.setTicker("PhoneLab update available")
        .setContentTitle("Click here to install.")
        .setContentInfo("New PhoneLab system update is available.")
        .setSmallIcon(R.drawable.phonelab_logo_black)
        .setContentIntent(activity)
        .getNotification();
		
		//notification.flags |= Notification.FLAG_INSISTENT;
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    notification.flags |= Notification.FLAG_NO_CLEAR;
		
		notification.defaults = Notification.DEFAULT_ALL;
		
//		notification.setLatestEventInfo(context, "New PhoneLab system update is available",
//				"Click to Install", activity);
		
		notification.number += 1;
		notificationManager.notify(1729, notification);//make a constant for the id
	}

}
