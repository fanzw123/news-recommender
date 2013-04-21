package com.fiit.lusinda.topicmodelling;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.store.Directory;

import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithmDescriptor;
import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.clustering.synthetic.ByUrlClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.LanguageCode;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
import org.carrot2.text.linguistic.DefaultLexicalDataFactoryDescriptor;
import org.carrot2.text.linguistic.LexicalDataLoaderDescriptor;
import org.carrot2.text.preprocessing.pipeline.BasicPreprocessingPipelineDescriptor;
import org.carrot2.util.resource.DirLocator;
import org.carrot2.util.resource.ResourceLookup;

import com.fiit.lusinda.carrot.StemmerFactory;
import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.clustering.Hac;
import com.fiit.lusinda.clustering.ResultsClustering;
import com.fiit.lusinda.clustering.ResultsDocuments;
import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.DocumentCluster;
import com.fiit.lusinda.entities.DocumentStream;
import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.NGram;
import com.fiit.lusinda.entities.Phrases;
import com.fiit.lusinda.entities.Query;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.entities.StringDoublePair;
import com.fiit.lusinda.graph.GraphViz;
import com.fiit.lusinda.mallet.CharSequence2TokenSequencePreserveOriginal;
import com.fiit.lusinda.mallet.TokenSequenceLemmatize;
import com.fiit.lusinda.mallet.TokenSequenceRemovePunct;
import com.fiit.lusinda.mallet.TokenSequenceNGramsFiltered;
import com.fiit.lusinda.mallet.TokenSequenceNormalize;
import com.fiit.lusinda.rss.FeedSettings;
import com.fiit.lusinda.similarity.JackardSimilarity;
import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.utils.Logging;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Directory2FileIterator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceNGrams;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.HierarchicalLDA;
import cc.mallet.topics.LDAStream;
import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.topics.TopicModelDiagnostics;
import cc.mallet.topics.TopicModelDiagnostics.TopicScores;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import cc.mallet.util.CharSequenceLexer;
import cc.mallet.util.CollectionUtils;
import cc.mallet.util.Randoms;
import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.CompleteLinkage;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class LdaModel {

	public LdaModel() {

	}

	/*
	 * public static void fillDocumentsAttributes(List<DocumentProbability>
	 * docs, int maxDocuments, SmeDataSource dataSource) throws SQLException {
	 * 
	 * maxDocuments = maxDocuments == -1 ? docs.size() : maxDocuments;
	 * 
	 * for (int i = 0; i < maxDocuments; i++) {
	 * 
	 * DocumentAttributes docAtts = docs.get(i).getDocAttributes();
	 * 
	 * docs.get(i).setDocAttributes(dataSource.fillDocAttributes(docAtts));
	 * 
	 * } }
	 * 
	 * public static void fillDocumentsAttributesInTopic(List<Topic> topics,
	 * ParallelTopicModel lda, SmeDataSource dataSource, int maxDocuments)
	 * throws IOException, SQLException {
	 * 
	 * for (Topic topic : topics) {
	 * 
	 * fillDocumentsAttributes(topic.getDocumentProbabilities(), maxDocuments,
	 * dataSource); }
	 * 
	 * }
	 * 
	 * public static void printDocumentsForTopics(File f, List<Topic> topics,
	 * ParallelTopicModel lda, int maxDocuments) throws IOException,
	 * SQLException {
	 * 
	 * OutputStream os = (OutputStream) new FileOutputStream(f); String encoding
	 * = "UTF8"; OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
	 * BufferedWriter bw = new BufferedWriter(osw);
	 * 
	 * bw.write("document title"); bw.write(","); bw.write("probability");
	 * bw.newLine();
	 * 
	 * SmeDataSource sme = new SmeDataSource(); sme.connect(); for (Topic topic
	 * : topics) { bw.newLine(); bw.write("Topic"); bw.newLine();
	 * 
	 * for (int i = 0; i < maxDocuments; i++) {
	 * 
	 * DocumentAttributes docAtts = topic.getDocumentProbability(i)
	 * .getDocAttributes();
	 * 
	 * topic.getDocumentProbability(i).setDocAttributes(
	 * sme.fillDocAttributes(docAtts));
	 * 
	 * bw.write(topic.getDocumentProbability(i).getDocAttributes().title);
	 * bw.write(",");
	 * bw.write(Double.toString(topic.getDocumentProbability(i).prob));
	 * bw.newLine(); } }
	 * 
	 * bw.flush(); bw.close();
	 * 
	 * sme.disconnect();
	 * 
	 * }
	 * 
	 * public static void exportDocumentTopics(File f, ParallelTopicModel lda,
	 * List<Topic> topics, List<DocumentProbability> docs, double threshold, int
	 * max) throws IOException {
	 * 
	 * OutputStream os = (OutputStream) new FileOutputStream(f); String encoding
	 * = "UTF-8"; OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
	 * BufferedWriter bw = new BufferedWriter(osw);
	 * 
	 * bw.write("title\t"); bw.write("category\t");
	 * 
	 * for (int i = 0; i < topics.size(); i++) { bw.write("topic" +
	 * Integer.toString(i) + "\t"); }
	 * 
	 * bw.newLine();
	 * 
	 * int docLen; int numTopics = lda.getNumTopics();
	 * ArrayList<TopicAssignment> data = lda.getData();
	 * 
	 * int[] topicCounts = new int[numTopics];
	 * 
	 * IDSorter[] sortedTopics = new IDSorter[numTopics]; for (int topic = 0;
	 * topic < numTopics; topic++) { // Initialize the sorters with dummy values
	 * sortedTopics[topic] = new IDSorter(topic, topic); }
	 * 
	 * if (max < 0 || max > numTopics) { max = numTopics; }
	 * 
	 * for (int doc = 0; doc < data.size(); doc++) { LabelSequence topicSequence
	 * = (LabelSequence) data.get(doc).topicSequence; int[] currentDocTopics =
	 * topicSequence.getFeatures();
	 * 
	 * bw.write(docs.get(doc).getDocAttributes().title + "\t");
	 * bw.write(docs.get(doc).getDocAttributes().category + "\t");
	 * 
	 * docLen = currentDocTopics.length;
	 * 
	 * // Count up the tokens for (int token = 0; token < docLen; token++) {
	 * topicCounts[currentDocTopics[token]]++; }
	 * 
	 * // And normalize for (int topic = 0; topic < numTopics; topic++) {
	 * sortedTopics[topic].set(topic, (float) topicCounts[topic] / docLen); }
	 * 
	 * // Arrays.sort(sortedTopics);
	 * 
	 * for (int i = 0; i < max; i++) { if (sortedTopics[i].getWeight() <
	 * threshold) { break; }
	 * 
	 * bw.write(sortedTopics[i].getWeight() + "\t"); } bw.newLine();
	 * 
	 * Arrays.fill(topicCounts, 0);
	 * 
	 * } bw.flush(); bw.close(); }
	 * 
	 * public static List<DocumentProbability> getDocuments(ParallelTopicModel
	 * lda) {
	 * 
	 * ArrayList<TopicAssignment> data = lda.getData();
	 * 
	 * ArrayList<DocumentProbability> docs = new
	 * ArrayList<DocumentProbability>();
	 * 
	 * String docId = null;
	 * 
	 * for (int doc = 0; doc < data.size(); doc++) { LabelSequence topicSequence
	 * = (LabelSequence) data.get(doc).topicSequence;
	 * 
	 * if (data.get(doc).instance.getSource() != null) { docId = (String)
	 * String.valueOf(data.get(doc).instance .getSource()); docs.add(new
	 * DocumentProbability(docId, 0)); } else { }
	 * 
	 * }
	 * 
	 * return docs;
	 * 
	 * }
	 * 
	 * public static List<Topic> getDocumentTopics(ParallelTopicModel lda,
	 * double threshold, int maxDocuments) { int docLen; int numTopics =
	 * lda.getNumTopics(); ArrayList<TopicAssignment> data = lda.getData();
	 * 
	 * ArrayList<Topic> topics = new ArrayList<Topic>();
	 * 
	 * int[] topicCounts = new int[numTopics];
	 * 
	 * Topic currTopic = null;
	 * 
	 * IDSorter[] sortedTopics = new IDSorter[numTopics]; for (int topic = 0;
	 * topic < numTopics; topic++) { // Initialize the sorters with dummy values
	 * sortedTopics[topic] = new IDSorter(topic, topic); // create result list
	 * currTopic = new Topic(topic, maxDocuments); topics.add(currTopic); }
	 * 
	 * String docId = null;
	 * 
	 * for (int doc = 0; doc < data.size(); doc++) { LabelSequence topicSequence
	 * = (LabelSequence) data.get(doc).topicSequence; int[] currentDocTopics =
	 * topicSequence.getFeatures();
	 * 
	 * DocumentProbability docProb = new DocumentProbability(); if
	 * (data.get(doc).instance.getSource() != null) { docId = (String)
	 * String.valueOf(data.get(doc).instance .getSource()); } else { }
	 * 
	 * docLen = currentDocTopics.length;
	 * 
	 * // Count up the tokens for (int token = 0; token < docLen; token++) {
	 * topicCounts[currentDocTopics[token]]++; }
	 * 
	 * // And normalize for (int topic = 0; topic < numTopics; topic++) {
	 * sortedTopics[topic].set(topic, (float) topicCounts[topic] / docLen); }
	 * 
	 * Arrays.sort(sortedTopics);
	 * 
	 * for (int i = 0; i < numTopics; i++) { currTopic =
	 * topics.get(sortedTopics[i].getID()); currTopic.Add(new
	 * DocumentProbability(docId, sortedTopics[i] .getWeight())); //
	 * currTopic.AddOrLeave(docId, sortedTopics[i].getWeight()); // if
	 * (sortedTopics[i].getWeight() < threshold) { break; }
	 * 
	 * }
	 * 
	 * Arrays.fill(topicCounts, 0); }
	 * 
	 * for (Topic t : topics) t.Sort();
	 * 
	 * return topics; }
	 */

	int initNumTopics = 20;
	// int maxTopics = 20;
	// int step = 10;
	// int averageRun = 1;

	// double alpha = 10.00;
	// double beta = 0.01;
	Lda lda = null;

	double likelihood = 0;

	String outputPath;

	InstanceList trainList;

	InstanceList testList;

	MarginalProbEstimator evaluator;

	Map<Integer, Double> ldaLikelihoodPairs = new HashMap<Integer, Double>();

	public Lda getLda() {
		return lda;
	}

	public void init(int numTopics, InstanceList trainList,
			InstanceList testList, String outputPath) {
		this.initNumTopics = numTopics;
		this.testList = testList;
		this.trainList = trainList;
		this.outputPath = outputPath;
		File f = new File(outputPath);
		if (!f.exists())
			f.mkdir();
		
		
	}
	
//	public LdaModel(int numTopics, InstanceList trainList,
//			InstanceList testList, String outputPath) {
//		this.initNumTopics = numTopics;
//		this.testList = testList;
//		this.trainList = trainList;
//		this.outputPath = outputPath;
//		File f = new File(outputPath);
//		if (!f.exists())
//			f.mkdir();
//
//	}
//
//	public LdaModel(int numTopics, String trainListPath, String testListPath,
//			String outputPath) {
//		this(numTopics, InstanceList.load(new File(trainListPath)),
//				InstanceList.load(new File(testListPath)), outputPath);
//
//	}
//
//	public LdaModel(int numTopics, String trainListPath, String outputPath) {
//		this(numTopics, InstanceList.load(new File(trainListPath)), null,
//				outputPath);
//	}

	public static InstanceList createInstanceList(List<String> texts, long ts) {
		InstanceList instanceList = createInstanceList(ts);
		instanceList.addThruPipe(new ArrayIterator(texts));
		return instanceList;
	}

	public static InstanceList createInstanceList(long ts) {
	ArrayList<Pipe> pipes = new ArrayList<Pipe>();

		// for bigrams
		// pipes.add(new
		// CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\.,!\\?]+")));
		// for non bigrams

		String preprocessedRegex = "\\p{L}[\\p{L}\\p{P}]+\\p{L}";
		String originalRegex = "[\\p{L}\\.,!\\?]+";

		pipes.add(new CharSequence2TokenSequencePreserveOriginal(Pattern
				.compile(originalRegex), Pattern.compile(preprocessedRegex)));

		pipes.add(new TokenSequenceLowercase());

		// pipes.add(new TokenSequenceRemovePunct());
		pipes.add(new TokenSequenceLemmatize());
		pipes.add(new TokenSequenceRemoveStopwords(StandardTextProcessing
				.getStopWordsFile(Lang.SLOVAK), "UTF-8", false, false, false));

		// pipes.add(new TokenSequenceRemoveStopwords(new
		// File(LdaModel.class.getResource("/sk.txt").getFile()),"UTF-8",false,false,false));
		// pipes.add(new TokenSequenceNormalize(ts));

		// remove bigrams for now
		// pipes.add(new TokenSequenceNGramsFiltered(ts));

		// pipes.add(new TokenSequenceLowercaseWithNGrams());
		// pipes.add(new TokenSequence2FeatureSequenceWithBigrams());
		pipes.add(new TokenSequence2FeatureSequence());

		InstanceList instanceList = new InstanceList(new SerialPipes(pipes));

		return instanceList;
	}

	
	
	public static LdaProperties getDefaultProperties(String outputDir,FeedSettings settings) {
		LdaProperties properties = new LdaProperties();
		properties.topics = settings.topics;
		properties.averageNumRuns = settings.averageNumRuns;
		properties.evaluate = settings.evaluate;
		properties.estimateTopicCountsUsingHLDA = settings.estimateTopicCountsUsingHLDA;

		// properties.seed = 20;
		// properties.steps = 25;
		properties.DocumentTopicsExportFilePath = outputDir
				+ "/topics-in-documents.csv";
		properties.WordTopicsExportFilePath = outputDir
				+ "/topics-in-words.csv";

		properties.maxTopics = properties.topics;
		properties.topicIncrement = 1;

		return properties;
	}

	public Alphabet getAlphabet() {
		return lda.getAlphabet();
	}

	public int getWordCount() {
		return lda.getAlphabet().size();
	}

	public double[] infer(Instance instance) {
		return lda.getInferencer().getSampledDistribution(instance, 10, 1, 5);
		

	}

	public double[] getDocumentTopic(int docId) {
		return lda.getTopicProbabilities(docId);
	}

	public double[] getTopicWordDistribution(int topicId) {
		Iterator<IDSorter> iterator = lda.getSortedWords().get(topicId)
				.iterator();
		double[] dist = new double[lda.getAlphabet().size()];

		while (iterator.hasNext()) {
			IDSorter idCountPair = iterator.next();
			dist[idCountPair.getID()] = idCountPair.getWeight()
					/ lda.geTtokensPerTopic()[topicId];
		}

		return dist;
	}

	public Dataset runInternal(LdaProperties properties) throws IOException {
		Lda curr_lda = estimate(properties);
		if (!properties.evaluate)
			lda = curr_lda;
		else {
			// this.lda = curr_lda;

			evaluator = curr_lda.getProbEstimator();
			double currLikelihood = properties.heldOut ? evaluator
					.evaluateLeftToRight(testList, 30, true, null) : curr_lda
					.modelLogLikelihood();

			// currLikelihood = Math.log10(currLikelihood); // 10,
			// false, null);
			//
			Double old = ldaLikelihoodPairs.get(properties.topics);
			if (old == null)
				old = 0.0;
			ldaLikelihoodPairs.put(properties.topics, old + currLikelihood);
			//
			// //
			Logging.Log(String.format("numTopics:{0} \t ll:{1} \best ll:{2}",
					properties.topics, currLikelihood, likelihood));
			properties.bw.write(Integer.toString(properties.topics));
			properties.bw.write(",");

			properties.bw.write(Double.toString(currLikelihood));
			properties.bw.write(",");
			// double bigrams = this.lda.wordsPruning();
			// lda.topicPhraseXMLReport(new PrintWriter(new
			// FileOutputStream("/var/lusinda/solr/rss/new/sme_sk/phrases.xml")),
			// 200);
			// properties.bw.write(Double.toString(bigrams));
			properties.bw.newLine();
			if (likelihood == 0 || currLikelihood > likelihood) {
				this.likelihood = currLikelihood;
				this.lda = curr_lda;
			}

		}
		// export
		// List<Topic> exportTopics = Utils.getDocumentTopics(curr_lda, 0, 10);
		// List<DocumentProbability> docs = Utils.getDocuments(curr_lda);
		// Utils.fillDocumentsAttributes(docs,trainList, -1);

		// curr_lda.topicPhraseXMLReport(new PrintWriter(new
		// File("/var/lusinda/solr/mallet/9/topicPhrase.xml")),curr_lda.getAlphabet().size());
		// curr_lda.topicXMLReport(new PrintWriter(new
		// File("/var/lusinda/solr/mallet/9/topicReport.xml")),curr_lda.getAlphabet().size());

		// Utils.exportWordsTopics(curr_lda,properties.WordTopicsExportFilePath,";");

		// return
		// Utils.exportDocumentTopics(properties.DocumentTopicsExportFilePath,
		// curr_lda, docs, 0, -1, ";");

		return null;
	}

	public Dataset run(LdaProperties properties) throws IOException {

		
		
		String fileName = "likelihood";
		double beta = 0.01;
		double alpha = 50.00 / initNumTopics;
		Dataset exportedDocuments = null;

		for (int run = 1; run <= properties.averageNumRuns; run++) {

			int topics = initNumTopics;
			OutputStream os = (OutputStream) new FileOutputStream(new File(
					outputPath + fileName + Integer.toString(run) + ".csv"));
			String encoding = "UTF8";
			OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
			BufferedWriter bw = new BufferedWriter(osw);

			properties.bw = bw;

			bw.write("topic number");
			bw.write(",");
			bw.write("left-to-right");
			bw.newLine();

			for (; topics <= properties.maxTopics; topics += properties.topicIncrement) {

				alpha = 50.00 / topics;
				properties.alpha = alpha;
				properties.beta = beta;
				properties.topics = topics;

				exportedDocuments = runInternal(properties);

			}

			bw.flush();
			bw.close();

			// return exportedDocuments;
		}

		// dokoncit priemer, pozret ako v hashmap zmnit vlozenu double hodnotu

		if (properties.evaluate) {
			OutputStream os = (OutputStream) new FileOutputStream(new File(
					outputPath + fileName + "-average.csv"));
			String encoding = "UTF8";
			OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write("topic number");
			bw.write(",");
			bw.write("left-to-right");
			bw.newLine();
			int winnerTopics = 0;
			double avg_best = ldaLikelihoodPairs.get(100);
			for (Entry<Integer, Double> entry : ldaLikelihoodPairs.entrySet()) {
				double avg = entry.getValue() / properties.averageNumRuns;
				if (avg > avg_best) {
					avg_best = avg;
					winnerTopics = entry.getKey();
				}
				entry.setValue(avg);

				bw.write(Integer.toString(entry.getKey()));
				bw.write(",");

				bw.write(Double.toString(entry.getValue()));
				bw.newLine();
			}

			bw.flush();
			bw.close();

			// choose best and train
			properties.topics = winnerTopics;
			properties.heldOut = false;

			this.lda = estimate(properties);

		}

		return exportedDocuments;

	}

	private Lda estimate(LdaProperties properties) throws IOException {
		Lda curr_lda = new Lda(properties.topics, properties.alpha,
				properties.beta);
		curr_lda.setNumIterations(1000);
		if (properties.heldOut) {
			InstanceList[] lists = trainList.splitInTwoByModulo(5);
			curr_lda.addInstances(lists[1]);
			this.testList = lists[0];
		} else
			curr_lda.addInstances(trainList);

		curr_lda.estimate();

		return curr_lda;

	}
	
	public static void wordLemaClustering() throws Exception
	{
FeedSettings settings = FeedSettings.getSmeExperimentFeedSettings(7);
		
		
		String rootDir = settings.outputDirectory+"/lda/";
		Lda lda = (Lda) Lda.read(new File(rootDir + "1/model.mallet"));
		lda.wordsPruning();
		
	}
	public static void computeTopicChain(int topic) throws Exception {
		
		FeedSettings settings = FeedSettings.getSmeExperimentFeedSettings(7);
		
		
		String rootDir = settings.outputDirectory+"/lda/";
		
		String type = "pdf";
		// int start = 1;

		int windowLength = 2;
		List<Lda> models = new ArrayList<Lda>();

		int start = 0;
		int end = 3;
		

		Lda lda = null;

		String[][][][] commonWords = new String[end][windowLength][][];
		double[][][][] edges = new double[end][windowLength][][];

		for (int window = 1; window <= windowLength + 1; window++) {
			lda = (Lda) Lda.read(new File(rootDir + window + "/model.mallet"));
			lda.calculatePhi();
			models.add(lda);
		}

		for (int index = start; index < end; index++) {
			int next = index + windowLength + 1;
			if (next >= models.size() && next < end) {
				lda = (Lda) Lda
						.read(new File(rootDir + next + "/model.mallet"));
				models.add(lda);
			}
			lda = models.get(index);
			// lda.findBigrams2();
			for (int window = 1; window <= windowLength; window++) {
				int inx = window - 1;
				int current = index + window;
				if (current >= models.size())
					break;
				Lda currentLda = models.get(current);
				currentLda.buildDocumentsTopic();

				commonWords[index][inx] = new String[lda.numTopics][currentLda.numTopics];
				edges[index][inx] = new double[lda.numTopics][currentLda.numTopics];

				// currentLda.findBigrams2();

				lda.buildDocumentsTopic();
				lda.calculateDiagnostics();
				lda.findSimilarTopics2(currentLda, index, inx, commonWords,
						edges, end);
				System.out.println("finding similar topics: " + index + "->"
						+ current);
			}

		}

		System.out.println("topics for model 0");
		models.get(0).printTopWords(System.out, 10, false);

		List<String> topWords = new ArrayList<String>();
		int topCount = 30;

		List<Stack<Integer>> stack = new ArrayList<Stack<Integer>>();

		// init
		int[][] chain = new int[end][70];
		for (int i = 0; i < end; i++)
			for (int j = 0; j < chain[i].length; j++)
				chain[i][j] = -1;

		chain[0][0] = topic;

		for (int m1 = 0; m1 < edges.length; m1++) {

			for (int k1 = chain[m1][0], i = 0; i < chain[m1].length; i++) {
				k1 = chain[m1][i];
				if (k1 == -1)
					continue;

				for (int m2 = 0, j = 0; m2 < edges[m1].length; m2++) {

					int next_m = m1 + m2 + 1;

					if (edges[m1][m2] == null)
						break;

					for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {
						if (edges[m1][m2][k1][k2] > 0) {

							chain[next_m][j] = k2;
							j++;
						}
					}
				}

			}
		}

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("rankdir=LR;");

		// timeline
		gv.addln("{");
		for (int m = 0; m < end; m++) {
			stack.add(new Stack<Integer>());
			gv.add("ts");
			gv.add(String.valueOf(m));
			if (m < end - 1)
				gv.add("->");
		}
		gv.addln("}");

		// ranks
		for (int m = 0; m < chain.length; m++) {
			gv.addln("{");
			gv.add("rank=same;");
			gv.add("\"ts");
			gv.add(String.valueOf(m));
			gv.add("\"");
			gv.add(";");
			String ts = "ts" + m + "_";
			for (int i = 0; i < chain[m].length; i++) {
				if (chain[m][i] > -1) {
					gv.add(ts + String.valueOf(chain[m][i]));
					gv.add(";");
				}

			}
			gv.addln("}");
		}

		/*
		 * 
		 * // edges for (int m1 = 0; m1 < chain.length; m1++) {
		 * 
		 * String ts1 = "ts" + m1 + "_";
		 * 
		 * for (int i = 0, k1 = chain[m1][i]; i < chain[m1].length; i++) {
		 * 
		 * k1 = chain[m1][i];
		 * 
		 * if (k1 == -1) continue;
		 * 
		 * gv.add(ts1 + String.valueOf(k1)); gv.add("[label=\"");
		 * gv.add(models.get(m1).getLabel(k1, 5,"\\n")); gv.addln("\"];");
		 * 
		 * for (int m2 = 0; m2 < edges[m1].length; m2++) {
		 * 
		 * if (edges[m1][m2] == null) break;
		 * 
		 * int next_m = m1 + m2 + 1; String ts2 = "ts" + next_m + "_";
		 * 
		 * for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {
		 * 
		 * if (edges[m1][m2][k1][k2] > 0) {
		 * 
		 * gv.add(ts1 + String.valueOf(k2)); gv.add("[label=\"");
		 * gv.add(models.get(next_m).getLabel(k2, 5,"\\n")); gv.addln("\"];");
		 * 
		 * 
		 * } }
		 * 
		 * } } }
		 */

		for (int m = 0; m < chain.length; m++) {

			String ts = "ts" + m + "_";
			for (int i = 0; i < chain[m].length; i++) {
				int k1 = chain[m][i];
				if (k1 > -1) {
					gv.add(ts + String.valueOf(k1));
					gv.add("[label=\"");
					gv.add(models.get(m).getLabel(k1, 5, "\\n"));
					gv.addln("\"];");
				}

			}

		}

		// edges
		for (int m1 = 0; m1 < chain.length; m1++) {

			String ts1 = "ts" + m1 + "_";

			for (int i = 0, k1 = chain[m1][i]; i < chain[m1].length; i++) {

				k1 = chain[m1][i];

				if (k1 == -1)
					continue;

				for (int m2 = 0; m2 < edges[m1].length; m2++) {

					if (edges[m1][m2] == null)
						break;

					int next_m = m1 + m2 + 1;
					String ts2 = "ts" + next_m + "_";

					for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {

						if (edges[m1][m2][k1][k2] > 0) {

							gv.add(ts1 + String.valueOf(k1));
							gv.add("->");
							gv.add(ts2 + String.valueOf(k2));
							gv.add("[label=\"");
							gv.add(commonWords[m1][m2][k1][k2]);
							gv.addln("\"];");
						}
					}

				}
			}
		}

		gv.addln(gv.end_graph());
		File out = new File("/tmp/out." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		String source = gv.getDotSource();
		System.out.println("done");

	}

	private static void printDocumentsCompleteGraph(double[][][][] edges,
			int end, List<Lda> models, int docId) {

		String type = "pdf";
		// init
		int[][] chain = new int[end][200];
		for (int i = 0; i < end; i++)
			for (int j = 0; j < chain[i].length; j++)
				chain[i][j] = -1;

		chain[0][0] = docId;

		for (int m1 = 0; m1 < edges.length; m1++) {

			for (int k1 = chain[m1][0], i = 0; i < chain[m1].length; i++) {
				k1 = chain[m1][i];
				if (k1 == -1)
					continue;

				for (int m2 = 0, j = 0; m2 < edges[m1].length; m2++) {

					int next_m = m1 + m2 + 1;

					if (edges[m1][m2] == null)
						break;

					for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {
						if (edges[m1][m2][k1][k2] > 0) {

							chain[next_m][j] = k2;
							j++;
						}
					}
				}

			}
		}

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("rankdir=LR;");

		StringBuilder strBuilder = new StringBuilder();

		// timeline
		gv.addln("{");
		for (int m = 0; m < end; m++) {

			gv.add("ts");
			gv.add(String.valueOf(m));
			if (m < end - 1)
				gv.add("->");
		}
		gv.addln("}");

		// ranks
		for (int m = 0; m < chain.length; m++) {
			gv.addln("{");
			gv.add("rank=same;");
			gv.add("\"ts");
			gv.add(String.valueOf(m));
			gv.add("\"");
			gv.add(";");
			String ts = "ts" + m + "_";
			for (int i = 0; i < chain[m].length; i++) {
				if (chain[m][i] > -1) {
					gv.add(ts + String.valueOf(chain[m][i]));
					gv.add(";");
				}

			}
			gv.addln("}");
		}

		for (int m = 0; m < chain.length; m++) {

			String ts = "ts" + m + "_";
			for (int i = 0; i < chain[m].length; i++) {
				int k1 = chain[m][i];
				if (k1 > -1) {
					gv.add(ts + String.valueOf(k1));
					gv.add("[label=\"");
					gv.add(models.get(m).getDocumentLabel(k1));
					gv.addln("\"];");
				}

			}

		}

		// edges
		for (int m1 = 0; m1 < chain.length; m1++) {

			String ts1 = "ts" + m1 + "_";

			for (int i = 0, k1 = chain[m1][i]; i < chain[m1].length; i++) {

				k1 = chain[m1][i];

				if (k1 == -1)
					continue;

				for (int m2 = 0; m2 < edges[m1].length; m2++) {

					if (edges[m1][m2] == null)
						break;

					int next_m = m1 + m2 + 1;
					String ts2 = "ts" + next_m + "_";

					for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {

						if (edges[m1][m2][k1][k2] > 0) {

							gv.add(ts1 + String.valueOf(k1));
							gv.add("->");
							gv.add(ts2 + String.valueOf(k2));

							gv.add("[label=\"");
							gv.add(Double.toString(edges[m1][m2][k1][k2]));
							gv.addln("\"];");
						}
					}

				}
			}
		}

		gv.addln(gv.end_graph());
		File out = new File("/tmp/out." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		System.out.println("done");
	}

	private static void printDocumentsGraph2(int docId, int windowLength,
			int end, double[][][][] edges, List<Lda> models) {

		String type = "pdf";

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("rankdir=LR;");

		// timeline
		gv.addln("{");
		for (int m = 0; m < windowLength; m++) {

			gv.add("ts");
			gv.add(String.valueOf(m));
			if (m < windowLength - 1)
				gv.add("->");
		}
		gv.addln("}");

		List<TreeSet<IDSorter>> sortedList = new ArrayList<TreeSet<IDSorter>>();

		for (int m = 0; m < edges[0].length; m++) {
			TreeSet<IDSorter> sorted = new TreeSet<IDSorter>();

			for (int d = 0; d < edges[0][m][docId].length; d++) {

				double sim = edges[0][m][docId][d];
				if (sim > 0) {
					sorted.add(new IDSorter(d, sim));
				}
			}

			sortedList.add(sorted);
		}

		int max = 10;

		for (int i = 0; i < windowLength; i++) {

			TreeSet<IDSorter> sorted = sortedList.get(i);
			int next_m = i + 1;
			gv.addln("{");
			gv.add("rank=same;");
			gv.add("\"ts");
			gv.add(String.valueOf(next_m));
			gv.add("\"");
			gv.add(";");

			String ts = "ts" + next_m + "_";
			Iterator<IDSorter> it = sorted.iterator();
			int count = 0;
			while (it.hasNext() && count < max) {
				IDSorter s = it.next();
				gv.add(ts + String.valueOf(s.getID()));
				gv.add(";");
				count++;

			}
			gv.addln("}");

		}

		gv.add("ts0_" + String.valueOf(docId));
		gv.add("[label=\"");
		gv.add(models.get(0).getDocumentLabel(docId));
		gv.add("\\n");
		gv.add(models.get(0).getDocumentTopics(docId, 0.1, 5, "\\n"));
		gv.addln("\"];");

		for (int i = 0; i < windowLength; i++) {

			TreeSet<IDSorter> sorted = sortedList.get(i);
			int next_m = i + 1;
			String ts = "ts" + next_m + "_";

			Iterator<IDSorter> it = sorted.iterator();
			int count = 0;
			while (it.hasNext() && count < max) {
				IDSorter s = it.next();

				gv.add(ts + String.valueOf(s.getID()));
				gv.add("[label=\"");
				gv.add(models.get(next_m).getDocumentLabel(s.getID()));
				gv.add("\\n");
				gv.add(models.get(next_m).getDocumentTopics(s.getID(), 0.1, 5,
						"\\n"));
				gv.addln("\"];");
				count++;

			}
		}

		String ts1 = "ts0_";
		for (int i = 0; i < windowLength; i++) {

			TreeSet<IDSorter> sorted = sortedList.get(i);
			int next_m = i + 1;
			String ts2 = "ts" + next_m + "_";

			Iterator<IDSorter> it = sorted.iterator();
			int count = 0;
			while (it.hasNext() && count < max) {
				IDSorter s = it.next();

				gv.add(ts1 + String.valueOf(docId));
				gv.add("->");
				gv.add(ts2 + String.valueOf(s.getID()));

				gv.add("[label=\"");
				gv.add(Double.toString(s.getWeight()));
				gv.addln("\"];");
				count++;
			}
		}

		gv.addln(gv.end_graph());
		File out = new File("/tmp/out." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		System.out.println("done");

	}

	private static void printDocumentsGraph(int docId, int windowLength,
			int end, double[][][][] edges, List<Lda> models) {

		String type = "pdf";

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("rankdir=LR;");

		// timeline
		gv.addln("{");
		for (int m = 0; m < windowLength; m++) {

			gv.add("ts");
			gv.add(String.valueOf(m));
			if (m < windowLength - 1)
				gv.add("->");
		}
		gv.addln("}");

		// ranks
		for (int m = 0; m < windowLength; m++) {
			int next_m = m + 1;
			gv.addln("{");
			gv.add("rank=same;");
			gv.add("\"ts");
			gv.add(String.valueOf(next_m));
			gv.add("\"");
			gv.add(";");

			String ts = "ts" + next_m + "_";
			for (int d = 0; d < edges[0][m][docId].length; d++) {
				if (edges[0][m][docId][d] > 0) {

					gv.add(ts + String.valueOf(d));
					gv.add(";");
				}
			}
			gv.addln("}");
		}

		gv.add("ts0_" + String.valueOf(docId));
		gv.add("[label=\"");
		gv.add(models.get(0).getDocumentLabel(docId));
		gv.add("\\n");
		gv.add(models.get(0).getDocumentTopics(docId, 0.1, 5, "\\n"));
		gv.addln("\"];");

		for (int i = 0; i < windowLength; i++) {
			int next_m = i + 1;
			String ts = "ts" + next_m + "_";

			for (int d = 0; d < edges[0][i][docId].length; d++) {
				if (edges[0][i][docId][d] > 0) {

					gv.add(ts + String.valueOf(d));
					gv.add("[label=\"");
					gv.add(models.get(next_m).getDocumentLabel(d));
					gv.add("\\n");
					gv.add(models.get(next_m).getDocumentTopics(d, 0.1, 5,
							"\\n"));
					gv.addln("\"];");
				}
			}
		}

		String ts1 = "ts0_";
		for (int i = 0; i < windowLength; i++) {
			int next_m = i + 1;
			String ts2 = "ts" + next_m + "_";

			for (int d = 0; d < edges[0][i][docId].length; d++) {
				if (edges[0][i][docId][d] > 0) {

					gv.add(ts1 + String.valueOf(docId));
					gv.add("->");
					gv.add(ts2 + String.valueOf(d));

					gv.add("[label=\"");
					gv.add(Double.toString(edges[0][i][docId][d]));
					gv.addln("\"];");
				}
			}
		}

		gv.addln(gv.end_graph());
		File out = new File("/tmp/out." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		System.out.println("done");
	}

	public static void computeDocumentChain2() throws Exception {
		String rootDir = "/var/lusinda/solr/rss/new/sme_sk/lda/";

		// int start = 1;

		int windowLength = 3;
		List<Lda> models = new ArrayList<Lda>();

		int start = 0;
		int end = 1;

		Lda lda = null;

		double[][][][] edges = new double[end][windowLength][][];

		for (int window = 1; window <= windowLength + 1; window++) {
			lda = (Lda) Lda.read(new File(rootDir + window + "/model.mallet"));
			models.add(lda);
		}

		models.get(0).findSimilarDocuments(models.get(1), 0, 1, edges, 2);
	}

	public static void computeDocumentChain() throws Exception {
		
FeedSettings settings = FeedSettings.getSmeExperimentFeedSettings(7);
		
		
		String rootDir = settings.outputDirectory+"/lda/";

		// int start = 1;

		int windowLength = 3;
		List<Lda> models = new ArrayList<Lda>();

		int start = 0;
		int end = 1;

		Lda lda = null;

		double[][][][] edges = new double[end][windowLength][][];

		for (int window = 1; window <= windowLength + 1; window++) {
			lda = (Lda) Lda.read(new File(rootDir + window + "/model.mallet"));
			models.add(lda);
			lda.calculatePhi();
			lda.calculateDiagnostics();
		}

		for (int index = start; index < end; index++) {
			int next = index + windowLength + 1;
			if (next >= models.size() && next < end) {
				lda = (Lda) Lda
						.read(new File(rootDir + next + "/model.mallet"));
				models.add(lda);
			}
			lda = models.get(index);
			// lda.findBigrams2();
			for (int window = 1; window <= windowLength; window++) {
				int inx = window - 1;
				int current = index + window;
				if (current >= models.size())
					break;
				Lda currentLda = models.get(current);

				edges[index][inx] = new double[lda.getNumDocuments()][currentLda
						.getNumDocuments()];

				// currentLda.findBigrams2();
				lda.findSimilarDocuments2(currentLda, index, inx, edges, end);
				System.out.println("finding similar documents: " + index + "->"
						+ current);
			}

		}

		// printDocumentsCompleteGraph(edges,end,models,100);
		printDocumentsGraph2(10, windowLength, end, edges, models);

	}

	public static void heldOut() throws IOException {
		String rootDir = "/var/lusinda/solr/rss/new/sme_sk/lda/";

		String path = "/var/lusinda/solr/mallet/new";

		LdaProperties properties = LdaModel.getDefaultProperties(rootDir,null);

		InstanceList train = InstanceList.load(new File(
				"/var/lusinda/solr/rss/new/sme_sk/lda/train.instances"));
		InstanceList test = InstanceList.load(new File(
				"/var/lusinda/solr/rss/new/sme_sk/lda/test.instances"));

		properties.topics = 40;

		LdaModel lda = new LdaModel();
		
		lda.init(properties.topics, train, null, rootDir);

		lda.run(properties);

		lda.getLda().inferNewDocs(test);
	}

	/*
	 * stack.get(0).push(1);
	 * 
	 * for (int m1 = 0; m1 < edges.length; m1++) { gv.addln("{");
	 * gv.add("rank=same;"); gv.add("\"ts"); gv.add(String.valueOf(m1));
	 * gv.add("\""); gv.add(";"); String ts = "ts" + m1 + "_"; while
	 * (!stack.get(m1).isEmpty()) { int k1 = stack.get(m1).pop(); for (int m2 =
	 * 0; m2 < edges[m1].length; m2++) {
	 * 
	 * 
	 * for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) { if
	 * (edges[m1][m2][k1][k2] > 0) { int inx = m1+m2+1;
	 * if(!stack.get(inx).contains(k2)) stack.get(inx).push(k2); gv.add(ts +
	 * String.valueOf(k1)); gv.add(";"); } }
	 * 
	 * } } // gv.addln("node [shape=box,style=filled,color=\"red\"];");
	 * 
	 * // TreeSet<IDSorter> sortedWords = models.get(i).getSortedWords(j);
	 * 
	 * // } }
	 */

	//
	// for (int m1 = 0; m1 < edges.length; m1++) {
	// for (int m2 = 0; m2 < edges[m1].length; m2++) {
	// if (edges[m1][m2] == null)
	// continue;
	// int second = m1 + m2 + 1;
	// String ts1 = "ts" + m1 + "_";
	// String ts2 = "ts" + second + "_";
	//
	// for (int k1 = 0; k1 < edges[m1][m2].length; k1++) {
	// for (int k2 = 0; k2 < edges[m1][m2][k1].length; k2++) {
	// if (edges[m1][m2][k1][k2] > 0) {
	// gv.add(ts1 + String.valueOf(k1));
	// gv.add("->");
	// gv.add(ts2 + String.valueOf(k2));
	// gv.addln(";");
	// }
	// }
	// }
	// }
	// }

	/*
	 * public void computeSimilarTopicWord() { String rootDir =
	 * "/var/lusinda/solr/rss/new/sme_sk/lda/";
	 * 
	 * String type = "pdf"; // int start = 1;
	 * 
	 * int windowLength = 3; //int current = 5; // Lda currentLda = (Lda)
	 * Lda.read(new File(rootDir+current+"/model.mallet")); List<Lda> models =
	 * new ArrayList<Lda>();
	 * 
	 * int start=0; int end=10;
	 * 
	 * Map<String,double[][][]> commonWords = new
	 * HashMap<String,double[][][]>();
	 * 
	 * for(int window=1;window<=windowLength+1;window++) { Lda lda = (Lda)
	 * Lda.read(new File(rootDir+window+"/model.mallet")); models.add(lda); }
	 * 
	 * for(int index=start;index<end;index++) { int next = index
	 * +windowLength+1; if(next>=models.size() && next<end) { Lda lda = (Lda)
	 * Lda.read(new File(rootDir+next+"/model.mallet")); models.add(lda); } Lda
	 * lda = models.get(index);
	 * 
	 * 
	 * for(int window=1;window<=windowLength;window++) { int current =
	 * index+window; if(current>=models.size()) break; Lda currentLda =
	 * models.get(current);
	 * 
	 * lda.findSimilarTopics(currentLda, current,commonWords,end);
	 * System.out.println("finding similar topics: "+index+"->"+current); }
	 * 
	 * }
	 * 
	 * List<String> topWords = new ArrayList<String>(); int topCount = 30;
	 * 
	 * for(Entry<String,double[][][]> cw:commonWords.entrySet()) { int counts=0;
	 * for(int m=0;m<end;m++) {
	 * 
	 * for(int i=0;i<cw.getValue()[m].length;i++) { for(int
	 * j=0;j<cw.getValue()[m][i].length;j++) { if(cw.getValue()[m][i][j]>0) {
	 * counts++; } } } }
	 * 
	 * if(counts>topCount) topWords.add(cw.getKey()); }
	 * 
	 * 
	 * 
	 * for(String key:topWords) { double[][][] value = commonWords.get(key);
	 * 
	 * GraphViz gv = new GraphViz(); gv.addln(gv.start_graph());
	 * gv.addln("rankdir=LR;");
	 * 
	 * gv.addln("{"); for(int m=0;m<end;m++) { gv.add("ts");
	 * gv.add(String.valueOf(m)); if(m<end-1) gv.add("->"); } gv.addln("}");
	 * 
	 * for(int m=0;m<end;m++) { gv.addln("{"); gv.add("rank=same;");
	 * gv.add("ts"); gv.add(String.valueOf(m)); gv.add(";");
	 * 
	 * //gv.addln("[shape=box,style=filled,color=\"green\"];");
	 * 
	 * for(int i=0;i<value[m].length;i++) { //if(value[i][j]>0) // {
	 * gv.add(String.valueOf(i)); gv.add(";");
	 * 
	 * // gv.addln("node [shape=box,style=filled,color=\"red\"];");
	 * 
	 * //TreeSet<IDSorter> sortedWords = models.get(i).getSortedWords(j);
	 * 
	 * //} } gv.addln("}");
	 * 
	 * }
	 * 
	 * for(int m=0;m<end;m++) { for(int i=0;i<end;i++) { for(int j=0;j<end;j++)
	 * { if(value[m][i][j]>0) { gv.add(String.valueOf(i)); gv.add("->");
	 * gv.add(String.valueOf(j)); gv.addln(";"); } } } }
	 * 
	 * gv.addln(gv.end_graph()); File out = new File("/tmp/out."+type);
	 * gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	 * 
	 * 
	 * } }
	 */

	public static void getRecomendation() throws Exception {

	//	Sorter<Article> a = new Sorter<Article>(null, 0);
	//	Type t = a.getClass().getTypeParameters()[0];
		FeedSettings settings = 		FeedSettings.getSmeExperimentFeedSettings(7);
		String rootDir = settings.outputDirectory+"/lda/";
		

		// int start = 1;

		List<Lda> models = new ArrayList<Lda>();

		int start = 0;
		int end = 2;
		int maxItems = 10;
		double treshold = 0.8;
		//String fileName="eval.txt";
		String fileName="eval2.txt";
		
		OutputStream os = (OutputStream) new FileOutputStream(new File(
				rootDir + fileName));
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);
		PrintWriter out = new PrintWriter(System.out);//new PrintWriter(osw);
		PrintWriter fout = new PrintWriter(osw);
		

	
	
		
		
		
		File dir = new File(rootDir);
		
		
		File[] dirs = dir.listFiles(new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		});
		
		
		Lda base = (Lda) Lda.read(new File(dirs[0],"model.mallet"));
		int maxModelsRead = 2;//dirs.length
		for(int i=1;i<maxModelsRead;i++)
		{
			
			File model = new File(dirs[i],"model.mallet");
			Lda lda = (Lda) Lda.read(model);
			lda.calculatePhi();
			lda.calculateDiagnostics();
		
			lda.findBigrams3();
			models.add(lda);
		}
		
		
		
		
		List<TreeSet<Sorter<Article>>> all = new ArrayList<TreeSet<Sorter<Article>>>();
		
		for (int doc = 0; doc < base.getNumDocuments(); doc++) {

			TreeSet<Sorter<Article>> recomendations = new TreeSet<Sorter<Article>>();
			TreeSet<Sorter<Article>> recomendationsExperimental = new TreeSet<Sorter<Article>>();
			TreeSet<Sorter<NGram>> allTopWords = new TreeSet<Sorter<NGram>>();
			String docName = (String)base.data.get(doc).instance.getName();

			Article newArticle = new Article(doc,null,docName,base.getDocuments().get(doc),0,null);
//			newArticle.id = doc;
//			newArticle.title = (String)base.getData().get(doc).instance.getName();
//			
//			newArticle.content = base.getDocuments().get(doc);
			
			all.add(recomendations);
						
			
			
			
			for (int i = 0; i < models.size(); i++) {
				
			
//models.get(i).printTopWords(System.out, 10, false);
				

				newArticle.dist = models.get(i).inferDist(base.getDocuments().get(doc));
				
				TreeSet<Sorter<NGram>> docWords = models.get(i).getDocumentWordsLikelihood(newArticle);
			//	printTopRecomendedWords((String)base.data.get(doc).instance.getName(),docWords,30,0);
				
				allTopWords.addAll(docWords);
			//	List<NGram> query = Lda.getListOfNgrams(docWords, 10);
			//	TreeSet<Sorter<Article>> partialRecomendations = models.get(i).getWordsDocumentsLikelihood(query, i, 20, false);
				
				TreeSet<Sorter<Article>> partialRecomendations =  models.get(i).getSimilarDocuments(base
						.getDocuments().get(doc),
						(String) base.data.get(doc).instance.getName(),i,
						maxItems,false);
			
				 printTopRecomendations((String)base.data.get(doc).instance.getName(),partialRecomendations,20,treshold,out,rootDir,models);
			
				 TreeSet<Sorter<Article>> partialRecomendationsExperimental =  models.get(i).getSimilarDocuments(base
						.getDocuments().get(doc),
						(String) base.data.get(doc).instance.getName(),i,
						maxItems,true);
				 System.out.println("filtered");
				 printTopRecomendations((String)base.data.get(doc).instance.getName(),partialRecomendationsExperimental,20,treshold,out,rootDir,models);
			
				recomendations.addAll(partialRecomendations);
			//	recomendationsExperimental.addAll(partialRecomendationsExperimental);
				

			}
			
			int maxDocuments = 30;
		
		
		
			
			
			
			hierarchicalClustering(recomendations,models);
			
			
			TreeSet<Sorter<DocumentCluster>>  clusters= clusterDocuments(recomendations, 30, treshold, out, rootDir, models);
		//	printClusters(clusters,10);
			
		//	TreeSet<Sorter<Article>> sortedReccomendations =	sortDocumentsByQuery(recomendations, 100, treshold, out, rootDir, models, allTopWords);
		//	Query q = new Query(allTopWords);
		//	q.flush(30);
		//	List<Cluster> results = clusterResults(newArticle, recomendations,30,0,models,q);
		//	printClusteredResults(docName, results, 30, 0);
		//	printTopRecomendedWords((String)base.data.get(doc).instance.getName(),allTopWords,10,0);
			
		//	printTopRecomendations((String)base.data.get(doc).instance.getName(),sortedReccomendations,20,treshold,out,rootDir,models);
			
		//	printTopRecomendations((String)base.data.get(doc).instance.getName(),recomendations,20,treshold,out,rootDir,models);
		
			
			System.out.println();
		
	//	 HierarchicalLDA hlda = new HierarchicalLDA();
		 
		 //hlda.initialize(list, null, 3, new Randoms());
		//	hlda.estimate(5000);
	//	 reccomendationLda.getLda().printDocumentTopics(new PrintWriter(rootDir+"/reccomend/docs.txt"));
		 
			//	 System.out.println("original");
		// printTopRecomendations((String)base.data.get(doc).instance.getName(),recomendations,20,treshold,out,rootDir,models);
		// System.out.println("experimental");
		// printTopRecomendations((String)base.data.get(doc).instance.getName(),recomendationsExperimental,20,treshold,out,rootDir,models);
		 
		
		}
		for (int doc = 0; doc < base.getNumDocuments(); doc++) {
		TreeSet<Sorter<Article>> recomendations = all.get(doc);
		
			printTopRecomendations((String)base.data.get(doc).instance.getName(),recomendations,maxItems,treshold,out,rootDir,models);
			
		}
		
		System.out.println("done");
		

	}
	
	public static void hierarchicalClustering(TreeSet<Sorter<Article>> recomendations,List<Lda> models) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException
	{
		int i=0;
		int maxDocuments = 30;
		int maxWords = 40;
		Iterator<Sorter<Article>> it = recomendations.iterator();
		
		while(it.hasNext() && i<maxDocuments)
		{
			
			
			Sorter<Article> sd = it.next();
			
			sd.data.docWords = models.get(sd.modelId).getDocumentWordsLikelihood(sd.data);
			//printTopRecomendedWords(sd.data.title, sd.data.docWords, maxWords,0);
			
			i++;
		}
		
		ResultsDocuments documents = new ResultsDocuments(recomendations,maxDocuments,maxWords);
		ResultsClustering clustering = new ResultsClustering(documents);
		List<DocumentCluster> clusters = clustering.cluster(0.01);
		printClusters(clusters);
		
		//Hac hac = new Hac();
		//hac.runHac(documents);
		//hac.runHac(documents, new JackardSimilarity(), new CompleteLinkage());
		
	}
	
	
	
	
	
	public static void printClusters(List<DocumentCluster>  clusters)
	{
		for(DocumentCluster c:clusters)
		{
			c.flush();
			System.out.println(c.getLabel());
			System.out.println("\t");
			printSortedDocuments(c.getDocs(), 10,0);
			System.out.println();
		}
	}
	
	public static void printClusters(TreeSet<Sorter<DocumentCluster>>  clusters,int top)
	{
		int i=0;
		Iterator<Sorter<DocumentCluster>> it = clusters.iterator();
		
		
		while(it.hasNext() && i<top)
		{
			Sorter<DocumentCluster> sd = it.next();
			//if(sd.value<treshold)
				//break;
			sd.data.flush();
			System.out.print("\t");
			System.out.print(sd.data.getLabel());
			System.out.print(" :");
			System.out.print(sd.weight);
			System.out.println();
			printSortedDocuments(sd.data.getDocs(), 10,0);
			i++;
		}
		
		
	}
	
	
	public static void printTopRecomendedWords(String docName,TreeSet<Sorter<NGram>> words,int top,double treshold)
	{
		int i=0;
		Iterator<Sorter<NGram>> it = words.iterator();
		
		System.out.println(docName);
		
		while(it.hasNext() && i<top)
		{
			Sorter<NGram> sd = it.next();
			//if(sd.value<treshold)
				//break;
			System.out.print("\t");
			System.out.print(sd.data.getNiceNGram());
			System.out.print(" :");
			System.out.print(sd.weight);
			System.out.println();
			i++;
		}
	}
	
	
	public static TreeSet<Sorter<NGram>> sortWordsCounts(Map<String,NGram> wordsCounts)
	{
		TreeSet<Sorter<NGram>> sorted=  new TreeSet<Sorter<NGram>>();
		for(Entry<String,NGram> wc:wordsCounts.entrySet())
		{
			sorted.add(new Sorter<NGram>(-1,wc.getValue(),wc.getValue().getWeight()));
		}
		
		return sorted;
	}
	
	public static void processWordsCounts(TreeSet<Sorter<NGram>> words,Map<String,NGram> wordsCounts )
	{
		int i=0;
		Iterator<Sorter<NGram>> it = words.iterator();
				
		while(it.hasNext())
		{
			Sorter<NGram> sd = it.next();

			NGram n = wordsCounts.get(sd.data.getKey());
			if(n==null)
			{
				n=sd.data;
				n.setWeight(sd.weight);
			}
			else
				n.setWeight(n.getWeight());//+sd.weight);
			
			wordsCounts.put(sd.data.getKey(), n);
			
			
			i++;
		}	
	}
	
	public static void processClusters(Sorter<Article> a,TreeSet<Sorter<NGram>> words,Map<String,DocumentCluster> clusters )
	{
		int i=0;
		Iterator<Sorter<NGram>> it = words.iterator();
				
		while(it.hasNext())
		{
			Sorter<NGram> sd = it.next();

			DocumentCluster c = clusters.get(sd.data.getKey());
			if(c==null)
				c=new DocumentCluster();
	
			c.addDoc(a);
			c.addLabel(sd.data);
			
			
			
			clusters.put(sd.data.getKey(), c);
			
			
			i++;
		}	
	}
	
	public static TreeSet<Sorter<DocumentCluster>>  clusterDocuments(TreeSet<Sorter<Article>> recomendations,int top,double treshold,PrintWriter out,String rootDir,List<Lda> models) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException
	{
		Map<String,DocumentCluster> clusters = new  HashMap<String, DocumentCluster>();
		
		
		
		
		int i=0;
		Iterator<Sorter<Article>> it = recomendations.iterator();
		
		while(it.hasNext() && i<top)
		{
			Map<String,NGram> wordsCounts = new HashMap<String,NGram>();
			
			Sorter<Article> sd = it.next();
			
			TreeSet<Sorter<NGram>> docWords = models.get(sd.modelId).getDocumentWordsLikelihood(sd.data);
			processClusters(sd,docWords,clusters);
	//		printTopRecomendedWords(sd.data.title, docWords,30, 0);
	//		processWordsCounts(docWords, wordsCounts);
	//		TreeSet<Sorter<NGram>> sorted=  sortWordsCounts(wordsCounts);
//			printTopRecomendedWords(sd.data.title, sorted,30, 0);
	//		processClusters(sd,sorted,clusters);
			
		//	printTopRecomendedWords(sd.data.title, sorted,10, 0);
			i++;
		
		}
		
		TreeSet<Sorter<DocumentCluster>> sortedClusters = new TreeSet<Sorter<DocumentCluster>>();
		
		for(DocumentCluster c:clusters.values())
			sortedClusters.add(new Sorter<DocumentCluster>(-1,c,c.getWeight()));
		
		
		printClusters(sortedClusters,10);
		
		return sortedClusters;
	}
	
	public static TreeSet<Sorter<Article>> sortDocumentsByQuery(TreeSet<Sorter<Article>> recomendations,int top,double treshold,PrintWriter out,String rootDir,List<Lda> models,TreeSet<Sorter<NGram>> query)
	{	
		int i=0;
		Iterator<Sorter<Article>> it = recomendations.iterator();
		
		TreeSet<Sorter<Article>> sorted = new TreeSet<Sorter<Article>>();
		
		
		while(it.hasNext() && i<top)
		{
			Sorter<Article> sd = it.next();
			sd.weight  =models.get(sd.modelId).getDocumentWordsLikelihoodScore(sd.data, query,10);
			sorted.add(sd);
			
			i++;

		}
		
		return sorted;
	}
	
	public static void printSortedDocuments(TreeSet<Sorter<Article>> recomendations,int top,double treshold)
	{
		int i=0;
		Iterator<Sorter<Article>> it = recomendations.iterator();
		
		
		while(it.hasNext() && i<top)
		{
			Sorter<Article> sd = it.next();
			//if(sd.value<treshold)
				//break;
			System.out.print("\t");
			System.out.print(sd.data.title);
			System.out.print(" :");
			System.out.print(sd.weight);
			
		//	String docTopics = models.get(sd.data.modelId).getDocumentTopics(sd.data.dist, 0.1, 4, "\n");
		//	System.out.println(docTopics);
		//	TreeSet<StringDoublePair> topSimilarWords = models.get(sd.data.modelId).getTopSimilarWords(sd.data);
			//	printTopRecomendedWords("",topSimilarWords,10,0);
			System.out.println();
			i++;
		//	list.addThruPipe(new Instance(models.get(sd.modelId).getDocuments().get(sd.data.id),null,sd.data.title,null));
		}
	}
	
	public static void printClusteredResults(String docName,List<Cluster> clusters,int top,double treshold) throws URISyntaxException
	{
		System.out.println(docName);
		for(Cluster c: clusters)
		{
			String label = c.getLabel().contains(" ")? c.getLabel():Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(StringUtils.lowerCase(c.getLabel()));
			System.out.println(label);
			
			for(org.carrot2.core.Document doc:c.getDocuments())
			{
				System.out.print("\t");
				System.out.println(doc.getTitle());
			}
		}
	}
	
	
	
	public static List<Cluster> clusterResults(Article a,TreeSet<Sorter<Article>> recomendations,int top,double treshold,List<Lda> models,Query query) throws URISyntaxException, FileNotFoundException, UnsupportedEncodingException
	{
		final ArrayList<org.carrot2.core.Document> documents = new ArrayList<org.carrot2.core.Document>();
		int i=0;
		Iterator<Sorter<Article>> it = recomendations.iterator();
		int queryLimit = 10;
		query.flush(queryLimit);
	//	System.out.println(query.getQueryString());
		
		
		TreeSet<Sorter<NGram>> totalWords = new TreeSet<Sorter<NGram>>();
		
		while(it.hasNext() && i<top)
		{
			Sorter<Article> sd = it.next();
		//	if(totalWords.isEmpty())
			//tu pridam vsetky
				totalWords.addAll(models.get(sd.modelId).getDocumentWordsLikelihood(sd.data));
//			else
//			{
//			//Collection col = CollectionUtils.intersection(totalWords, totalWords);
//				//		urobit intersection
//				totalWords.retainAll(models.get(sd.modelId).getDocumentWordsLikelihood(sd.data));
//			}
			i++;
			
		}
		
		System.out.println(query.getQueryString());
		Query commonQuery = new Query(totalWords);
		commonQuery.flush(queryLimit);
		System.out.println(commonQuery.getQueryString());
		
//		totalWords.retainAll(query.getQuery());
//		 commonQuery = new Query(totalWords);
//			commonQuery.flush(queryLimit);
//			System.out.println(commonQuery.getQueryString());
		
			it = recomendations.iterator();
			i=0;
		while(it.hasNext() && i<top)
		{
			Sorter<Article> sd = it.next();
			
			TreeSet<Sorter<NGram>> docWords = models.get(sd.modelId).getDocumentWordsLikelihood(sd.data);
			Query localQuery = new Query(docWords);
			
			localQuery.flush(queryLimit);
			System.out.println("\t");
			System.out.println(localQuery.getQueryString());
			String doc = getSummary(sd.data.content,query,queryLimit,10,true);
			System.out.println(doc);
			
//			try {
//				doc = StandardTextProcessing.translate(doc);
//			} catch (Exception e) {}
		//	System.out.println(doc);
			documents.add(new org.carrot2.core.Document(sd.data.getNiceTitle(),
								doc,
								LanguageCode.SLOVAK));
			
			
		
	//	documents.add(new org.carrot2.core.Document(sd.data.getNiceTitle(),
	//			getSummary(sd.data.content,query,20,10),
	//			LanguageCode.SLOVAK));
		
		i++;
		}
		 
		/* A controller to manage the processing pipeline. */
		final Controller controller = ControllerFactory.createSimple();
		 
		/*
		* Perform clustering by topic using the Lingo algorithm. Lingo can
		* take advantage of the original query, so we provide it along with the documents.
		*/
		
		String q  =  commonQuery.getQueryString();//query.getQueryString();
//		try {
//			q = StandardTextProcessing.translate(q);
//		} catch (Exception e) {}
		
		 Map<String, Object> attributes = new HashMap<String, Object>();
		 //BasicPreprocessingPipelineDescriptor.Keys.
		 CommonAttributesDescriptor.attributeBuilder(attributes).query(q);
		 CommonAttributesDescriptor.attributeBuilder(attributes).documents(documents);
		 	 BasicPreprocessingPipelineDescriptor.attributeBuilder(attributes).stemmerFactory(StemmerFactory.class);
		 	 File resourcesDir = new File("/Users/teo/Documents/workspace-new/news-recommender/src/main/resources/carrot");
		 	File resourcesDir2= new File(LdaModel.class.getClassLoader().getResource("carrot").getFile());
		 	
		 	DefaultLexicalDataFactoryDescriptor.attributeBuilder(attributes)
            .mergeResources(true);
		        ResourceLookup resourceLookup = new ResourceLookup(new DirLocator(resourcesDir));
		        LexicalDataLoaderDescriptor.attributeBuilder(attributes)
	            .resourceLookup(resourceLookup);
		        DefaultLexicalDataFactoryDescriptor.attributeBuilder(attributes).resourceLookup(resourceLookup);
		        
		       LingoClusteringAlgorithmDescriptor.attributeBuilder(attributes).query(q);
		 	controller.init(attributes);
		ProcessingResult byTopicClusters = controller.process(attributes,LingoClusteringAlgorithm.class);
		List<Cluster> clustersByTopic = byTopicClusters.getClusters();
		
		printClusteredResults(q, clustersByTopic, 30, 0);
		
	//	System.out.println(query.getQueryString());
		
		System.out.println(a.getNiceTitle());
		
		
		
		return clustersByTopic;
	}
	
	//private static Pattern p = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)");
	private static Pattern p = Pattern.compile("(\\S.+?[.!?])(?=\\s+|$)");
	
	private static boolean findInLemmatizedSentence(String sentence,String delimeter,String wordToBreak)
    {
		
    
		try {
			String lemma = Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(wordToBreak);
		
    	String[] words = sentence.split(delimeter);
    	for(int i=0;i<words.length;i++)
    	{
    		if(lemma.equals(Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(words[i])))
    			return true;
    	}
		} catch (Exception e) {
			//ingonre
		}
		finally
		{}
		
		return false;
    }
	
	private static boolean findInTranslatedSentence(String sentence,String delimeter,String wordToBreak)
    {
		
    
		try {
			
		
    	String[] words = sentence.split(delimeter);
    	for(int i=0;i<words.length;i++)
    	{
    		if(wordToBreak.equals(words[i]))
    			return true;
    	}
		} catch (Exception e) {
			//ingonre
		}
		finally
		{}
		
		return false;
    }
	
	public static void getSummaries(TreeSet<Sorter<Article>> articles,Query query,int queryLimit,int sentenceLimit,boolean summarize)
	{
		int i=0;
		Iterator<Sorter<Article>> it = articles.iterator();
		
		
		while(it.hasNext())
		{
			Sorter<Article> sd = it.next();
			sd.data.summarizedText = getSummary(sd.data.content,query,queryLimit,sentenceLimit,summarize);
			
		}
	}
	
	public static String getSummary(String doc,String query,int queryLimit,int sentenceLimit)
	{
		
		
		return null;
//		StringBuilder summary = new StringBuilder();
//		try
//		{
//		int i=0;
//		//Iterator<Sorter<NGram>> it = query.split(regex)
//		
//		
//		while(it.hasNext() && i<queryLimit)
//		{
//			Sorter<NGram> sd = it.next();
//			
//			//TODO something better than this
//		//Pattern.compile("\\w*"+sd.data.getNiceNGram()+"*\\w*[.?!](?=\\s)");
//		String[] result = p.split(doc);
//		 Matcher reMatcher = p.matcher(doc);
//	        while (reMatcher.find()) {
//	        	String g =reMatcher.group(); 
//	        	
//	           if(findInLemmatizedSentence(g," ",sd.data.getLemmatizedNGram()) && !summary.toString().contains(g))
//	        	   summary.append(g).append(" ");
//	        } 
//	        
//		i++;
//		}
//		}
//		finally{}
//		
//		return summary.toString();

	}
	
	public static String getSummary2(String doc,Query query,int queryLimit,int sentenceLimit,boolean summarize)
	{
		if(!summarize)
			return doc;
		else
		{
		
		StringBuilder summary = new StringBuilder();
		try
		{
		int i=0;
		Iterator<Sorter<NGram>> it = query.getQuery().iterator();
		
		
		
		while(it.hasNext() && i<queryLimit)
		{
			Sorter<NGram> sd = it.next();
			
			//TODO something better than this
		//Pattern.compile("\\w*"+sd.data.getNiceNGram()+"*\\w*[.?!](?=\\s)");
		String[] result = p.split(doc);
		 Matcher reMatcher = p.matcher(doc);
	        while (reMatcher.find()) {
	        	String g =reMatcher.group(); 
	        	
	           if(findInLemmatizedSentence(g," ",sd.data.getLemmatizedNGram()) && !summary.toString().contains(g))
	        	   summary.append(g).append(" ");
	        } 
	        
		i++;
		}
		}
		finally{}
		
		return summary.toString();
		}

		
	}
	
	public static String getSummary(String doc,Query query,int queryLimit,int sentenceLimit,boolean summarize)
	{
		if(!summarize)
			return doc;
		else
		{
		
		StringBuilder summary = new StringBuilder();
		try
		{
		
		Iterator<Sorter<NGram>> it = query.getQuery().iterator();
		//String[] result = p.split(doc);
		List<String> sentences = new ArrayList<String>();
		try
		{
		Matcher reMatcher = p.matcher(doc);
		
		while (reMatcher.find()) {
        	sentences.add(reMatcher.group());
		}
		}
		catch(Exception ex)
		{
			
		}
		
		int qi=0;
		int si=0;
		while(it.hasNext())
		{
			if(si>sentenceLimit)
				break;
			
			Sorter<NGram> sd = it.next();
			
		
		        for(String sentence:sentences) {
		        	 
		        	
		           if(findInLemmatizedSentence(sentence," ",sd.data.getLemmatizedNGram()) && !summary.toString().contains(sentence))
		           {
		        	   summary.append(sentence).append(" ");
		        	   si++;
		           }
		        } 
		        
		        qi++;
		}
		
		
		
			
			//TODO something better than this
		//Pattern.compile("\\w*"+sd.data.getNiceNGram()+"*\\w*[.?!](?=\\s)");
		
	        
		
		}
		finally{}
		return summary.toString();
		}

		
	}
	
	
	public static void printTopRecomendations(String docName,TreeSet<Sorter<Article>> recomendations,int top,double treshold,PrintWriter out,String rootDir,List<Lda> models) throws IOException, URISyntaxException
	{	
		
		
		InstanceList list = createInstanceList(0);
	//	 LdaProperties properties = LdaModel.getDefaultProperties(rootDir);
	//	 properties.topics = 10;
	//	 properties.maxTopics = properties.topics;
			
		
		
		System.out.println(docName);
		printSortedDocuments(recomendations, top, treshold);
		
//		 LdaModel reccomendationLda = new LdaModel(properties.topics, list, null, rootDir+"/reccomend");
//		 
//		 reccomendationLda.run(properties);
//		 reccomendationLda.getLda().calculatePhi();
//		 reccomendationLda.getLda().buildDocumentsTopic();
//		 reccomendationLda.getLda().findBigrams3();
//		 for(int k=0;k<reccomendationLda.getLda().numTopics;k++)
//		 {
//			 String phrasesLabel = reccomendationLda.getLda().getPhrasesLabel(k, 10, " ");
//			 System.out.println(phrasesLabel);
//			 //System.out.print("\t");
//			 String docsLabel = reccomendationLda.getLda().getTopDocumentsString(k,3,"\t\n");//.getPhrasesLabel(k, 10, " ");
//			 System.out.println(docsLabel);
//		 }
//		 System.out.println();
	}
	
	public static void getKeywordsFromCalais()
	{
		
	}

	public static void findNGrams() throws Exception
	{
		FeedSettings settings = FeedSettings.getSmeExperimentFeedSettings(7);
		
		
		String rootDir = settings.outputDirectory+"/lda/";
		int end = 5;
		for (int i = 1; i <= end ; i++) {
			Lda lda = (Lda) Lda.read(new File(rootDir + i + "/model.mallet"));
			System.out.println("numWords: "+lda.getAlphabet().size());
		
		lda.calculateDiagnostics();
		lda.calculatePhi();
		lda.findBigrams3();
		List<Phrases> phrasesList = lda.getPhrases();
		
		for(int k=0;k<lda.numTopics;k++)
		{
		List<NGram> sortedPhrases = phrasesList.get(k).getSortedNGrams(
				100, 0,2);

		for (NGram ng : sortedPhrases) {
			
			 System.out.print(ng.getNiceNGram());
			 System.out.println(" ");
 


		}
		
		}
		}
		
		System.out.println("done");
		
		
		
	}
	
 	public static void main(String[] args) throws Exception {

		getRecomendation();

		// heldOut();
		//
 		
 	//	findNGrams();
 		
 	//	wordLemaClustering();
 		
 	//	computeDocumentChain();
	//	 computeTopicChain(33);
		// computeDocumentChain();
	}

	// LdaModel lda = new LdaModel(50, "/var/lusinda/solr/mallet/9/train.lda",
	//
	// "/var/lusinda/solr/mallet/9/likelihood/");
	// // lda.run(steps, seed, averageNumRuns, evaluate)
	//
	// LdaProperties properties = new LdaProperties();
	// properties.averageNumRuns = 1;
	// properties.evaluate = false;
	// //properties.exportFilePath =
	// "/var/lusinda/solr/mallet/9/topics-in-documents.csv";
	// properties.seed = 5;
	// properties.steps = 1;
	//
	// lda.run(properties);

}

/*
 * 
 * public static void main(String[] args) throws Exception {
 * 
 * String inputFile = "/var/lusinda/solr/mallet/test.model";
 * 
 * double ltr = 0; double best_likelihood = 0;
 * 
 * boolean generateCloud = true; boolean estimateLikelihood = false;
 * 
 * File f = new File("out/topic-ll.csv");
 * 
 * OutputStream os = (OutputStream) new FileOutputStream(f); String encoding =
 * "UTF8"; OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
 * BufferedWriter bw = new BufferedWriter(osw);
 * 
 * bw.write("topic number"); bw.write(","); bw.write("LogLikehood");
 * bw.write(","); bw.write("left-to-right"); bw.newLine();
 * 
 * for (; numTopics <= maxTopics; numTopics += step) { InstanceList instances =
 * InstanceList.load(new File(inputFile));
 * 
 * alpha = 50.00 / numTopics;
 * 
 * // System.out.println("Training Data loaded."); int average=0; double
 * averageLikelihood=0.0;
 * 
 * for (int j = 0; j < averageRun; j++) {
 * 
 * lda = new ParallelTopicModel(numTopics, alpha, beta);
 * lda.setNumIterations(1000); lda.addInstances(instances);
 * 
 * lda.estimate(); likelihood = lda.modelLogLikelihood();
 * 
 * average+=likelihood; }
 * 
 * 
 * 
 * /*
 * 
 * averageLikelihood = (double) average / (double) averageRun;
 * 
 * bw.write(Integer.toString(numTopics)); bw.write(",");
 * bw.write(Double.toString(averageLikelihood)); bw.write(",");
 * bw.write(Double.toString(ltr)); bw.newLine();
 * 
 * // lda.printDocumentTopics(new File("out/document-topics")); //
 * MarginalProbEstimator evaluator = lda.getProbEstimator();
 * 
 * if (!estimateLikelihood) { List<Topic> topics = Lda.getDocumentTopics(lda, 0,
 * 10); List<DocumentProbability> docs = Lda.getDocuments(lda);
 * 
 * SmeDataSource sme = new SmeDataSource(); sme.connect();
 * 
 * Lda.fillDocumentsAttributes(docs, -1, sme);
 * 
 * Lda.fillDocumentsAttributesInTopic(topics, lda, sme, 10);
 * 
 * sme.disconnect();
 * 
 * if (generateCloud) { Cloud cloud = null;
 * 
 * f = new File("out/clouds"); if (!f.exists()) f.mkdirs();
 * 
 * int i = 0; for (Topic topic : topics) {
 * 
 * cloud = new Cloud(); cloud.setMaxTagsToDisplay(10);
 * 
 * cloud.setMaxWeight(10.0);
 * 
 * for (DocumentProbability docprob : topic .getDocumentProbabilities())
 * cloud.addTag(new Tag( docprob.getDocAttributes().title, docprob.prob * 10));
 * 
 * FileOutputStream fos = new FileOutputStream(
 * "/Users/teo/Documents/workspace/TopicModelling/out/clouds/cloud" +
 * Integer.toString(i)); ObjectOutputStream oos = new ObjectOutputStream(fos);
 * oos.writeObject(cloud); oos.close();
 * 
 * i++; } }
 * 
 * Lda.printDocumentsForTopics( new File("out/documents-per-topic"), topics,
 * lda, 10); Lda.exportDocumentTopics( new File("out/topics-in-documents.csv"),
 * lda, topics, docs, 0, -1);
 * 
 * lda.printDocumentTopics(new File("out/topics-in-documents"));
 * lda.printTopWords(new File("out/top-words"), 10, true);
 * 
 * }
 * 
 * 
 * }
 * 
 * 0902 393 871 po,ba
 * erzika3 lv,40 0902158937 
 * 	0910556328 lv 55 OSAMELAH
 * 0940 855 567 BACULKA99999,TT
 * 0944148284.TT18
 * 
 * // bw.flush(); // bw.close(); System.out.println("done");
 * 
 * 
 * }
 */

