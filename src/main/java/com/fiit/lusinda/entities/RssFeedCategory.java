package com.fiit.lusinda.entities;

public class RssFeedCategory {

	public static final String UNKNOWN="0";
	public static final String INTERNATIONAL="1";
	public static final String INTERNATIONAL_EUROPE="1_1";
	public static final String INTERNATIONAL_ASIA="1_2";
	public static final String INTERNATIONAL_AMERICA="1_3";
	public static final String INTERNATIONAL_AFRICA="1_4";

	
	public static final String BUSINESS="2";
	
	public static String resolveCategory(String category)
	{
		String resolved = UNKNOWN;
		try {
			resolved = (String)RssFeedCategory.class.getField(category).get(null);
		} catch (Exception e) {
			
			resolved = UNKNOWN;
			
		}

		return resolved;
	}
	
}
