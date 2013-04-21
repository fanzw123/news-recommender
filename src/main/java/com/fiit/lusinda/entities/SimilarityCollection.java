package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.List;

import com.fiit.lusinda.similarity.SimilarityMeasureStrategy;

public class SimilarityCollection extends ArrayList<SimilarityItem> {
	
	SimilarItem item;
	double treeshold ;
	SimilarityMeasureStrategy measure;
	
	public SimilarityCollection(SimilarItem item,double treeshold,SimilarityMeasureStrategy measure)
	{
		this.item = item;
		this.treeshold = treeshold;
		this.measure = measure;
		
	}
	
	public void addItems(List<SimilarItem> items,int max)
	{
		int totalItems=0;
		for(SimilarItem i:items)
		{
			if(totalItems>max)
				break;
			
			if(i.getTs()==item.getTs())
			{
			double result = 1 - measure.computeSimmilarity(item.getDist(), i.getDist());
			if(result > treeshold)
				this.add(new SimilarityItem(i, result));
			totalItems++;
			}
		}
	}

}
