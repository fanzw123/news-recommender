package com.fiit.lusinda.entities;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fiit.lusinda.textprocessing.StandardTextProcessing;

public class RssFeed {

	private static Pattern p = Pattern.compile("^.+/c/(\\d+)/.+");
	
	
	public String link;
	public String preprocessedBody;
	public String originalBody;
	public String title;
	public List<Keyword> keywords;
	public int articleId=-1;
	public List<TopicCategory> categories;

	public long ts;
	public Lang lang;
	public String site;
	private String key;
	private String normalizedTitle=null;

	public int getArticleId()
	{
		String id = null;
		try
		{
		
		Matcher m = p.matcher(link);

		if (m.find()) {
		   id = m.group(1);
		   articleId = Integer.parseInt(id);
		}
		}
		catch(Exception e)
		{}
		
		return articleId;
	}
	
	public String getNormalizedTitle() throws IOException
	{
		if(normalizedTitle==null)
		normalizedTitle =StandardTextProcessing.normalizeKeyword(this.title).replaceAll("/", "_");

		
		return normalizedTitle;
				
	}
	
	
	
	// TODO vsetky public property nahradit geterom a seterom
	public String getKey() throws IOException {
		if (key == null) {
			StringBuilder builder = new StringBuilder();

			if (lang == Lang.ENGLISH)
				builder.append("en_"); // en_

			if (lang == Lang.SLOVAK)
				builder.append("sk_"); // en_
			
			builder.append(this.site); // en_nytimes
//			builder.append("_");
//			builder.append(category); // en_nytimes_1.2.1
			builder.append("_");
			builder.append(getNormalizedTitle()); // en_nytimes_1.2.1_name_of_the_title_normalized

			key = builder.toString();
		}
		
		return key;

	}

	// public getNormalizedUrl()
	// {
	// URI url = new URI(link);
	// //url.getHost().s
	// }
}
