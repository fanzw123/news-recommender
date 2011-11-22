package com.fiit.lusinda.entities;

public class Keyword {

	public String name;
	public ExtractionSource source;
	public String normalizedName;

	public String escapedName;
	
	public Double score;
	
	public String getEscapedKeyword()
	{
		return escapedName;
	}
	
	public String getNormalizedKeyword()
	{
		return escapedName;
	}
	
	public Double getScore()
	{
		return score;
	}
}
