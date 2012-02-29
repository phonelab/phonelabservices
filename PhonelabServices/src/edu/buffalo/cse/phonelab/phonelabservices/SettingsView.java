/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */

package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import edu.buffalo.cse.phonelab.utilities.Util;


public class SettingsView extends Activity {

	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        ToggleButton toggle1 = (ToggleButton) findViewById(R.id.toggleButton1);
        ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		toggle1.setChecked(settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, false));
		toggle2.setChecked(settings.getBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false));
    }
    
    public void uploadSettingsChanged (View view) {
    	SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		Editor editor = settings.edit();
    	ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
    	if (toggle.isChecked()) {
    		Log.i("PhoneLab-" + getClass().getSimpleName(), "Upload setings has been changed by the user: mobile data is not allowed");
    		editor.putBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, true);
    	} else {
    		Log.i("PhoneLab-" + getClass().getSimpleName(), "Upload setings has been changed by the user: mobile data is permitted");
    		editor.putBoolean(Util.SHARED_PREFERENCES_SETTINGS_WIFI_FOR_LOG, false);
    	}
    	editor.commit();
    }
    
    public void uploadSettingsChangedPower (View view) {
    	SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		Editor editor = settings.edit();
    	ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton2);
    	if (toggle.isChecked()) {
    		Log.i("PhoneLab-" + getClass().getSimpleName(), "Upload setings has been changed by the user: upload only device is plugged in");
    		editor.putBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, true);
    	} else {
    		Log.i("PhoneLab-" + getClass().getSimpleName(), "Upload setings has been changed by the user: can upload without plugged in");
    		editor.putBoolean(Util.SHARED_PREFERENCES_SETTINGS_POWER_FOR_LOG, false);
    	}
    	editor.commit();
    }
}
