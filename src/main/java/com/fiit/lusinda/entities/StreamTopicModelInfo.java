package com.fiit.lusinda.entities;

import com.fiit.lusinda.rss.FeedSettings;

public class StreamTopicModelInfo {

	public StreamTopicModelInfo (FeedSettings feedSettings,String rd,int w,int maxRecommendations,int maxWordsPerTopic,int queryLimit)
	{
		this.rootDir= rd;
		this.windowLength= w;
		this.maxRecommendations = maxRecommendations;
		this.queryLimit = queryLimit;
		this.maxWordsPerTopic =maxWordsPerTopic;
		this.feedSettings = feedSettings;
	}
	String rootDir;
	int windowLength;
	int maxRecommendations;
	int queryLimit;
	int maxWordsPerTopic;
	FeedSettings feedSettings;
}
