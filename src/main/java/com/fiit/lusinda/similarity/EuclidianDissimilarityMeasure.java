package com.fiit.lusinda.similarity;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.topicmodelling.Document;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class EuclidianDissimilarityMeasure implements DissimilarityMeasure,  SimilarityMeasureStrategy {

	private double distance(double[] v1, double[] v2) {
		int m = Math.min(v1.length, v2.length);
		int n = Math.max(v1.length, v2.length);
		double sum = 0;
		for (int i = 0; i < n; i++) {
			double d = 0;
			if (i < m) {
				d = v1[i] - v2[i];
			} else if (i < v1.length) {
				d = v1[i];
			} else {
				d = v2[i];
			}
			sum = sum + (d * d);
		}
		return Math.sqrt(sum);
	}

	public double computeDissimilarity(Experiment experiment, int observation1,
			int observation2) {

		double result = 0;
		Dataset dataset = (Dataset) experiment;

		Document doc1 = dataset.get(observation1);
		Document doc2 = dataset.get(observation2);

		result = computeSimmilarity(doc1.probability,doc2.probability);

		return result;
	}

	public double computeSimmilarity(double[] probability1,
			double[] probability2) {
		
		return distance(probability1,probability2);
	}
}
