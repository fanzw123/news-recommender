package com.fiit.lusinda.hbase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeSet;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.carrot2.core.Document;
import org.getopt.stempel.Stemmer;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.carrot.SlovakStemmerAdapter;
import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.DocumentStream;
import com.fiit.lusinda.entities.KeywordsCluster;
import com.fiit.lusinda.entities.NGram;
import com.fiit.lusinda.entities.Query;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.entities.SortedList;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.entities.StringDoublePair;
import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.utils.Logging;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HBaseProxy {

	private HTable articles;
	private HTable words;
	private HTable topics;

	public final static String CF_META = "m";
	public final static String CF_ARTICLES = "a";
	public final static String CF_WORDS = "w";
	public final static String CF_TOPICS = "t";
	public final static String CF_STEMS = "s";
	public final static String CF_SUMARIZED_ARTICLES = "s";

	private HBaseProxy() throws IOException {
		articles = HBaseConnectionManager.getConnection().getTable("articles");
		articles.setAutoFlush(true);

		words = HBaseConnectionManager.getConnection().getTable("words");
		words.setAutoFlush(true);

		topics = HBaseConnectionManager.getConnection().getTable("topics");
		topics.setAutoFlush(true);

	}

	public static HBaseProxy create() throws IOException {
		return new HBaseProxy();
	}
	
	
	public void HBaseToSqlImport(SmeDataSource smeDs,int limit,int treshold) throws IOException, SQLException
	{
		
Scan scan = new Scan();
		
		//scan.addFamily(Bytes.toBytes("m"));

int i =0;
		ResultScanner scanner = articles.getScanner(scan);
		try {
		  
		  
		  for (Result rr = scanner.next(); rr != null && i<limit; rr = scanner.next()) {

			  
			  String title = Bytes.toString(rr.getValue(Bytes.toBytes("m"),Bytes.toBytes("title")));
		    int article_id = Bytes.toInt(rr.getValue(Bytes.toBytes("m"),Bytes.toBytes("article_id")));
		    
		    System.out.println(title);
		    
		    NavigableMap<byte[], byte[]> articles_cf = rr.getFamilyMap(Bytes
					.toBytes("a"));

		    
			if (articles_cf != null) {
			
			
				if(articles_cf.size()>treshold)
				{
					smeDs.insertToEvaluatedArticles(article_id);
					
					for (Entry<byte[], byte[]> en : articles_cf.entrySet()) {
					String key_to_recommended_article = Bytes.toString(en.getKey());
					
					System.out.println("\t"+key_to_recommended_article);
					}
					System.out.println("---------------------------");
					i++;
				}
			}
		    
		  }
		}
		finally{}
		
		
	}
	
	public void dumpArticles() throws IOException
	{
		Scan scan = new Scan();
		
		scan.addFamily(Bytes.toBytes("m"));
		ResultScanner scanner = articles.getScanner(scan);
		try {
		  // Scanners return Result instances.
		  // Now, for the actual iteration. One way is to use a while loop like so:
		  for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
		    // print out the row we found and the columns we were looking for
			  System.out.print(Bytes.toString(rr.getValue(Bytes.toBytes("m"),Bytes.toBytes("title"))));
		    System.out.print(" --> " + Bytes.toInt(rr.getValue(Bytes.toBytes("m"),Bytes.toBytes("article_id"))));
		    System.out.println();
		  }
		}
		finally{}
		  
	}
	
	

	public Map<String, KeywordsCluster> getKeywordsClusters(String key)
			throws IOException, URISyntaxException {
		Map<String, KeywordsCluster> clusters = Maps.newHashMap();
		Map<String,String[]> ngrams = Maps.newHashMap();
		
		
		Get get = new Get(Bytes.toBytes(key));
		// get.addFamily(Bytes.toBytes("a"));
		Result res = articles.get(get);
		NavigableMap<byte[], byte[]> articles_cf = res.getFamilyMap(Bytes
				.toBytes("a"));

		if (articles_cf != null) {

			for (Entry<byte[], byte[]> en : articles_cf.entrySet()) {

			//	System.out.println(Bytes.toString(en.getKey()));
				// Bytes.toString(en.getKey());
				byte[] key_to_recommended_article = en.getKey();

				Get a_get = new Get(key_to_recommended_article);

				Result recommedned_article_res = articles.get(a_get);
				NavigableMap<byte[], byte[]> recommended_articles_m_cf = recommedned_article_res
						.getFamilyMap(Bytes.toBytes("m"));
				NavigableMap<byte[], byte[]> recommended_articles_w_cf = recommedned_article_res
						.getFamilyMap(Bytes.toBytes("w"));

				//Article article = new Article();
				org.carrot2.core.Document doc = new org.carrot2.core.Document();
				
				doc.setSummary(Bytes.toString(recommended_articles_m_cf
						.get(Bytes.toBytes("body"))));
				doc.setTitle(Bytes.toString(recommended_articles_m_cf
						.get(Bytes.toBytes("title"))));

			//	 System.out.println(article.title);
				for (Entry<byte[], byte[]> kw : recommended_articles_w_cf
						.entrySet()) {
					String kw_key = Bytes.toString(kw.getKey());
					double weight = Bytes.toDouble(kw.getValue());
			//		 System.out.println(kw_key);
					
					String[] parts = kw_key.split(" ");
					if(parts.length>1)
					{
						
						ngrams.put(kw_key,Lemmatizer.getLemmatizer().lemmatizeFirst(parts));
					}
					
					KeywordsCluster k = clusters.get(kw_key);
					if (k == null) {
						k = new KeywordsCluster();

					}
					
					


					//zobrat min a nasladne v controlleri sa spravi sum(ta minimalna)*documents.count
				
					
					k.sum+=weight;
					
					k.add(doc);

					clusters.put(kw_key, k);
				}

			}
		}
		
		for(Entry<String,String[]> ng:ngrams.entrySet())
		{
			KeywordsCluster ngramKwCluster = clusters.get(ng.getKey());
			
			for(String part:ng.getValue())
			{
				KeywordsCluster k = clusters.get(part);
				if (k != null) {
					ngramKwCluster.sum+=k.sum; 
					
				}
			}
			
			clusters.put(ng.getKey(), ngramKwCluster);
		}

		return clusters;
	}

	public void putWord(String key, String word, long ts) throws IOException {
		Put put = new Put(Bytes.toBytes(key), ts);
		put.add(Bytes.toBytes("w"), Bytes.toBytes("stem"), Bytes.toBytes(word));
		words.put(put);

	}

	public String putWord(String token, int min, long ts, boolean lookup,
			int tolerance) throws IOException {
		String key = null;
		if (lookup)
			key = lookupWord(token, min, tolerance);

		if (key == null)
			putWord(token, token, ts);
		else
			putWord(key, token, ts);

		return key;

	}

	public void putRecommendedArticles(Article article,
			TreeSet<Sorter<Article>> topArticles, int limit) throws IOException {
		Put put = new Put(Bytes.toBytes(article.key), article.ts);

		int i = 0;
		Iterator<Sorter<Article>> it = topArticles.iterator();

		while (it.hasNext() && i < limit) {
			Sorter<Article> sd = it.next();

			Put reversePut = new Put(Bytes.toBytes(sd.data.key), sd.data.ts);
			reversePut.add(Bytes.toBytes(HBaseProxy.CF_ARTICLES),
					Bytes.toBytes(article.key), article.ts,
					Bytes.toBytes(sd.weight));

			if (article.summarizedText != null) {
				reversePut.add(Bytes.toBytes(HBaseProxy.CF_SUMARIZED_ARTICLES),
						Bytes.toBytes(article.key), article.ts,
						Bytes.toBytes(article.summarizedText));
			}
			put.add(Bytes.toBytes(HBaseProxy.CF_ARTICLES),
					Bytes.toBytes(sd.data.key), sd.data.ts,
					Bytes.toBytes(sd.weight));
			if (sd.data.summarizedText != null) {
				put.add(Bytes.toBytes(HBaseProxy.CF_SUMARIZED_ARTICLES),
						Bytes.toBytes(sd.data.key), sd.data.ts,
						Bytes.toBytes(sd.data.summarizedText));
			}
			i++;
		}

		if (i > 0)
			articles.put(put);

	}

	public void putTopProbWords(Article article, Query query, int limit)
			throws IOException {
		query.flush(limit);

		Put put = new Put(Bytes.toBytes(article.key), article.ts);
		int i = 0;
		Iterator<Sorter<NGram>> it = query.getQuery().iterator();

		while (it.hasNext() && i < limit) {
			Sorter<NGram> sd = it.next();

			put.add(Bytes.toBytes(HBaseProxy.CF_WORDS),
					Bytes.toBytes(sd.data.getNiceNGram()), article.ts,
					Bytes.toBytes(sd.weight));
			i++;
		}

		if (i > 0)
			articles.put(put);
	}

	public void putNGram(NGram word, int ts) throws IOException {
		putWord(word.getKey(), word.getNiceNGram(), ts);
	}

	public String lookupWord(String token, int min, int tolerance)
			throws IOException {
		String key = null;

		for (int i = token.length(); i > min; i--) {

			Scan scan = new Scan();

			scan.setStartRow(Bytes.toBytes(token.substring(0, i)));
			scan.setStopRow(Bytes.toBytes(token + 1));
			// scan.addFamily(Bytes.toBytes(CF_STEMS));
			scan.setFilter(new KeyOnlyFilter());
			ResultScanner scanner = words.getScanner(scan);

			// get first
			Result res = scanner.next();

			if (res != null && scanner.next() == null) {
				key = Bytes.toString(res.getRow());

				double tf = (double) i / token.length();
				double idf = (double) i / key.length();
				double tfidf = tf * idf;
				if (tfidf < 0.3) {
					// Logging.Log("xxx "+ token+"->"+key+": "+tfidf);
					return null;
				}
				if (tfidf != 1.0)
					Logging.Log(token + "->" + key + ": " + tfidf);
				//
				// int score = StandardTextProcessing.computeStringDistance(key,
				// token);
				// if(score>4)
				// {
				// Logging.Log( token+"->"+key+": "+score);
				//
				// }
				// if(score>tolerance)
				// key = null;

				return key;

			}

		}

		return key;
	}

	// public void computeSimilarTopics(long ts1,long ts2)
	// {
	//
	// }
	//
	// public void dumpSimilarTopics(long ts)
	// {
	//
	// }

	public void putWords(List<NGram> words, int k, long ts) throws IOException {

		for (int i = 0; i < words.size(); i++) {
			NGram word = words.get(i);

			Put put = new Put(Bytes.toBytes(word.getKey()), ts);
			put.add(Bytes.toBytes(HBaseProxy.CF_STEMS), Bytes.toBytes("nice"),
					ts, Bytes.toBytes(word.getNiceNGram()));
			put.add(Bytes.toBytes(HBaseProxy.CF_TOPICS),
					Bytes.toBytes("topic" + k), ts,
					Bytes.toBytes(word.getWeight()));

			this.words.put(put);
		}
	}

	public void saveTopicModel(DocumentStream ds) throws IOException {
		List<ArrayList<StringDoublePair>> topicsWordsDistribution = ds
				.getModel().getLda().getTopicWordsDistribution();
		int numTopics = ds.getModel().getLda().numTopics;
		// int numWords = ds.getModel().getLda().numTypes;
		long ts = ds.getTs();
		for (int k = 0; k < numTopics; k++) {
			Put topicPut = new Put(Bytes.toBytes("topic" + k), ts);

			ArrayList<StringDoublePair> topicsList = topicsWordsDistribution
					.get(k);
			for (StringDoublePair sd : topicsList) {
				topicPut.add(Bytes.toBytes(HBaseProxy.CF_TOPICS),
						Bytes.toBytes(sd.name), Bytes.toBytes(sd.value));
			}

			topics.put(topicPut);
		}
	}

	public void importRssFeed(RssFeed feed) {
		// TODO temporary to file
		try {

			// TODO get normalized URL

			Put put = new Put(Bytes.toBytes(feed.getKey()), feed.ts);

			put.add(Bytes.toBytes(HBaseProxy.CF_META), Bytes.toBytes("title"),
					Bytes.toBytes(feed.title));

			put.add(Bytes.toBytes(HBaseProxy.CF_META),
					Bytes.toBytes("prep_body"),
					Bytes.toBytes(feed.preprocessedBody));
			put.add(Bytes.toBytes(HBaseProxy.CF_META), Bytes.toBytes("body"),
					Bytes.toBytes(feed.originalBody));
			put.add(Bytes.toBytes(HBaseProxy.CF_META),
					Bytes.toBytes("article_id"), Bytes.toBytes(feed.articleId));

			put.add(Bytes.toBytes(HBaseProxy.CF_META), Bytes.toBytes("link"),
					Bytes.toBytes(feed.link));

			articles.put(put);

			// for(Keyword keyword: feed.keywords)
			// {
			// put = new
			// Put(Bytes.toBytes(keyword.getNormalizedKeyword()),feed.ts);
			// put.add(Bytes.toBytes("info"), Bytes.toBytes("name"),
			// Bytes.toBytes(keyword.name));
			// put.add(Bytes.toBytes("info"), Bytes.toBytes("escaped_name"),
			// Bytes.toBytes(keyword.escapedName));
			//
			// put.add(Bytes.toBytes("a"), Bytes.toBytes(feed.getKey()),
			// Bytes.toBytes(keyword.getScore()));
			//
			// words.put(put);
			// }
			//

			Logging.Log("imported to hbase");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
