package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import com.fiit.lusinda.topicmodelling.LdaModel;

public class Query{

	private String queryString;
	TreeSet<Sorter<NGram>> query;
	
	public Query(TreeSet<Sorter<NGram>> query)
	{
	//	LdaModel.printTopRecomendedWords("before", query,10, 0);
		
		Map<String,NGram> wordsCounts = new HashMap<String,NGram>();
		LdaModel.processWordsCounts(query, wordsCounts);
		 this.query=  LdaModel.sortWordsCounts(wordsCounts);
		
	//		LdaModel.printTopRecomendedWords("after", query,10, 0);
		
	}
	
	public TreeSet<Sorter<NGram>> getQuery()
	{
		return query;
	}
	
	public void flush(int top)
	{
		
		
		StringBuilder builder = new StringBuilder();
		int i =0;
		Iterator<Sorter<NGram>> it = query.iterator();
		while(it.hasNext() && i<top)
		{
			Sorter<NGram> sd = it.next();
			builder.append(sd.data.getNiceNGram()).append(" ");
			i++;
		}
		
		queryString = builder.toString();
	}
	
	public String getQueryString()
	{
		return queryString;
	}
}
