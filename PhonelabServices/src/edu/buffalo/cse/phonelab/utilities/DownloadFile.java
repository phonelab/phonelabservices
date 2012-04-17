/**
 * @author fatih
 * 
 */
package edu.buffalo.cse.phonelab.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

public class DownloadFile {

	/**
	 * Downlad any file to any directory
	 * @param webUrl url to download
	 * @param fileFullPath path to put downloaded file
	 * @return true if successful, otherwise error
	 */
	public static boolean downloadToDirectory(Context context, String webUrl, String fileFullPath) {
		try {
			Log.i("URLs", "webUrl: " + webUrl + "\n Path: " + fileFullPath);
			int BUFFER_SIZE = 1024;
			URL url = new URL(webUrl);
			long startTime = System.currentTimeMillis();
			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
			FileOutputStream fos;
			if (fileFullPath.equals("new_manifest.xml")) {
				fos = context.openFileOutput(fileFullPath, Context.MODE_PRIVATE);
			} else {
				File file = new File(fileFullPath);
				fos = new FileOutputStream(file);
			}
			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			byte[] buf = new byte[BUFFER_SIZE];
			double counter = 0;
			int current = 0;
			while (current != -1) {
				counter += current/1024;
				fos.write(buf, 0, current);
				current = bis.read(buf, 0, BUFFER_SIZE);
				//Log.i("PhoneLab-" + "DownloadFile", counter + " bytes downloaded");
			}
			fos.close();
			Log.i("PhoneLab-" + "DownloadFile", "Download completed in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

			return true;
		} catch (IOException e) {
			Log.e("PhoneLab-" + "DownloadFile", "Download Failed! Error: " + e.toString());
		}

		return false;
	}
}
