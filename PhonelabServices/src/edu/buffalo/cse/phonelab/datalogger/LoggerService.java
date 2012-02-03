package edu.buffalo.cse.phonelab.datalogger;

import com.loopj.android.http.*;

import edu.buffalo.cse.phonelab.database.DatabaseAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author mike
 *
 */
public class LoggerService extends Service {
	private final IBinder mBinder = new LogBinder();
	private Timer timer = new Timer();
	private final String LOG_DIR = Environment.getExternalStorageDirectory() + "/" + DataLoggerConstants.LOG_DIR + "/";
	private DatabaseAdapter db;
	
	public void onCreate() {
		super.onCreate();
		Log.d(DataLoggerConstants.TAG, "Start Service");
		db = new DatabaseAdapter(getApplicationContext());
		db.open(1);
		start();
	}
	
	
	public void start() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Log.d(DataLoggerConstants.TAG, "Service Running");
				// Check for LogCat Process
				checkLogCatProcess();
				
			}
		},0, DataLoggerConstants.UPDATE_INTERVAL);
	}
	
	/**
	 * Checks whether LogCat Process is running or not, if not initiates the process 
	 */
	private void checkLogCatProcess() {
		List<Integer> pids = getPID("logcat");
		int pidFromDb = db.getLastPID();
		Log.d(DataLoggerConstants.TAG, "Checking Logcat process ... pid from Dbase " + pidFromDb);
		boolean foundLogCat = false;
		if (pids.size() > 0) {
			for(int pid:pids) {
				// If logcat is found
				if(pidFromDb == pid) {
					Log.d(DataLoggerConstants.TAG, "Logcat process found " + pid);
					// Process Found
					if (logFileThreshold()) {
						transferLogFiles();
					}
					foundLogCat = true;
					break;
				} else {
					Log.d(DataLoggerConstants.TAG, "Killing bogus process " + pid);
					// Kill other bogus processes
					android.os.Process.killProcess(pid);
					// Start a new LogCat process
					foundLogCat = false;
				}
			}
			// Start LogCat if no process found
			if (!foundLogCat) {
				startLogcat();
			}
		} else {
			startLogcat();
		}
	}
	
	private boolean logFileThreshold() {
		File f = new File(LOG_DIR + "log.out");
		boolean threshold = f.length() < DataLoggerConstants.THRESHOLD * 1024;
		Log.d(DataLoggerConstants.TAG, "Checking Threshold ... " + threshold);
		return threshold;
	}
		
	/**
	 * Check for valid Log Files.
	 * Log.out is continously written
	 * Log.out.1
	 * Log.out.2
	 * ...
	 * Log.out.n will be created in worst case
	 * else Log.out.1 will be transferred successfully
	 * After Successful transfer, delete all files except Log.out
	 * 
	 * @param String filename - filename
	 * @return boolean
	 */
	private void transferLogFiles() {
		Log.d(DataLoggerConstants.TAG, "Starting to Merge files");
		File[] allFiles = new File(LOG_DIR).listFiles();
		String mergedFileSrc = LOG_DIR + "merged.txt";
		Log.d(DataLoggerConstants.TAG, "Files found .. " + allFiles.length);
		String line = "";
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		String deviceId = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		String url = DataLoggerConstants.POST_URL + "?deviceId=" + deviceId;
		
		// If Log file exists in the directory
		if (allFiles.length > 1) {
			// Check all files
			for(int i=0; i<allFiles.length; i++) {
				String fileName = allFiles[i].getName();
				
				// Merge files together & save as merged.txt
				if (fileName != "log.out" && fileName.startsWith("log.out.")) {
					Log.d(DataLoggerConstants.TAG, "File " + i + " " + allFiles[i].getName());
					// Merge files
					try {
						BufferedReader ip = new BufferedReader(new FileReader(allFiles[i]));
						BufferedWriter op = new BufferedWriter(new FileWriter(mergedFileSrc, true));
						
						while((line = ip.readLine()) != null) {
							op.write(line);
						}
						op.flush(); 
						op.close(); 
						ip.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Log.d(DataLoggerConstants.TAG, "Removing File " + i + " " + allFiles[i].delete());
				}
			}
			
			// Now lets send Merged File
			final File f = new File(mergedFileSrc);
			Log.d(DataLoggerConstants.TAG, "Merged File " + f.length());
			// If File is new i.e created after last uploaded time
			// Set last successful upload time			
			try {
			    params.put("file", f);
			} catch(FileNotFoundException e) {}
			client.post(url , params, new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			    	Date now = new Date();
					db.setLastUpdateTime((System.currentTimeMillis()/1000 - ((now.getMinutes() * 60  + now.getSeconds()))));
					Log.d(DataLoggerConstants.TAG, "Removing Merged File " + f.delete());
					Log.d(DataLoggerConstants.TAG, "Response " + response);
			    }
			});
		} else {
			// No File
		}
	}
	
	/**
	 * Create Log Dir if it doesn`t exist
	 * @return boolean
	 */
	private void createLogDir() {
		File f = new File(LOG_DIR);
		if (f.exists() && f.isDirectory()) {
			return;
		} else {
			f.mkdirs();
		}
	}
	
	/**
	 * Returns PIDs of process
	 * @param String processName - process name
	 * @return List<Integer> pids - 0 is not found and pid > 0 is found
	 */
	private List<Integer> getPID(String processName) {
		List<Integer> pids = new ArrayList<Integer>();
		
		StringBuilder psString = new StringBuilder();
		try {
			Process process = Runtime.getRuntime().exec("ps");// Verbose filter
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        while ((line = bufferedReader.readLine()) != null) {
	        	// check whether processName exists in line
	        	if (line.endsWith(processName)) {
	        		psString.append(line + "\n");
	        	}
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Check whether some processes were collected & Terminated is not contained
		if(psString.length() > 0 && !psString.toString().matches("Terminated")) {
			String[] splitString = psString.toString().split("\n");
			
			for(int i = 0; i<splitString.length; i++) {
				if (splitString[i].startsWith("app_")) {
					Log.d(DataLoggerConstants.TAG, splitString[i].toString());
					String[] splitPs = splitString[i].split("\\s+");
					pids.add(Integer.parseInt(splitPs[1]));
				}
			}
		}
		
	    return pids;
	}
	
	/**
	 * Create Logcat process if it doesn`t exist
	 */
	private void startLogcat() {
		//Process process;
		int pid = 0;
		try {
			Log.d(DataLoggerConstants.TAG, "Logcat process not found, starting process");
			// create log dir if it doesn`t exist
			createLogDir();
			Runtime.getRuntime().exec("logcat -v long -f " + LOG_DIR + "log.out -r " + DataLoggerConstants.LOG_FILE_SIZE + " -n " + DataLoggerConstants.AUX_LOG_FILES + " &");
			pid = getPID("logcat").iterator().next();
	        db.setLastPID(pid);
	        Log.d(DataLoggerConstants.TAG, "PID to db" + pid);
		} catch (IOException e) {
			Log.d(DataLoggerConstants.TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(DataLoggerConstants.TAG, "Kill Service");
		Log.i(DataLoggerConstants.TAG, "Timer stopped.");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class LogBinder extends Binder {
		LoggerService getService() {
			return LoggerService.this;
		}
	}
}
