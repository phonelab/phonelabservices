package edu.buffalo.cse.phonelab.ota;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.DownloadManager.Query;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import edu.buffalo.cse.phonelab.phonelabservices.PeriodicCheckReceiver;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * This class is responsible for downloading OTA image(s)
 * 
 * @author ans25
 * 
 */

public class OTADownloader extends IntentService
{
	DownloadManager	downloadmanager;
	long			enqueueid;

	public OTADownloader()
	{
		super("OTADownloader");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{

		OTADownloadCompletedReceiver receiver = new OTADownloadCompletedReceiver();

		registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		String url = intent.getStringExtra(Util.OTA_DOWNLOAD);
		Log.i("PhoneLab-" + getClass().getSimpleName(),
				"Enqueing downloadrequest, The file path is: "
						+ getApplicationContext().getExternalFilesDir(null)
						+ " The download URL is " + url);

		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url))
				.setDescription("Downloading Phonelab update.")
				.setTitle("PhoneLab")
				.setDestinationInExternalPublicDir(null, "ota.zip")
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
				.setVisibleInDownloadsUi(false)
				.setNotificationVisibility(
						DownloadManager.Request.VISIBILITY_HIDDEN);

		// get download service and enqueue file
		downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		enqueueid = downloadmanager.enqueue(request);

	}

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
						String path = c
								.getString(c
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						Log.i("PhoneLab-" + getClass().getSimpleName(),
								"Successfully downloaded the OTA image, The OTA file path is "
										+ path);
						AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
						Intent newIntent = new Intent(getApplicationContext(), OTANotifier.class);
						newIntent.putExtra(Util.DOWNLOADED_OTA_FILE_FILEPATH,path);
						PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
						mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() , pending);

					}
				}
			}
		}
	}

}
