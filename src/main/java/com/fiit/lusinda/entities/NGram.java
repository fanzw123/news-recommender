package com.fiit.lusinda.entities;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.getopt.stempel.lucene.StempelFilter;

import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;

import cc.mallet.types.IDSorter;

public class NGram implements Comparable<NGram>{

	
	private List<String> parts = new ArrayList<String>();
	private List<String> lemmas = new ArrayList<String>();
	private List<IDSorter> features = new ArrayList<IDSorter>();
	
	private StringBuilder key = new StringBuilder();
	private String keyStr;
	private StringBuilder originalNGram = new StringBuilder();
	private String originalNGramStr;
	private StringBuilder lemmatizedNGram = new StringBuilder();
	private String lemmatizedNGramStr;
	private String niceNGramString;
	
	
	
	private int upperCouts = 0;
	
	public int Counts = 1;
	
	double weight = 0;
	
	double tfidf = 0;
	
	public double getWeight()
	{
		return weight;
	}
	
	public boolean hasPart(String lemma)
	{
		for(String l:lemmas)
		{
			if(l.equals(lemma))
				return true;
		}
		return false;
	}
	
	public void setWeight(double w)
	{
		this.weight =w ;
	}
	
	public int getUpperCounts()
	{
		return upperCouts;
	}
	
	public int getSize()
	{
		return parts.size();
	}
	public NGram(String part,String lemma,IDSorter feature,boolean stem,boolean isUpper)
	{
		this.add(part, lemma, feature,stem,isUpper);
	}
	
	
	
	public String getKey()
	{
		return keyStr;
	}
	
	public String getNiceNGram()
	{
		return niceNGramString;
		
	}
	
	public String getOriginalNGram()
	{
		return originalNGramStr;
	}
	
	public String getLemmatizedNGram()
	{
		return lemmatizedNGramStr;
	}
	
	public void add(String part,String lemma,IDSorter feature,boolean stem,boolean isUpper)
	{
		
		if(isUpper)
			upperCouts++;
			
		
		//stem = false;
		this.parts.add(part);
	this.lemmas.add(lemma);
	
	if(stem)
	{
	 int len = (int) (lemma.length() * 0.75);
				  len = len > lemma.length() ? lemma.length() : len;
				 lemma = lemma.substring(0, len);
				 	
	}
		this.key.append(lemma);
	this.key.append("_");
	
		this.originalNGram.append(part);
		this.originalNGram.append("_");
		
		
		this.lemmatizedNGram.append(lemma);
		this.lemmatizedNGram.append("_");
		
		if(feature!=null)
		{
			if(feature.getWeight()>this.weight)
				weight +=feature.getWeight();
			//weight += feature.getWeight();
			this.features.add(feature);
		}
	}
	
	public List<String> getLemmatizedParts()
	{
		return lemmas;
	}
	
	public List<String> getOriginalParts()
	{
		return parts;
	}
	
	public List<IDSorter> getFeatures()
	{
		return features;
	}

	public void computeTfIdf(int[][] topicWordsAssigments,int totalAssigments,int numTopics)
	{
		this.tfidf = ( (double)this.Counts/(double)totalAssigments ) * this.weight;
		
//		double tf = (double)this.weight/(double)totalAssigments;
//		double idf = 0;
//		int counts=0;
//		for(IDSorter feature:features)
//		{
//			for(int k =0;k<numTopics;k++)
//			{
//				if(topicWordsAssigments[feature.getID()][k]>0)
//					counts++;
//			}
//		}
//		
//		idf = Math.log((double)numTopics/(double) counts);
		
		//this.tfidf =  idf*tf;
	}
	


	
	public double getTfIdf()
	{
		return tfidf;
	}
	
	public void flush()
	{
		
		
		originalNGramStr = StringUtils.removeEnd(originalNGram.toString(),"_");
		
		lemmatizedNGramStr = StringUtils.removeEnd(lemmatizedNGram.toString(),"_");
		keyStr = key.toString();
		niceNGramString = lemmas.size()>1?originalNGramStr.replace("_", " "):lemmas.get(0);
	}

	@Override
	public boolean equals(Object obj) {
	
		NGram o = (NGram) obj;
		
		if (!this.getKey().equals(o.getKey()))
			return false;
		
		return this.Counts == o.Counts;
	}
	
	@Override
	public int hashCode() {
	
		return getKey().hashCode()^Counts;
	}
	
	public int compareTo(NGram o) {
		

		if(this.getKey().equals(o.getKey()))
		{
			if(this.Counts>o.Counts)
				return -1;
			
			if(this.Counts<o.Counts)
				return 1;
		
			return 0;
		}
		
		return this.getKey().compareTo(o.getKey());
		
	}
	
}
