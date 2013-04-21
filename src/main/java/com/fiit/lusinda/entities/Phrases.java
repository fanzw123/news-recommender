package com.fiit.lusinda.entities;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;

import cc.mallet.types.IDSorter;

public class Phrases {

	HashMap<String, IDSorter> words;
	HashMap<String, NGram> phrasesCounts = new HashMap<String, NGram>();;

	public Phrases() {

	}
	
	public boolean containsKey(String key)
	{
		return words.containsKey(key);
	}

	public void setWords(HashMap<String, IDSorter> words) {
		this.words = words;
	}

	public WordTO getWordTO(String word) {
		boolean isUpper = StandardTextProcessing.isUpperCase(word);

		word = StandardTextProcessing.removePunct(StandardTextProcessing
				.toLowerCase(word));
		String lemmatizedWord = null;
		try {
			lemmatizedWord = Lemmatizer.getLemmatizer().lemmatizeFirst(word);
		} catch (URISyntaxException e) {

		}
		boolean stem = false;
		if (lemmatizedWord == null) {
			lemmatizedWord = word;
			stem = true;
		} else
			stem = false;

		if (words.containsKey(lemmatizedWord))
			return new WordTO(word, lemmatizedWord, words.get(lemmatizedWord),
					stem, isUpper);
		else
			return null;
	}

	public NGram createNGram(String word) {
		NGram ngram = null;
		WordTO wto = getWordTO(word);
		if (wto != null)
			ngram = new NGram(wto.word, wto.lemmatizedWord, wto.feature,
					wto.stem, wto.isUpper);

		return ngram;
	}

	public NGram addNGram(NGram ngram) {
	//	if (ngram.getUpperCounts() > 0) {
		
		
			ngram.flush();

			NGram ng = phrasesCounts.get(ngram.getKey());
			if (ng != null)
				ng.Counts++;
			else 		
				ng = ngram;
			
			phrasesCounts.put(ngram.getKey(), ng);
	//	}
		
			return ngram;
	}

	public void computeTfIdf(int[][] topicWordsAssigments, int totalAssigments,
			int numTopics) {
		for (NGram ngram : phrasesCounts.values()) {
			ngram.computeTfIdf(topicWordsAssigments, totalAssigments, numTopics);
		}
	}
	
	public List<NGram> getSortedNGrams(int max, double treeshold,int numParts) {
		List<NGram> sorted = new ArrayList<NGram>();

		TreeSet<IDSorter> sortedTree = new TreeSet<IDSorter>();

		NGram[] values = (NGram[]) phrasesCounts.values().toArray(new NGram[0]);

		for (int i = 0; i < values.length; i++) {

			if (values[i].Counts > 1 & values[i].getOriginalParts().size()>1)
				sortedTree.add(new IDSorter(i, values[i].Counts));//.getWeight()));
		}

		if (sortedTree.isEmpty())
			return sorted;

		Iterator<IDSorter> it = sortedTree.iterator();
		int i = 0;
		IDSorter ids = it.next();
		double weight = ids.getWeight();
		while (it.hasNext() && i < max && weight > treeshold) {


			sorted.add(values[ids.getID()]);

			ids = it.next();
			weight = ids.getWeight();
			i++;
		}

		return sorted;

	}

	public List<NGram> getSortedNGrams(int max, double treeshold) {
		List<NGram> sorted = new ArrayList<NGram>();

		TreeSet<IDSorter> sortedTree = new TreeSet<IDSorter>();

		NGram[] values = (NGram[]) phrasesCounts.values().toArray(new NGram[0]);

		for (int i = 0; i < values.length; i++) {

			if (values[i].Counts > 1)
				sortedTree.add(new IDSorter(i, values[i].Counts));//.getWeight()));
		}

		if (sortedTree.isEmpty())
			return sorted;

		Iterator<IDSorter> it = sortedTree.iterator();
		int i = 0;
		IDSorter ids = it.next();
		double weight = ids.getWeight();
		while (it.hasNext() && i < max && weight > treeshold) {


			sorted.add(values[ids.getID()]);

			ids = it.next();
			weight = ids.getWeight();
			i++;
		}

		return sorted;

	}
}
