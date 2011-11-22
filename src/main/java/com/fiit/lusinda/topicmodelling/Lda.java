package com.fiit.lusinda.topicmodelling;

import cc.mallet.topics.ParallelTopicModel;

public class Lda extends ParallelTopicModel {

	public Lda(int numberOfTopics) {
		super(numberOfTopics);
		// TODO Auto-generated constructor stub
	}
	
	public Lda(int topics, double alpha, double beta) {
		super(topics,alpha,beta);
		// TODO Auto-generated constructor stub
	}

	public int[] geTtokensPerTopic()
	{
		return this.tokensPerTopic;
	}

}
