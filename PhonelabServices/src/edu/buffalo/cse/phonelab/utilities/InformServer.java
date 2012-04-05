package edu.buffalo.cse.phonelab.utilities;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.util.Log;

/**
 * Use this class to inform server about any bit of information.
 *  
 * @author Rishi
 *
 */
public class InformServer {
	
	final static private String devId = "dev_id";
	final static private String applicationId = "application_id";
	final static private String action = "action";
	final static private String install = "install";
	final static private String uninstall = "uninstall";

	public static void installSuccessMessage(Context context, final String AppId) {
		
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		try {
			params.put(devId, Util.getDeviceId(context));
			params.put(applicationId, AppId);
			params.put(action, install);

			client.post(Util.APPLICATION_ACTION_SUCCESS_URL, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend has been informed about installation success : " + AppId);
				}

				@Override
				public void onFailure(Throwable e) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend could not be informed about installation success : " + AppId +
																	"\nReason : " + e.getMessage());
				}
			});
		} catch (Exception e) {
			Log.e("PhoneLab-" + InformServer.class.getClass().getSimpleName(), e.getMessage());
		}
	}

	public static void uninstallSuccessMessage(Context context, final String AppId) {

		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		try {
			params.put(devId, Util.getDeviceId(context));
			params.put(applicationId, AppId);
			params.put(action, uninstall);

			client.post(Util.APPLICATION_ACTION_SUCCESS_URL, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend has been informed about uninstallation success : " + AppId);
				}

				@Override
				public void onFailure(Throwable e) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend could not be informed about uninstallation success : " + AppId +
																	"\nReason : " + e.getMessage());
				}
			});
		} catch (Exception e) {
			Log.e("PhoneLab-" + InformServer.class.getClass().getSimpleName(), e.getMessage());
		}
		
	}
}
