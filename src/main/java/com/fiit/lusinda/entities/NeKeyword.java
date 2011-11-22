package com.fiit.lusinda.entities;

public class NeKeyword extends Keyword {

	public String nameEntityType;
	

	public String getEscapedKeyword() {
		
		return getNEShortcut(nameEntityType) + "_" + escapedName;
	}
	
public String getNormalizedKeyword() {
		
		return getNEShortcut(nameEntityType) + "_" + normalizedName;
	}

	public static String getNEShortcut(String nameEntityType) {
		if (nameEntityType == null || nameEntityType.length() == 0)
			return null;

		if (nameEntityType.equals("Person"))
			return "PE";
		if (nameEntityType.equals("Organization"))
			return "ORG";
		if (nameEntityType.equals("Company"))
			return "COMP";
		if (nameEntityType.equals("City"))
			return "CIT";

		return nameEntityType.toUpperCase();
	}

}
