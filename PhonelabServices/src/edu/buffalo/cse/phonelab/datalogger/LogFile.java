package edu.buffalo.cse.phonelab.datalogger;

public class LogFile {
	private String fileName;
	private long fileSize;
	private long timestamp;
	
	public LogFile(String fileName, long fileSize, long timestamp) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.timestamp = timestamp;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
}
