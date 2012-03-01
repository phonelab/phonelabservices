/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */

package edu.buffalo.cse.phonelab.phonelabservices;

import java.io.File;
import java.io.FileFilter;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import edu.buffalo.cse.phonelab.utilities.Util;


public class LogListView extends ListActivity {
	private final String LOG_DIR = Environment.getExternalStorageDirectory() + "/" + Util.LOG_DIR + "/";
	private String[] values;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.getName().startsWith("1");
		    }
		};
		File[] allFiles = new File(LOG_DIR).listFiles(fileFilter);
		if (allFiles != null && allFiles.length > 0) {
			Log.i("PhoneLab-" + getClass().getSimpleName(), allFiles.length + " found");
			values = new String[allFiles.length];
			String[] values2 = new String[allFiles.length];
			for (int i=0;i < allFiles.length;i++) {
				String fileName = allFiles[i].getName();
				values[i] = fileName;
				values2[i] = "";
			}
			ApplicationListArrayAdapter adapter = new ApplicationListArrayAdapter(this, values, values2);
			setListAdapter(adapter);
		} else {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "No log files to show");
		}
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	}
}
