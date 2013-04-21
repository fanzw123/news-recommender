package com.fiit.lusinda.similarity;

import java.util.ArrayList;
import java.util.List;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.clustering.ResultsClustering;
import com.fiit.lusinda.clustering.ResultsDocuments;
import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.topicmodelling.Document;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class JackardSimilarity implements DissimilarityMeasure, SimilarityStrategy {

	
	public static int[] getCommonWords(int[] set1,int[] set2)
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
	
	public double computeSimilarity(int[] set1,int[] set2,double[] weights)
	{
		
//		if(set1.length!=set2.length)
//			throw new IllegalArgumentException("sets have to be same length");
//		
	//	int[] intersection = new int[set1.length];
		
		int count = 0;
		double sum = 0;
		
		for(int i=0;i<set1.length;i++)
		{
			for(int j=0;j<set2.length;j++)
			{
				if(set1[i] == set2[j])
				{
					sum+=weights[set1[i]]; 
					//intersection[index] = set1[i];
					count++;
					break;
				}
			}
		}
		
		return sum;
	}
	
	public double computeSimilarity(int[] set1,int[] set2)
	{
		
//		if(set1.length!=set2.length)
//			throw new IllegalArgumentException("sets have to be same length");
//		
	//	int[] intersection = new int[set1.length];
		
		int count = 0;
		
		for(int i=0;i<set1.length;i++)
		{
			if(set1[i]==-1)
				continue;
			for(int j=0;j<set2.length;j++)
			{
				if(set2[j]==-1)
					continue;
				
				if(set1[i] == set2[j])
				{
					//intersection[index] = set1[i];
					count++;
					break;
				}
			}
		}
		
		return (double)count / (double)(set1.length + set2.length - count);
	}
	
	public double computeDissimilarity(Experiment experiment, int observation1, int observation2) {

		double result = 0;
		
		ResultsDocuments dataset = (ResultsDocuments) experiment;
		
		Article doc1 = dataset.get(observation1);
		Article doc2 = dataset.get(observation2);
		
		result = computeSimilarity(doc1.keywords, doc2.keywords);

		int[] commonWords = getCommonWords(doc1.keywords, doc2.keywords);
		String res = dataset.getWords(commonWords, " "); 
		if(res!=null)
		{
			System.out.println(doc1.title);
			System.out.println(doc2.title);
			System.out.println(res);
		}
		
		return result;

	}
}
