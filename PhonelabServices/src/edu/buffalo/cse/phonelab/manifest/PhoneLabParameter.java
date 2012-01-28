/**
 * @author fatih
 */
package edu.buffalo.cse.phonelab.manifest;

public class PhoneLabParameter {

	private String name;
	private String value;
	private String units;
	private String setBy;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getSetBy() {
		return setBy;
	}
	public void setSetBy(String setBy) {
		this.setBy = setBy;
	}
	public String toString() {
		return "name: " + name + "\n" +
				"value: " + value + "\n" +
				"units: " + units + "\n" + 
				"setBy: " + setBy + "\n";
	}
	
}
