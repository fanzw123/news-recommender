package com.fiit.lusinda.topicmodelling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Topic {

	List <DocumentProbability> documentProbabilities;
	int topicId;
	
	public List <DocumentProbability> getDocumentProbabilities()
	{
		return documentProbabilities;
	}

	
	public Topic(int topicId,int maxDocuments)
	{
		this.topicId = topicId;
		documentProbabilities = new ArrayList<DocumentProbability>();
		
	}
	public String toString()
	{
		return Integer.toString(topicId);
		
	}
	
	public void Sort()
	{
		Collections.sort(documentProbabilities);
		
		Collections.reverse(documentProbabilities);
	}
	
	public DocumentProbability getDocumentProbability(int i)
	{
		return documentProbabilities.get(i);
	}
	
	public void Add(DocumentProbability docProb)
	{
		this.documentProbabilities.add(docProb);
	}
	
	public void AddOrLeave(String docId,double prob)
	{
		for(int i=0;i<documentProbabilities.size();i++)
		{
			if(prob>documentProbabilities.get(i).prob)
			{
				
				for(int j=documentProbabilities.size()-1;j>i;j--)
				{
					documentProbabilities.set(j, documentProbabilities.get(j-1));
				}
				
				documentProbabilities.set(i,new DocumentProbability(docId,prob));
				
				break;
			}
		}
	}
	
}
