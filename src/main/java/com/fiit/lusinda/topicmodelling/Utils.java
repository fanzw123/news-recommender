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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import org.apache.lucene.store.Directory;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.clustering.Dataset;

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

public class Utils {

	public static void exportWordsTopics(Lda lda,String filePath,String separator) throws IOException {

		String[] words = (String[]) lda.getAlphabet().toArray(new String[0]);
		TopicWordMatrix topicWordMatrix = new TopicWordMatrix(words, lda.getNumTopics());
		
		TreeSet[] topicSortedWords = lda.getSortedWords();
		for (int ti = 0; ti < lda.getNumTopics(); ti++) {
			Iterator<IDSorter> iterator = topicSortedWords[ti].iterator();
			while (iterator.hasNext() ) {
				IDSorter info = iterator.next();
				topicWordMatrix.addWordProbability(ti, (String)lda.getAlphabet().lookupObject(info.getID()), info.getWeight()/lda.geTtokensPerTopic()[ti]);
				
			}
		}
		
		topicWordMatrix.save(filePath,separator);
		
		
	}

	public static void fillDocumentsAttributes(List<DocumentProbability> docs,
			InstanceList instanceList, int maxDocuments) {

		maxDocuments = maxDocuments == -1 ? docs.size() : maxDocuments;

		for (int i = 0; i < maxDocuments; i++) {

			DocumentAttributes docAtts = docs.get(i).getDocAttributes();

			docs.get(i).setDocAttributes(
					fillDocAttributes(instanceList, docAtts));

		}
	}

	public static void fillDocumentsAttributesInTopic(List<Topic> topics,
			InstanceList instanceList, Lda lda, int maxDocuments)
			throws IOException, SQLException {

		for (Topic topic : topics) {

			fillDocumentsAttributes(topic.getDocumentProbabilities(),
					instanceList, maxDocuments);
		}

	}

	public static DocumentAttributes fillDocAttributes(
			InstanceList instanceList, DocumentAttributes attributes) {
		for (Instance instance : instanceList) {
			String instanceId = (String) instance.getSource();

			if (instanceId.equals(attributes.docId)) {
				attributes.title = (String) instance.getTarget();
				break;
			}
		}

		return attributes;
	}

	public static void printDocumentsForTopics(File f, List<Topic> topics,
			Lda lda, InstanceList instanceList, int maxDocuments)
			throws IOException, SQLException {

		OutputStream os = (OutputStream) new FileOutputStream(f);
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write("document title");
		bw.write(",");
		bw.write("probability");
		bw.newLine();

		SmeDataSource sme = new SmeDataSource();
		sme.connect();
		for (Topic topic : topics) {
			bw.newLine();
			bw.write("Topic");
			bw.newLine();

			for (int i = 0; i < maxDocuments; i++) {

				DocumentAttributes docAtts = topic.getDocumentProbability(i)
						.getDocAttributes();

				topic.getDocumentProbability(i).setDocAttributes(
						fillDocAttributes(instanceList, docAtts));

				bw.write(topic.getDocumentProbability(i).getDocAttributes().title);
				bw.write(",");
				bw.write(Double.toString(topic.getDocumentProbability(i).prob));
				bw.newLine();
			}
		}

		bw.flush();
		bw.close();

		sme.disconnect();

	}

	public static Dataset exportDocumentTopics(String filePath,
			Lda lda, List<DocumentProbability> docs,
			double threshold, int max, String separator) throws IOException {

		Dataset documents = new Dataset();
		Document currDocument = null;

		int docLen;
		int numTopics = lda.getNumTopics();
		ArrayList<TopicAssignment> data = lda.getData();

		int[] topicCounts = new int[numTopics];

		IDSorter[] sortedTopics = new IDSorter[numTopics];
		for (int topic = 0; topic < numTopics; topic++) {
			// Initialize the sorters with dummy values
			sortedTopics[topic] = new IDSorter(topic, topic);
		}

		if (max < 0 || max > numTopics) {
			max = numTopics;
		}

		for (int doc = 0; doc < data.size(); doc++) {
			LabelSequence topicSequence = (LabelSequence) data.get(doc).topicSequence;
			int[] currentDocTopics = topicSequence.getFeatures();

			currDocument = new Document();
			currDocument.documentAttributes = docs.get(doc).getDocAttributes();
			currDocument.probability = new double[max];

			docLen = currentDocTopics.length;

			// Count up the tokens
			for (int token = 0; token < docLen; token++) {
				topicCounts[currentDocTopics[token]]++;
			}

			// And normalize
			for (int topic = 0; topic < numTopics; topic++) {
				sortedTopics[topic].set(topic, (float) topicCounts[topic]
						/ docLen);
			}

			// Arrays.sort(sortedTopics);

			for (int i = 0; i < max; i++) {
				if (sortedTopics[i].getWeight() < threshold) {
					break;
				}

				currDocument.probability[i] = sortedTopics[i].getWeight();
			}

			documents.add(currDocument);

			Arrays.fill(topicCounts, 0);

		}

		documents.saveDataset(separator, filePath);

		return documents;
	}

	public static List<DocumentProbability> getDocuments(Lda lda) {

		ArrayList<TopicAssignment> data = lda.getData();

		ArrayList<DocumentProbability> docs = new ArrayList<DocumentProbability>();

		String docId = null;
		String label = null;
		String category = null;

		for (int doc = 0; doc < data.size(); doc++) {
			LabelSequence topicSequence = (LabelSequence) data.get(doc).topicSequence;

			if (data.get(doc).instance.getSource() != null) {
				docId = (String) String.valueOf(data.get(doc).instance
						.getSource());
				label = (String) String.valueOf(data.get(doc).instance
						.getTarget());
				category = (String) String.valueOf(data.get(doc).instance
						.getName());

				DocumentProbability documentProbability = new DocumentProbability(
						docId, 0);
				documentProbability.docAttributes.title = label;
				documentProbability.docAttributes.category = category;

				docs.add(documentProbability);
			} else {
			}

		}

		return docs;

	}

	public static List<Topic> getDocumentTopics(Lda lda,
			double threshold, int maxDocuments) {
		int docLen;
		int numTopics = lda.getNumTopics();
		ArrayList<TopicAssignment> data = lda.getData();

		ArrayList<Topic> topics = new ArrayList<Topic>();

		int[] topicCounts = new int[numTopics];

		Topic currTopic = null;

		IDSorter[] sortedTopics = new IDSorter[numTopics];
		for (int topic = 0; topic < numTopics; topic++) {
			// Initialize the sorters with dummy values
			sortedTopics[topic] = new IDSorter(topic, topic);
			// create result list
			currTopic = new Topic(topic, maxDocuments);
			topics.add(currTopic);
		}

		String docId = null;

		for (int doc = 0; doc < data.size(); doc++) {
			LabelSequence topicSequence = (LabelSequence) data.get(doc).topicSequence;
			int[] currentDocTopics = topicSequence.getFeatures();

			DocumentProbability docProb = new DocumentProbability();
			if (data.get(doc).instance.getSource() != null) {
				docId = (String) String.valueOf(data.get(doc).instance
						.getSource());
			} else {
			}

			docLen = currentDocTopics.length;

			// Count up the tokens
			for (int token = 0; token < docLen; token++) {
				topicCounts[currentDocTopics[token]]++;
			}

			// And normalize
			for (int topic = 0; topic < numTopics; topic++) {
				sortedTopics[topic].set(topic, (float) topicCounts[topic]
						/ docLen);
			}

			Arrays.sort(sortedTopics);

			for (int i = 0; i < numTopics; i++) {
				currTopic = topics.get(sortedTopics[i].getID());
				currTopic.Add(new DocumentProbability(docId, sortedTopics[i]
						.getWeight()));
				// currTopic.AddOrLeave(docId, sortedTopics[i].getWeight());
				// if (sortedTopics[i].getWeight() < threshold) { break; }

			}

			Arrays.fill(topicCounts, 0);
		}

		for (Topic t : topics)
			t.Sort();

		return topics;
	}

}
