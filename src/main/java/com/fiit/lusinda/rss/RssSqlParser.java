package com.fiit.lusinda.rss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordFactory;
import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.entities.SemanticsData;
import com.fiit.lusinda.exceptions.ParserException;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.feed.synd.SyndEntry;

public class RssSqlParser extends RssParser {

	public RssSqlParser(Lang lang)
	{
		super(lang);

	}
	
	
	
	public RssFeed parseFeed(SyndEntry entry,boolean translate) throws Exception {
	
		RssFeed parsedFeed = new RssFeed();
		parsedFeed.link = entry.getLink();
		parsedFeed.title = entry.getTitle();
		
		
		String analyzedBody = null;
		String body = null;
		
			body = entry.getDescription().getValue();
			analyzedBody = body;
			
			if(body==null || body.length()==0)
				throw new ParserException("unable to get text for given URL");
			if(body.length()<minArticleLength)
				throw new ParserException("article is not enought length"+body.length()+"/"+minArticleLength);
			
			
			if(lang!=Lang.ENGLISH && translate)
				analyzedBody = StandardTextProcessing.translate(body);
			
		//	analyzedBody = StandardTextProcessing.analyze(text, minLength);
			
			parsedFeed.originalBody = body;
			parsedFeed.preprocessedBody = analyzedBody;
	

		return parsedFeed;

	}

	
}
