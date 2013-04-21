package com.fiit.lusinda.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.usi.inf.sape.hac.experiment.Experiment;

import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.NGram;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.topicmodelling.Document;
import com.fiit.lusinda.topicmodelling.Lda;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ResultsDocuments extends ArrayList<Article> implements Experiment{

	TreeSet<Sorter<Article>> sortedDocuments;
	Map<String,Integer> commonWordsDict = new HashMap<String,Integer>();
	List<NGram> ngrams = new ArrayList<NGram>();
	private List<Lda> models;
	
	
	public ResultsDocuments(TreeSet<Sorter<Article>> sorted,int maxDocs,int maxWords)
	{
		
		this.sortedDocuments = sorted;
		
		int i=0;
		Iterator<Sorter<Article>> it = sorted.iterator();
		
		while(it.hasNext() && i<maxDocs)
		{
			Sorter<Article> sd = it.next();
			this.add(sd.data);
			processArticle(sd.data, maxWords);
			i++;
		}
		
	
	}
	
	public int[] getNeWords(int[] indicies,int max)
	{
		int[] result = new int[max];
		for(int i=0;i<result.length;i++)
			result[i]=-1;
		
		int i=0;
		for(int pos=0;pos<indicies.length  && i<max;pos++)
		{
			if(ngrams.get(indicies[pos]).getUpperCounts()>=1)
			{
				result[i]=indicies[pos];
				i++;
			}
		}
		
		return result;
	}
	
	public int[] getNgramsWords(int[] indicies,int max)
	{
		int[] result = new int[max];
		for(int i=0;i<result.length;i++)
			result[i]=-1;
		
		int i=0;
		for(int pos=0;pos<indicies.length && i<max;pos++)
		{
			if(ngrams.get(indicies[pos]).getLemmatizedParts().size()>1)
			{
				result[i]=indicies[pos];
				i++;
			}
		}
		
		return result;
	}
	
	public  int[] getCommonNgramsWords(int[] set1,int[] set2)
	{
		List<Integer> common = new ArrayList<Integer>();
		int count=0;
		for(int i=0;i<set1.length;i++)
		{
			for(int j=0;j<set2.length;j++)
			{
				if(set1[i] == set2[j] && ngrams.get(set1[i]).getLemmatizedParts().size()>1)
				{
					common.add(set1[i]);
					count++;
					break;
				}
			}
		}
		
		int[] ret = new int[common.size()];
		for(int i = 0;i<ret.length;i++)
			ret[i] = common.get(i);
		
		return ret;
	}
	
	public  int[] getCommonNeWords(int[] set1,int[] set2)
	{
		List<Integer> common = new ArrayList<Integer>();
		int count=0;
		for(int i=0;i<set1.length;i++)
		{
			for(int j=0;j<set2.length;j++)
			{
				if(set1[i] == set2[j] && ngrams.get(set1[i]).getUpperCounts()>0)
				{
					common.add(set1[i]);
					count++;
					break;
				}
			}
		}
		
		int[] ret = new int[common.size()];
		for(int i = 0;i<ret.length;i++)
			ret[i] = common.get(i);
		
		return ret;
	}
	
	
	public  int[] getCommonWords(int[] set1,int[] set2)
	{
		List<Integer> common = new ArrayList<Integer>();
		int count=0;
		for(int i=0;i<set1.length;i++)
		{
			for(int j=0;j<set2.length;j++)
			{
				if(set1[i] == set2[j])
				{
					common.add(set1[i]);
					count++;
					break;
				}
			}
		}
		
		int[] ret = new int[common.size()];
		for(int i = 0;i<ret.length;i++)
			ret[i] = common.get(i);
		
		return ret;
	}
	
	
	public int[] mergeCommonWords(int[] commonWords,int[] ngramsToMerge)
	{
		//int[] result = new int[commonWords.length];
		Set<Integer> result = Sets.newHashSet();
		
		for(int i=0;i<commonWords.length;i++)
		{
			NGram word = this.ngrams.get(commonWords[i]);
			if(word.getLemmatizedParts().size()>1)
			{
				result.add(commonWords[i]);
				continue;
			}
			
			boolean allocated = false;
			for(int j=0;j<ngramsToMerge.length && ngramsToMerge[j]!=-1;j++)
			{
				
				
				
				NGram ngram = this.ngrams.get(ngramsToMerge[j]);
				if(ngram.hasPart(word.getLemmatizedNGram()))
				{
					result.add(ngramsToMerge[j]);
					allocated = true;
				}
			}
			
			if(!allocated)
				result.add(commonWords[i]);
			
			
		}
		
		int[] result2 = new int[result.size()];
		Iterator<Integer> it = result.iterator();
		int i=0;
		while(it.hasNext())
		{
			result2[i] = it.next();
			i++;
		}
		
		return result2;
	}
	
	public NGram getWord(int i)
	{
		return ngrams.get(i);
	}
	
	public List<NGram> getNgrams(int[] indicies)
	{
		List<NGram> result = new ArrayList<NGram>();
		
		for(int i=0;i<indicies.length;i++)
			result.add(ngrams.get(i));
		
		return result;
	}
	
	public List<String> getWords(int[] indicies)
	{
		return null;
	}
	
	public String getWords(int[] indicies,String sep)
	{
		if(indicies==null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		
		for(int i=0;i<indicies.length;i++)
		{
			if(indicies[i]==-1)
				continue;
			
			builder.append(ngrams.get(indicies[i]).getNiceNGram());
			builder.append(sep);
		}
		
		return builder.toString();
	}
	
	public double getRankedWordsScore(int[] indicies)
	{
		double score = 0;
		
		for(int i=0;i<indicies.length;i++)
		{
			score += ngrams.get(indicies[i]).getWeight();			
		}
		
		
		return score;
	}

	private void processArticle(Article a,int maxWords)
	{
		a.keywords = new int[maxWords];
		for(int i=0;i<maxWords;i++)
			a.keywords[i]=-1;
		
		if(a.docWords==null)
			return;
		Iterator<Sorter<NGram>> it = a.docWords.iterator();
		
		
		
		
			int i=0;
		while(it.hasNext() && i<maxWords)
		{
			Sorter<NGram> sd = it.next();
			
			Integer refNgram = commonWordsDict.get(sd.data.getKey());
			if(refNgram==null)
			{
				this.ngrams.add(sd.data);
				refNgram = this.ngrams.size()-1;
				commonWordsDict.put(sd.data.getKey(), refNgram);				
			}
			
			a.keywords[i] = refNgram;
			
			i++;
		}
		
	}
	
	@Override
	public int getNumberOfObservations() {
		
		return this.size();
	}
	

}
