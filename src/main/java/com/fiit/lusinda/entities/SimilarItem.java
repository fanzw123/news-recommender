package com.fiit.lusinda.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fiit.lusinda.similarity.JsSimilarityMeasure;

public class SimilarItem implements Similar{

	public Map<Long, double[]> dist = new HashMap<Long, double[]>();
	long ts;
	SimilarityCollection similarItems;
	public int id;


	public SimilarItem(long ts,int id) {
		this.ts = ts;
		this.id = id;
		
		this.dist.put(ts, null);
		
		similarItems = new SimilarityCollection(this, 0.8, new JsSimilarityMeasure());

	}
	

	public double[] getDist() {
		return dist.get(this.ts);
	}
	
	public void addTopicDist(long ts,double[] dist)
	{
		this.dist.put(ts, dist);
	}
	

public void cumputeSimilarItems(List<SimilarItem> items,int max) {
		
	similarItems.addItems(items,max);
	}

public long getTs() {
	
	return ts;
}
}