package edu.buffalo.cse.phonelab.ota;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * This class is responsible for downloading OTA image(s)
 * 
 * @author ans25
 * 
 */

public class OTADownloader extends Service
{
	DownloadManager	downloadmanager;
	long			enqueueid;
	private OTADownloadCompletedReceiver	receiver;

	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	OTADownloader getService() {
            return OTADownloader.this;
        }
    }

	

    @Override
    public void onCreate() {
    	receiver = new OTADownloadCompletedReceiver();
    	registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    	
    	
		
    	
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("OTADownloader", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        
        String url = intent.getStringExtra(Util.OTA_DOWNLOAD);
        
        Log.i("PhoneLab-" + getClass().getSimpleName(),
				"Enqueing downloadrequest, The file path is: "
						+ getApplicationContext().getExternalFilesDir(null)
						+ " The download URL is " + url);

		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url))
				.setDescription("Downloading Phonelab update.")
				.setTitle("PhoneLab")
				.setDestinationInExternalFilesDir(getApplicationContext(), null, "ota.zip")
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
				.setVisibleInDownloadsUi(false)
				.setNotificationVisibility(
						DownloadManager.Request.VISIBILITY_HIDDEN);

		// get download service and enqueue file
		downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		enqueueid = downloadmanager.enqueue(request);
        
        return START_STICKY;
    }
	
	 @Override
	    public void onDestroy() {
	        unregisterReceiver(receiver);
	    }

	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }

	    // This is the object that receives interactions from clients.  See
	    // RemoteService for a more complete example.
	    private final IBinder mBinder = new LocalBinder();
	    
	    /**
	     * receiver for the download complete intent
	     * @author ans25
	     *
	     */
	private class OTADownloadCompletedReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
			{
				long downloadId = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				Query query = new Query();
				query.setFilterById(enqueueid);
				Cursor c = downloadmanager.query(query);
				if (c.moveToFirst())
				{
					int columnIndex = c
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c
							.getInt(columnIndex))
					{
						// successfully downloaded
						String uri = c
								.getString(c
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						Log.i("PhoneLab-" + getClass().getSimpleName(),
								"Successfully downloaded the OTA image, The OTA file path is "
										+ uri);
						AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
						Intent newIntent = new Intent(getApplicationContext(), OTANotifier.class);
						newIntent.putExtra(Util.DOWNLOADED_OTA_FILE_FILEPATH_URI,uri);
						PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
						mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() , pending);

					}
				}
			}
		}
	}

}
