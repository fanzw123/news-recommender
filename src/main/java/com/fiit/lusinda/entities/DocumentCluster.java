package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DocumentCluster {

	private TreeSet<Sorter<Article>> docs = new TreeSet<Sorter<Article>>();
	private List<NGram> labels = new ArrayList<NGram>();
	private String label;
	private double weight=0;
	private StringBuilder labelsBuffer = new StringBuilder();
	
	
	public String getLabel()
	{
		return label;
	//	return labels.size()==0?null:labels.get(0).getNiceNGram();
	}
	
	public TreeSet<Sorter<Article>> getDocs()
	{
		return docs;
	}
	
	public void addDoc(Sorter<Article> a)
	{
		docs.add(a);
	}
	
	public void flush()
	{
		for(NGram n:labels)
			weight+=n.getWeight();
		
		label = labelsBuffer.toString();
	}
	
	public double getWeight()
	{
		return weight;
	}
	
	public void addLabels(List<NGram> labels)
	{
		for(NGram l:labels)
			addLabel(l);
	}
	
	public void addLabel(NGram l)
	{
		if(!labels.contains(l))
		{
		labels.add(l);
		labelsBuffer.append(l.getNiceNGram());
		labelsBuffer.append(", ");
		}
	}
	
	
}
