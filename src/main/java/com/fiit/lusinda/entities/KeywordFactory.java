package com.fiit.lusinda.entities;

import java.io.IOException;

import com.fiit.lusinda.textprocessing.StandardTextProcessing;

public class KeywordFactory {

	public String extractionSource;
	public boolean extractNEOnly;
	
	public KeywordFactory(String extractionSource,boolean extractNEOnly)
	{
		this.extractionSource = extractionSource;
		this.extractNEOnly = extractNEOnly;
	}
	
	public  Keyword getKeyword(String name,String type) throws IOException
	{
		if(extractNEOnly && (type==null || type.length()==0))
			return null;

		if(type!=null)
		{
			NeKeyword neKeyword = new NeKeyword();
			neKeyword.nameEntityType = type;
			neKeyword.name = name;
			neKeyword.escapedName = null;// StandardTextProcessing.analyze(name,0);
			neKeyword.normalizedName = StandardTextProcessing.normalizeKeyword(name);

			return neKeyword;
		}
		else
		{
			Keyword keyword = new Keyword();
		
			keyword.name = name;
			keyword.escapedName = null;// StandardTextProcessing.analyze(name,0);
			keyword.normalizedName = StandardTextProcessing.normalizeKeyword(name);

			return keyword;
		}
		
	}
	
}
