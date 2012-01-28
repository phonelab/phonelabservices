/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter {
	
	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	
	public DatabaseAdapter (Context context) {
		this.context = context;
	}
	
	public DatabaseAdapter open(int option) throws SQLException{
		dbHelper = new DatabaseHelper(context);
		if (option == 0) {
			database = dbHelper.getReadableDatabase();
		} else {
			database = dbHelper.getWritableDatabase();
		}
		return this;
	}
	
	public void close () {
		dbHelper.close();
	}
	
	public long insertEntry (ContentValues values, int tableId) {
		if (tableId == 0) {
			return database.insert("device", null, values);
		} else if (tableId == 1) {
			return database.insert("application", null, values);
		} else if (tableId == 2) {
			return database.insert("status_monitor_parameter", null, values);
		} else {
			return 0;
		}
	}
	
	public Cursor selectEntry (String whereClause, int tableId, String groupBy, String having, String orderBy, String limit) {
		if (tableId == 0) {
			Cursor cursor = database.query("device", null, whereClause, null, groupBy, having, orderBy, limit);
			return cursor;
		} else if (tableId == 1) {
			Cursor cursor = database.query("application", null, whereClause, null, groupBy, having, orderBy, limit);
			return cursor;
		} else if (tableId == 2) {
			Cursor cursor = database.query("status_monitor_parameter", null, whereClause, null, groupBy, having, orderBy, limit);
			return cursor;
		} else {
			return null;
		}
	}
	
	public int deleteEntry(String whereClause, int tableId) {
		if (tableId == 0) {
			return database.delete("device", whereClause, null);
		} else if (tableId == 1) {
			return database.delete("application", whereClause, null);
		} else if (tableId == 2) {
			return database.delete("status_monitor_parameter", whereClause, null);
		}
		return 0;
	}
	
	public Cursor select(String sql) {
		return database.rawQuery(sql, null);
	}
	
	public int update (ContentValues values, int tableId, String where) {
		if (tableId == 0) {
			return database.update("device", values, where, null);
		} else if (tableId == 1) {
			return database.update("application", values, where, null);
		} else if (tableId == 2) {
			return database.update("status_monitor_parameter", values, where, null);
		}
		return 0;
	}
	
	public void truncateTable(int tableId) {
		if (tableId == 0) {
			database.rawQuery("TRUNCATE TABLE device", null);
		} else if (tableId == 1) {
			database.rawQuery("TRUNCATE TABLE application", null);
		} else if(tableId == 2) {
			database.rawQuery("TRUNCATE TABLE status_monitor_parameter", null);
		}
	}
	
	public void dropTable(int tableId) {
		if (tableId == 0) {
			database.execSQL("drop table user");
		}
	}
	
	public void executeRawQuery (String sql) {
		database.execSQL(sql);
	}
	
	public String toString() {
		String strToReturn = "Device Table\n";
		Cursor cursor = this.selectEntry("", 0, null, null, null, null);
		while (cursor.moveToNext()) {
			strToReturn += "DeviceId= " + cursor.getString(cursor.getColumnIndex("device_id"))
					+ "\nRegId= " + cursor.getString(cursor.getColumnIndex("reg_id")) 
					+ "\nSynced=" + cursor.getInt(cursor.getColumnIndex("synced"))
					+ "\n------------------------------------\n";
		}
		cursor.close();
		strToReturn += "Application Table\n";
		Cursor cursor2 = this.selectEntry("", 1, null, null, null, null);
		while (cursor2.moveToNext()) {
			strToReturn += "Name= " + cursor2.getString(cursor2.getColumnIndex("name")) 
					+ "\nPackageName= " +  cursor2.getString(cursor2.getColumnIndex("package_name"))
					+ "\nDescription= " + cursor2.getString(cursor2.getColumnIndex("description"))
					+ "\nType= " + cursor2.getString(cursor2.getColumnIndex("type"))
					+ "\nDownloadUrl= " + cursor2.getString(cursor2.getColumnIndex("download_url"))
					+ "\nVersion= " + cursor2.getString(cursor2.getColumnIndex("version"))
					+ "\nAction= " + cursor2.getString(cursor2.getColumnIndex("action"))
					+ "\n****\n";
		}
		cursor2.close();
		strToReturn += "------------------------------------\n";
		strToReturn += "Status Monitor Parameters\n";
		Cursor cursor3 = this.selectEntry("", 2, null, null, null, null);
		while (cursor3.moveToNext()) {
			strToReturn += "Name= " +  cursor3.getString(cursor3.getColumnIndex("name"))
					+ "\nValue= " + cursor3.getString(cursor3.getColumnIndex("value"))
					+ "\nUnits= " + cursor3.getString(cursor3.getColumnIndex("units"))
					+ "\nSetBy= " + cursor3.getString(cursor3.getColumnIndex("set_by"))
					+ "\n****\n";
		}
		
		cursor3.close();
		return strToReturn;
	}
}
