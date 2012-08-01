/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Locks;
import edu.buffalo.cse.phonelab.utilities.Util;

public class RegistrationService extends IntentService {

	
	public RegistrationService() {
		super("RegistrationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Registration Service is started!");
		Bundle extras = intent.getExtras();
		String deviceId = extras.getString("device_id");
		String regId = extras.getString("reg_id");
		//getting the devices's phone number
		TelephonyManager tMgr =(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String num = tMgr.getLine1Number();
		if(num == null){
			num = "0";
		}
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Registration Id: " + regId);
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Phone Number: " + num);
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Sending registration ID to server");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(Util.URLTOUPLOAD); 
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
			nameValuePairs.add(new BasicNameValuePair("reg_id", regId));
			nameValuePairs.add(new BasicNameValuePair("phone_no", num));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			String response = client.execute(post, responseHandler);
			Log.i("PhoneLab-" + getClass().getSimpleName(), response);
			
			JSONObject responseJ = new JSONObject(response);
			SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
			Editor editor = settings.edit();
			if(responseJ.getString("error").equals("")) {
				editor.putBoolean(Util.SHARED_PREFERENCES_SYNC_KEY, true);
			} else {
				editor.putBoolean(Util.SHARED_PREFERENCES_SYNC_KEY, false);
				Log.e("PhoneLab-" + getClass().getSimpleName(), "Shared Preferences SYNC KEY Response Error");
			}
			//now commit changes to shared preferences
			if (editor.commit()) {
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Shared Preferences Settings updated successfully");
			} else {
				Log.e("PhoneLab-" + getClass().getSimpleName(), "Shared Preferences Settings couldn't be updated");
			}
		} catch (IOException e) {
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
		} catch (JSONException e) {
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
		}
		
		Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Registration Service is done!");
		
		Locks.releaseWakeLock();
	}
}
