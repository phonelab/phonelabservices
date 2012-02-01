/**
 * @author fatih
 */

package edu.buffalo.cse.phonelab.manifest;

public class PhoneLabApplication {
	private String intentName;
	private String packageName;
	private String name;
	private String description;
	private String type;
	private boolean participantInitiated;
	private String download;
	private String version;
	private String action;
	private String startTime;
	private String endTime;
	
	public String getIntentName() {
		return intentName;
	}
	public void setIntentName(String intentName) {
		this.intentName = intentName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isParticipantInitiated() {
		return participantInitiated;
	}
	public void setParticipantInitiated(boolean participantInitiated) {
		this.participantInitiated = participantInitiated;
	}
	public String getDownload() {
		return download;
	}
	public void setDownload(String download) {
		this.download = download;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String toString() {
		return "intent_name: " + intentName + "\n" +
				"package_name: " + packageName + "\n" +
				"name: " + name +  "\n" +
				"description: " + description + "\n" +
				"type: " + type + "\n" +
				"participantinitiated: " + participantInitiated + "\n" +
				"download: " + download + "\n" +
				"version: " + version + "\n" + 
				"action: " + action + "\n";
				
	}
}
