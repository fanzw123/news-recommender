package com.fiit.lusinda.entities;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.fiit.lusinda.textprocessing.StandardTextProcessing;

public class RssFeed {

	public String link;
	public String preprocessedBody;
	public String originalBody;
	public String title;
	public List<Keyword> keywords;
	public long ts;
	public Lang lang;
	public String site;
	public String category;
	private String key;

	// TODO vsetky public property nahradit geterom a seterom
	public String getKey() throws IOException {
		if (key == null) {
			StringBuilder builder = new StringBuilder();

			if (lang == Lang.ENGLISH)
				builder.append("en_"); // en_
			builder.append(this.site); // en_nytimes
			builder.append("_");
			builder.append(category); // en_nytimes_1.2.1
			builder.append("_");
			builder.append(StandardTextProcessing.normalizeKeyword(this.title)); // en_nytimes_1.2.1_name_of_the_title_normalized

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
