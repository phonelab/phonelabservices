/**
 * 
 */
package edu.buffalo.cse.phonelab.phonelabservices;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import edu.buffalo.cse.phonelab.database.DatabaseAdapter;

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

		DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
		dbAdapter.open(0);
		Cursor cursor = getAppNames(dbAdapter);
		if (cursor != null) {
			Log.i(getClass().getSimpleName(), "Cursor is not empty!");
			startManagingCursor(cursor);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.application_list_entry, cursor, 
					new String[] {"name", "description"},
					new int[] {R.id.name_entry, R.id.description_entry});
			setListAdapter(adapter);

			ListView list = getListView();
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					Log.i(getClass().getSimpleName(), "Position: " + position + " Row _id: " + id);
				}
			});
		}

		dbAdapter.close();
	}

	/**
	 * @param dbAdapter 
	 * @param Context context
	 * @return cursor for all the applications
	 */
	private Cursor getAppNames(DatabaseAdapter dbAdapter) {
		Cursor cursor = dbAdapter.selectEntry("", 1, null, null, null, null);
		return cursor;
	}
}
