/**
 * @author fatih
 */

package edu.buffalo.cse.phonelab.c2dm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			Log.i(getClass().getSimpleName(), "C2DM Message Receiver called");
			final String payload = intent.getStringExtra("payload");
			Log.i(getClass().getSimpleName(), "Payload: " + payload);
			Intent messageService = new Intent(context, MessageService.class);
			messageService.putExtra("payload", payload);
			context.startService(messageService);
		}
	}
}
