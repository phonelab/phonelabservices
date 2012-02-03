/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.database;

import edu.buffalo.cse.phonelab.utilities.Util;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter {

	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;

	private static final String UPLOADTIME = "uploadTime";
	private static final String PROCESSID = "pid";

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
			dropTable(0);
			createTable(0);
		} else if (tableId == 1) {
			dropTable(1);
			createTable(1);
		} else if(tableId == 2) {
			dropTable(2);
			createTable(2);
		}
	}

	public void dropTable(int tableId) {
		if (tableId == 0) {
			database.execSQL("drop table device");
		} else if (tableId == 1) {
			database.execSQL("drop table application");
		} else if (tableId == 2) {
			database.execSQL("drop table status_monitor_parameter");
		}
	}

	public void createTable (int tableId) {
		if (tableId == 0) {
			String createSql = "create table if not exists device ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, device_id TEXT," 
					+ "reg_id TEXT, synced INTEGER DEFAULT 0)";
			database.execSQL(createSql);
			String sql = "insert or replace into device(device_id) values('" + Util.getDeviceId(context) + "')";
			database.execSQL(sql);
		} else if (tableId == 1) {
			String createSql = "create table if not exists application ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT, name TEXT," 
					+ "intent_name TEXT, description TEXT, type TEXT,"
					+ "start_time TIMESTAMP, end_time TIMESTAMP,"
					+ "download TEXT, version TEXT, action TEXT)";
			database.execSQL(createSql);
		} else if (tableId == 2) {
			String createSql = "create table if not exists status_monitor_parameter ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT," 
					+ "units TEXT, set_by TEXT)";
			database.execSQL(createSql);
		}
	}

	public void executeRawQuery (String sql) {
		database.execSQL(sql);
	}

	/*
	 * Data Logger Opearations
	 */
	public int getLastPID() {
		String command = "SELECT pid FROM " + PROCESSID + " ORDER BY pid DESC LIMIT 0,1";
		Cursor c = database.rawQuery(command, null);
		int res = 0;
		if (c != null ) {
			if  (c.moveToFirst()) {
				res = c.getInt(c.getColumnIndex("pid"));
			}
		}
		return res;
	}

	public void setLastPID(int pid) {
		String command = "INSERT OR REPLACE INTO " + PROCESSID + " (_id,pid) VALUES (1,"+ pid +")";
		database.execSQL(command);
	}


	public long getLastUpdateTime() {
		String command = "SELECT time FROM " + UPLOADTIME + " ORDER BY time DESC LIMIT 0,1";
		Cursor c = database.rawQuery(command, null);
		long res = 0L;
		if (c != null ) {
			if  (c.moveToFirst()) {
				res = c.getLong(c.getColumnIndex("timestamp"));
			}
		}
		return res;
	}

	public void setLastUpdateTime(long timestamp) {
		String command = "INSERT OR REPLACE INTO " + UPLOADTIME + " (_id,timestamp) VALUES (1,"+ timestamp +")";
		database.execSQL(command);
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
					+ "\\nIntentName= " +  cursor2.getString(cursor2.getColumnIndex("intent_name"))
					+ "\nDescription= " + cursor2.getString(cursor2.getColumnIndex("description"))
					+ "\nType= " + cursor2.getString(cursor2.getColumnIndex("type"))
					+ "\nStartTime= " + cursor2.getString(cursor2.getColumnIndex("start_time"))
					+ "\nEndTime= " + cursor2.getString(cursor2.getColumnIndex("end_time"))
					+ "\nDownloadUrl= " + cursor2.getString(cursor2.getColumnIndex("download"))
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
