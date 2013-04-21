package com.fiit.lusinda.clustering;

import java.util.ArrayList;
import java.util.List;

import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.DocumentCluster;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.similarity.JackardSimilarity;
import com.google.common.collect.Lists;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;

public class ResultsClustering {

	ResultsDocuments documents;
//	double[][] clusterMap ;
	List<DocumentCluster> clusters = new ArrayList<DocumentCluster>();
	
	
	public ResultsClustering(ResultsDocuments documents)
	{
		this.documents = documents;
	}

	private void init()
	{
//		clusterMap = new double[documents.size()][documents.size()];
//		for(int i=0;i<clusterMap.length;i++)
//			for(int j=0;j<clusterMap[i].length;j++)
//			{
//				clusterMap[i][j]=0;
//			}
		
	}
	
	public List<DocumentCluster> cluster(double treeshold)
	{
		init();
		JackardSimilarity sim = new JackardSimilarity();
		for(int i=0;i<documents.size();i++)
		{
			DocumentCluster c = new DocumentCluster();
			clusters.add(c);
			
			c.addDoc(new Sorter<Article>(-1, documents.get(1),0));
			
			
			System.out.println(documents.get(i).title);
			
			System.out.println(documents.getWords(documents.get(i).keywords, " "));
			
			System.out.println("ne: "+documents.getWords(documents.getNeWords(documents.get(i).keywords,10), " "));
			System.out.println("ngrams: "+documents.getWords(documents.getNgramsWords(documents.get(i).keywords,10), ", "));
			
			for(int j=0;j<documents.size();j++)
			{
				if(i==j)
					continue;
				int[] commonWords = documents.getCommonWords(documents.get(i).keywords,documents.get(j).keywords);
				
				if(commonWords!=null && commonWords.length>0)
				{
					
				//double score = documents.getRankedWordsScore(commonWords);
				
				
					int[] ngrams =documents.getNgramsWords(documents.get(j).keywords,10);
					
					System.out.println(documents.get(j).title);
					System.out.println("--->"+documents.getWords(documents.get(j).keywords," "));
					System.out.println("ne: "+documents.getWords(documents.getNeWords(documents.get(j).keywords,10), " "));
					System.out.println("ngrams: "+documents.getWords(ngrams, ", "));
					System.out.println("---->"+documents.getWords(commonWords," "));
					
					int[] merged = documents.mergeCommonWords(commonWords, ngrams);
					System.out.println("merged: "+documents.getWords(merged, ", "));
					System.out.println();
					
					double score = documents.getRankedWordsScore(merged);
					if(score>treeshold)
					{
						c.addDoc(new Sorter<Article>(-1, documents.get(j),score));
						c.addLabels(documents.getNgrams(commonWords));
					}
				
				}
					
				//clusterMap[i][j] = score;
				
			}
			
		}
		
		return clusters;
	}
	
}
