package com.fiit.lusinda.entities;

import java.util.TreeSet;

public class Article implements Comparable<Article> {

	public int id;
//	public int modelId;
	public String title;
	public double weight;
	public double[] dist;
	public String content;
	private String niceTitle;
	public String key;
	public long ts;
	public String summarizedText;
	public int keywords[];
	public TreeSet<Sorter<NGram>> docWords;
	
	public Article(int id,String key,String title,String content,double weight,double[] dist)
	{
		this.id = id;
	//	this.modelId = modelId;
		this.title = title;
		this.key = key;
		this.niceTitle = title.replace("_"," ");
		this.weight = weight;
		this.dist = dist;
		this.content = content;
	}
	
	public String getNiceTitle()
	{
		return niceTitle;
	}
	
	public Article()
	{
		
	}

	@Override
	public int compareTo(Article o) {
		
		if(o==null)
			return -1;
		if(this.title==null && o.title==null)
			return -1;
		if(this.title.equals(o.title))
			return 0;
		
		return -1;
	}
}
