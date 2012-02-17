/**
 * @author fatih
 * Good for uploading small files like manifest, but not good for large files. Need to be reviewed (fatih).
 */


package edu.buffalo.cse.phonelab.utilities;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class UploadFile {

	private static final String Tag = "com.phonelab.controller";

	public boolean upload(Context context, String uploadURL, String fileDir) {		
		
		try {
			File dir = Environment.getExternalStorageDirectory();
			File file = new File(dir, fileDir);
			FileInputStream fis = new FileInputStream(file);// context.openFileInput(fileDir);
			HttpFileUploader htfu = new HttpFileUploader(uploadURL, "noparamshere", fileDir);
			if (htfu.doStart(fis))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
		}
		
		return false;
	}

	public class HttpFileUploader {
		URL connectURL;
		String params;
		String responseString;
		String fileName;
		byte[] dataToServer;
		FileInputStream fileInputStream = null;

		HttpFileUploader(String urlString, String params, String fileName ) {
			try {
				connectURL = new URL(urlString);
			} catch(Exception ex) {
				Log.i("PhoneLab-" + Tag,"MALFORMATED URL");
			}
			this.params = params+"=";
			this.fileName = fileName;
		}

		boolean doStart(FileInputStream stream) { 
			fileInputStream = stream;
			return uploadNow();
		} 

		boolean uploadNow(){
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			try {
				Log.i("PhoneLab-" + Tag,"Uploading now...");
				HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();// Open a HTTP connection to the URL
				conn.setDoInput(true);// Allow Inputs
				conn.setDoOutput(true);// Allow Outputs
				conn.setUseCaches(false);// Don't use a cached copy
				conn.setRequestMethod("POST");// Use a post method.
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

				DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
				dos.writeBytes(lineEnd);

				Log.i("PhoneLab-" + Tag,"Headers are written");

				// create a buffer of maximum size
				int bytesAvailable = fileInputStream.available();
				int maxBufferSize = 1024;
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				byte[] buffer = new byte[bufferSize];

				// read file and write it into form...
				int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bufferSize = Math.min(fileInputStream.available(), maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data neccesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// close streams
				Log.i("PhoneLab-" + Tag,"File is written");
				fileInputStream.close();
				dos.flush();

				InputStream is = conn.getInputStream();
				// retrieve the response from server
				int ch;
				StringBuffer b = new StringBuffer();
				while( ( ch = is.read() ) != -1 ) {
					b.append( (char)ch );
				}

				String s=b.toString(); 
				Log.i("PhoneLab-" + "Response",s);
				dos.close();
				
				return true;
			} catch (MalformedURLException ex) {
				Log.e("PhoneLab-" + Tag, "error: " + ex.getMessage(), ex);
			} catch (IOException ioe) {
				Log.e("PhoneLab-" + Tag, "error: " + ioe.getMessage(), ioe);
			}
			
			return false;
		}
	}
}
