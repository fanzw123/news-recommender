package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import cc.mallet.types.IDSorter;

public class WordCluster {

	public int id;
	public String word;
	public TreeSet<IDSorter> words = new TreeSet<IDSorter>();
	public int sum = 0;
	public void sum()
	{
		Iterator<IDSorter> it = words.iterator();
		sum = 0;
		while(it.hasNext())
		{
			IDSorter word = it.next();
			sum+=word.getWeight();
		}
	}
}
