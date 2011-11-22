package com.fiit.lusinda.topicmodelling;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import org.apache.lucene.store.Directory;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.utils.Logging;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.LDAStream;
import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class LdaRunner {

	public LdaRunner() {

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

	public LdaRunner(int numTopics, InstanceList trainList, InstanceList testList,
			String outputPath) {
		this.initNumTopics = numTopics;
		this.testList = testList;
		this.trainList = trainList;
		this.outputPath = outputPath;
		File f = new File(outputPath);
		if (!f.exists())
			f.mkdir();

	}

	public LdaRunner(int numTopics, String trainListPath, String testListPath,
			String outputPath) {
		this(numTopics, InstanceList.load(new File(trainListPath)),
				InstanceList.load(new File(testListPath)), outputPath);

	}

	public LdaRunner(int numTopics, String trainListPath, String outputPath) {
		this(numTopics, InstanceList.load(new File(trainListPath)), null,
				outputPath);
	}

	public Dataset runInternal(LdaProperties properties) throws IOException {
		Lda curr_lda = estimate(properties);

		if (properties.evaluate) {
			evaluator = curr_lda.getProbEstimator();
			double currLikelihood = evaluator.evaluateLeftToRight(testList, 10,
					false, null);

			ldaLikelihoodPairs.put(properties.topics, currLikelihood);

			// Logging.Log(String.format("numTopics:{0} \t ll:{1} \best ll:{2}",this.numTopics,currLikelihood,likelihood));
			properties.bw.write(Integer.toString(properties.topics));
			properties.bw.write(",");

			properties.bw.write(Double.toString(currLikelihood));
			properties.bw.newLine();
			if (likelihood == 0 || currLikelihood > likelihood) {
				this.likelihood = currLikelihood;
				this.lda = curr_lda;
			}

		}
		// export
	//	List<Topic> exportTopics = Utils.getDocumentTopics(curr_lda, 0, 10);
		List<DocumentProbability> docs = Utils.getDocuments(curr_lda);
		// Utils.fillDocumentsAttributes(docs,trainList, -1);

	//	 curr_lda.topicPhraseXMLReport(new PrintWriter(new File("/var/lusinda/solr/mallet/9/topicPhrase.xml")),curr_lda.getAlphabet().size());
	//	 curr_lda.topicXMLReport(new PrintWriter(new File("/var/lusinda/solr/mallet/9/topicReport.xml")),curr_lda.getAlphabet().size());

		Utils.exportWordsTopics(curr_lda,properties.WordTopicsExportFilePath,";");

		return Utils.exportDocumentTopics(properties.DocumentTopicsExportFilePath, curr_lda,  docs, 0, -1, ";");

	}

	public Dataset run(LdaProperties properties) throws IOException {

		String fileName = "likelihood";
		double beta = 0.01;
		double alpha = 50.00 / initNumTopics;
		Dataset exportedDocuments = null;

		for (int run = 1, topics = initNumTopics; run <= properties.averageNumRuns; run++, topics = initNumTopics) {

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

			for (int i = 0; i < properties.steps; topics += properties.seed, i++) {

				alpha = 50.00 / topics;
				properties.alpha = alpha;
				properties.beta = beta;
				properties.topics = topics;

				exportedDocuments = runInternal(properties);

			}

			bw.flush();
			bw.close();
			
			return exportedDocuments;
		}

		// dokoncit priemer, pozret ako v hashmap zmnit vlozenu double hodnotu

		OutputStream os = (OutputStream) new FileOutputStream(new File(
				outputPath + fileName + "-average.csv"));
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write("topic number");
		bw.write(",");
		bw.write("left-to-right");
		bw.newLine();
		for (Entry<Integer, Double> entry : ldaLikelihoodPairs.entrySet()) {
			entry.setValue(entry.getValue() / properties.averageNumRuns);

			bw.write(Integer.toString(entry.getKey()));
			bw.write(",");

			bw.write(Double.toString(entry.getValue()));
			bw.newLine();
		}

		bw.flush();
		bw.close();
		return exportedDocuments;

	}

	private Lda estimate(LdaProperties properties)
			throws IOException {
		Lda curr_lda = new Lda(properties.topics,
				properties.alpha, properties.beta);
		curr_lda.setNumIterations(1000);
		curr_lda.addInstances(trainList);

		curr_lda.estimate();

		return curr_lda;

	}
	
	

	public static void main(String[] args) throws Exception {

		LdaRunner lda = new LdaRunner(50, "/var/lusinda/solr/mallet/9/train.lda",

		"/var/lusinda/solr/mallet/9/likelihood/");
		// lda.run(steps, seed, averageNumRuns, evaluate)

		LdaProperties properties = new LdaProperties();
		properties.averageNumRuns = 1;
		properties.evaluate = false;
		//properties.exportFilePath = "/var/lusinda/solr/mallet/9/topics-in-documents.csv";
		properties.seed = 5;
		properties.steps = 1;

		lda.run(properties);

		
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
	 * OutputStream os = (OutputStream) new FileOutputStream(f); String encoding
	 * = "UTF8"; OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
	 * BufferedWriter bw = new BufferedWriter(osw);
	 * 
	 * bw.write("topic number"); bw.write(","); bw.write("LogLikehood");
	 * bw.write(","); bw.write("left-to-right"); bw.newLine();
	 * 
	 * for (; numTopics <= maxTopics; numTopics += step) { InstanceList
	 * instances = InstanceList.load(new File(inputFile));
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
	 * if (!estimateLikelihood) { List<Topic> topics =
	 * Lda.getDocumentTopics(lda, 0, 10); List<DocumentProbability> docs =
	 * Lda.getDocuments(lda);
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
	 * cloud.addTag(new Tag( docprob.getDocAttributes().title, docprob.prob *
	 * 10));
	 * 
	 * FileOutputStream fos = new FileOutputStream(
	 * "/Users/teo/Documents/workspace/TopicModelling/out/clouds/cloud" +
	 * Integer.toString(i)); ObjectOutputStream oos = new
	 * ObjectOutputStream(fos); oos.writeObject(cloud); oos.close();
	 * 
	 * i++; } }
	 * 
	 * Lda.printDocumentsForTopics( new File("out/documents-per-topic"), topics,
	 * lda, 10); Lda.exportDocumentTopics( new
	 * File("out/topics-in-documents.csv"), lda, topics, docs, 0, -1);
	 * 
	 * lda.printDocumentTopics(new File("out/topics-in-documents"));
	 * lda.printTopWords(new File("out/top-words"), 10, true);
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * // bw.flush(); // bw.close(); System.out.println("done");
	 * 
	 * 
	 * }
	 */

}