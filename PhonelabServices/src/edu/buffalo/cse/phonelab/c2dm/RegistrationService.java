/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import edu.buffalo.cse.phonelab.database.DatabaseAdapter;
import edu.buffalo.cse.phonelab.utilities.Util;

public class RegistrationService extends IntentService {

	private static final String URLTOUPLOAD = "http://50.19.247.145/phonelab/register.php";
	
	public RegistrationService() {
		super("RegistrationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(getClass().getSimpleName(), "C2DM Registration Service is started!");
		Bundle extras = intent.getExtras();
		String deviceId = extras.getString("device_id");
		String regId = extras.getString("reg_id");
		
		Log.i(getClass().getSimpleName(), "Registration Id: " + regId);
		Log.i(getClass().getSimpleName(), "Sending registration ID to server");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(URLTOUPLOAD);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
			nameValuePairs.add(new BasicNameValuePair("reg_id", regId));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				Log.i(getClass().getSimpleName(), line);
				DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext());
				dbAdapter.open(1);
				ContentValues values = new ContentValues();
				if (line.equals("OK")) {
					values.put("synced", 1);
				} else {
					values.put("synced", 0);
				}
				dbAdapter.update(values, 0, "device_id='" + Util.getDeviceId(getApplicationContext()) + "'");
				dbAdapter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.i(getClass().getSimpleName(), "C2DM Registration Service is done!");
	}
}
