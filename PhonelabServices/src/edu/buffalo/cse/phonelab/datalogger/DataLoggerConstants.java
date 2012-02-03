package edu.buffalo.cse.phonelab.datalogger;

public class DataLoggerConstants {
	protected final static long UPDATE_INTERVAL = 1000*30;
	protected final static String TAG = "LogService";
	protected final static int LOG_FILE_SIZE = 10;
	protected final static int AUX_LOG_FILES = 10;
	protected final static String LOG_DIR = "log";
	protected final static long THRESHOLD = 7;
	protected final static String POST_URL = "http://phonelab-logger.nodester.com/";
}
