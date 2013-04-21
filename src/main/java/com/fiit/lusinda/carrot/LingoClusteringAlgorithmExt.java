package com.fiit.lusinda.carrot;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;

public class LingoClusteringAlgorithmExt extends LingoClusteringAlgorithm {

	public LingoClusteringAlgorithmExt()
	{
		super();
		clusterBuilder.featureScorer = new LdaFeatureScorer();
	}
}
