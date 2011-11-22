package com.fiit.lusinda.textprocessing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tartarus.snowball.ext.PorterStemmer;

import com.alchemyapi.api.AlchemyAPI;
import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordFactory;
import com.fiit.lusinda.rss.RssParser;
import com.fiit.lusinda.services.AlchemyClient;
import com.fiit.lusinda.services.MetallClient;
import com.fiit.lusinda.services.OpenCalaisClient;
import com.fiit.lusinda.translate.BingTranslateStrategy;
import com.fiit.lusinda.translate.GoogleTranslateStrategy;
import com.fiit.lusinda.translate.TextTranslator;

public class StandardTextProcessing {

	private static MetallClient metall = new MetallClient();
	private static OpenCalaisClient calaisClient = new OpenCalaisClient();
	private static AlchemyClient alchemyClient = new AlchemyClient();
	private static ArticleTextExtractor articleTextExtractor= new ArticleTextExtractor();
	// TODO jeden spolocny interface pre web service client

	private static TextTranslator translator = new TextTranslator(
			new BingTranslateStrategy());
	private static KeywordFactory keywordFactory = new KeywordFactory(null,
			false);

	// TODO allow set keyword factory preferencies

	// private static final Analyzer[] analyzers = new Analyzer[]{
	// new WhitespaceAnalyzer(),
	// new SimpleAnalyzer(),
	// new StopAnalyzer(),
	// new StandardAnalyzer(),
	// new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS),
	// };
	//

	public static String translate(String text) throws Exception {
		return translator.translateText(text);
	}
	
	private static String getEncoding(URLConnection connection)
	{
		String contentType = connection.getContentType();
		String[] values = contentType.split(";"); //The values.length must be equal to 2...
		String charset = "";

		for (String value : values) {
		    value = value.trim();

		    if (value.toLowerCase().startsWith("charset=")) {
		        charset = value.substring("charset=".length());
		    }
		}
		
		if("".equals(charset))
		{
			
		}

		if ("".equals(charset)) {
		    charset = "UTF-8"; //Assumption....it's the mother of all f**k ups...lol
		}
		
		return charset;
	}
	
	public static String getHTml(String url,String encoding) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		
		
		
		URL u = new URL(url);
		HttpURLConnection conn =(HttpURLConnection) u.openConnection();
		conn.setInstanceFollowRedirects(false);
	//	conn.setRequestProperty("Cookie", value)//
		//String redirect = conn.getHeaderField("Location");
		
		conn.connect();
		
			
			DataInputStream in =new DataInputStream(new BufferedInputStream(conn.getInputStream()));
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName(encoding)));  


		String s =null;
		while((s = reader.readLine())!=null)
			builder.append(s);
			  in.close();
			  
			   return builder.toString();
			
			   
	}

	public static String getText(String url) throws Exception {
		
		
		
	//	String html = getHTml(url,encoding);
		
		String result = articleTextExtractor.getArticleText(url);
	//	String result = alchemyClient.cleartext(url); // metall.cleartext(url);
		return result;
	}

	public static List<Keyword> getKeywords(String url) throws Exception {
		// return metall.getKeywords(url,keywordFactory);

		return calaisClient.getKeywords(url, keywordFactory);
	}

	public static String analyze(String text,int minLength) throws IOException {
		
		
		StringBuilder output = new StringBuilder();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);// ,"English",
																	// StandardAnalyzer.STOP_WORDS_SET);

		TokenStream tokenStream = (TokenStream) analyzer.tokenStream(null,
				new StringReader(text));
		// tokenStream = new LengthFilter(tokenStream,4, 12);

		OffsetAttribute offsetAttribute = tokenStream
				.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream
				.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();
			if (term.length() > minLength) {
				output.append(term);
				output.append(" ");
			}

			
		}

		return output.toString();
	}

	public static String normalizeKeyword(String keyword) throws IOException {
		return analyze(keyword,0).trim().replaceAll("\\s+", "_");
	}
	
	public static void main(String[] args) throws Exception {
	
		//String url = "http://www.sme.sk/c/6142711/pitovcov-obvinili-z-podvodov-s-americkymi-hypotekami.html";
		//String encoding = "Windows-1250";
		
	//	String url = "http://www.nytimes.com/2011/11/16/us/ann-patchett-bucks-bookstore-tide-opening-her-own.html";
//		String encoding = "UTF-8";
		
		
		
//	String html = getHTml(url,encoding);
	
//	System.out.print(html);
	
		String url = "http://feeds.nytimes.com/click.phdo?i=0ecaa3b10e5506bf4fb49abcc13a08fd";
		String body = getText(url);

	String	analyzedBody = StandardTextProcessing.analyze(body,4);

		
		List<Keyword> keywords = StandardTextProcessing.getKeywords(body);
		
		if (keywords != null) {
			analyzedBody = RssParser.processKeywords(keywords, analyzedBody);
		}
		
		//String result = articleTextExtractor.getArticleText(url);
		
		System.out.println(analyzedBody);
	}

}
