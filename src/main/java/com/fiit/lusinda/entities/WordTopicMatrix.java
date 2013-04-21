package com.fiit.lusinda.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import cc.mallet.types.Alphabet;
import cc.mallet.types.DenseMatrix;
import cc.mallet.types.IDSorter;
import cc.mallet.types.MatrixOps;

import com.fiit.lusinda.similarity.CosineSimilarity;
import com.fiit.lusinda.similarity.JackardSimilarity;
import com.fiit.lusinda.similarity.JsSimilarityMeasure;
import com.fiit.lusinda.similarity.SimilarityStrategy;
import com.fiit.lusinda.topicmodelling.Lda;
import com.fiit.lusinda.topicmodelling.LdaModel;

public class WordTopicMatrix {

	// private double[][] word2WordSimilarityMatrix;
	RealMatrix matrix;
	// private List<SimilarItem> topics = new ArrayList<SimilarItem>();
	// private List<SimilarItem> words = new ArrayList<SimilarItem>();
	Alphabet alphabet;

	int topicCounts;
	int wordCounts;
	JsSimilarityMeasure js = new JsSimilarityMeasure();
	LdaModel lda;
	
	public LdaModel getLdaModel()
	{
		return lda;
	}
	
	public WordTopicMatrix(int topics, int words, Alphabet alphabet) {

		this.topicCounts = topics;
		this.wordCounts = words;
		this.alphabet = alphabet;

		matrix = new Array2DRowRealMatrix(topics, words);
		// word2WordSimilarityMatrix = new double[words][words];
		// org.apache.commons.math.
		// matrix = new
		// matrix = new double[topics][words];

	}

	public WordTopicMatrix(LdaModel ldaModel,double[][] phi, Alphabet alphabet) {

		this.alphabet = alphabet;
this.lda = ldaModel;
		matrix = new Array2DRowRealMatrix(phi);
		this.topicCounts = matrix.getColumnDimension();
		this.wordCounts = matrix.getRowDimension();
		// word2WordSimilarityMatrix = new double[words][words];
		// org.apache.commons.math.
		// matrix = new
		// matrix = new double[topics][words];

	}

	//
	// public List<SimilarItem> getTopics()
	// {
	// return topics;
	// }
	//
	// public List<SimilarItem> getWords()
	// {
	// return words;
	// }

	public void setTopicDistribution(int topic, long ts, double[] dist) {
		matrix.setRow(topic, dist);
		//
		// Topic t =new Topic(topic,ts);
		// t.addTopicDist(ts, dist);
		// topics.add(t);

	}

	private TreeSet<IDSorter> getSortedDist(double[] dist) {
		TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

		for (int i = 0; i < dist.length; i++)
			sorted.add(new IDSorter(i, dist[i]));

		return sorted;
	}

	public void printSimilarDocuments(String w, TreeSet<IDSorter> sortedDist,
			int max, PrintWriter out, List<SimilarItem> documents) {

		System.out.print("[" + w + "]" + ": ");

		Iterator<IDSorter> iterator = sortedDist.iterator();
		int i = 0;
		while (iterator.hasNext() && i < max) {
			IDSorter info = iterator.next();
			String doc = (String) documents.get(info.getID()).toString();
			// if (word != null && word.contains("_"))
			// {
			System.out.print(doc + ": " + info.getWeight() + " ");
			i++;
			// }
		}
		System.out.println();

	}

	
	
	public void printSimilarWords(String w, TreeSet<IDSorter> sortedDist,
			int max, PrintWriter out) {

		out.print("[" + w + "]" + ": ");

		Iterator<IDSorter> iterator = sortedDist.iterator();
		int i = 0;
		while (iterator.hasNext() && i < max) {
			IDSorter info = iterator.next();
			String word = (String) alphabet.lookupObject(info.getID());
			// if (word != null && word.contains("_"))
			// {
			out.print(alphabet.lookupObject(info.getID()) + ": "
					+ info.getWeight() + " ");
			i++;
			// }
		}
		out.println();

	}
	
	

	public List<TreeSet<IDSorter>> computeSimilarTopics2(WordTopicMatrix wtMatrix2,SimilarityStrategy strategy)
	{
		List<TreeSet<IDSorter>> result = new ArrayList<TreeSet<IDSorter>>();
		JsSimilarityMeasure js = new JsSimilarityMeasure();
		JackardSimilarity jackard = new JackardSimilarity();
		CosineSimilarity cosine = new CosineSimilarity();
		
		TreeSet<IDSorter> sortedDistJs = null;
		TreeSet<IDSorter> sortedDistJackard = null;
		TreeSet<IDSorter> sortedDistCosine = null;
		double sim = 0;
		int max = 50;
		
		for(int k1=0;k1<this.topicCounts;k1++)
		{
			sortedDistJs = new TreeSet<IDSorter>();
			sortedDistJackard = new TreeSet<IDSorter>();
			sortedDistCosine = new TreeSet<IDSorter>();
			
			for(int k2=0;k2<wtMatrix2.topicCounts;k2++)
			{
				
				sim = 1- js.computeSimmilarity(this.matrix.getRow(k1), wtMatrix2.matrix.getRow(k2));
				 sortedDistJs.add(new IDSorter(k2, sim));
				
					sim = jackard.computeSimilarity(this.getLdaModel().getLda().getWordIndicies(k1, max),
							 this.getLdaModel().getLda().getWordIndicies(k2, max));
					 sortedDistJackard.add(new IDSorter(k2, sim));
				
				
				
					
				//	sim = -1111;//cosine.calculateSimilarity(this.matrix.getRow(k1), wtMatrix2.matrix.getRow(k2));
					// sortedDistCosine.add(new IDSorter(k2, sim));
				
					
			
			}	
			
		//	result.add(sortedDist);
			System.out.println("----------------------");
			System.out.println("js:");
			lda.getLda().printTopics(k1, sortedDistJs, 10);
			System.out.println("jackard:");
			lda.getLda().printTopics(k1, sortedDistJackard, 10);
			System.out.println("cosine:");
			lda.getLda().printTopics(k1, sortedDistCosine, 10);
		}
		
		return result;
	}
	
	public List<TreeSet<IDSorter>> computeSimilarTopics(WordTopicMatrix wtMatrix2)
	{
		List<TreeSet<IDSorter>> result = new ArrayList<TreeSet<IDSorter>>();
		JsSimilarityMeasure js = new JsSimilarityMeasure();
		TreeSet<IDSorter> sortedDist = null;
		double sim = 0;
		
		for(int k1=0;k1<this.topicCounts;k1++)
		{
			sortedDist = new TreeSet<IDSorter>();
			
			for(int k2=0;k2<wtMatrix2.topicCounts;k2++)
			{
				sim = 1- js.computeSimmilarity(this.matrix.getRow(k1), wtMatrix2.matrix.getRow(k2));
				sortedDist.add(new IDSorter(k2, sim));
			}
			
			result.add(sortedDist);
		}
		
		return result;
	}
	
	public void computeSimilarWords(long ts, int max, double treeshold, File f)
			throws FileNotFoundException, UnsupportedEncodingException {
		// create
		// for(int w=0,i=0;w<wordCounts && i<max;w++)
		// {
		// Word word = new Word(w, ts);
		// word.value = (String) alphabet.lookupObject(w);
		// double[] dist = matrix.getColumn(w);
		// word.addTopicDist(ts, dist);
		// this.words.add(word);
		// }

		PrintWriter out = new PrintWriter(f, "UTF-8");

		JsSimilarityMeasure js = new JsSimilarityMeasure();

		TreeSet<IDSorter> sortedDist = null;
		RealMatrix wordmatrix = matrix.transpose();

		for (int w1 = 0; w1 < wordmatrix.getRowDimension(); w1++) {

			sortedDist = new TreeSet<IDSorter>();
			String word = (String) alphabet.lookupObject(w1);

			if (word != null) {

				for (int w2 = 0; w2 < wordmatrix.getRowDimension(); w2++) {

					double probWordWordTotal = 0;
					for (int z = 0; z < wordmatrix.getColumnDimension(); z++) {

						double prob = wordmatrix.getEntry(w2, z);
						prob = prob * wordmatrix.getEntry(w1, z);
						probWordWordTotal += prob;

					}
					// probWordWordTotal = js.computeSimmilarity(
					// wordmatrix.getRow(w1), wordmatrix.getRow(w2));
					if (probWordWordTotal > 0)
						sortedDist.add(new IDSorter(w2, probWordWordTotal));

				}

				printSimilarWords(word, sortedDist, max, out);
			}
		}
		out.flush();
	}

	public void computeSimilarWordsExtended(List<SimilarItem> documents,
			List<List<WordCluster>> clusters, int max, double treeshold, File f)
			throws FileNotFoundException, UnsupportedEncodingException {
		// create
		// for(int w=0,i=0;w<wordCounts && i<max;w++)
		// {
		// Word word = new Word(w, ts);
		// word.value = (String) alphabet.lookupObject(w);
		// double[] dist = matrix.getColumn(w);
		// word.addTopicDist(ts, dist);
		// this.words.add(word);
		// }

		PrintWriter out = new PrintWriter(f, "UTF-8");
		System.out.println();
		System.out.println();
		System.out.println();

		TreeSet<IDSorter> sortedDist = null;
		RealMatrix wordmatrix = matrix;// matrix.transpose();
		// double res = MatrixOps.sum(wordmatrix.getColumn(41));

		for (int k = 0; k < 50; k++) {
			List<WordCluster> words1 = clusters.get(k);

			// List<WordCluster> words2 = clusters.get(45);
			for (WordCluster word1 : words1) {
				if (word1.words.size() == 1)
					continue;

				sortedDist = new TreeSet<IDSorter>();
				for (int d = 0; d < documents.size(); d++) {
					SimilarItem doc = documents.get(d);
					double probWordWordTotal = 0;
					Iterator<IDSorter> it1 = word1.words.iterator();

					// while(it1.hasNext())
					// {
					// IDSorter w1 = it1.next();
					for (int z = 0; z < wordmatrix.getColumnDimension(); z++) {

						double prob = doc.getDist()[z]; // wordmatrix.getEntry(word2.id,
														// z);
						prob = prob
								* wordmatrix.getEntry(word1.words.first()
										.getID(), z);
						probWordWordTotal += prob;

					}
					// }
					// probWordWordTotal = js.computeSimmilarity(
					// wordmatrix.getRow(w1), wordmatrix.getRow(w2));
					if (probWordWordTotal > 0)
						sortedDist.add(new IDSorter(d, probWordWordTotal));
				}
				printSimilarDocuments(word1.word, sortedDist, max, out,
						documents);
			}
			out.flush();
			out.println("done");
			out.println();
			out.println();
			out.println();

			for (WordCluster word1 : words1) {
				if (word1.words.size() == 1)
					continue;

				sortedDist = new TreeSet<IDSorter>();
				for (int d = 0; d < documents.size(); d++) {
					SimilarItem doc = documents.get(d);
					double probWordWordTotal = 0;
					Iterator<IDSorter> it1 = word1.words.iterator();

					while (it1.hasNext()) {
						IDSorter w1 = it1.next();
						for (int z = 0; z < wordmatrix.getColumnDimension(); z++) {

							double prob = doc.getDist()[z]; // wordmatrix.getEntry(word2.id,
															// z);
							prob = prob * wordmatrix.getEntry(w1.getID(), z);
							probWordWordTotal += prob;

						}
					}
					// probWordWordTotal = js.computeSimmilarity(
					// wordmatrix.getRow(w1), wordmatrix.getRow(w2));
					if (probWordWordTotal > 0)
						sortedDist.add(new IDSorter(d, probWordWordTotal));
				}
				printSimilarDocuments(word1.word, sortedDist, max, out,
						documents);
			}
		}
		out.print("done");

	}
	// public List<IntDoublePair> cumputeSimilarTopics(int topic,int max,double
	// treeshold)
	// {
	//
	// List<IntDoublePair> similarTopics = new ArrayList<IntDoublePair>(max);
	//
	// for(int k=0,i=0;k<topicCounts && i<max;k++)
	// {
	// double result =
	// js.computeSimmilarity(matrix.getRow(topic),matrix.getRow(k));
	// if(result>treeshold)
	// {
	// similarTopics.add(new IntDoublePair(i, result));
	// i++;
	// }
	// }
	//
	// return similarTopics;
	// }
	//
	// public List<IntDoublePair> cumputeSimilarWords(int word,int max,double
	// treeshold)
	// {
	// List<IntDoublePair> similarWords = new ArrayList<IntDoublePair>(max);
	//
	//
	// for(int w=0,i=0;w<wordCounts && i<max;w++)
	// {
	// double result =
	// js.computeSimmilarity(matrix.getColumn(word),matrix.getColumn(w));
	// if(result>treeshold)
	// {
	// similarWords.add(new IntDoublePair(i, result));
	// i++;
	// }
	// }
	//
	// return similarWords;
	// }

}
