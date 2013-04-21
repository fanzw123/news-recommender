package com.fiit.lusinda.topicmodelling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeSet;

import org.wicketstuff.datatable_autocomplete.trie.ITrieConfiguration;
import org.wicketstuff.datatable_autocomplete.trie.PatriciaTrie;

import com.fiit.lusinda.entities.AlphabetMapping;
import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.Bigram;
import com.fiit.lusinda.entities.NGram;
import com.fiit.lusinda.entities.Phrases;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.entities.StringDoublePair;
import com.fiit.lusinda.entities.WordTO;

import com.fiit.lusinda.entities.WordCluster;
import com.fiit.lusinda.similarity.CosineSimilarity;
import com.fiit.lusinda.similarity.JackardSimilarity;
import com.fiit.lusinda.similarity.JsSimilarityMeasure;
import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;

import cc.mallet.extract.StringTokenization;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.topics.TopicModelDiagnostics;
import cc.mallet.types.FeatureCounter;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.FeatureSequenceWithBigrams;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import ds.tree.RadixTree;
import ds.tree.RadixTreeImpl;

public class Lda extends ParallelTopicModel {

	public Lda(int numberOfTopics) {
		super(numberOfTopics);
		// TODO Auto-generated constructor stub
	}

	public Lda(int topics, double alpha, double beta) {
		super(topics, alpha, beta);
		// TODO Auto-generated constructor stub
	}

	public int[] geTtokensPerTopic() {
		return this.tokensPerTopic;
	}

	private int getTopicIndex(int wordIndex, int targetTopic) {
		int currentTopic = -1;
		int topicIndex = -1;

		for (topicIndex = 0; topicIndex < typeTopicCounts[wordIndex].length; topicIndex++) {
			currentTopic = typeTopicCounts[wordIndex][topicIndex];
			if (currentTopic == 0) {
				currentTopic = -1;
				continue;
			}
			currentTopic = typeTopicCounts[wordIndex][topicIndex] & topicMask;
			if (currentTopic == targetTopic)

				break;
			else
				currentTopic = -1;
		}

		if (targetTopic == currentTopic)
			return topicIndex;
		else
			return -1;
	}

	private void printWordClustersOrderBySum(List<WordCluster> wordClusters,
			int max) {

		TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

		for (WordCluster cluster : wordClusters) {
			sorted.add(new IDSorter(cluster.words.first().getID(),
					(double) cluster.sum));
		}

		printSortedWords(sorted, null, max, 2);

	}

	private void printSortedWordClusters(List<WordCluster> wordClusters, int max) {

		for (int i = 0; i < wordClusters.size() && i < max; i++) {
			WordCluster cluster = wordClusters.get(i);
			printWordCluster(cluster, 10);

		}

		System.out.println();

	}

	private int printWordCluster(WordCluster cluster, int count) {
		Iterator<IDSorter> it = cluster.words.iterator();
		int i = 0;
		if (cluster.words.size() > 1)
			System.out.print("{ ");
		while (it.hasNext() && i < count) {

			IDSorter word = it.next();
			System.out.print((String) alphabet.lookupObject(word.getID()));
			System.out.print(":");
			System.out.print(word.getWeight());
			System.out.print(", ");
			i++;

		}
		if (cluster.words.size() > 1)
			System.out.print(" }, ");

		return i;
	}

	private int countKeyWords(List<WordCluster> wordClusters) {
		int count = 0;
		for (WordCluster cluster : wordClusters) {
			if (cluster.words.size() > 1) {
				count++;
			}

		}

		return count;
	}

	private int printKeywords(List<WordCluster> wordClusters, String[] prefix,
			int max) {
		int count = 0;
		int count2 = 0;
		for (WordCluster cluster : wordClusters) {
			if (count == max)
				break;
			if (cluster.words.size() > 1) {
				if (prefix == null || cluster.word.startsWith(prefix[0])) {
					count2 += printWordCluster(cluster, 20);
					count++;
				}
			}

		}
		System.out.println();
		// System.out.println("total count: " + count + " ");

		return count2;
	}

	private TreeSet<IDSorter> sortWordClusters(List<WordCluster> wordClusters) {

		TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

		for (WordCluster cluster : wordClusters) {

			Iterator<IDSorter> it = cluster.words.iterator();
			IDSorter word = cluster.words.first();
			int sum = 0;
			while (it.hasNext()) {
				sum += it.next().getWeight();
			}
			word.set(word.getID(), sum);
			sorted.add(word);
		}

		return sorted;
	}

	public List<Phrases> getPhrases() {
		return phrasesList;
	}

	transient Map<String,NGram> uniqueKeywordsDict;
	transient List<NGram> uniqueKeywords;
	
	public List<NGram> getUniqueKeywords()
	{
		return uniqueKeywords;
		
	}
	
	public TreeSet<StringDoublePair> getTopSimilarDocuments(String word)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {

		TreeSet<StringDoublePair> similarDocuments = new TreeSet<StringDoublePair>();

		return similarDocuments;
	}

	public void printTopSimilarDocumentsGivenWord()
	{
		List<Phrases> phrases = getPhrases();

		List<NGram> keywords = new ArrayList<NGram>();

		for (Phrases p : phrases) {
			keywords.addAll(p.getSortedNGrams(200000, 0));
		}

		
		

//		for(int w=0;w<numTypes;w++)
//		{
//			double probWordDocumentTotal = 0;
//			
//				for (int z = 0; z < numTopics; z++) {
//
//					double prob = phi[w][z];
//					prob = prob * article.dist[z];
//					probWordDocumentTotal += prob;
//
//				}
//				similarWords.add(new StringDoublePair((String)this.alphabet.lookupObject(w), probWordDocumentTotal));
//		}

		int max = 10;
		for(int k=0;k<numTopics;k++)
		{
			int[] topIndicies = getWordIndicies(k, max);
		for(int w=topIndicies[0],j=0;j<max;w=topIndicies[j],j++){
		
			

			double probWordDocumentTotal = 0;

			TreeSet<StringDoublePair> similarDocs = new TreeSet<StringDoublePair>();
			
			for(int doc=0;doc<data.size();doc++)
			{
			
			
				
				for (int z = 0; z < numTopics; z++) {

					double prob = phi[w][z];
					prob = prob * getTopicProbabilities(doc)[z];
					probWordDocumentTotal+=prob;

				}
				
			similarDocs.add(new StringDoublePair((String)data.get(doc).instance.getName(), probWordDocumentTotal));
			}
			
		
			//print
			Iterator<StringDoublePair> it = similarDocs.iterator();
			
			System.out.println((String)this.alphabet.lookupObject(w));
			int i=0;
			while(it.hasNext() && i<10)
			{
				StringDoublePair sd = it.next();
				//if(sd.value<treshold)
					//break;
				System.out.print("\t");
				System.out.print(sd.name);
				System.out.print(" :");
				System.out.print(sd.value);
				System.out.println();
				i++;
			}
			System.out.println();
		}
		}

	}
	
	
	public double getDocumentWordsLikelihoodScore(Article article,List<NGram> query)
	{
		double score = 0;
		
		for(NGram q:query)
		{
			score+=getLikelihood(q, article.dist);
		}
		
		return score;
	}
	
	public double getDocumentWordsLikelihoodScore(Article article,TreeSet<Sorter<NGram>> query,int max)
	{
		double score = 0;
		int i=0;
		Iterator<Sorter<NGram>> it = query.iterator();
		while(it.hasNext() && i <max)
		{
			Sorter<NGram> sd = it.next();
			score+=sd.weight*getLikelihood(sd.data, article.dist);
			i++;
		}
		
		
		return score;
	}
	
	public TreeSet<Sorter<NGram>> getDocumentWordsLikelihood(Article article,List<NGram> keywords)
	{
		TreeSet<Sorter<NGram>> similarWords = new TreeSet<Sorter<NGram>>();

//		for(int w=0;w<numTypes;w++)
//		{
//			double probWordDocumentTotal = 0;
//			
//				for (int z = 0; z < numTopics; z++) {
//					if(this.diagnostics.getRank1Percent().scores[z]<0.1)
//						continue;
//					double prob = phi[w][z];
//					prob = prob * article.dist[z];
//					probWordDocumentTotal += prob;
//
//				}
//			
//				similarWords.add(new StringDoublePair((String)this.alphabet.lookupObject(w), probWordDocumentTotal));
//		}

		
		for (int kw = 0; kw < keywords.size(); kw++) {

			if(article.content.contains(keywords.get(kw).getNiceNGram()))
////			if(keywords.get(kw).getOriginalParts().size()>1)
		{
				//System.out.println(keywords.get(kw).getNiceNGram());
				similarWords.add(new Sorter<NGram>(-1,keywords.get(kw), getLikelihood(keywords.get(kw), article.dist)));
			}
				
	
			
		
		}

		return similarWords;
	}
	
	public TreeSet<Sorter<NGram>> getDocumentWordsLikelihood(Article article,boolean onlyNE) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException
	{
		if(!onlyNE)
			return getDocumentWordsLikelihood(article);
		else
		{
			List<NGram> keywords = getUniqueKeywords();
			List<NGram> keywords2 = new ArrayList<NGram>();
			for(NGram keyword:keywords)
			{
				if(keyword.getUpperCounts()>0 || article.content.contains(keyword.getNiceNGram()))
				{
					//System.out.println(keyword.getNiceNGram());
					keywords2.add(keyword);	
				}
			}
			return getDocumentWordsLikelihood(article,keywords2);

		}
	}
	
	public TreeSet<Sorter<NGram>> getDocumentWordsLikelihood(Article article)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {

		List<NGram> keywords = getUniqueKeywords();
		
//		List<Phrases> phrases = getPhrases();
//
//		List<NGram> keywords = new ArrayList<NGram>();
//
//		for (Phrases p : phrases) {
//			keywords.addAll(p.getSortedNGrams(200000, 0));
//		}

		return getDocumentWordsLikelihood(article,keywords);
	}

	public double getLikelihood(NGram keyword, double[] dist)
	{
		List<String> parts = keyword.getLemmatizedParts();

		double probWordDocumentTotal = 0;
		double avg = 0;
		
			
			for (int z = 0; z < numTopics; z++) {
				if(this.diagnostics.getRank1Percent().scores[z]<0.1)
					continue;
				
				double weight = 0;
				
				if(this.uniqueKeywordsDict.containsKey(keyword.getKey()))
				{
					
					weight = getWordPhi(parts.get(0), z);
					for (String part : parts) {
						double wNew = getWordPhi(part, z);
						if(wNew<weight)
							weight = wNew;
					}
				}
				double prob = weight;//phi[partials.get(0).getID()][z];
				prob = prob * dist[z];
				probWordDocumentTotal +=prob;
					
				

			}
			
			return probWordDocumentTotal;
	}
	private double getWordPhi(int id,int z)
	{
		return phi[id][z];
	}
	
	private double getWordPhi(String word,int z)
	{
		int inx = alphabet.lookupIndex(word);
		if(inx<phi.length)
			return phi[inx][z];
		else
		{
			//System.out.println("bad index: "+word);
			return 0;
		}
	}
	
	public List<Phrases> extractPhrases(Instance doc)
	{
		List<Phrases> phrases = new ArrayList<Phrases>();
		
		
		
		return phrases;
	}
	
	
	private double computePredictiveLikelihood(int w1, int w2) {
		double probWordWordTotal = 0;
		for (int z = 0; z < numTopics; z++) {

			double prob = getPhi()[w2][z];
			prob = prob * getPhi()[w1][z];
			probWordWordTotal += prob;

		}
		// probWordWordTotal = js.computeSimmilarity(
		// wordmatrix.getRow(w1), wordmatrix.getRow(w2));

		return probWordWordTotal;

	}

	public void printTopics(int topic, TreeSet<IDSorter> simTopics, Lda lda,
			int max, boolean useBigrams) throws FileNotFoundException,
			UnsupportedEncodingException, URISyntaxException {
		TreeSet<IDSorter> sorted = getSortedWords(topic);

		System.out.println(topic);

		// if (useBigrams)
		// findBigrams(sorted, topic);
		// else
		printSortedWords(sorted, null, 10, 0);
		//
		// System.out.print("original doc: ");
		// System.out.println(getTopDocumentsString(topic, 1, "\n"));
		// printSortedWords(sorted, null, 10, 0);
		// System.out.println();
		Iterator<IDSorter> it = simTopics.iterator();
		int i = 0;
		while (it.hasNext() && i < max) {

			IDSorter s = it.next();
			// System.out.print("\trelated topic: " + s.getID() + ": " +
			// s.getWeight() + " ");
			// System.out.println(s.getWeight() + "\t");
			// printWords(commonWords[topic][s.getID()]);

			// System.out.print("\trelated topic: ");
			// if (useBigrams)
			// lda.findBigrams(lda.getSortedWords(s.getID()), s.getID());
			// else
			lda.printSortedWords(lda.getSortedWords(s.getID()), null, 10, 0);
			//
			// System.out.print("\trelated article: ");
			// System.out.println(lda.getTopDocumentsString(s.getID(), 1,
			// "\n"));
			i++;
		}
	}

	public void printTopics(int topic, TreeSet<IDSorter> simTopics, int max) {
		printSortedWords(getSortedWords(topic), null, 10, 0);

		// System.out.println();
		Iterator<IDSorter> it = simTopics.iterator();
		int i = 0;
		while (it.hasNext() && i < max) {

			IDSorter s = it.next();
			System.out.print("related topic: " + s.getID() + ": "
					+ s.getWeight() + " ");
			printSortedWords(getSortedWords(s.getID()), null, 10, 0);
			i++;
		}
	}

	private List<List<String>> topicBigrams = new ArrayList<List<String>>();

	private Map<String, String> bigramsNiceNames = new HashMap<String, String>();
	private Map<String, Integer> bigramsDict = new HashMap<String, Integer>();
	private ArrayList<String> bigramsIndicies = new ArrayList<String>();

	private void addBigramToDict(String bigram, String niceName, int k) {
		if (!bigramsDict.containsKey(bigram)) {
			bigramsIndicies.add(bigram);
			int index = bigramsIndicies.size() - 1;
			bigramsDict.put(bigram, index);
			bigramsNiceNames.put(bigram, niceName);
			topicBigrams.get(0).add(bigram);
		}
	}

	public void findBigrams() throws FileNotFoundException,
			UnsupportedEncodingException, URISyntaxException {
		ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
		for (int k = 0; k < sorted.size(); k++) {
			topicBigrams.add(new ArrayList<String>());
			findBigrams(sorted.get(k), k);
		}
	}

	public List<String> getBigrams(int k) {
		return topicBigrams.get(k);
	}

	public void findBigrams2() throws FileNotFoundException,
			UnsupportedEncodingException, URISyntaxException {
		ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
		phrasesList = new ArrayList<Phrases>();
		for (int k = 0; k < sorted.size(); k++) {
			phrasesList.add(findBigrams2(sorted.get(k), k));
		}
	}

	transient private List<Phrases> phrasesList;

	public Phrases findBigrams2(TreeSet<IDSorter> sorted, int k)
			throws URISyntaxException, FileNotFoundException,
			UnsupportedEncodingException {

		int n = 3000;

		NGram ngram = null;

		Phrases phrases = new Phrases();

		HashMap<String, IDSorter> words = getSortedWords(sorted,k, null, n, 0);
		phrases.setWords(words);

		// System.out.println(words.keySet().size());

		for (int d = 0; d < data.size(); d++) {
			TopicAssignment t = this.getData().get(d);
			Instance instance = t.instance;

			TokenSequence ts = (TokenSequence) instance.getSource();

			int doclen = ts.size();

			int topic = -1;

			String word = null;
			String lemmatizedWord = null;
			int nextFeatureTopic = -1;
			String nextWord = null;
			String lemmatizedNextWord = null;
			String previousWord = null;
			boolean stem = false;
			for (int pi = 0, next = 1; pi < doclen; pi += next + 1, next = 1) {

				word = (String) ts.get(pi).getText();

				// previousWord = pi == 0 ? word : ts.get(pi - 1).getText();

				// if (!StandardTextProcessing.containsPunct(previousWord)
				// && StandardTextProcessing.startsWithUpper(word)) {

				// int index = alphabet.lookupIndex(lemmatizedWord);

				// topic =
				// this.getData().get(d).topicSequence.getIndexAtPosition(pi);

				ngram = phrases.createNGram(word);

				if (ngram != null) {// || (topic == k &&
									// StandardTextProcessing.startsWithUpper(word))
									// ) {

					try {

						nextWord = (String) ts.get(pi + next).getText(); // while
																			// (nextFeatureTopic
																			// ==
																			// topic
																			// ){

						WordTO wto = phrases.getWordTO(nextWord);
						previousWord = word;

						// int index =
						// alphabet.lookupIndex(lemmatizedNextWord);
						// nextFeatureTopic =
						// this.getData().get(d).topicSequence
						// .getIndexAtPosition(index);

						// or index<0
						while (wto != null
								&& words.containsKey(wto.lemmatizedWord)
						// &&
						// !StandardTextProcessing.containsPunct(previousWord)
						// &&
						// StandardTextProcessing.startsWithUpper(nextWord)
						) { // ||
							// (topic
							// ==
							// nextFeatureTopic
							// &&
							// StandardTextProcessing.startsWithUpper(nextWord)))
							// )
							// {

							ngram.add(wto.word, wto.lemmatizedWord,
									wto.feature, wto.stem, wto.isUpper);

							next++;
							previousWord = nextWord;
							nextWord = (String) ts.get(pi + next).getText();
							wto = phrases.getWordTO(nextWord);

							// index =
							// alphabet.lookupIndex(lemmatizedNextWord);
							// nextFeatureTopic =
							// this.getData().get(d).topicSequence
							// .getIndexAtPosition(pi + next);

						}
					} catch (IndexOutOfBoundsException e) {
					}

					// if (next > 1) {

					phrases.addNGram(ngram);

				}

				// }

				// phrases.computeTfIdf(topicWordsAssigments,tokensPerTopic[k],numTopics);

			}

			List<NGram> sortedPhrases = phrases.getSortedNGrams(10, 0);

			for (NGram ng : sortedPhrases) {
				// addBigramToDict(ng.getKey(), ng.getNiceNGram());
				// System.out.print(ng.getNiceNGram());
				// System.out.print(" ");
				// System.out.print(ng.getWeight());
				// System.out.print(" ");
				// System.out.print(ng.getTfIdf());
				// System.out.print(" ");

				// System.out.print(ng.Counts);
				// System.out.print(" ");

			}
			// System.out.println();
		}

		return phrases;

	}

	public void findBigrams3() throws URISyntaxException,
			FileNotFoundException, UnsupportedEncodingException {

		int n = 3000;

		NGram ngram = null;
		uniqueKeywordsDict = new HashMap<String,NGram>();

		ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
		phrasesList = new ArrayList<Phrases>();
		for (int k = 0; k < sorted.size(); k++) {
			Phrases phrases = new Phrases();
			phrases.setWords(getSortedWords(sorted.get(k),k, null, n, 0));
			phrasesList.add(phrases);
		}

		// System.out.println(words.keySet().size());

		for (int d = 0; d < data.size(); d++) {
			TopicAssignment t = this.getData().get(d);
			Instance instance = t.instance;

			TokenSequence ts = (TokenSequence) instance.getSource();

			int doclen = ts.size();

			int topic = -1;
			String word = null;
			String lemmatizedWord = null;
			int nextFeatureTopic = -1;
			String nextWord = null;
			String lemmatizedNextWord = null;
			String previousWord = null;
			boolean stem = false;
			for (int pi = 0, next = 1; pi < t.topicSequence.getLength(); pi++, next = 1) {

				word = (String) ts.get(pi).getText();
				

				topic = t.topicSequence.getIndexAtPosition(pi);
				
//				if(this.diagnostics.getRank1Percent().scores[topic]<0.1)
//				{
//					System.out.println("ingoring: "+word);
//						continue;
//				}
//				
				// previousWord = pi == 0 ? word : ts.get(pi - 1).getText();

				// if (!StandardTextProcessing.containsPunct(previousWord)
				// && StandardTextProcessing.startsWithUpper(word)) {

				// int index = alphabet.lookupIndex(lemmatizedWord);

				// topic =
				// this.getData().get(d).topicSequence.getIndexAtPosition(index);

				ngram = phrasesList.get(topic).createNGram(word);

				if (ngram != null) {// || (topic == k &&
									// StandardTextProcessing.startsWithUpper(word))
									// ) {

					try {

						nextWord = (String) ts.get(pi + next).getText(); // while
																			// (nextFeatureTopic
																			// ==
																			// topic
																			// ){

						WordTO wto = phrasesList.get(topic).getWordTO(nextWord);
						previousWord = word;

						// int index =
						// alphabet.lookupIndex(lemmatizedNextWord);
						// nextFeatureTopic =
						// this.getData().get(d).topicSequence
						// .getIndexAtPosition(index);

						// or index<0
						while (wto != null
								&& phrasesList.get(topic).containsKey(
										wto.lemmatizedWord)
						// &&
						// !StandardTextProcessing.containsPunct(previousWord)
						// &&
						// StandardTextProcessing.startsWithUpper(nextWord)
						) { // ||
							// (topic
							// ==
							// nextFeatureTopic
							// &&
							// StandardTextProcessing.startsWithUpper(nextWord)))
							// )
							// {

							ngram.add(wto.word, wto.lemmatizedWord,
									wto.feature, wto.stem, wto.isUpper);

							next++;
							previousWord = nextWord;
							nextWord = (String) ts.get(pi + next).getText();
							wto = phrasesList.get(topic).getWordTO(nextWord);

							// index =
							// alphabet.lookupIndex(lemmatizedNextWord);
							// nextFeatureTopic =
							// this.getData().get(d).topicSequence
							// .getIndexAtPosition(pi + next);

						}
					} catch (IndexOutOfBoundsException e) {
					}

					// if (next > 1) {

					ngram =  phrasesList.get(topic).addNGram(ngram);
					if(!uniqueKeywordsDict.containsKey(ngram.getKey()) )
						uniqueKeywordsDict.put(ngram.getKey(), ngram);

				}

				// }

				// phrases.computeTfIdf(topicWordsAssigments,tokensPerTopic[k],numTopics);

			}

			List<NGram> sortedPhrases = phrasesList.get(topic).getSortedNGrams(
					10, 0);

			for (NGram ng : sortedPhrases) {
				// addBigramToDict(ng.getKey(), ng.getNiceNGram());
				// System.out.print(ng.getNiceNGram());
				// System.out.print(" ");
				// System.out.print(ng.getWeight());
				// System.out.print(" ");
				// System.out.print(ng.getTfIdf());
				// System.out.print(" ");

				// System.out.print(ng.Counts);
				// System.out.print(" ");

			}

		}
		
		//uniqueWords

	 this.uniqueKeywords = new ArrayList<NGram>(uniqueKeywordsDict.values());
		
		
		System.out.println();

	}

	InstanceList testing = null;

	public double[] inferDist(String content) {
		testing = new InstanceList(this.getPipe());
		testing.addThruPipe(new Instance(content, null, null, null));

		return this.getInferencer().getSampledDistribution(
				testing.get(testing.size() - 1), 10, 1, 5);
	}
	
	transient List<Article>  articles;
	
	public List<Article> getDocumentsAsArticles()
	{
		return articles;
	}
	
	public void convertDocumentsToArticles()
	{
		articles = new ArrayList<Article>();
		
		for (int d1 = 0; d1 < data.size(); d1++) {
	
				String docName = (String) data.get(d1).instance.getName();
					
				String key =(String) data.get(d1).instance.getTarget();
				
					Article a= new Article(d1,key,docName,this.getDocuments().get(d1),0,getTopicProbabilities(d1));
		
					
					articles.add(a);
			}
		
		
	}
	
	public static List<NGram> getListOfNgrams(TreeSet<Sorter<NGram>> ngrams,int max)
	{
	
		List<NGram> list = new ArrayList<NGram>();
		
		int i = 0;
		Iterator<Sorter<NGram>> it = ngrams.iterator();
		// build bigrams dictionary
		while (it.hasNext() && i < max) {
			Sorter<NGram> s = it.next();

			list.add(s.data);
				i++;
		}
		
		return list;
	}

	public TreeSet<Sorter<Article>> getWordsDocumentsLikelihood(List<NGram> query,
			int modelId, int max,boolean experimental) {
		testing = new InstanceList(this.getPipe());

		TreeSet<Sorter<Article>> sorted = new TreeSet<Sorter<Article>>();

		JsSimilarityMeasure js = new JsSimilarityMeasure();
		CosineSimilarity cosine = new CosineSimilarity();

		

		double sim = 0;
		double score = 0;
		Map<String,Sorter<Article>> docFreq = new HashMap<String,Sorter<Article>>(); 
		
		for(NGram q:query)
		{
		for (int d1 = 0; d1 < data.size(); d1++) {

			double[] sourceTheta = this.getTopicProbabilities(d1);

			score = getLikelihood(q,sourceTheta);
				// if(score>0.1)
				String docName = new StringBuilder().append("(")
						.append(modelId).append(") ")
						.append((String) data.get(d1).instance.getName())
						.toString();
				
				String key =(String) data.get(d1).instance.getTarget();
				
				Sorter<Article> sorter = docFreq.get(docName);
				if(sorter == null)
					docFreq.put(docName, new Sorter<Article>(modelId,new Article(d1,key,docName,this.getDocuments().get(d1),sim,getTopicProbabilities(d1)),score));
				else
				{
					sorter.weight+=score;
					docFreq.put(docName, sorter);
				}
				
				//sorted.add(new Sorter<Article>(new Article(d1,modelId,docName,sim,getTopicProbabilities(d1)),sim));// StringDoublePair(d1,docName, sim));
				
				
				// sortedDistJs.add(new IDSorter(d1,sim));
				// sortedDistJsCosine.add(new IDSorter(d1,score));

			}
		
		
	}
		for(Entry<String,Sorter<Article>> entry:docFreq.entrySet())
		{
		//	System.out.println(entry.getKey());
			sorted.add(entry.getValue());
		}
		
	
		return sorted;
	}
	
	
	
	public TreeSet<Sorter<Article>> getSimilarDocuments(String doc,
			String name, int modelId, int max,boolean experimental) {
		testing = new InstanceList(this.getPipe());

		TreeSet<Sorter<Article>> sorted = new TreeSet<Sorter<Article>>();

		JsSimilarityMeasure js = new JsSimilarityMeasure();
		CosineSimilarity cosine = new CosineSimilarity();

		testing.addThruPipe(new Instance(doc, null, name, null));
		
		double sim = 0;
		double score = 0;
		double[] destTheta = this.getInferencer().getSampledDistribution(
				testing.get(testing.size() - 1), 10, 1, 5);
		double[][] phi = getTopicWordDist();
		double averageTokens = 1600;

		TreeSet<IDSorter> sortedDistJs = new TreeSet<IDSorter>();
		TreeSet<IDSorter> sortedDistJsCosine = new TreeSet<IDSorter>();
		
//		if(experimental)
//		{
//			for(int i=0;i<destTheta.length;i++)
//			{
//				if(diagnostics.getRank1Percent().scores[i]<0.09)
//					destTheta[i]=0;
//			}
//		}
		

		for (int d1 = 0; d1 < data.size(); d1++) {

			double[] sourceTheta = this.getTopicProbabilities(d1);
			
//			if(experimental)
//			{
//				for(int i=0;i<sourceTheta.length;i++)
//				{
//					if(diagnostics.getRank1Percent().scores[i]<0.09)
//						sourceTheta[i]=0;
//						
//				}
//				
//			}

			sim = 1 - js.computeSimmilarity(sourceTheta, destTheta);

			if (sim > 0.6) {

				// score = (double)score/(double)numTopics*2;

				score = cosine.calculateSimilarity(sourceTheta, destTheta,
						diagnostics.getRank1Percent().scores, 0.09);
				// score = score*sim;
			
			if(experimental)	
			{	
				score = score*sim;
			}
			else
				score = sim;
				
	
				
				// if(score>0.1)
				String docName = new StringBuilder().append("(")
						.append(modelId).append(") ")
						.append((String) data.get(d1).instance.getName())
						.toString();
				String key =(String) data.get(d1).instance.getTarget();
				
				//sorted.add(new Sorter<Article>(new Article(d1,modelId,docName,sim,getTopicProbabilities(d1)),sim));// StringDoublePair(d1,docName, sim));
				sorted.add(new Sorter<Article>(modelId,new Article(d1,key,docName,getDocuments().get(d1),sim,getTopicProbabilities(d1)),score));
				
				// sortedDistJs.add(new IDSorter(d1,sim));
				// sortedDistJsCosine.add(new IDSorter(d1,score));

			}
		}

		TreeSet<Sorter<Article>> result = new TreeSet<Sorter<Article>>();
		int i = 0;
		Iterator<Sorter<Article>> it = sorted.iterator();

		while (it.hasNext() && i < max) {
			result.add(it.next());
			i++;
		}

		return result;
	}
	
	
	public TreeSet<Sorter<Article>> getSimilarDocuments(Article article, int max,boolean experimental) {
		

		TreeSet<Sorter<Article>> sorted = new TreeSet<Sorter<Article>>();

		JsSimilarityMeasure js = new JsSimilarityMeasure();
		CosineSimilarity cosine = new CosineSimilarity();

		
		
		double sim = 0;
		double score = 0;
		
		double[][] phi = getTopicWordDist();
		double averageTokens = 1600;

		TreeSet<IDSorter> sortedDistJs = new TreeSet<IDSorter>();
		TreeSet<IDSorter> sortedDistJsCosine = new TreeSet<IDSorter>();

		
		for (Article a:articles) {

			if(article.equals(a))
				continue;
			

			sim = 1 - js.computeSimmilarity(a.dist, article.dist);

			if (sim > 0.6) {

				// score = (double)score/(double)numTopics*2;

				score = cosine.calculateSimilarity(a.dist, article.dist,
						diagnostics.getRank1Percent().scores, 0.09);
				// score = score*sim;
			
			if(experimental)	
			{
				score = score*sim;
				if(score>0.4)
					sorted.add(new Sorter<Article>(-1,a,score));
			}
			else
			{
				score = sim;
				sorted.add(new Sorter<Article>(-1,a,score));
			}
				
	
				
				// if(score>0.1)
				
				//sorted.add(new Sorter<Article>(new Article(d1,modelId,docName,sim,getTopicProbabilities(d1)),sim));// StringDoublePair(d1,docName, sim));
				
				
				// sortedDistJs.add(new IDSorter(d1,sim));
				// sortedDistJsCosine.add(new IDSorter(d1,score));

			}
		}

		TreeSet<Sorter<Article>> result = new TreeSet<Sorter<Article>>();
		int i = 0;
		Iterator<Sorter<Article>> it = sorted.iterator();

		while (it.hasNext() && i < max) {
			result.add(it.next());
			i++;
		}

		return result;
	}
	

	public void findBigrams(TreeSet<IDSorter> sorted, int k)
			throws URISyntaxException, FileNotFoundException,
			UnsupportedEncodingException {

		int n = 3000;
		// double[][] phi = this.getPhi();
		// int[][] topicWordsAssigments = this.getTopicsWordsAssigments();

		NGram ngram = null;
		// PrintWriter out = new PrintWriter(new OutputStreamWriter(new
		// FileOutputStream("/var/lusinda/solr/rss/new/sme_sk/bigrams.txt"),"UTF-8"));
		// PrintWriter out = new PrintWriter(new
		// OutputStreamWriter(System.out));

		 Phrases phrases = new Phrases();

		HashMap<String, IDSorter> words = getSortedWords(sorted,k, null, n, 0);
		phrases.setWords(words);

		// System.out.println(words.keySet().size());

		for (int d = 0; d < data.size(); d++) {
			TopicAssignment t = this.getData().get(d);
			Instance instance = t.instance;

			TokenSequence ts = (TokenSequence) instance.getSource();

			int doclen = ts.size();

			int topic = -1;

			String word = null;
			String lemmatizedWord = null;
			int nextFeatureTopic = -1;
			String nextWord = null;
			String lemmatizedNextWord = null;
			String previousWord = null;
			boolean stem = false;
			for (int pi = 0, next = 1; pi < doclen; pi += next + 1, next = 1) {

				word = (String) ts.get(pi).getText();

				// previousWord = pi == 0 ? word : ts.get(pi - 1).getText();

				// if (!StandardTextProcessing.containsPunct(previousWord)
				// && StandardTextProcessing.startsWithUpper(word)) {

				// int index = alphabet.lookupIndex(lemmatizedWord);

				// topic =
				// this.getData().get(d).topicSequence.getIndexAtPosition(index);

				ngram = phrases.createNGram(word);

				if (ngram != null) {// || (topic == k &&
									// StandardTextProcessing.startsWithUpper(word))
									// ) {

					try {

						nextWord = (String) ts.get(pi + next).getText(); // while
																			// (nextFeatureTopic
																			// ==
																			// topic
																			// ){

						WordTO wto = phrases.getWordTO(nextWord);
						previousWord = word;

						// int index =
						// alphabet.lookupIndex(lemmatizedNextWord);
						// nextFeatureTopic =
						// this.getData().get(d).topicSequence
						// .getIndexAtPosition(index);

						// or index<0
						while (wto != null
								&& words.containsKey(wto.lemmatizedWord)
						// &&
						// !StandardTextProcessing.containsPunct(previousWord)
						// &&
						// StandardTextProcessing.startsWithUpper(nextWord)
						) { // ||
							// (topic
							// ==
							// nextFeatureTopic
							// &&
							// StandardTextProcessing.startsWithUpper(nextWord)))
							// )
							// {

							ngram.add(wto.word, wto.lemmatizedWord,
									wto.feature, wto.stem, wto.isUpper);

							next++;
							previousWord = nextWord;
							nextWord = (String) ts.get(pi + next).getText();
							wto = phrases.getWordTO(nextWord);

							// index =
							// alphabet.lookupIndex(lemmatizedNextWord);
							// nextFeatureTopic =
							// this.getData().get(d).topicSequence
							// .getIndexAtPosition(pi + next);

						}
					} catch (IndexOutOfBoundsException e) {
					}

					// if (next > 1) {

					phrases.addNGram(ngram);

				}

			}

			// phrases.computeTfIdf(topicWordsAssigments,tokensPerTopic[k],numTopics);

		}

		List<NGram> sortedPhrases = phrases.getSortedNGrams(10, 0);

		for (NGram ng : sortedPhrases) {
			// System.out.print(ng.Counts);
			// System.out.print(" ");
			// System.out.print(ng.getWeight());
			// System.out.print(" ");
			// System.out.print(ng.getTfIdf());
			// System.out.print(" ");
			System.out.print(ng.getNiceNGram());
			System.out.print(" ");
			// System.out.print(ng.Counts);
			// System.out.print(" ");

		}

		System.out.println();

	}

	public int wordsPruning() {

		ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
		ArrayList<TreeSet<IDSorter>> finalWordsCollection = new ArrayList<TreeSet<IDSorter>>();
		List<List<WordCluster>> topicWordClusters = new ArrayList<List<WordCluster>>();

		int ratio = 1;
		int total = 0;
		for (int k = 0; k < sorted.size(); k++) {

			TreeSet<IDSorter> words = sorted.get(k);
			TreeSet<IDSorter> finalWords = new TreeSet<IDSorter>();
			finalWordsCollection.add(finalWords);

			if (words.size() == 0)
				continue;

			Iterator<IDSorter> it = words.iterator();

			IDSorter word = it.next();
			int max = tokensPerTopic[k] / ratio;
			Map<String, String> bigrams_dict = new HashMap<String, String>();
			Map<String, String> words_dict = new HashMap<String, String>();
			Map<String, String> wordsToRemove = new HashMap<String, String>();

			Map<String, IDSorter> dict = new HashMap<String, IDSorter>();

			// build bigrams dict
			for (int i = 0; it.hasNext() && i < max; i++, word = it.next()) {
				String w = (String) alphabet.lookupObject(word.getID());

				if (w.startsWith("b_"))
					bigrams_dict.put(w.replaceAll("b_", ""), w); // e.g.
																	// Ivana_Gasparovica,b_u_1_2_Ivana_Gasparovica
				else
					words_dict.put(w, w);

				dict.put(w, word);
			}

			for (Entry<String, String> entry : bigrams_dict.entrySet()) {
				String[] parts = entry.getKey().split("_");
				if (parts.length != 2)
					continue;

				String word1 = words_dict.get(parts[0]); // word1 = u_1_Ivan
				String word2 = words_dict.get(parts[1]);

				if (word1 != null && word2 != null) {
					int word1Index = alphabet.lookupIndex(word1);
					int word2Index = alphabet.lookupIndex(word2);

					int word1TopicIndex = getTopicIndex(word1Index, k);
					int word2TopicIndex = getTopicIndex(word2Index, k);

					int wordTopic = typeTopicCounts[word1Index][word1TopicIndex]
							& topicMask;
					int word1Count = typeTopicCounts[word1Index][word1TopicIndex] >> topicBits;
					int word2Count = typeTopicCounts[word2Index][word2TopicIndex] >> topicBits;

					int bigramIndex = alphabet.lookupIndex(entry.getValue());

					int bigramTopicIndex = getTopicIndex(bigramIndex, k);

					if (bigramTopicIndex != -1) {

						int bigramCount = typeTopicCounts[bigramIndex][bigramTopicIndex] >> topicBits;

						int maxCount = Math.max(word1Count, word2Count);
						maxCount = Math.max(maxCount, bigramCount);

						typeTopicCounts[bigramIndex][bigramTopicIndex] = ((maxCount) << topicBits)
								+ wordTopic;

						wordsToRemove.put(word1, parts[0]);
						wordsToRemove.put(word2, parts[1]);

					}

				}
			}

			for (Entry<String, String> entry : wordsToRemove.entrySet()) {

				int wordIndex = alphabet.lookupIndex(entry.getKey());
				int wordTopicIndex = getTopicIndex(wordIndex, k);
				int wordTopic = typeTopicCounts[wordIndex][wordTopicIndex]
						& topicMask;
				int count = typeTopicCounts[wordIndex][wordTopicIndex] >> topicBits;

				typeTopicCounts[wordIndex][wordTopicIndex] = ((0) << topicBits)
						+ wordTopic;

				// count = typeTopicCounts[wordIndex][wordTopicIndex] >>
				// topicBits;

				// remove part bigram
				tokensPerTopic[k]--;
			}

			// create clusters
			 /// WORD CLUSTERS BEGIN
			
			 List<WordCluster> wordClusters = new ArrayList<WordCluster>();
			 topicWordClusters.add(wordClusters);
			
			 it = getSortedWords(k).iterator();
			 word = it.next();
			
			 for (int i = 0; it.hasNext() && i < max; i++, word = it.next()) {
			 String w = (String) alphabet.lookupObject(word.getID());
			 if (dict.get(w) == null)
			 continue;
			
			 WordCluster wordCluster = new WordCluster();
			 int len = 0;
			 String prefix = null;
			
			 wordCluster.word = w;
			 wordCluster.words.add(word);
			 wordCluster.id = word.getID();
			
			 if (w.startsWith("b_")) {
			 String[] wordparts = w.replaceAll("b_", "").split("_");
			 if (wordparts.length != 2)
			 continue;
			
			 len = (int) (wordparts[0].length() * 0.5);
			 // len = len > wordparts[0].length() ? wordparts[0].length()
			 // : len;
			 String prefix1 = wordparts[0].substring(0, len);
			 len = (int) (wordparts[1].length() * 0.5);
			 // len = len > wordparts[1].length() ? wordparts[1].length()
			 // : len;
			 String prefix2 = wordparts[1].substring(0, len);
			
			 for (String key : dict.keySet()) {
			 if (wordCluster.word.equals(key)
			 || !key.startsWith("b_"))
			 continue;
			
			 String[] keyparts = key.replaceAll("b_", "").split("_");
			 if (keyparts.length != 2)
			 continue;
			
			 if (keyparts[0].startsWith(prefix1)
			 && keyparts[1].startsWith(prefix2)) {
			 IDSorter value = dict.get(key);
			 if (value != null) {
			 wordCluster.words.add(value);
			 dict.put(key, null);
			 }
			
			 }
			
			 }
			 } else {
			 len = wordCluster.word.length() / 2 + 2;
			 len = len > wordCluster.word.length() ? wordCluster.word
			 .length() : len;
			 prefix = wordCluster.word.substring(0, len);
			 for (String key : dict.keySet()) {
			 if (!wordCluster.word.equals(key)
			 && key.startsWith(prefix)) {
			 IDSorter value = dict.get(key);
			 if (value != null) {
			 wordCluster.words.add(value);
			
			 dict.put(key, null);
			 }
			 }
			 }
			 }
			
			 wordCluster.sum();
			 wordClusters.add(wordCluster);
			
			 // pruning
			
			 }
			
			 /// WORD CLUSTERS END

			int treeshold = 1;
			
			printSortedWords(sorted.get(k), null, 10, treeshold);
			printSortedWordClusters(wordClusters, 10);
			//printSortedWords(sorted.get(k), new String[] { "b_" }, 20,
				//	treeshold);

			 printSortedPhrases(sorted.get(k), 200);

			System.out.println();
		}

		System.out.println(total);

		return total;
		// System.out.println("done");
	}

	// public void printPhi(File f, double threshold) throws IOException{
	// PrintWriter pw = new PrintWriter(new FileWriter(f));
	// FeatureCounter[] wordCountsPerTopic = new FeatureCounter[numTopics];
	// for (int ti = 0; ti < numTopics; ti++) {
	// wordCountsPerTopic[ti] = new FeatureCounter(alphabet);
	// }
	//
	// for (int fi = 0; fi < numTypes; fi++) {
	// int[] topics = typeTopicCounts[fi].keys();
	// for (int i = 0; i < topics.length; i++) {
	// wordCountsPerTopic[topics[i]].increment(fi,
	// typeTopicCounts[fi].get(topics[i]));
	// }
	// }
	//
	// for(int ti = 0; ti < numTopics; ti++){
	// pw.println("Topic\t" + ti);
	// FeatureCounter counter = wordCountsPerTopic[ti];
	// FeatureVector fv = counter.toFeatureVector();
	// for(int pos = 0; pos < fv.numLocations(); pos++){
	// int fi = fv.indexAtLocation(pos);
	// String word = (String) alphabet.lookupObject(fi);
	// int count = (int) fv.valueAtLocation(pos);
	// double prob;
	// prob = (double) (count+beta)/(tokensPerTopic[ti] + betaSum);
	// pw.println(word + "\t" + prob);
	// }
	// pw.println();
	// }
	// pw.close();
	// }

	private void printSortedPhrases(TreeSet<IDSorter> sorted, int max) {
		try {
			Iterator<IDSorter> it = sorted.iterator();

			List<String> phrases = new ArrayList<String>();
			Map<String, Bigram> bigrams_dict = new HashMap<String, Bigram>();
			TreeSet<IDSorter> bigrams = new TreeSet<IDSorter>();

			String prefix = "b_";
			int i = 0;

			// build bigrams dictionary
			while (it.hasNext() && i < max) {
				IDSorter s = it.next();

				String word = (String) alphabet.lookupObject(s.getID());

				if (word.startsWith(prefix)) {

					bigrams_dict.put(word, new Bigram(word));
					bigrams.add(s);
					i++;
				}
			}

			// chaining
			it = bigrams.iterator();
			while (it.hasNext()) {
				IDSorter s = it.next();

				String key = (String) alphabet.lookupObject(s.getID());
				Bigram bigram = bigrams_dict.get(key);
				if (bigram.isRoot(bigrams_dict)) {
					bigram.buildLinks(bigrams_dict);
					phrases.addAll(bigram.getPhrases());
				}

			}

			// print
			for (String phrase : phrases) {
				int count = phrase.split("_").length;
				if (count > 2)
					System.out.print(phrase + ",");

			}
			System.out.println();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private HashMap<String, IDSorter> getSortedWords(TreeSet<IDSorter> sorted,int k,
			String[] prefix, int max, double treeshold) {
		Iterator<IDSorter> it = sorted.iterator();
		HashMap<String, IDSorter> words = new HashMap<String, IDSorter>();
		int i = 0;
		while (it.hasNext() && i < max) {
			IDSorter s = it.next();
			if (s.getWeight() < treeshold)
				continue;

			String word = (String) alphabet.lookupObject(s.getID());
			
			//words.put(word, s);
			words.put(word, new IDSorter(s.getID(),phi[s.getID()][k]));

		}

		return words;

	}

	private HashMap<String, IDSorter> printSortedWords(
			TreeSet<IDSorter> sorted, String[] prefix, int max, double treeshold) {
		Iterator<IDSorter> it = sorted.iterator();
		HashMap<String, IDSorter> words = new HashMap<String, IDSorter>();
		int i = 0;
		while (it.hasNext() && i < max) {
			IDSorter s = it.next();
			if (s.getWeight() < treeshold)
				continue;

			String word = (String) alphabet.lookupObject(s.getID());
			words.put(word, s);

			if (prefix == null) {
				System.out.print(word + ":" + s.getWeight() + ",");
				i++;
			} else {
				for (int p = 0; p < prefix.length; p++) {
					if (word.startsWith(prefix[p])) {
						System.out.print(word + ":" + s.getWeight() + ",");
						i++;
					}
				}
			}
		}

		System.out.println();

		return words;

	}

	// private int relatedTopicsWindow;
	// public void setRelatedTopicsWindow(int r)
	// {
	// relatedTopicsWindow = r;
	// relatedTopics = new double[relatedTopicsWindow][this.numTopics][];
	// }

	// private double[][][] relatedTopics = null;

	// /sims[m][k1][k2]
	public void findSimilarTopics(Lda another, int model,
			Map<String, double[][]> commonWords, int models)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {
		ArrayList<TreeSet<IDSorter>> thisWords = this.getSortedWords();
		ArrayList<TreeSet<IDSorter>> anotherWords = another.getSortedWords();

		TreeSet<IDSorter> sortedDistJackard;

		AlphabetMapping mapping = new AlphabetMapping(this.alphabet,
				another.alphabet);

		int[][] sourceIndicies = new int[numTopics][];
		int[][][] maps = new int[this.numTopics][another.numTopics][];
		// int[][][] commonWords = new int[this.numTopics][another.numTopics][];

		double[][] sourceWeights = new double[numTopics][];
		double[][][] weights = new double[this.numTopics][another.numTopics][];
		double sim = 0;
		int max = 30;

		boolean useBigrams = false;

		// System.out.println(max);
		for (int k1 = 0; k1 < numTopics; k1++) {

			sourceIndicies[k1] = getWordIndicies(thisWords.get(k1), max);
			sourceWeights[k1] = getSortedWeights(thisWords.get(k1), max);

			for (int k2 = 0; k2 < another.numTopics; k2++) {
				int[] targetIndicies = getWordIndicies(anotherWords.get(k2),
						max);

				maps[k1][k2] = mapping.map(sourceIndicies[k1], targetIndicies);

			}
		}

		JackardSimilarity jackard = new JackardSimilarity();
		for (int k1 = 0; k1 < numTopics; k1++) {

			sortedDistJackard = new TreeSet<IDSorter>();

			for (int k2 = 0; k2 < another.numTopics; k2++) {

				sim = jackard.computeSimilarity(sourceIndicies[k1],
						maps[k1][k2]);
				if (sim > 0.02) {
					sortedDistJackard.add(new IDSorter(k2, sim));

					int words[] = jackard.getCommonWords(sourceIndicies[k1],
							maps[k1][k2]);

					for (int i = 0; i < words.length; i++) {
						String key = (String) alphabet.lookupObject(words[i]);
						double[][] value = commonWords.get(key);
						if (value == null) {
							value = new double[models][another.numTopics];
						}

						value[model][k2] = sim;
						commonWords.put(key, value);

					}

				}
			}

			// printTopics(k1, sortedDistJackard, another,commonWords, 5,
			// useBigrams);
		}
	}

	private List<String> documents = new ArrayList<String>();	

	public List<String> getDocuments() {
		return documents;
	}

	
	
	public void setDocuments(List<String> documents) {
		this.documents = documents;
		}

	
	private Pipe myPipe;

	public Pipe getPipe() {
		return myPipe;
	}

	public void inferNewDocs(InstanceList test) {

		JsSimilarityMeasure js = new JsSimilarityMeasure();

		JackardSimilarity jackard = new JackardSimilarity();
		CosineSimilarity cosine = new CosineSimilarity();

		for (int d2 = 0; d2 < test.size(); d2++) {

			// testing.addThruPipe(new Instance(newDocs.get(d2),
			// null,t2.instance.getName(), null));

			
			
			double[] destTheta = this.getInferencer().getSampledDistribution(
					test.get(d2), 10, 1, 5);

			TreeSet<IDSorter> sortedDistJackard = new TreeSet<IDSorter>();
			for (int d1 = 0; d1 < data.size(); d1++) {
				TopicAssignment t1 = this.getData().get(d1);

				double[] sourceTheta = this.getTopicProbabilities(d1);

				double sim = 1 - js.computeSimmilarity(sourceTheta, destTheta);

				sortedDistJackard.add(new IDSorter(d1, sim));

			}
			System.out.println(test.get(d2).getName());
			// System.out.println(newModel.getDocumentTopics(d2, 0, 4, " "));
			Iterator<IDSorter> it = sortedDistJackard.iterator();

			int i = 0;
			while (it.hasNext() && i < 4) {

				IDSorter s = it.next();
				System.out.println(this.data.get(s.getID()).instance.getName());
				System.out.print(" :");
				System.out.print(s.getWeight());

				System.out.println();

				i++;
			}
			System.out.println();

		}

	}

	@Override
	public void addInstances(InstanceList list) {

		InstanceList[] lists = list.splitInTwoByModulo(10);

		// lists[0].save(new
		// File("/var/lusinda/solr/rss/new/sme_sk/lda/test.instances"));
		// lists[1].save(new
		// File("/var/lusinda/solr/rss/new/sme_sk/lda/train.instances"));

		this.myPipe = list.getPipe();

		super.addInstances(list);
	}

	public int getNumDocuments() {
		return this.getData().size();
	}
	
	public String getDocumentTopics(double[] dist, double treeshold, int max,
			String topicsSep) {
		
		TreeSet<IDSorter> topics = new TreeSet<IDSorter>();
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < dist.length; i++)
			topics.add(new IDSorter(i, dist[i]));

		Iterator<IDSorter> it = topics.iterator();

		int i = 0;
		while (it.hasNext() && i < max) {

			IDSorter s = it.next();
			if (s.getWeight() > treeshold) {
				builder.append("topic+");
				builder.append(s.getID());
				builder.append(": ");
				builder.append(s.getWeight());
				builder.append(this.getLabel(s.getID(), 5, ", "));
				builder.append(topicsSep);
				i++;
			}
		}

		return builder.toString();
	}


	public String getDocumentTopics(int docId, double treeshold, int max,
			String topicsSep)
	{
		return getDocumentTopics(this.getTopicProbabilities(docId), treeshold, max, topicsSep);
	}
	public String getDocumentLabel(int id) {
		return (String) this.data.get(id).instance.getName();
	}

	public void findSimilarDocuments(Lda newModel, int sourceModel,
			int destModel, double[][][][] edges, int models)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {
		ArrayList<TreeSet<IDSorter>> thisWords = this.getSortedWords();
		// ArrayList<TreeSet<IDSorter>> anotherWords = another.getSortedWords();

		List<String> newDocs = newModel.getDocuments();

		InstanceList testing = new InstanceList(this.getPipe());

		double[][] phi = getTopicWordDist();
		double averageTokens = 1600;

		for (int i = 0; i < numTopics; i++) {
			System.out.print(tokensPerTopic[i] + " ");
			printTopic(i, 10, " ");
		}

		// this.printDocuments();
		// System.out.println("another:");
		// another.printDocuments();

		double sim = 0;
		int max = 30;

		boolean useBigrams = false;
		JsSimilarityMeasure js = new JsSimilarityMeasure();

		// System.out.println(max);

		// for (int k1 = 0; k1 < numTopics; k1++) {
		//
		// sourceIndicies[k1] = getWordIndicies(thisWords.get(k1), max);
		//
		// for (int k2 = 0; k2 < another.numTopics; k2++) {
		// int[] targetIndicies = getWordIndicies(anotherWords.get(k2),
		// max);
		//
		// maps[k1][k2] = mapping.map(sourceIndicies[k1], targetIndicies);
		//
		// }
		// }

		JackardSimilarity jackard = new JackardSimilarity();
		CosineSimilarity cosine = new CosineSimilarity();
		// double[][] topicSimMatrix = new
		// double[this.numTopics][this.numTopics];
		// for(int k1=0;k1<numTopics;k1++){
		// for(int k2=0;k2<numTopics;k2++){
		// //topicSimMatrix[k1][k2] = 1- js.computeSimmilarity(phi[k1],phi[k2]);
		//
		// double w_k1 = (double)tokensPerTopic[k1]/(double)totalTokens;
		// double w_k2 = (double)tokensPerTopic[k2]/(double)totalTokens;
		//
		// topicSimMatrix[k1][k2] =
		// w_k1*w_k2*jackard.computeSimilarity(getWordIndicies(k1,
		// 100),getWordIndicies(k2, 100),phi[k1]);
		// }
		// }

		// JackardSimilarity jackard = new JackardSimilarity();
		//
		//
		// for (int k1 = 0; k1 < numTopics; k1++) {
		// //double st_k1 =sourceTheta[k1];
		// for (int k2 = 0; k2 < another.numTopics; k2++) {
		//
		// //double dt_k2 = destTheta[k2];
		// topicSimMatrix[k1][k2]= jackard.computeSimilarity(sourceIndicies[k1],
		// maps[k1][k2], phi[k1]);
		//
		// }
		//
		//
		// }

		for (int d2 = 0; d2 < newDocs.size(); d2++) {
			TopicAssignment t2 = this.getData().get(d2);

			testing.addThruPipe(new Instance(newDocs.get(d2), null, t2.instance
					.getName(), null));

			double[] destTheta = this.getInferencer().getSampledDistribution(
					testing.get(testing.size() - 1), 10, 1, 5);

			TreeSet<IDSorter> sortedDistJackard = new TreeSet<IDSorter>();
			for (int d1 = 0; d1 < data.size(); d1++) {
				TopicAssignment t1 = this.getData().get(d1);

				double[] sourceTheta = this.getTopicProbabilities(d1);

				sim = 1 - js.computeSimmilarity(sourceTheta, destTheta);

				sortedDistJackard.add(new IDSorter(d1, sim));

				// if (sim > 0.5) {
				//
				// double score =0;
				// // for(int k1=0;k1<numTopics;k1++){
				// //
				// // for(int k2=0;k2<numTopics;k2++){
				// // score +=
				// sourceTheta[k1]*destTheta[k2]*topicSimMatrix[k1][k2];
				// // }
				// // }
				//
				// score =
				// cosine.calculateSimilarity(sourceTheta,destTheta,tokensPerTopic,averageTokens);
				//
				// //if(score>0.1)
				// edges[sourceModel][destModel][d1][d2] = score;
				//
				// }
			}
			System.out.println(newModel.data.get(d2).instance.getName());
			// System.out.println(newModel.getDocumentTopics(d2, 0, 4, " "));
			Iterator<IDSorter> it = sortedDistJackard.iterator();

			int i = 0;
			while (it.hasNext() && i < 4) {

				IDSorter s = it.next();
				System.out.print(this.data.get(s.getID()).instance.getName());
				System.out.print(" :");
				System.out.print(s.getWeight());

				System.out.println();
				// System.out.println(this.getDocumentTopics(s.getID(), 0, 10,
				// " "));
				i++;
			}
			System.out.println();

			// printTopics(k2, sortedDistJackard, another, 5,false);
		}

		// for (int d2 = 0; d2 < newDocs.size(); d2++) {
		// System.out.print(newModel.data.get(d2).instance.getName());
		// // System.out.println(another.getDocumentTopics(d2, 0, 10, " "));
		//
		// this.printSimilarDocuments(edges[sourceModel][destModel],d2,10);
		//
		// }
	}

	public void findSimilarDocuments2(Lda newModel, int sourceModel,
			int destModel, double[][][][] edges, int models)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {
		ArrayList<TreeSet<IDSorter>> thisWords = this.getSortedWords();
		ArrayList<TreeSet<IDSorter>> anotherWords = newModel.getSortedWords();

		AlphabetMapping mapping = new AlphabetMapping(this.alphabet,
				newModel.alphabet);

		Map<String, Integer> unionBigrams = new HashMap<String, Integer>();
		ArrayList<String> unionBigramsIndicies = new ArrayList<String>();

		int[][] sourceBigrams = new int[numTopics][];

		int[][] sourceIndicies = new int[numTopics][];
		int[][][] maps = new int[this.numTopics][newModel.numTopics][];

		OutputStream os = (OutputStream) new FileOutputStream(new File(
				"/var/lusinda/solr/rss/new/sme_sk/lda/eval.txt"));
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);

		List<String> newDocs = newModel.getDocuments();

		InstanceList testing = new InstanceList(this.getPipe());

		double[][] phi = getTopicWordDist();
		double averageTokens = 1600;

		PrintWriter out = new PrintWriter(osw);

		// for(int i=0;i<numTopics;i++)
		// {
		// System.out.print(tokensPerTopic[i]+" ");
		// printTopic(i, 10, " ");
		// }

		// this.printDocuments();
		// System.out.println("another:");
		// another.printDocuments();

		double sim = 0;
		int max = 30;

		JsSimilarityMeasure js = new JsSimilarityMeasure();

		// System.out.println(max);

		for (int k1 = 0; k1 < numTopics; k1++) {

			sourceIndicies[k1] = getWordIndicies(thisWords.get(k1), max);

			for (int k2 = 0; k2 < newModel.numTopics; k2++) {
				int[] targetIndicies = getWordIndicies(anotherWords.get(k2),
						max);

				maps[k1][k2] = mapping.map(sourceIndicies[k1], targetIndicies);

			}
		}

		JackardSimilarity jackard = new JackardSimilarity();
		CosineSimilarity cosine = new CosineSimilarity();
		double[][] topicSimMatrix = new double[this.numTopics][this.numTopics];
		// for(int k1=0;k1<numTopics;k1++){
		// for(int k2=0;k2<numTopics;k2++){
		// //topicSimMatrix[k1][k2] = 1- js.computeSimmilarity(phi[k1],phi[k2]);
		//
		// double w_k1 = (double)tokensPerTopic[k1]/(double)totalTokens;
		// double w_k2 = (double)tokensPerTopic[k2]/(double)totalTokens;
		//
		// topicSimMatrix[k1][k2] =
		// w_k1*w_k2*jackard.computeSimilarity(getWordIndicies(k1,
		// 100),getWordIndicies(k2, 100),phi[k1]);
		// }
		// }

		// JackardSimilarity jackard = new JackardSimilarity();
		//
		//
		for (int k1 = 0; k1 < numTopics; k1++) {
			// double st_k1 =sourceTheta[k1];
			for (int k2 = 0; k2 < newModel.numTopics; k2++) {

				// double dt_k2 = destTheta[k2];
				topicSimMatrix[k1][k2] = jackard.computeSimilarity(
						sourceIndicies[k1], maps[k1][k2]);//, phi[k1]);

			}

		}

		for (int d2 = 0; d2 < newDocs.size(); d2++) {
			TopicAssignment t2 = this.getData().get(d2);

			testing.addThruPipe(new Instance(newDocs.get(d2), null, t2.instance
					.getName(), null));

			double[] destTheta = this.getInferencer().getSampledDistribution(
					testing.get(testing.size() - 1), 10, 1, 5);

			TreeSet<IDSorter> sortedDistJs = new TreeSet<IDSorter>();
			TreeSet<IDSorter> sortedDistJsCosine = new TreeSet<IDSorter>();

			for (int d1 = 0; d1 < data.size(); d1++) {
				TopicAssignment t1 = this.getData().get(d1);

				double[] sourceTheta = this.getTopicProbabilities(d1);

				sim = 1 - js.computeSimmilarity(sourceTheta, destTheta);

				if (sim > 0.5) {

					double score = 0;
					for (int k1 = 0; k1 < numTopics; k1++) {

						for (int k2 = 0; k2 < numTopics; k2++) {
							score += topicSimMatrix[k1][k2];
						}
					}
					// score = (double)score/(double)numTopics*2;

					score = cosine.calculateSimilarity(sourceTheta, destTheta,
							diagnostics.getRank1Percent().scores, 0.1);

					// if(score>0.1)
					edges[sourceModel][destModel][d1][d2] = sim;// sim*score;
					sortedDistJs.add(new IDSorter(d1, sim));
					sortedDistJsCosine.add(new IDSorter(d1, score));

				}
			}
			out.println();
			out.println(d2 + ": " + newModel.data.get(d2).instance.getName());
			// System.out.println(newModel.getDocumentTopics(d2, 0, 4, " "));
			Iterator<IDSorter> itJs = sortedDistJs.iterator();
			Iterator<IDSorter> itJsCosine = sortedDistJsCosine.iterator();

			int i = 0;
			out.print("js: ");
			while (itJs.hasNext() && i < 4) {

				IDSorter s = itJs.next();
				if (s.getWeight() > 0.0) {
					out.print(this.data.get(s.getID()).instance.getName());
					out.print(" :");
					out.print(s.getWeight());
					out.println();
				}
				// System.out.println(this.getDocumentTopics(s.getID(), 0, 10,
				// " "));
				i++;
			}

			i = 0;
			out.println("jsCosine: ");
			while (itJsCosine.hasNext() && i < 4) {

				IDSorter s = itJsCosine.next();
				if (s.getWeight() > 0.0) {
					out.print(this.data.get(s.getID()).instance.getName());
					out.print(" :");
					out.print(s.getWeight());
					out.println();
				}
				// System.out.println(this.getDocumentTopics(s.getID(), 0, 10,
				// " "));
				i++;
			}
			out.println();

			// printTopics(k2, sortedDistJackard, another, 5,false);
		}

		// for (int d2 = 0; d2 < newDocs.size(); d2++) {
		// System.out.print(newModel.data.get(d2).instance.getName());
		// // System.out.println(another.getDocumentTopics(d2, 0, 10, " "));
		//
		// this.printSimilarDocuments(edges[sourceModel][destModel],d2,10);
		//
		// }

		out.flush();
		out.close();
	}

	public void printDocuments() {
		for (int d1 = 0; d1 < data.size(); d1++) {
			TopicAssignment t1 = this.getData().get(d1);

			System.out.println(t1.instance.getName());
		}

	}

	public void printSimilarDocuments(double[][] simMatrix, int source, int max) {
		TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

		for (int i = 0; i < simMatrix[source].length; i++) {
			sorted.add(new IDSorter(i, simMatrix[source][i]));
		}

		Iterator<IDSorter> it = sorted.iterator();

		int i = 0;
		while (it.hasNext() && i < max) {

			IDSorter s = it.next();
			System.out.print(this.data.get(s.getID()).instance.getName());
			System.out.print(" :");
			System.out.print(s.getWeight());
			System.out.println();
			// System.out.println(this.getDocumentTopics(s.getID(), 0, 10,
			// " "));
			i++;
		}
		System.out.println();
	}

	public void findSimilarTopics2(Lda another, int sourceModel, int destModel,
			String[][][][] commonWords, double[][][][] edges, int models)
			throws FileNotFoundException, UnsupportedEncodingException,
			URISyntaxException {
		ArrayList<TreeSet<IDSorter>> thisWords = this.getSortedWords();
		ArrayList<TreeSet<IDSorter>> anotherWords = another.getSortedWords();

		TreeSet<IDSorter> sortedDistJackard;

		double[][] phi = getTopicWordDist();

		AlphabetMapping mapping = new AlphabetMapping(this.alphabet,
				another.alphabet);

		Map<String, Integer> unionBigrams = new HashMap<String, Integer>();
		ArrayList<String> unionBigramsIndicies = new ArrayList<String>();

		int[][] sourceBigrams = new int[numTopics][];

		int[][] sourceIndicies = new int[numTopics][];
		int[][][] maps = new int[this.numTopics][another.numTopics][];
		// int[][][] commonWords = new int[this.numTopics][another.numTopics][];

		double[][] sourceWeights = new double[numTopics][];
		double[][][] weights = new double[this.numTopics][another.numTopics][];
		double sim = 0;
		int max = 30;

		boolean useBigrams = false;

		// System.out.println(max);
		for (int k1 = 0; k1 < numTopics; k1++) {

			sourceIndicies[k1] = getWordIndicies(thisWords.get(k1), max);
			sourceWeights[k1] = getSortedWeights(thisWords.get(k1), max);

			for (int k2 = 0; k2 < another.numTopics; k2++) {
				int[] targetIndicies = getWordIndicies(anotherWords.get(k2),
						max);

				maps[k1][k2] = mapping.map(sourceIndicies[k1], targetIndicies);

			}
		}

		JackardSimilarity jackard = new JackardSimilarity();
		for (int k1 = 0; k1 < numTopics; k1++) {

			if(diagnostics.getRank1Percent().scores[k1]<0.1)
				continue;
			
			sortedDistJackard = new TreeSet<IDSorter>();

			for (int k2 = 0; k2 < another.numTopics; k2++) {
				if(diagnostics.getRank1Percent().scores[k1]<0.1)
					continue;

				sim = jackard.computeSimilarity(sourceIndicies[k1],
						maps[k1][k2]);//, phi[k1]);
				if (sim > 0.03) {
					sortedDistJackard.add(new IDSorter(k2, sim));

					int words[] = jackard.getCommonWords(sourceIndicies[k1],
							maps[k1][k2]);

					StringBuilder wordBuilder = new StringBuilder();
					for (int i = 0; i < words.length; i++) {
						wordBuilder.append((String) alphabet
								.lookupObject(words[i]));
						wordBuilder.append(";");
					}

					commonWords[sourceModel][destModel][k1][k2] = wordBuilder
							.toString();
					edges[sourceModel][destModel][k1][k2] = sim;

				}
			}

			printTopics(k1, sortedDistJackard, another, 1, false);
			System.out.println();
		}
	}

	public String getPhrasesLabel(int k, int numWords, String separator) {

		StringBuilder builder = new StringBuilder();

		List<NGram> sortedPhrases = phrasesList.get(k).getSortedNGrams(10, 0);

		int i = 0;
		for (NGram ng : sortedPhrases) {
			if (i > numWords)
				break;

			builder.append(ng.getNiceNGram());
			builder.append(separator);
			i++;
		}

		return builder.toString();

	}

	List<TreeSet<IDSorter>> topicsDocs = new ArrayList<TreeSet<IDSorter>>();

	public void buildDocumentsTopic() {

		for (int t = 0; t < numTopics; t++) {
			topicsDocs.add(new TreeSet<IDSorter>());
		}

		for (int d = 0; d < data.size(); d++) {
			double[] dist = getTopicProbabilities(d);

			for (int t = 0; t < numTopics; t++) {
				topicsDocs.get(t).add(new IDSorter(d, dist[t]));
			}
		}
	}

	public String getTopDocumentsString(int k, int max, String separator) {
		List<StringDoublePair> result = getTopDocuments(k, max);
		StringBuilder builder = new StringBuilder();
		for (StringDoublePair pair : result) {
			builder.append(pair.name);
			builder.append(": ");
			builder.append(pair.value);
			builder.append(separator);
		}

		return builder.toString();
	}

	public List<StringDoublePair> getTopDocuments(int k, int max) {

		List<StringDoublePair> result = new ArrayList<StringDoublePair>();

		Iterator<IDSorter> it = topicsDocs.get(k).iterator();
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (it.hasNext() && i < max) {
			IDSorter s = it.next();
			result.add(new StringDoublePair(
					(String) this.data.get(s.getID()).instance.getName(), s
							.getWeight()));

			i++;
		}
		return result;
	}

	public String getLabel(int k, int numWords, String separator) {

		TreeSet<IDSorter> words = getSortedWords(k);

		Iterator<IDSorter> it = words.iterator();
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (it.hasNext() && i < numWords) {
			builder.append((String) alphabet.lookupObject(it.next().getID()));
			builder.append(separator);
			i++;
		}

		return builder.toString();
	}

	public int[] getWordIndicies(int k, int max) {
		TreeSet<IDSorter> sortedWords = getSortedWords(k);

		return getWordIndicies(sortedWords, max);
	}

	public void printTopic(int k, int maxWords, String wordSep) {
		TreeSet<IDSorter> sorted = getSortedWords(k);
		Iterator<IDSorter> it = sorted.iterator();

		int i = 0;
		while (it.hasNext() && i < maxWords) {

			IDSorter s = it.next();

			System.out.print((String) alphabet.lookupObject(s.getID()));
			System.out.print(wordSep);
			i++;
		}
		System.out.println();
	}

	public void printTopics(double[] dist, int max, String sep) {

		TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

		for (int i = 0; i < dist.length; i++) {
			sorted.add(new IDSorter(i, dist[i]));
		}

		Iterator<IDSorter> it = sorted.iterator();

		int i = 0;
		while (it.hasNext() && i < max) {

			IDSorter s = it.next();

			System.out.print("topic" + s.getID() + " :");
			System.out.print(s.getWeight());
			printTopic(s.getID(), 5, ", ");

			i++;
		}
		System.out.println();
	}

	public void printWords(int[] indicies) {
		System.out.print("\tcommon words: ");
		for (int i = 0; i < indicies.length; i++)
			System.out
					.print((String) alphabet.lookupObject(indicies[i]) + ", ");

		System.out.println();
	}

	public double[] getSortedWeights(TreeSet<IDSorter> sortedWords, int max) {
		double[] weights = new double[max];

		Iterator<IDSorter> it = sortedWords.iterator();

		int i = 0;
		while (it.hasNext() && i < max) {
			weights[i] = it.next().getWeight();

			i++;
		}

		return weights;
	}

	public int[] getWordIndicies(TreeSet<IDSorter> sortedWords, int max) {

		int[] indicies = new int[max];

		Iterator<IDSorter> it = sortedWords.iterator();

		int i = 0;
		while (it.hasNext() && i < max) {
			indicies[i] = it.next().getID();

			i++;
		}

		return indicies;
	}

	public TreeSet<IDSorter> getSortedWords(int k) {

		TreeSet<IDSorter> topicSortedWords = new TreeSet<IDSorter>();

		// Collect counts
		for (int type = 0; type < numTypes; type++) {

			int[] topicCounts = typeTopicCounts[type];

			int index = 0;
			while (index < topicCounts.length && topicCounts[index] > 0) {

				int topic = topicCounts[index] & topicMask;
				if (topic == k) {
					int count = topicCounts[index] >> topicBits;
					topicSortedWords.add(new IDSorter(type, count));
				}
				index++;
			}
		}

		return topicSortedWords;
	}

	public List<ArrayList<StringDoublePair>> getTopicWordsDistribution() {
		List<ArrayList<StringDoublePair>> topicWordsDistribution = new ArrayList<ArrayList<StringDoublePair>>();

		for (int topic = 0; topic < numTopics; topic++) {
			topicWordsDistribution.add(new ArrayList<StringDoublePair>());
		}

		for (int type = 0; type < numTypes; type++) {

			int[] topicCounts = typeTopicCounts[type];

			int index = 0;
			while (index < topicCounts.length && topicCounts[index] > 0) {

				int topic = topicCounts[index] & topicMask;
				int count = topicCounts[index] >> topicBits;

				double weight = (double) count / (double) tokensPerTopic[topic];

				if (weight > 0)
					topicWordsDistribution.get(topic).add(
							new StringDoublePair(type, (String) alphabet
									.lookupObject(type), weight));

				index++;
			}
		}

		return topicWordsDistribution;

	}

	public double[][] getTopicWordDist() {
		
		return phi;
	}
	
	double[][] phi;
	public void calculatePhi()
	{
		phi= new double[numTypes][numTopics];

		for (int i = 0; i < numTypes; i++) {
			int[] topicCounts = typeTopicCounts[i];

			int index = 0;
			while (index < topicCounts.length && topicCounts[index] > 0) {

				int topic = topicCounts[index] & topicMask;
				double count = topicCounts[index] >> topicBits;

				//phi[i][topic] = (double) count
					//	/ (double) tokensPerTopic[topic];
				phi[i][topic] = (double) (count+beta) / (double)
				 (tokensPerTopic[topic] + betaSum);

				index++;
			}
		}

		
	}

	public double[][] getPhi() {
		return phi;
	}

	transient private TopicModelDiagnostics diagnostics;
	
	public void calculateDiagnostics()
	{
		diagnostics = new TopicModelDiagnostics(this,20);
	}
	
	public int[][] getTopicsWordsAssigments() {
		int[][] weightedWords = new int[numTypes][numTopics];

		for (int i = 0; i < numTypes; i++) {
			int[] topicCounts = typeTopicCounts[i];

			int index = 0;
			while (index < topicCounts.length && topicCounts[index] > 0) {

				int topic = topicCounts[index] & topicMask;
				double count = topicCounts[index] >> topicBits;

				weightedWords[i][topic] = (int) count;
				// / (double) tokensPerTopic[topic];
				// weightedWords[i][topic] = (double) (count+beta) / (double)
				// (tokensPerTopic[topic] + betaSum);

				index++;
			}
		}

		return weightedWords;
	}
	
	//	0949786901 NR,50
	//rm, sal19, 0904 216173
	

	//
	// private String[] getBigramParts(String bigram) { String[] parts = new
	// String[2];
	//
	// bigram = bigram.replaceAll("b_u_[12]_", ""); parts = bigram.split("_");
	// return parts; }
	//
	// public void clusterBySuffix() { ArrayList<TreeSet<IDSorter>> sorted =
	// getSortedWords();
	//
	// for (int k = 0; k < sorted.size(); k++) {
	//
	// TreeSet<IDSorter> words = sorted.get(k);
	//
	// Iterator<IDSorter> it = words.iterator(); IDSorter word = it.next(); int
	// max
	// = tokensPerTopic[k] / 5; List<WordCluster> wordClusters = new
	// ArrayList<WordCluster>(); Map<String, IDSorter> dict = new
	// HashMap<String,
	// IDSorter>(); // build bigrams dict for (int i = 0; it.hasNext() && i <
	// max;
	// i++, word = it.next()) { String w = (String)
	// alphabet.lookupObject(word.getID()); // if (!w.startsWith("b_"))
	// dict.put((String) alphabet.lookupObject(word.getID()), word);
	//
	// }
	//
	// it = words.iterator(); word = it.next();
	//
	// for (int i = 0; it.hasNext() && i < max; i++, word = it.next()) { String
	// w =
	// (String) alphabet.lookupObject(word.getID()); if (dict.get(w) == null)
	// continue;
	//
	// WordCluster wordCluster = new WordCluster(); int len = 0; String prefix =
	// null;
	//
	// wordCluster.word = "{" + w; wordCluster.words.add(word); wordCluster.id =
	// wordClusters.size() + 1;
	//
	// if (w.startsWith("b_")) { String[] wordparts = w.replaceAll("b_",
	// "").split("_");
	//
	// len = wordparts[0].length() / 2 + 2; String prefix1 =
	// wordparts[0].substring(0, len); len = wordparts[1].length() / 2 + 2;
	// String
	// prefix2 = wordparts[1].substring(0, len);
	//
	// for (String key : dict.keySet()) { if (wordCluster.word.equals(key) ||
	// !key.startsWith("b_")) continue;
	//
	// String[] keyparts = key.replaceAll("b_", "").split("_");
	//
	// if (keyparts[0].startsWith(prefix1) || keyparts[1].startsWith(prefix2)) {
	// wordCluster.words.add(dict.get(key)); wordCluster.word += key + ",";
	// dict.put(key, null);
	//
	// }
	//
	// } } else { len = wordCluster.word.length() / 2 + 2; prefix =
	// wordCluster.word.substring(0, len); for (String key : dict.keySet()) { if
	// (!wordCluster.word.equals(key) && key.startsWith(prefix)) {
	// wordCluster.words.add(dict.get(key)); wordCluster.word += key + ",";
	// dict.put(key, null);
	//
	// } } } wordCluster.word += "}"; wordClusters.add(wordCluster);
	//
	// }
	//
	// // printWordClusters(wordClusters); } }
	//
	//
	//
	// public void wordsPruning__() {
	//
	// clusterBySuffix();
	//
	// ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
	// ArrayList<TreeSet<IDSorter>> finalWordsCollection = new
	// ArrayList<TreeSet<IDSorter>>();
	//
	// for (int k = 0; k < sorted.size(); k++) {
	//
	// TreeSet<IDSorter> words = sorted.get(k); TreeSet<IDSorter> finalWords =
	// new
	// TreeSet<IDSorter>(); finalWordsCollection.add(finalWords);
	//
	// Iterator<IDSorter> it = words.iterator(); IDSorter word = it.next(); int
	// max
	// = tokensPerTopic[k] / 5; Map<String, String> bigrams_dict = new
	// HashMap<String, String>(); Map<String, String> words_dict = new
	// HashMap<String, String>(); Map<String, String> wordsToRemove = new
	// HashMap<String, String>();
	//
	// // build bigrams dict for (int i = 0; it.hasNext() && i < max; i++, word
	// =
	// it.next()) { String w = (String) alphabet.lookupObject(word.getID());
	//
	// if (w.startsWith("b_")) bigrams_dict.put(w.replaceAll("b_", ""), w); //
	// e.g.
	// // Ivana_Gasparovica,b_u_1_2_Ivana_Gasparovica else words_dict.put(w, w);
	// }
	//
	// for (Entry<String, String> entry : bigrams_dict.entrySet()) { String[]
	// parts
	// = entry.getKey().split("_"); if (parts.length != 2) continue;
	//
	// String word1 = words_dict.get(parts[0]); // word1 = u_1_Ivan String word2
	// =
	// words_dict.get(parts[1]);
	//
	// if (word1 != null && word2 != null) { int word1Index =
	// alphabet.lookupIndex(word1); int word2Index =
	// alphabet.lookupIndex(word2);
	//
	// int word1TopicIndex = getTopicIndex(word1Index, k); int word2TopicIndex =
	// getTopicIndex(word2Index, k);
	//
	// int wordTopic = typeTopicCounts[word1Index][word1TopicIndex] & topicMask;
	// int
	// word1Count = typeTopicCounts[word1Index][word1TopicIndex] >> topicBits;
	// int
	// word2Count = typeTopicCounts[word2Index][word2TopicIndex] >> topicBits;
	//
	// int bigramIndex = alphabet.lookupIndex(entry.getValue());
	//
	// int bigramTopicIndex = getTopicIndex(bigramIndex, k);
	//
	// if (bigramTopicIndex != -1) {
	//
	// int bigramCount = typeTopicCounts[bigramIndex][bigramTopicIndex] >>
	// topicBits;
	//
	// int maxCount = Math.max(word1Count, word2Count); maxCount =
	// Math.max(maxCount, bigramCount);
	//
	// typeTopicCounts[bigramIndex][bigramTopicIndex] = ((maxCount) <<
	// topicBits) +
	// wordTopic;
	//
	// wordsToRemove.put(word1, parts[0]); wordsToRemove.put(word2, parts[1]);
	//
	// }
	//
	// } }
	//
	// for (Entry<String, String> entry : wordsToRemove.entrySet()) {
	//
	// int wordIndex = alphabet.lookupIndex(entry.getKey()); int wordTopicIndex
	// =
	// getTopicIndex(wordIndex, k); int wordTopic =
	// typeTopicCounts[wordIndex][wordTopicIndex] & topicMask; int count =
	// typeTopicCounts[wordIndex][wordTopicIndex] >> topicBits;
	//
	// typeTopicCounts[wordIndex][wordTopicIndex] = ((0) << topicBits) +
	// wordTopic;
	//
	// count = typeTopicCounts[wordIndex][wordTopicIndex] >> topicBits;
	//
	// // remove part bigram tokensPerTopic[k]--; }
	//
	// }
	//
	// ArrayList<TreeSet<IDSorter>> sorted2 = getSortedWords();
	//
	// for (int i = 0; i < sorted.size(); i++) {
	//
	// printSortedWords(sorted.get(i), null, 20);
	//
	// printSortedWords(sorted2.get(i), null, 20); System.out.println(); }
	//
	// System.out.println(); }
	//
	//
	// public void wordsPruning_() {
	//
	// // printTopWords(System.out, 100, false);
	//
	// System.out.println("pruned");
	//
	// ArrayList<TreeSet<IDSorter>> sorted = getSortedWords();
	// ArrayList<TreeSet<IDSorter>> finalWordsCollection = new
	// ArrayList<TreeSet<IDSorter>>();
	//
	// for (int k = 0; k < sorted.size(); k++) {
	//
	// TreeSet<IDSorter> words = sorted.get(k); TreeSet<IDSorter> finalWords =
	// new
	// TreeSet<IDSorter>(); finalWordsCollection.add(finalWords); List<IDSorter>
	// nonOrderedWords = new ArrayList<IDSorter>();
	//
	// Iterator<IDSorter> it = words.iterator(); IDSorter word = it.next(); int
	// max
	// = tokensPerTopic[k] / 4; String[] dict = new String[max];
	//
	// // build bigrams dict for (int i = 0; it.hasNext() && i < max; i++, word
	// =
	// it.next()) { String w = (String) alphabet.lookupObject(word.getID());
	//
	// dict[i] = w; } it = words.iterator(); word = it.next(); List<String>
	// bigrams
	// = null; for (int i = 0; it.hasNext() && i < max; i++, word = it.next()) {
	// String w = (String) alphabet.lookupObject(word.getID()); if
	// (w.startsWith("b_")) { boolean present = false; for (IDSorter obj :
	// nonOrderedWords) { if (obj.getID() == word.getID()) { present = true;
	// break;
	// } } if (!present) nonOrderedWords.add(word);
	//
	// continue; } if (w.startsWith("u_")) w = w.replaceAll("u_[12]_", "");
	//
	// bigrams = searchBigram(w, dict); if (bigrams.size() == 0)
	// nonOrderedWords.add(word); else {
	//
	// int wordTopicIndex = getTopicIndex(word.getID(), k); int wordTopic =
	// typeTopicCounts[word.getID()][wordTopicIndex] & topicMask; int wordCount
	// =
	// typeTopicCounts[word.getID()][wordTopicIndex] >> topicBits;
	//
	// for (String bigram : bigrams) { int bigramIndex =
	// alphabet.lookupIndex(bigram);
	//
	// int bigramTopicIndex = getTopicIndex(bigramIndex, k);
	//
	// if (bigramTopicIndex != -1) {
	//
	// int bigramCount = typeTopicCounts[bigramIndex][bigramTopicIndex] >>
	// topicBits;
	//
	// // typeTopicCounts[bigramIndex][bigramTopicIndex] = // ((bigramCount +
	// wordCount) << topicBits) + // wordTopic;
	// typeTopicCounts[bigramIndex][bigramTopicIndex] = ((Math .max(bigramCount,
	// wordCount)) << topicBits) + wordTopic;
	//
	// int bigramCount2 = typeTopicCounts[bigramIndex][bigramTopicIndex] >>
	// topicBits;
	//
	// // int count2 = // typeTopicCounts[word.getID()][wordTopicIndex] >> //
	// topicBits;
	//
	// boolean present = false; for (IDSorter obj : nonOrderedWords) { if
	// (obj.getID() == bigramIndex) { obj.set(bigramIndex, bigramCount2);
	// present =
	// true; break; } } if (!present) nonOrderedWords.add(new
	// IDSorter(bigramIndex,
	// bigramCount2));
	//
	// } }
	//
	// typeTopicCounts[word.getID()][wordTopicIndex] = ((0) << topicBits) +
	// wordTopic;
	//
	// // remove part bigram tokensPerTopic[k]--;
	//
	// }
	//
	// } for (IDSorter obj : nonOrderedWords) finalWords.add(obj);
	//
	// System.out.print(k + " " + tokensPerTopic[k] + "/" + max);
	// System.out.println(); printSortedWords(words, null, 10); //
	// printSortedWords(words, new String[]{"b_","u_"}, 20);
	// System.out.println();
	// printSortedWords(finalWords, null, 10); // printSortedWords(finalWords,
	// new
	// String[]{"b_","u_"}, 20); System.out.println(); }
	//
	// System.out.println("end."); // try { // this.estimate(); // //
	// System.out.println("..."); // sorted = getSortedWords(); // for(int k =
	// 0;k<sorted.size();k++) // { // printSortedWords(sorted.get(k), null, 20);
	// //
	// System.out.println(); // } // // } catch (IOException e) { // // TODO
	// Auto-generated catch block // e.printStackTrace(); // } }
	//
	// private void processBigram() {
	//
	// }
	//
	//
	// private List<String> searchBigram(String pattern, String[] array) {
	// List<String> bigrams = new ArrayList<String>(); String[] bigramParts =
	// new
	// String[2]; for (int i = 0; i < array.length; i++) { String word =
	// array[i];
	// // only bigrams if (word == null || !word.startsWith("b_")) continue;
	//
	// bigramParts = getBigramParts(word);
	//
	// if (bigramParts[0].equals(pattern) || bigramParts[1].equals(pattern)) {
	// bigrams.add(word); continue; }
	//
	// }
	//
	// return bigrams;
	//
	// }
	//
	// public void ___wordsPruning() {
	//
	// // printTopWords(System.out, 100, false); ArrayList<TreeSet<IDSorter>>
	// sorted
	// = getSortedWords(); int limit = 100;
	//
	// int[][] phrases;
	//
	// for (int di = 0; di < this.getData().size(); di++) { TopicAssignment t =
	// this.getData().get(di); Instance instance = t.instance;
	//
	// FeatureSequence fvs = (FeatureSequence) instance.getData();
	//
	// int doclen = fvs.size(); for (int pos = 0; pos < doclen; pos++) { for
	// (int k
	// = 0; k < sorted.size(); k++) { TreeSet<IDSorter> words = sorted.get(k);
	//
	// Iterator<IDSorter> it = words.iterator(); IDSorter word = null; for (int
	// w =
	// 0; w < limit || w < words.size(); w++) { word = it.next();
	//
	// } }
	//
	// }
	//
	// }
	//
	// printTopWords(System.out, 100, false);
	//
	// topicPhraseXMLReport(new PrintWriter(System.out), 100);
	//
	// }
	//
	// public void __wordsPruning() {
	//
	// Object[] objects = (Object[]) this.alphabet.toArray();
	//
	// words = Arrays.copyOf(objects, objects.length, String[].class);
	//
	// buildWordWeights();
	//
	// for (int w1 = 0; w1 < numTypes; w1++) { System.out.print(words[w1] +
	// "->");
	// TreeSet<IDSorter> sorted = getSimilarWords(w1); printSortedWords(sorted,
	// new
	// String[] { "b_" }, 10); System.out.println(); }
	//
	// }
	//
	// public void _wordsPruning() { Object[] objects = (Object[])
	// this.alphabet.toArray();
	//
	// words = Arrays.copyOf(objects, objects.length, String[].class);
	//
	// buildWordWeights();
	//
	// buildRadixTree(words); ArrayList<Integer> indicies; for (int w = 0; w <
	// words.length; w++) { int len = words[w].length() / 2 + 1; indicies =
	// tree.searchPrefix(words[w].substring(0, len), 20);
	//
	// System.out.println("----------------");
	//
	// for (int i = 0; i < indicies.size(); i++) {
	// System.out.print(words[indicies.get(i)] + "->"); TreeSet sorted = new
	// TreeSet<IDSorter>(); for (int j = i; j < indicies.size(); j++) { if (i ==
	// j)
	// continue;
	//
	// double score = computePredictiveLikelihood(indicies.get(i),
	// indicies.get(j));
	// sorted.add(new IDSorter(indicies.get(j), score));
	//
	// } printSortedWords(sorted, null, 10); System.out.println(); }
	// System.out.println(); }
	//
	// // this.typeTopicCounts }
	//
	// private void pruneSortedWords(int w, TreeSet sorted) { Iterator<IDSorter>
	// it
	// = sorted.iterator(); String word = (String) alphabet.lookupObject(w);
	//
	// int len = (int) 0.75 word.length(); String prefix = word.substring(0,
	// len);
	// while (it.hasNext()) { IDSorter s = it.next();
	//
	// String sw = (String) this.alphabet.lookupObject(s.getID()); //
	// if(sw.startsWith(prefix))
	//
	// } }
	//
	// // PatriciaTrie<String> wordsIndex; RadixTree<Integer> tree;
	// double[][] weightedWords;
	//
	// private void buildRadixTree(String[] words) { // wordsIndex = new
	// PatriciaTrie<String>(new TrieConfiguration()); tree = new
	// RadixTreeImpl<Integer>();
	//
	// for (String w : words) tree.insert(w, alphabet.lookupIndex(w));
	//
	// }
	//
	//
	//
	// String[] words = null;
	//
	// public TreeSet<IDSorter> getSimilarWords(int w1) { TreeSet<IDSorter>
	// sorted =
	// new TreeSet<IDSorter>(); for (int w2 = 0; w2 < numTypes; w2++) { if (w2
	// ==
	// w1) continue;
	//
	// double score = computePredictiveLikelihood(w1, w2); if (score > 0)
	// sorted.add(new IDSorter(w2, score));
	//
	// }
	//
	// return sorted; }
	//
	// private int[] getWords(TreeSet<IDSorter> sorted, int limit) {
	// Iterator<IDSorter> it = sorted.iterator(); int[] result = new int[limit];
	// int
	// i = 0; while (it.hasNext() && i < limit) { result[i] = it.next().getID();
	// }
	//
	// return result;
	//
	// }
	//
	//
	// }
	//
}