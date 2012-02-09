/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
import edu.buffalo.cse.phonelab.phonelabservices.PeriodicCheckService;
import edu.buffalo.cse.phonelab.statusmonitor.StatusMonitor;
import edu.buffalo.cse.phonelab.utilities.DownloadFile;
import edu.buffalo.cse.phonelab.utilities.UploadFile;
import edu.buffalo.cse.phonelab.utilities.Util;

public class MessageService extends IntentService {

	public MessageService() {
		super("MessageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(getClass().getSimpleName(), "C2DM Message Service is started!");

		String payload = intent.getExtras().getString("payload");
		try {
			JSONObject jsonObject = new JSONObject(payload);
			String message = (String) jsonObject.get("message");
			if (message.equals("new_manifest")) {
				if (DownloadFile.downloadToDirectory(Util.MANIFEST_DOWNLOAD_URL, Util.NEW_MANIFEST_DIR)) {
					PhoneLabManifest newManifest = new PhoneLabManifest(Util.NEW_MANIFEST_DIR, getApplicationContext());
					if (newManifest.getManifest()) {
						PhoneLabManifest currentManifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
						if (currentManifest.getManifest()) {
							handleExistingManifest(currentManifest, newManifest);
							try {
								currentManifest.saveDocument();
							} catch (TransformerException e) {
								e.printStackTrace();
							}
						} else {
							try {
								FileOutputStream fos = openFileOutput(Util.CURRENT_MANIFEST_DIR, Context.MODE_PRIVATE);
								File newFile = new File(Util.NEW_MANIFEST_DIR);
								InputStream newInputStream = new FileInputStream(newFile);
								byte[] buf = new byte[1024];
								int len;
								while ((len = newInputStream.read(buf)) > 0){
									fos.write(buf, 0, len);
								}
								newInputStream.close();
								fos.close();
								
								Log.i(getClass().getSimpleName(), "New manifest transfered to Current manifest!");
								PhoneLabManifest currentManifest2 = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
								if (currentManifest2.getManifest()) {
									handleNewManifest(currentManifest2);
									try {
										currentManifest2.saveDocument();
									} catch (TransformerException e) {
										e.printStackTrace();
									}
								}
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					//Remove manifest from where it is downloaded
					removeFile (Util.NEW_MANIFEST_DIR);
				}
			} else if (message.equals("get_device_info")) {
				Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
				registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
				registrationIntent.putExtra("sender", Util.C2DM_EMAIL);
				startService(registrationIntent);
			} else if (message.equals("get_device_status")) {
				UploadDeviceStatus uploadDeviceStatus = new UploadDeviceStatus();
				uploadDeviceStatus.uploadDeviceStatus(getApplicationContext(), Util.DEVICE_STATUS_UPLOAD_URL + Util.getDeviceId(getApplicationContext()));
			} else if (message.equals("flash")) {

			} else if (message.equals("upload_manifest")) { //Send updated manifest to the server
				UploadFile uploadManifest = new UploadFile();
				uploadManifest.upload(getApplicationContext(), Util.MANIFEST_UPLOAD_URL, Util.CURRENT_MANIFEST_DIR);
			} else if (message.equals("uninstall_all_apps")) {
				uninstallAllApps();
			} else if (message.equals("remove_manifest")) {
				uninstallAllApps();
				if (deleteFile(Util.CURRENT_MANIFEST_DIR)) {
					Log.i(getClass().getSimpleName(), "Manifest successfully deleted");
				} else {
					Log.w(getClass().getSimpleName(), "Manifest couldn't be deleted");
				}
			} else if (message.equals("start_status_monitoring")) {
				Intent statusMonitorIntent = new Intent(this, StatusMonitor.class);
				startService(statusMonitorIntent);
			} else if (message.equals("start_periodic_checking")) {
				Intent periodicMonitorIntent = new Intent(this, PeriodicCheckService.class);
				startService(periodicMonitorIntent);
			} 
		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.w(getClass().getSimpleName(), "C2DM Message cannot be parsed to JSON object!");
		}

		Log.i(getClass().getSimpleName(), "C2DM Message Service is done!");
	}

	/**
	 * This method uninstall all the applications from both manifest and device
	 */
	private void uninstallAllApps() {
		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
		if (manifest.getManifest()) {
			try {
				ArrayList<PhoneLabApplication> applications = manifest.getAllApplications();
				for (PhoneLabApplication app:applications) {
					removeapplication(app);
					try {
						manifest.removeApplication(app.getPackageName());
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			try {
				manifest.saveDocument();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * If there is no old manifest this method will take care of the operations 
	 * @param currentManifest current Manifest
	 */
	private void handleNewManifest(PhoneLabManifest currentManifest) {
		//Handle applications
		try {
			ArrayList<PhoneLabApplication> applications = currentManifest.getApplicationsByConstraints(null);
			for (PhoneLabApplication app:applications) {
				if (app.getAction().equals("update")) {
					if (!updateApplication(app)) {
						currentManifest.removeApplication(app.getPackageName());
					}
				} else if (app.getAction().equals("uninstall")) {
					if (!removeapplication(app)) {
						currentManifest.removeApplication(app.getPackageName());
					}
				} else if (app.getAction().equals("install")) {
					if (!installApplication(app)) {
						currentManifest.removeApplication(app.getPackageName());
					}
				} 
			} 
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		//Handle Status Monitor
		try {
			ArrayList<PhoneLabParameter> parameters = currentManifest.getStatParamaterByConstraints(null);
			if (parameters.size() > 0) {
				startStatusMonitor(currentManifest);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If there is existing manifest, this method will take care of the operations by comparing with the old one
	 * @param currentManifest current manifest
	 * @param newManifest new manifest
	 */
	private void handleExistingManifest(PhoneLabManifest currentManifest, PhoneLabManifest newManifest) {
		//Handle applications
		try {
			ArrayList<PhoneLabApplication> applications = newManifest.getApplicationsByConstraints(null);
			for (PhoneLabApplication app:applications) {
				HashMap<String, String> constraints = new HashMap<String, String>();
				constraints.put("package_name", app.getPackageName());
				ArrayList<PhoneLabApplication> currentApplication = currentManifest.getApplicationsByConstraints(constraints);
				if (currentApplication != null && currentApplication.size() == 1) {//App exists
					if (app.getAction().equals("update")) {
						if (updateApplication(app)) {
							currentManifest.updateApplication(app);
						}
					} else if (app.getAction().equals("uninstall")) {
						if (removeapplication(app)) {
							currentManifest.removeApplication(app.getPackageName());
						}
					} 
				} else {//No such app exists
					if (app.getAction().equals("install")) {
						if (installApplication(app)) {
							currentManifest.addApplication(app);
						}
					}
				}
			} 
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		//Handle Status Monitor
		try {
			ArrayList<PhoneLabParameter> parameters = newManifest.getStatParamaterByConstraints(null);
			if (parameters.size() > 0) {
				for (PhoneLabParameter param:parameters) {
					HashMap<String, String> constraints = new HashMap<String, String>();
					constraints.put("name", param.getName());
					ArrayList<PhoneLabParameter> currentParameters = currentManifest.getStatParamaterByConstraints(constraints);
					if (currentParameters != null && currentParameters.size() == 1) {//Parameter exists
						currentManifest.updateStatParameter(param);
					} else {//Parameter does not exist
						currentManifest.addStatParameters(param);
					}
				}
				
				try {
					currentManifest.saveDocument();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				
				startStatusMonitor(currentManifest);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method install a new application to the phone
	 * @param app PhoneLabApplication to install
	 * @return true if successful, otherwise false
	 */
	public boolean installApplication (PhoneLabApplication app) {
		if (DownloadFile.downloadToDirectory(Util.APP_DOWNLOAD_URL + app.getDownload(), Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Installing " + app.getName() + " now...");
			try {
				Process process = Runtime.getRuntime().exec("pm install " + Environment.getExternalStorageDirectory() + "/" + app.getDownload());

				String line = null;
				//Success to install APK
				BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader buf_e = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while((line = buf_i.readLine()) != null){
					if(line.compareTo("Success") == 0){
						if (app.getType().equals("background")) {//start it in the background
							startingapplication(app);
						} else if (app.getType().equals("interactive")) {//notify user
							new Util().nofityUser(this, "PhoneLab", "" + app.getName() + " is installed on your device");
						}
						Log.i(getClass().getSimpleName(), app.getName() + " installed successfully");

						//Remove .apk from where it is downloaded
						removeFile (Environment.getExternalStorageDirectory() + "/" + app.getDownload());
						return true;
					}
				}
				buf_i.close();

				//failure to install APK
				while((line = buf_e.readLine()) != null){
					if(line.split(" ")[0].compareTo("Failure") == 0){
						Log.i(getClass().getSimpleName(), app.getName() + " couldn't be installed");
					}
				}

				buf_e.close();
				process.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Remove .apk from where it is downloaded
			removeFile (Environment.getExternalStorageDirectory() + "/" + app.getDownload());
		}

		return false;
	}
	
	private void removeFile (String path) {
		File file = new File(path);
		if (file.delete()) {
			Log.i(getClass().getSimpleName(), "File at " + path + " deleted successfully");
		} else {
			Log.w(getClass().getSimpleName(), "File at " + path + " couldn't be deleted");
		}
	}

	/**
	 * This method will update the current installed application
	 * @param app PhoneLabApplication to update
	 * @return true if successful, otherwise false
	 */
	private boolean updateApplication(PhoneLabApplication app) {
		if (DownloadFile.downloadToDirectory(Util.APP_DOWNLOAD_URL + app.getName() + ".apk", Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Updating " + app.getName() + " now...");
			try {
				Process process = Runtime.getRuntime().exec("pm -r install " + Environment.getExternalStorageDirectory() + "/" + app.getDownload());

				String line = null;
				//Success to install APK
				BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader buf_e = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while((line = buf_i.readLine()) != null){
					if(line.compareTo("Success") == 0){
						if (app.getType().equals("background")) {//start it in the background
							startingapplication(app);
						} else if (app.getType().equals("interactive")) {//notify user
							new Util().nofityUser(this, "PhoneLab", app.getName() + " is updated");
						}
						Log.i(getClass().getSimpleName(), app.getName() + " updated successfully");

						//Remove .apk from where it is downloaded
						removeFile (Environment.getExternalStorageDirectory() + "/" + app.getDownload());
						return true;
					}
				}
				buf_i.close();

				//failure to install APK
				while((line = buf_e.readLine()) != null){
					if(line.split(" ")[0].compareTo("Failure") == 0){
						Log.i(getClass().getSimpleName(), app.getName() + " couldn't be updated");
					}
				}
				buf_e.close();
				process.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Remove .apk from where it is downloaded
			removeFile (Environment.getExternalStorageDirectory() + "/" + app.getDownload());
		}

		return false;
	}

	/**
	 * This method uninstall the application
	 * @param app PhoneLabApplication to uninstall
	 * @return true if successful, otherwise false
	 */
	private boolean removeapplication(PhoneLabApplication app) {
		Log.i(getClass().getSimpleName(), "Removing " + app.getName() + " now...");
		try {
			Process process = Runtime.getRuntime().exec("pm uninstall " + app.getPackageName());
			String line = null;
			BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((line = buf_i.readLine()) != null){
				if(line.compareTo("Success") == 0){
					//Success to uninstall APK
					new Util().nofityUser(this, "PhoneLab", app.getName() + " is uninstalled");
					Log.i(getClass().getSimpleName(), app.getName() + " uninstalled successfully");
					return true;

				} else {
					//failure to uninstall APK
					Log.i(getClass().getSimpleName(), app.getName() + " couldn't be uninstalled");
				}
			}
			buf_i.close();

			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * This method starts status monitoring
	 * It is called if there is any change on the new manifest
	 * @param currentManifest 
	 */
	private void startStatusMonitor(PhoneLabManifest currentManifest) {
		Intent statusMonitorIntent = new Intent(getApplicationContext(), StatusMonitor.class);
		this.startService(statusMonitorIntent);
	}

	/**
	 * Used to send manually start intents using android.intent.action.MAIN. 
	 * @param app the phonelab Application
	 * @param dbAdapter the hook to device database          
	 * @author rishi baldawa
	 */
	private void startingapplication(PhoneLabApplication app) {
		Log.i(getClass().getSimpleName(), "Starting " + app.getName() + " now...");

		if (app.getType().equals("interactive")) {
			Intent startAppIntent = new Intent(Intent.ACTION_MAIN);
			PackageManager manager = getPackageManager();
			try {
				startAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
				startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				startActivity(startAppIntent);
			} catch( Exception e ) {
				e.printStackTrace();
				Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
			}
		} else if (app.getType().equals("background")) {
			Intent intent = new Intent(app.getIntentName());
			startService(intent);
		}

		return;
	}


	/**
	 * Used to send manually stop intents using android.intent.action.MAIN. 
	 * @param app the phonelab Application
	 * @author rishi baldawa
	 */
	@SuppressWarnings("unused")
	private void stoppingapplication(PhoneLabApplication app) {
		Log.i(getClass().getSimpleName(), "Stopping " + app.getName() + " now...");
		Intent stopAppIntent = new Intent(Intent.ACTION_MAIN);
		PackageManager manager = getPackageManager();
		try {
			stopAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
			stopAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			stopService(stopAppIntent);
		} catch( Exception e ) {
			e.printStackTrace();
			Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
		}

		return;
	}
}

