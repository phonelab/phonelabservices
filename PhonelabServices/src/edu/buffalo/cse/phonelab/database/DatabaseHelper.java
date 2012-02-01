/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "phonelab_v_1";

	private String deviceId = "";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = tm.getDeviceId();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(getClass().getSimpleName(), "ONCREATE DATABASEHELPER");
		String createSql = "create table if not exists device ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, device_id TEXT," 
				+ "reg_id TEXT, synced INTEGER DEFAULT 0)";
		db.execSQL(createSql);

		String sql = "insert or replace into device(device_id) values('" + deviceId + "')";
		db.execSQL(sql);

		createSql = "create table if not exists application ("
				+ "package_name TEXT PRIMARY KEY, name TEXT," 
				+ "intent_name TEXT, description TEXT, type TEXT,"
				+ "start_time TIMESTAMP, end_time TIMESTAMP,"
				+ "download TEXT, version TEXT, action TEXT)";
		db.execSQL(createSql);

		createSql = "create table if not exists status_monitor_parameter ("
				+ "name TEXT PRIMARY KEY, value TEXT," 
				+ "units TEXT, set_by TEXT)";
		db.execSQL(createSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
