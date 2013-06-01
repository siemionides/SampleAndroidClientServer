package com.example.impaqsampleproject;

/**
 * Class representing contact on the list.
 * Contains it's given and family name and information whether it is selected
 * @author Michal Siemionczyk michal.siemionczyk@gmail.com
 *
 */
public class Contact {

	private String givenName;
	
	private String familyName;
	
	private boolean isSelected;
	
	public Contact(){
		givenName = "";
		familyName = "";
		isSelected = false;
	}
	
	public Contact(String _givenName, String _familyName){
		givenName = _givenName;
		familyName = _familyName;
		isSelected = false;
	}
	
	@Override
	public String toString(){
		return "given name: " + givenName +", family name:" + familyName + ", isSelected:" + isSelected;
	}
	
	/* --------------   Setters & Getters ----------------- */
	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	
}
