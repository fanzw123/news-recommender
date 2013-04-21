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

	public int maxTopics;
	public int topicIncrement;
	public int averageNumRuns; 
	public boolean heldOut;
	public boolean estimateTopicCountsUsingHLDA = false;
	
	public int getTopics()
	{
		return topics;
	}
	
//	public int getMaxTopics()
//	{
//		return maxTopics;
//	}
//	
//	public void setTopics(int t)
//	{
//		this.topics = t;
//		maxTopics= t;
//	}
//	
	public void setMaxTopics(int mt)
	{
		this.maxTopics = mt;
		
	}
	
	public LdaProperties()
	{
		
	}
	
}
