/**
 * 
 */
package edu.buffalo.cse.phonelab.phonelabservices;

import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */
public class ApplicationList extends ListActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_list);

		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR);
		try {
			if (manifest.getManifest()) {
				ArrayList<PhoneLabApplication> applications = manifest.getAllApplications();
				String[] values = new String[applications.size()];
				String[] descriptions = new String[applications.size()];
				for (int i = 0;i < applications.size();i++) {
					values[i] = applications.get(i).getName();
					descriptions[i] = applications.get(i).getDescription();
				}
				ApplicationListArrayAdapter adapter = new ApplicationListArrayAdapter(this, values, descriptions);
				setListAdapter(adapter);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Log.i(getClass().getSimpleName(), "Item: " + item + "Position: " + position + " id: " + id);
	}
}
