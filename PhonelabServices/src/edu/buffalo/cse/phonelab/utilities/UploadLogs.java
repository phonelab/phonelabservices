/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */
package edu.buffalo.cse.phonelab.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class UploadLogs extends Service {

	private final String LOG_DIR = Environment.getExternalStorageDirectory() + "/" + Util.LOG_DIR + "/";
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Transfering log files now");
		transferFiles();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "OnStartCommand");
		return START_STICKY;
	}

	/**
	 * Method to initiate log transfers
	 */
	private void transferFiles () {
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.getName().startsWith("1");
		    }
		};
		File[] allFiles = new File(LOG_DIR).listFiles(fileFilter);
		if (allFiles != null && allFiles.length > 0) {
			String fileName = allFiles[0].getName();
			transfer(fileName);
		} else {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "No log files to transfer");
			Locks.releaseWakeLock();
			this.stopSelf();
		}
	}

	/**
	 * Perform the file transfer to the server
	 * @param fileName
	 */
	private void transfer(String fileName) {
		final File f = new File(LOG_DIR + fileName);
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		try {
			params.put("file", f);
			params.put("filename", fileName);
			client.post(Util.POST_URL + Util.getDeviceId(getApplicationContext()) + "/" , params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					if (f.delete()) {
						Log.i("PhoneLab-" + getClass().getSimpleName(), "" + f.getName() + " is deleted");
					} else {
						Log.w("PhoneLab-" + getClass().getSimpleName(), "" + f.getName() + " couldn't be deleted");
					}
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Response: " + response);
					
					try {
						JSONObject responseJ = new JSONObject(response);
						if(responseJ.getString("err").equals("")) {
							UploadLogs.this.transferFiles();//transfer another one
						} else {
							Log.e("PhoneLab-" + getClass().getSimpleName(), "Server error occured while transfering log files");
						}
					} catch (JSONException e) {
						Log.e("PhoneLab-" + getClass().getSimpleName(), "Server response cannot be parsed to JSON object");
					}
				}

				@Override
				public void onFailure(Throwable e){ 
					Log.e("PhoneLab-" + getClass().getSimpleName(), "Transfering " + f.getName() + " failed");
					Locks.releaseWakeLock();
					UploadLogs.this.stopSelf();
				}
			});
		} catch (FileNotFoundException e) {
			Log.e("PhoneLab-" + getClass().getSimpleName(), "File not found");
		} catch (Exception e) {
			Log.e("PhoneLab-" + getClass().getSimpleName(), "Some other error occured");
		}
	}
}
