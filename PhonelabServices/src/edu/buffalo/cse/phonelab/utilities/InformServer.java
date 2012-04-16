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
	final static private String applicationId = "app_id";
	final static private String action = "action";
	final static private String install = "I";
	final static private String uninstall = "U";
	final static private String result = "result";
	final static private String success = "S";
	final static private String failure = "F";
	
	final static public int INSTALL = 1;
	final static public int UNINSTALL = 2;
	final static public int SUCCESS = 1;
	final static public int FAILURE = 1;

	public static void sendMessage(Context context, final String AppId,int command,int done) { // TODO better name than done

		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		try {
			params.put(devId, Util.getDeviceId(context));
			params.put(applicationId, AppId);
			
			if(command == INSTALL)				params.put(action, install);
			else if (command == UNINSTALL)		params.put(action, uninstall);
			
			if(command == SUCCESS)				params.put(result, success);
			else if (command == FAILURE)		params.put(result, failure);

			client.post(Util.APPLICATION_ACTION_URL, params, new AsyncHttpResponseHandler() {
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

