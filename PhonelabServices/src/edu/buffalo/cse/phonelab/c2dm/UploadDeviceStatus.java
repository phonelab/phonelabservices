/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.c2dm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import edu.buffalo.cse.phonelab.database.DatabaseAdapter;
import edu.buffalo.cse.phonelab.utilities.Util;

public class UploadDeviceStatus {
	public void uploadDeviceStatus(Context context, String urlToUpload) {
		DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
		dbAdapter.open(1);

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(urlToUpload);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			String response = "";
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			HashMap<String, JSONArray> map;
			JSONObject jsonObj;
			try {
				nameValuePairs.add(new BasicNameValuePair("device_id", Util.getDeviceId(context)));
				Cursor cursor = dbAdapter.selectEntry("", 0, null, null, null, null);
				if (cursor.moveToFirst()) {
					nameValuePairs.add(new BasicNameValuePair("reg_id", cursor.getString(cursor.getColumnIndex("reg_id"))));
				}
				cursor.close();
				map = new HashMap<String, JSONArray>();
				map.put("apps", getApss(dbAdapter));
				jsonObj = new JSONObject(map);
				nameValuePairs.add(new BasicNameValuePair("apps", jsonObj.toString()));
				map.clear();
				httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = httpclient.execute(httpost,responseHandler);
				Log.i(getClass().getSimpleName(), "Response: \n" + response);

				JSONObject responseJ = new JSONObject(response);
				if (responseJ.getString("error").equals("")) {//success
					
				} else {//error
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		dbAdapter.close();
	}

	public JSONArray getApss(DatabaseAdapter dbAdapter){
		List<JSONObject> apps = new ArrayList<JSONObject>();
		Cursor cursor = dbAdapter.selectEntry("", 1, null, null, null, null);
		if (cursor.moveToFirst()){
			do {
				HashMap<String, String> app = new HashMap<String, String>();
				app.put("name", cursor.getString(cursor.getColumnIndex("name")));
				app.put("package_name", cursor.getString(cursor.getColumnIndex("package_name")));
				app.put("description", "" + cursor.getString(cursor.getColumnIndex("description")));
				app.put("type", "" + cursor.getString(cursor.getColumnIndex("type")));
				app.put("start_time", "" + cursor.getString(cursor.getColumnIndex("start_time")));
				app.put("end_time", "" + cursor.getString(cursor.getColumnIndex("end_time")));
				app.put("download", "" + cursor.getString(cursor.getColumnIndex("download")));
				app.put("version", "" + cursor.getString(cursor.getColumnIndex("version")));
				app.put("action", "" + cursor.getString(cursor.getColumnIndex("action")));
				JSONObject object = new JSONObject(app);
				apps.add(object);
			} while(cursor.moveToNext());
		}

		cursor.close();

		JSONArray jsonArray = new JSONArray(apps);
		return jsonArray;
	}
}
