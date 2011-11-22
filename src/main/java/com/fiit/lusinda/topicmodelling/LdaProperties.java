package com.fiit.lusinda.topicmodelling;

import java.io.BufferedWriter;

public class LdaProperties {

	public boolean evaluate;
	public int topics;
	public double alpha;
	public double beta;
	public BufferedWriter bw;
	public String DocumentTopicsExportFilePath;
	public String WordTopicsExportFilePath;

	public int steps;
	public int seed;
	public int averageNumRuns; 
	
	
	public LdaProperties()
	{
		
	}
	
}
