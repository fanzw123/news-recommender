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
import com.fiit.lusinda.exceptions.ParserException;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.feed.synd.SyndEntry;

public class RssParser {

	//KeywordFactory keywordFactory ;
	Lang lang;
	private static int minArticleLength = 100;
	public RssParser(Lang lang)
	{
		this.lang = lang;
		//keywordFactory = new KeywordFactory(extractionSource, extractNEOnly);

	}
	
	public static String processKeywords(List<Keyword> keywords, String body)
	{
	
		String msg=null;
		for (Keyword keyword : keywords) {
			
		//	msg = keyword.normalizedName+"____"+keyword.escapedName+"_____"+keyword.name;
			
			if(body.contains(keyword.escapedName))
			{
				body = body.replace(keyword.escapedName,keyword.getNormalizedKeyword().concat(" "));
			}
			else
			{
				body.concat(keyword.getNormalizedKeyword());
				body.concat(" ");
		//		msg+="----NOT FOUD---";
				//TODO problem s encodingom a replace keyword

			}
			
		//	Logging.Log(msg);

		}
		
		return body;
	}
	
	public  RssFeed parseFeed(SyndEntry entry) throws Exception {
	
		RssFeed parsedFeed = new RssFeed();
		parsedFeed.link = entry.getLink();
		parsedFeed.title = entry.getTitle();
		
		
		String analyzedBody = null;
		String body = null;
		
	
			
			body = StandardTextProcessing.getText(entry.getLink());
			
			
			if(body==null || body.length()==0)
				throw new ParserException("unable to get text for given URL");
			if(body.length()<minArticleLength)
				throw new ParserException("article is not enought length"+body.length()+"/"+minArticleLength);
			
			parsedFeed.originalBody = body;
			
			if(lang!=Lang.ENGLISH)
				body = StandardTextProcessing.translate(body);
				
			analyzedBody = StandardTextProcessing.analyze(body,4);

			
			List<Keyword> keywords = StandardTextProcessing.getKeywords(body);
			
			if (keywords != null) {
				analyzedBody = processKeywords(keywords, analyzedBody);
			}
			
			parsedFeed.preprocessedBody = analyzedBody;
			parsedFeed.keywords = keywords;


		

		return parsedFeed;

	}

	
}
