/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
import edu.buffalo.cse.phonelab.statusmonitor.StatusMonitor;
import edu.buffalo.cse.phonelab.utilities.DownloadFile;
import edu.buffalo.cse.phonelab.utilities.UploadFile;
import edu.buffalo.cse.phonelab.utilities.Util;

public class MessageService extends IntentService {

	private static final String MANIFEST_DOWNLOAD_URL = "http://50.19.247.145/phonelab/server/new_manifest.xml";//url to download manifest
	private static final String NEW_MANIFEST_DIR = "/sdcard/new_manifest.xml";//directory to download new manifest
	private static final String CURRENT_MANIFEST_DIR = "/sdcard/manifest.xml";//directory for current, latest manifest 
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
			if (DownloadFile.downloadToDirectory(MANIFEST_DOWNLOAD_URL, NEW_MANIFEST_DIR)) {
				PhoneLabManifest newManifest = new PhoneLabManifest(NEW_MANIFEST_DIR);
				if (newManifest.getManifest()) {
					PhoneLabManifest currentManifest = new PhoneLabManifest(CURRENT_MANIFEST_DIR);
					if (currentManifest.getManifest()) {
						handleExistingManifest(currentManifest, newManifest);
						try {
							currentManifest.saveDocument();
						} catch (TransformerException e) {
							e.printStackTrace();
						}
					} else {
						File newFile = new File(NEW_MANIFEST_DIR);
						if (newFile.renameTo(new File(CURRENT_MANIFEST_DIR))) {
							Log.i(getClass().getSimpleName(), "Renaming successful!");
							PhoneLabManifest currentManifest2 = new PhoneLabManifest(CURRENT_MANIFEST_DIR);
							if (currentManifest2.getManifest()) {
								handleNewManifest(currentManifest2);
								try {
									currentManifest2.saveDocument();
								} catch (TransformerException e) {
									e.printStackTrace();
								}
							}
						} else {
							Log.w(getClass().getSimpleName(), "Renaming falied!");
						}
					}
				}

				File file = new File(NEW_MANIFEST_DIR);
				if (file.delete()) {
					Log.i(getClass().getSimpleName(), "New manifest successfully deleted");
				} else {
					Log.w(getClass().getSimpleName(), "New manifest cannot be deleted");
				}
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

		} else if (message.equals("upload_manifest")) { //Send updated manifest to the server
			UploadFile uploadManifest = new UploadFile();
			uploadManifest.upload(getApplicationContext(), MANIFEST_UPLOAD_URL, "manifest.xml");
		} else if (message.equals("uninstall_all_apps")) {
			uninstallAllApps();
		} else if (message.equals("remove_manifest")) {
			uninstallAllApps();
			File file = new File(CURRENT_MANIFEST_DIR);
			if (file.delete()) {
				Log.i(getClass().getSimpleName(), "Downloaded Manifest successfully deleted");
			} else {
				Log.w(getClass().getSimpleName(), "Downloaded Manifest couldn't be deleted");
			}
		}

		Log.i(getClass().getSimpleName(), "C2DM Message Service is done!");
	}

	/**
	 * This method uninstall all the applications from both manifest and device
	 */
	private void uninstallAllApps() {
		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR);
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
		if (DownloadFile.downloadToDirectory(APP_DOWNLOAD_URL + app.getDownload(), Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Installing " + app.getName() + " now...");
			try {
				Process process = Runtime.getRuntime().exec("pm install " + Environment.getExternalStorageDirectory() + "/" + app.getDownload());
				int statusCode = process.waitFor();
				Log.i(getClass().getSimpleName(), "Status Code: " + statusCode);
				if (statusCode == 0) {
					if (app.getType().equals("background")) {//start it in the background
						//startingapplication(app);
					} else if (app.getType().equals("interactive")) {//notify user
						new Util().nofityUser(this, "PhoneLab-New Application", "" + app.getName() + " is installed on your device");
					}

					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Remove .apk from where it is downloaded
			File file = new File(Environment.getExternalStorageDirectory() + "/" + app.getDownload());
			if (file.delete()) {
				Log.i(getClass().getSimpleName(), "Downloaded .apk file for " + app.getName() + " deleted successfully");
			} else {
				Log.w(getClass().getSimpleName(), "Downloaded .apk file for " + app.getName() + " couldn't be deleted");
			}
		}

		return false;
	}

	/**
	 * This method will update the current installed application
	 * @param app PhoneLabApplication to update
	 * @return true if successful, otherwise false
	 */
	private boolean updateApplication(PhoneLabApplication app) {
		if (DownloadFile.downloadToDirectory(APP_DOWNLOAD_URL + app.getName() + ".apk", Environment.getExternalStorageDirectory() + "/" + app.getDownload())) {
			Log.i(getClass().getSimpleName(), "Updating " + app.getName() + " now...");
			try {
				Runtime.getRuntime().exec("pm install " + Environment.getExternalStorageDirectory() + "/" + app.getDownload());
				if (app.getType().equals("background")) {//start it in the background
					//startingapplication(app);
				} else if (app.getType().equals("interactive")) {//notify user
					new Util().nofityUser(this, "PhoneLab-", app.getName() + " is updated");
				}

				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Remove .apk from where it is downloaded
			File file = new File(Environment.getExternalStorageDirectory() + "/" + app.getDownload());
			if (file.delete()) {
				Log.i(getClass().getSimpleName(), "Downloaded .apk file for " + app.getName() + " deleted successfully");
			} else {
				Log.w(getClass().getSimpleName(), "Downloaded .apk file for " + app.getName() + " couldn't be deleted");
			}
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
			int statusCode = process.waitFor();
			Log.i(getClass().getSimpleName(), "Status Code: " + statusCode);

			if (statusCode == 0) {
				//Update database here 
				new Util().nofityUser(this, "PhoneLab-", app.getName() + " is uninstalled");

				return true;
			}
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
		HashMap<String, String> constraintMap = new HashMap<String, String>();
		constraintMap.put("name", "runInterval");
		try {
			ArrayList<PhoneLabParameter> parameters = currentManifest.getStatParamaterByConstraints(constraintMap);
			if (parameters != null && parameters.size() == 1) {
				PhoneLabParameter param = parameters.get(0);
				Intent statusMonitorIntent = new Intent(getApplicationContext(), StatusMonitor.class);
				statusMonitorIntent.putExtra("units", param.getUnits());
				statusMonitorIntent.putExtra("value", param.getValue());
				this.startService(statusMonitorIntent);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to send manually start intents using android.intent.action.MAIN. 
	 * @param app the phonelab Application
	 * @param dbAdapter the hook to device database          
	 * @author rishi baldawa
	 */
	private void startingapplication(PhoneLabApplication app) {
		Log.i(getClass().getSimpleName(), "Starting " + app.getName() + " now...");
		Intent startAppIntent = new Intent(Intent.ACTION_MAIN);
		PackageManager manager = getPackageManager();
		try {
			startAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
		} catch( Exception e ) {
			e.printStackTrace();
			Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
		}

		startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		startActivity(startAppIntent);

		return;
	}


	/**
	 * Used to send manually stop intents using android.intent.action.MAIN. 
	 * @param app the phonelab Application
	 * @author rishi baldawa
	 */
	/*private void stoppingapplication(PhoneLabApplication app) {
		Log.i(getClass().getSimpleName(), "Stopping " + app.getName() + " now...");
		Intent stopAppIntent = new Intent(Intent.ACTION_MAIN);
		PackageManager manager = getPackageManager();
		try {
			stopAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
		} catch( Exception e ) {
			e.printStackTrace();
			Log.w(getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
		}

		stopAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		stopService(stopAppIntent);

		return;
	}*/
}

