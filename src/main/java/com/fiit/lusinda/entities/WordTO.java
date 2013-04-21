package com.fiit.lusinda.entities;

import cc.mallet.types.IDSorter;

public class WordTO {

	public String word;
	public String lemmatizedWord;
	public IDSorter feature;
	public boolean stem;
	public boolean isUpper;
	
	public WordTO(String w,String lw,IDSorter f,boolean s,boolean u)
	{
		word = w;
		lemmatizedWord =lw;
		feature = f;
		stem = s;
		isUpper = u;
	}
}
