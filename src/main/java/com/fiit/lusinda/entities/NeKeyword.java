package com.fiit.lusinda.entities;

public class NeKeyword extends Keyword {

	public String nameEntityType;
	

	public String getEscapedKeyword() {
		
		String neShortcut = getNEShortcut(nameEntityType);
		
		if(neShortcut==null ||  escapedName==null || "".equals(escapedName))
			return null;
		else
			return neShortcut + "_" + escapedName;
	}
	
public String getNormalizedKeyword() {
		
	String neShortcut = getNEShortcut(nameEntityType);
	
	if(neShortcut==null ||  normalizedName==null || "".equals(normalizedName))
		return null;
	else
		return neShortcut + "_" + normalizedName;
	}

	public static String getNEShortcut(String nameEntityType) {
		if (nameEntityType == null || nameEntityType.length() == 0)
			return null;

		

		return nameEntityType.toLowerCase();
	}

}
