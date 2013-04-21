package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.List;

public class KeywordsCluster {

	//public String keyword;
	public double sum=0;
	public List<org.carrot2.core.Document> documents = new ArrayList<org.carrot2.core.Document>();
	
	
	public boolean add(org.carrot2.core.Document doc)
	{
		for(org.carrot2.core.Document d:documents)
		{
			if(d.getTitle().equals(doc.getTitle()))
				return false;
		}
		
		documents.add(doc);
		return true;
	}
}
