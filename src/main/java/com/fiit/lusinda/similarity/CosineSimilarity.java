package com.fiit.lusinda.similarity;

public class CosineSimilarity implements SimilarityStrategy {

	public double calculateSimilarity(double[] vec1, double[] vec2,double[] rank_1_docs,double treeshold) {
		double similarity = 0;
		double len1 = 0;
		double len2 = 0;
		
		assert (vec1.length == vec2.length);
		for (int i = 0; i < vec1.length; i++) {

			
			if(rank_1_docs[i]<treeshold)
				continue;
			
			similarity += vec1[i] * vec2[i];
			len1 += vec1[i] * vec1[i];
			len2 += vec2[i] * vec2[i];
			
		}
		
		len1 = Math.sqrt(len1);
		len2 = Math.sqrt(len2);

		similarity = similarity / (len1 *len2);

		return similarity;
	}
	
	private static double vectorLength(double[] vec) {
		double len = 0;
		for (int i = 0; i < vec.length; i++) {
			len += vec[i] * vec[i];
		}
		len = Math.sqrt(len);
		return len;			
	}

}
