package com.fiit.lusinda.similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.topicmodelling.Document;
import com.fiit.lusinda.utils.Logging;
import com.fiit.lusinda.utils.MapUtilities;

public class DocumentSimilarity {

	Dataset dataset;
	double treeshold;
	SimilarityMeasureStrategy similarityStrategy;
	

	public DocumentSimilarity(Dataset dataset,double treeshold,SimilarityMeasureStrategy similarityStrategy) {
		this.dataset = dataset;
		this.treeshold = treeshold;
		this.similarityStrategy =similarityStrategy;
	}

	public List<Map.Entry<Document, Double>> getTopNSimilarDocuments(
			Document docToCompare, int n, String category) {
		HashMap<Document, Double> mapWeights = new HashMap<Document, Double>();

		int i = 0;
		for (Document doc : dataset) {
			if (category == null
					|| doc.documentAttributes.category.equals(category)) {
				double sim = similarityStrategy.computeSimmilarity(
						doc.probability, docToCompare.probability);
				if(sim<treeshold)
					mapWeights.put(doc, sim);
			}
		}

		List<Map.Entry<Document, Double>> sorted = MapUtilities
				.sortByValue(mapWeights);

		if (n > sorted.size())
			return sorted;
		else
			return sorted.subList(0, n);

	}

	public List<Map.Entry<Document, Double>> getTopNSimilarDocuments(int docId,
			int n, String category) {
		Document docToCompare = dataset.get(docId);

		return getTopNSimilarDocuments(docToCompare, n, category);
	}

	public static void main(String[] args) throws IOException {
		
		
		
		Dataset documents = new Dataset();
		
		documents.loadDataset(";",
				"/var/lusinda/solr/mallet/9/topics-in-documents.csv");

		Document docToCompare = documents.get(120);

		//parameters
		int n = 20;
String category = docToCompare.documentAttributes.category; 
double treeshold = 1;
DocumentSimilarity docSimilarity = new DocumentSimilarity(documents,treeshold,new JsSimilarityMeasure());


		Logging.Log("document to compare: ("
				+ docToCompare.documentAttributes.category + ")"
				+ docToCompare.documentAttributes.title);

		List<Map.Entry<Document, Double>> result = docSimilarity
				.getTopNSimilarDocuments(docToCompare, n,category);

		Logging.Log("TOP " + String.valueOf(n) + " documents:");

		for (Map.Entry<Document, Double> entry : result) {
			Logging.Log(entry.getKey().documentAttributes.title + ": ("
					+ entry.getKey().documentAttributes.category + ")"
					+ String.valueOf(entry.getValue()));
		}

	}
}
