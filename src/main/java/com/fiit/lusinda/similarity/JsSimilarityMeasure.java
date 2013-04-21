package com.fiit.lusinda.similarity;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.topicmodelling.Document;
import com.fiit.lusinda.topicmodelling.DocumentProbability;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class JsSimilarityMeasure implements DissimilarityMeasure, SimilarityMeasureStrategy {

	public   double computeSimmilarity(double[] probability1,double[] probability2)
	{
		if(probability1 == null || probability2==null)
			return 1;
		else
			return cc.mallet.util.Maths.jensenShannonDivergence(probability1, probability2);
	}
	
	public double computeDissimilarity(Experiment experiment, int observation1, int observation2) {

		double result = 0;
		
		Dataset dataset = (Dataset) experiment;
		
		Document doc1 = dataset.get(observation1);
		Document doc2 = dataset.get(observation2);
		
		result = computeSimmilarity(doc1.probability, doc2.probability);

		return result;

	}

}
