package com.fiit.lusinda.similarity;

import java.io.IOException;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.topicmodelling.Document;
import com.fiit.lusinda.topicmodelling.TopicWordMatrix;

public class TopicSimilarity {

	TopicWordMatrix matrix;
	double treeshold;
	SimilarityMeasureStrategy similarityStrategy;
	

	public TopicSimilarity(TopicWordMatrix matrix,double treeshold,SimilarityMeasureStrategy similarityStrategy) {
		this.matrix = matrix;
		this.treeshold = treeshold;
		this.similarityStrategy =similarityStrategy;
	}
	
	public void getTopNSimilarTopics()
	{
		
	}
	
	public static void main(String[] args) throws IOException {

		TopicWordMatrix matrix = new TopicWordMatrix();

		matrix.load("/var/lusinda/solr/mallet/9/topics-in-words.csv",";");


		// parameters
		double treeshold = 0.6;
		
		TopicSimilarity topicSimilarity = new TopicSimilarity(matrix,
				treeshold, new JsSimilarityMeasure());

		
	}
}
