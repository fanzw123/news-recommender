package com.fiit.lusinda.similarity;

public class SimilarityIndex implements SimilarityMeasureStrategy {

	SimilarityMeasureStrategy strategy;
	
	public SimilarityIndex(SimilarityMeasureStrategy strategy)
	{
		this.strategy = strategy;
	}

	public double computeSimmilarity(double[] probability1,
			double[] probability2) {
		// TODO Auto-generated method stub
		return strategy.computeSimmilarity(probability1, probability2);
	}
	
	
	
}
