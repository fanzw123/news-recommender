package com.fiit.lusinda.entities;

public class SimilarityMatrix {

	double[][] matrix;
	double[] rows;
	double[] columns;
	
	
	public SimilarityMatrix(double[] rows,double[] columns)
	{
		this.rows = rows;
		this.columns = columns;
		matrix = new double[rows.length][columns.length];
	}
	
	public double getWeight(int i,int j)
	{
		return matrix[i][j];
	}
	
	public void computeSimilarity()
	{
		
	}
	
}
