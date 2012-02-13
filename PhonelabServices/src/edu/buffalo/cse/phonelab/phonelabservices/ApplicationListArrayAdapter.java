package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List Adapter to get more info for an app in the Application List.
 * 
 * @author Muhammed Fatih Bulut
 *
 * mbulut@buffalo.edu
 */
public class ApplicationListArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] names;
	private final String[] descriptions;
	
	public ApplicationListArrayAdapter(Activity context, String[] names, String[] descriptions) {
		super(context, R.layout.application_list_entry, names);
		this.context = context;
		this.names = names;
		this.descriptions = descriptions;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.application_list_entry, null, true);
		TextView nameTextView = (TextView) rowView.findViewById(R.id.name_entry);
		TextView descriptionTextView = (TextView) rowView.findViewById(R.id.description_entry);
		String s = names[position];
		String d = descriptions[position];
		if (s != null) {
			nameTextView.setText(s);
		}
		if (d != null) {
			descriptionTextView.setText(d);
		}
		
		return rowView;
	}
}

