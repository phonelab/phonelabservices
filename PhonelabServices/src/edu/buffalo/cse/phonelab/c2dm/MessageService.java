/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


import edu.buffalo.cse.phonelab.database.DatabaseAdapter;
import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
import edu.buffalo.cse.phonelab.statusmonitor.StatusMonitor;
import edu.buffalo.cse.phonelab.utilities.DownloadFile;
import edu.buffalo.cse.phonelab.utilities.UploadFile;

public class MessageService extends IntentService {

	private static final String MANIFEST_DOWNLOAD_URL = "http://50.19.247.145/phonelab/server/manifest.xml";//url to download manifest
	private static final String MANIFEST_DIR = "/sdcard/manifest.xml";//directory to download manifest
	private static final String APP_DOWNLOAD_URL = "http://50.19.247.145/phonelab/server/";//base url to download application 
	private static final String MANIFEST_UPLOAD_URL = "http://50.19.247.145/phonelab/upload_manifest.php";
	private static final String DEVICE_STATUS_UPLOAD_URL = "http://50.19.247.145/phonelab/device_status_upload.php";
	
	public MessageService() {
		super("MessageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(getClass().getSimpleName(), "C2DM Message Service is started!");

		String message = intent.getExtras().getString("message");
		if (message.equals("new_manifest")) {
			if (DownloadFile.downloadToDirectory(MANIFEST_DOWNLOAD_URL, MANIFEST_DIR)) {
				DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext());
				dbAdapter.open(1);
				try {
					PhoneLabManifest manifest = new PhoneLabManifest(MANIFEST_DIR);
					//Handle applications
					try {
						ArrayList<PhoneLabApplication> applications = manifest.getApplicationsByConstraints(null);
						for (PhoneLabApplication app:applications) {
							Cursor cursor = dbAdapter.selectEntry("package_name='" + app.getPackageName() + "'", 1, null, null, null, null);
							if (cursor.moveToFirst()) {//App exists
								if (app.getAction().equals("update")) {
									updateApplication(app, dbAdapter);
								} else if (app.getAction().equals("uninstall")) {
									removeapplication(app, dbAdapter);
								} 
							} else {//No such app exists
								if (app.getAction().equals("install")) {
									installApplication(app, dbAdapter);
								}
							}
							cursor.close();
						} 
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
					//Handle Status Monitor
					try {
						ArrayList<PhoneLabParameter> parameters = manifest.getStatParamaterByConstraints(null);
						if (parameters.size() > 0) {
							for (PhoneLabParameter param:parameters) {
								Cursor cursor = dbAdapter.selectEntry("name='" + param.getName() + "'", 2, null, null, null, null);
								if (cursor.moveToFirst()) {//Parameter exists
									ContentValues values = new ContentValues();
									values.put("units", param.getUnits());
									values.put("value", param.getValue());
									values.put("set_by", param.getSetBy());
									dbAdapter.update(values, 2, "name='" + param.getName() + "'");
								} else {//Parameter does not exist
									ContentValues values = new ContentValues();
									values.put("name", param.getName());
									values.put("units", param.getUnits());
									values.put("value", param.getValue());
									values.put("set_by", param.getSetBy());
									dbAdapter.insertEntry(values, 2);
								}
								cursor.close();
							}
							startStatusMonitor(dbAdapter);
						}
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Log.i(getClass().getSimpleName(), dbAdapter.toString()); 
				dbAdapter.close();
			}
		} else if (message.equals("get_device_info")) {
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
			registrationIntent.putExtra("sender", "phonelab.at.buffalo@gmail.com");
			startService(registrationIntent);
		} else if (message.equals("get_device_status")) {
			UploadDeviceStatus uploadDeviceStatus = new UploadDeviceStatus();
			uploadDeviceStatus.uploadDeviceStatus(getApplicationContext(), DEVICE_STATUS_UPLOAD_URL);
		} else if (message.equals("flash")) {
			
		} else if (message.equals("upload_manifest")) { 
			//Send updated manifest to the server
			UploadFile uploadManifest = new UploadFile();
			uploadManifest.upload(getApplicationContext(), MANIFEST_UPLOAD_URL, "manifest.xml");
		}

		Log.i(getClass().getSimpleName(), "C2DM Message Service is done!");
	}
	
	public void installApplication (PhoneLabApplication app, DatabaseAdapter dbAdapter) {
		if (DownloadFile.downloadToDirectory(APP_DOWNLOAD_URL + app.getDownload(), Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Installing " + app.getName() + " now...");
			
			String fileName = Environment.getExternalStorageDirectory() + "/" + app.getDownload();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
			startActivity(intent);
			
			//After installed
			ContentValues values = new ContentValues();
			values.put("package_name", app.getPackageName());
			values.put("name", app.getName());
			values.put("description", app.getDescription());
			values.put("type", app.getType());
			values.put("start_time", app.getStartTime());
			values.put("end_time", app.getEndTime());
			values.put("download", app.getDownload());
			values.put("version", app.getVersion());
			values.put("action", app.getAction());
			dbAdapter.insertEntry(values, 1);
			
			if (app.getType().equals("background")) {//start it in the background
				//startingapplication(app, dbAdapter);
			} else if (app.getType().equals("interactive")) {//notify user
				
			}
		}
	}
	
	private void updateApplication(PhoneLabApplication app, DatabaseAdapter dbAdapter) {
		if (DownloadFile.downloadToDirectory(APP_DOWNLOAD_URL + app.getName() + ".apk", Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Updating " + app.getName() + " now...");
			//Update here...
			String fileName = Environment.getExternalStorageDirectory() + "/" + app.getDownload();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
			startActivity(intent);
			
			//Update local database here
			ContentValues values = new ContentValues();
			values.put("name", app.getName());
			values.put("description", app.getDescription());
			values.put("type", app.getType());
			values.put("start_time", app.getStartTime());
			values.put("end_time", app.getEndTime());
			values.put("download", app.getDownload());
			values.put("version", app.getVersion());
			values.put("action", app.getAction());
			dbAdapter.update(values, 1, "package_name='" + app.getPackageName() + "'");
		}
	}
	
	private void removeapplication(PhoneLabApplication app, DatabaseAdapter dbAdapter) {
		Log.i(getClass().getSimpleName(), "Removing " + app.getName() + " now...");
		//Remove here
		Uri packageURI = Uri.parse("package:" + app.getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(uninstallIntent);
        //Update database here 
		dbAdapter.deleteEntry("package_name='" + app.getPackageName() + "'", 1);
	}

	private void startStatusMonitor(DatabaseAdapter dbAdapter) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Cursor cursor = dbAdapter.selectEntry("name='runInterval'", 2, null, null, null, null);
		if (cursor.moveToFirst()) {
			Intent statusMonitorIntent = new Intent(getApplicationContext(), StatusMonitor.class);
			statusMonitorIntent.putExtra("units", cursor.getString(cursor.getColumnIndex("units")));
			statusMonitorIntent.putExtra("value", cursor.getString(cursor.getColumnIndex("value")));
			this.startService(statusMonitorIntent);
		}
	}
	
	/**
	 * Used to send manually start intents using android.intent.action.MAIN. 
	 * 
	 * @param app the phonelab Application
	 * @param dbAdapter the hook to device database
	 *            
	 * @author rishi baldawa
	 */
	private void startingapplication(PhoneLabApplication app, DatabaseAdapter dbAdapter) {
		//Runtime.getRuntime().exec("am start -a andriod.intent.action.MAIN -n  " + intentName);
		
		Log.i(getClass().getSimpleName(), "Starting " + app.getName() + " now...");
		
		//Running the App
		Intent startAppIntent = new Intent(Intent.ACTION_MAIN);
		PackageManager manager = getPackageManager();
		try{
			startAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
		} catch( Exception e ) {
			e.printStackTrace();
			Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
		}
		startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		startActivity(startAppIntent);
		
		
		//TODO notify database ??
		
		return;
	}
	
	
	/**
	 * Used to send manually stop intents using android.intent.action.MAIN. 
	 * 
	 * 
	 * @param app 
	 * 				the phonelab Application
	 * @param dbAdapter 
	 * 				the hook to device database
	 *            
	 * @author rishi baldawa
	 */
	private void stoppingapplication(PhoneLabApplication app, DatabaseAdapter dbAdapter) {
		//Runtime.getRuntime().exec("am start -a andriod.intent.action.MAIN -n  " + intentName);
		
		Log.i(getClass().getSimpleName(), "Stopping " + app.getName() + " now...");
		
		//Running the App
		Intent stopAppIntent = new Intent(Intent.ACTION_MAIN);
		PackageManager manager = getPackageManager();
		try{
			stopAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
		} catch( Exception e ) {
			e.printStackTrace();
			Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
		}
		stopAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		stopService(stopAppIntent);
		
		//TODO notify database ??
		
		return;
	}
}

