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

public class RssParser {

	//KeywordFactory keywordFactory ;
	Lang lang;
	
	protected static int minArticleLength = 200;
	public RssParser(Lang lang )
	{
		this.lang = lang;
		
		//keywordFactory = new KeywordFactory(extractionSource, extractNEOnly);

	}
	
	public static String processKeywords(List<Keyword> keywords, String body)
	{
	
		String msg=null;
		for (Keyword keyword : keywords) {
			
			
		//	msg = keyword.normalizedName+"____"+keyword.escapedName+"_____"+keyword.name;
			String normalizedKeyword = keyword.getNormalizedKeyword();
			
			
			
			if(body.contains(keyword.name))
			{
				body = body.replace(keyword.name,normalizedKeyword.concat(" "));
			}
			else
			{
				body.concat(normalizedKeyword);
				body.concat(" ");
		//		msg+="----NOT FOUD---";
				//TODO problem s encodingom a replace keyword

			}
			
		//	Logging.Log(msg);

		}
		
		return body;
	}
	
	public  RssFeed parseFeed(SyndEntry entry,boolean translate) throws Exception {
	
		RssFeed parsedFeed = new RssFeed();
		parsedFeed.link = entry.getLink();
		parsedFeed.title = entry.getTitle();
		
		
		String analyzedBody = null;
		String body = null;
		
	
			
			body = StandardTextProcessing.getPlainText(entry.getLink());
			analyzedBody = body;
			
			if(body==null || body.length()==0)
				throw new ParserException("unable to get text for given URL");
			if(body.length()<minArticleLength)
				throw new ParserException("article is not enought length"+body.length()+"/"+minArticleLength);
			
			
			if(lang!=Lang.ENGLISH && translate)
				analyzedBody = StandardTextProcessing.translate(body);
			
			
		//	analyzedBody = StandardTextProcessing.analyze(body,4);

		//	SemanticsData data= StandardTextProcessing.getSemanticsData(body);
			
		//	Logging.Log("---------");

		//	SemanticsData data2= StandardTextProcessing.getSemanticsData(analyzedBody);

			
//			if (data.keywords != null) {
//				analyzedBody = processKeywords(data.keywords, body);
//				analyzedBody = StandardTextProcessing.analyze(analyzedBody,4);
//			}
			
			parsedFeed.originalBody = body;
			parsedFeed.preprocessedBody = analyzedBody;
//			parsedFeed.keywords = data.keywords;
//			parsedFeed.categories = data.categories;
			

		

		return parsedFeed;

	}

	
}
