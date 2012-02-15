package edu.buffalo.cse.phonelab.datalogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Util;

class LoggerAsyncPusher extends AsyncTask<File, Integer, Long> {

	Context context;
	private final String LOG_DIR = Environment.getExternalStorageDirectory()
			+ "/" + Util.LOG_DIR + "/";
	private Editor editor;

	public LoggerAsyncPusher(Context logContext, Editor logEditor) {
		context = logContext;
		editor = logEditor;
	}

	@Override
	protected Long doInBackground(File... allFiles) {
		long totalSize = 0; // size of file uploaded
		String mergedFileSrc = LOG_DIR + "merged.txt";
		String line = "";

		if (allFiles.length < 1) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "No Log file exist");
			return totalSize; // do nothing
		}

		for (int j = 0; j < allFiles.length; j++) {
			String fileName = allFiles[j].getName();

			// Merge files together & save as merged.txt
			if (fileName != "log.out" && fileName.startsWith("log.out.")) {
				Log.i("PhoneLab-" + getClass().getSimpleName(), "File " + j + " " + allFiles[j].getName());
				// Merge files
				try {
					BufferedReader ip = new BufferedReader(new FileReader(allFiles[j]));
					BufferedWriter op = new BufferedWriter(new FileWriter(mergedFileSrc, true));

					while ((line = ip.readLine()) != null) {
						op.write(line);
						op.newLine();
					}
					op.flush();
					op.close();
					ip.close();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("PhoneLab-" + getClass().getSimpleName(),
							e.getMessage());
				}
				Log.i("PhoneLab-" + getClass().getSimpleName(),
						"Removing File " + j + " " + allFiles[j].delete());
			}
		}

		// Now lets send Merged File
		final File file = new File(mergedFileSrc);
		Log.i("PhoneLab-" + getClass().getSimpleName(),
				"Merged File " + file.length());
		// If File is new i.e created after last uploaded time
		// Set last successful upload time
		try {

			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStream.read(bytes);
			fileInputStream.close();

			URL url = new URL(Util.POST_URL + Util.getDeviceId(context.getApplicationContext()) + "/");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			OutputStream outputStream = connection.getOutputStream();

			int bufferLength = 1024;
			for (int i = 0; i < bytes.length; i += bufferLength) {
				// int progress = (int) ((i / (float) bytes.length) * 100);
				// publishProgress(progress);
				if (bytes.length - i >= bufferLength) {
					outputStream.write(bytes, i, bufferLength);
					totalSize += bufferLength;
				} else {
					outputStream.write(bytes, i, bytes.length - i);
					totalSize += (bytes.length - i);
				}
			}
			// publishProgress(100);

			outputStream.close();
			outputStream.flush();

			InputStream inputStream = connection.getInputStream();
			// read the response
			String response ="";
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder total = new StringBuilder(inputStream.available());
			while ((response = r.readLine()) != null) {
				total.append(response);
			}
			response = total.toString();
			inputStream.close();

			// TODO make sure of successful merge
			Date now = new Date();
			editor.putLong(Util.SHARED_PREFERENCES_DATA_LOGGER_LAST_UPDATE_TIME,
					(System.currentTimeMillis() / 1000 - ((now.getMinutes() * 60 + now.getSeconds()))));
			editor.commit();
			Log.i("PhoneLab-" + getClass().getSimpleName(),	"Removing Merged File " + file.delete());
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Response " + response);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalSize;
	}
	/* ***
	 * Unimplemented Methods. Can be used to show/get progress. ***
	 * 
	 * @Override protected void onProgressUpdate(Integer... progress) { }
	 * 
	 * @Override protected void onPostExecute(Void result) { }
	 * 
	 * @Override protected void onPreExecute() { }
	 */

}
