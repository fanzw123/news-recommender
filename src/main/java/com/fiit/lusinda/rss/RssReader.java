package com.fiit.lusinda.rss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


import cc.mallet.types.InstanceList;

import com.fiit.lusinda.clustering.Dataset;
import com.fiit.lusinda.clustering.Hac;
import com.fiit.lusinda.entities.DateTimeTickStrategy;
import com.fiit.lusinda.entities.FeedInfo;
import com.fiit.lusinda.entities.FeedState;
import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.NumberTickStrategy;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.entities.StreamTopicModel;
import com.fiit.lusinda.entities.StreamTopicModelInfo;
import com.fiit.lusinda.entities.TickStrategy;
import com.fiit.lusinda.entities.TsChangeListener;
import com.fiit.lusinda.entities.TsObject;
import com.fiit.lusinda.exceptions.ParserException;
import com.fiit.lusinda.hbase.HBaseProxyManager;

import com.fiit.lusinda.topicmodelling.LdaProperties;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.fiit.lusinda.utils.ContentWriter;
import com.fiit.lusinda.utils.CsvWriter;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssReader {

	private final Timer timer = new Timer();

	// private final int minutes;
	// private String url;
	// private Lang lang;
	// private long interval;
	// private int maxFeeds;
	private FeedSettings settings;

	public RssReader(FeedSettings settings) throws MalformedURLException {
		this.settings = settings;
		// this.minutes = minutes;
		// this.url = url;
		// this.lang = lang;
		// this.interval = interval;
		// this.maxFeeds = maxFeeds;

	}

	public void start() throws Exception {

		RssFetcherTimerTask task = null;
		try {
			task = new RssFetcherTimerTask(settings);

			if (settings.timer_interval > 0) {
				Logging.Log("Rss Fetch task run every: "
						+ String.valueOf(settings.timer_interval) + "minute");
				timer.schedule(task, 0, settings.timer_interval * 60 * 1000);
			} else {
				Logging.Log("Rss fetch running once");
				task.run();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			timer.cancel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			timer.cancel();

		}

	}

	public static void main(String[] args) {
//		boolean ok = false;
//		String url_sme = "http://rss.sme.sk/rss/rss.asp?sek=smeonline";
//		String url_pravda_sport = "http://servis.pravda.sk/rss.asp?o=sk_sport";
//		String url_nyt = "http://feeds.nytimes.com/nyt/rss/Europe";
//		int maxFeeds = 10;
//		long interval = -1;
		try {

			InputStream settingsIn = null;
			FeedSettings settings =null;
			if (args.length>0 && args[0] != null)
			{
				settingsIn = new FileInputStream(args[0]);
				settings = FeedSettings.read(settingsIn);
			}
//			else
//				settingsIn = RssReader.class
//						.getResourceAsStream("/feed-settings-default.xml");

			
			if(settings==null)
				settings = FeedSettings.getSmeExperimentFeedSettings(7);
			
			
			RssReader reader = new RssReader(settings);
			reader.start();

		} catch (Exception ex) {
			System.out.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
		}

	}

}



class RssFetcherTimerTask extends TimerTask implements TsChangeListener {

	FeedFetcher fetcher;
	Map<String, FeedInfo> feedCache;
	// RssParser rssParser;
	TsObject tsObject;
	String outputDirectory;
	String seqDirectory;
	String contentDirectory;
	String ldaDirectory;
	String localFeedCachePath;
	FeedSettings feedSettings;
	

	// CsvWriter csvWriter;
	ContentWriter contentWriter;
	
	ContentWriter originalContentWriter;
	
	//lda

	StreamTopicModel topicModel;




	public RssFetcherTimerTask(FeedSettings settings) throws Exception {

		this.feedSettings = settings;
		// feedUrl = new URL(settings.feedEntries.get(0).url);

		fetcher = new HttpURLFeedFetcher();
		// this.rssParser = new RssParser(settings.feedEntries.get(0).lang);

		this.outputDirectory = settings.outputDirectory;
		this.seqDirectory = outputDirectory + "seq/";
		this.contentDirectory = outputDirectory + "content/";
		this.localFeedCachePath = outputDirectory + "feedCache";
		this.ldaDirectory = outputDirectory + "lda/";
	
//		StreamTopicModelInfo info = new StreamTopicModelInfo(feedSettings,ldaDirectory,
//				settings.window_length,30,1000,15);
				
		topicModel = new StreamTopicModel(ldaDirectory,feedSettings);
	
	//	TickStrategy strategy = settings.localStore?new NumberTickStrategy():new DateTimeTickStrategy();
		
		TickStrategy strategy =  new DateTimeTickStrategy();
				
		this.tsObject = new TsObject(this,strategy);
		tsObject.maxFeeds = settings.ts_maxFeeds;
		tsObject.ts_interval = settings.ts_interval;
		tsObject.start();
		
		topicModel.createStream(tsObject.getCurrTs());
	//	if(settings.localStore)
			topicModel.loadLdaModels();
		
		feedCache = deserializeFeedCache();

	

		//ensureExistsDirs();

	}
	
	

	private void ensureExistsDirs() {
		File f = new File(seqDirectory);
		if (!f.exists())
			f.mkdirs();
		f = new File(contentDirectory);
		if (!f.exists())
			f.mkdirs();
	}

	// TODO add rsssources abstrakciu
	// public void Add

	public void run() {

		fetchFeeds();

	}

	private void cleanUpFeedCache() {
		List<String> toRemove = new ArrayList<String>();

		for (String cachedLink : feedCache.keySet())
			if (feedCache.get(cachedLink).equals(FeedState.DELETED))
				toRemove.add(cachedLink);

		for (String linkToRemove : toRemove)
			feedCache.remove(linkToRemove);
	}

	private void serializeFeedCache() throws IOException {
		CsvWriter writer = CsvWriter.create(localFeedCachePath, ";", "UTF8",false);

		for (String cachedLink : feedCache.keySet()) {

			FeedInfo info = feedCache.get(cachedLink);

			if (info.state.equals(FeedState.PROCESSED))
				writer.writeLine(cachedLink,Long.toString(info.ts));
		}
		
		writer.finish();

	}
	
	

	private Map<String, FeedInfo> deserializeFeedCache() {

		Map<String, FeedInfo> cache = new HashMap<String, FeedInfo>();

		try {
			File f = new File(localFeedCachePath);
			InputStream is = (InputStream) new FileInputStream(f);
			String encoding = "UTF-8";
			InputStreamReader isw = new InputStreamReader(is, encoding);
			BufferedReader br = new BufferedReader(isw);

			String strLine = "";
			StringTokenizer st = null;

			String separator = ";";
			FeedInfo info = null;
			String link=null;
			// read comma separated file line by line
			while ((strLine = br.readLine()) != null) {

				try
				{
				st = new StringTokenizer(strLine, separator);

				link = st.nextToken();
				
				info = new FeedInfo();
				info.state = FeedState.PROCESSED;
				info.ts = Long.parseLong(st.nextToken());
				cache.put(link, info);
				}
				catch(Exception ex)
				{
					Logging.Log("feed could not be deserialized");

				}

			}
		} catch (IOException e) {
			Logging.Log("feed cache has not been serialized yet");
		}
		return cache;

	}

	private void fetchFeeds() {

		
		SyndFeed feed;
		RssFeed parsedFeed;
		URL feedUrl;
		RssParser rssParser;
		int currFeedSource = 0;
		FeedInfo feedInfo=null;
		
		java.util.Date now = new java.util.Date();
		Logging.Log("Processing starts at: " + now.toString());

		// cleanUpFeedCache();

		for (FeedEntry feedEntry : feedSettings.feedEntries) {

			try {

				currFeedSource++;

				feedUrl = new URL(feedEntry.url);
				if("http".equals(feedUrl.getProtocol()))
				{
						feed = fetcher.retrieveFeed(feedUrl);
						rssParser = new RssParser(feedEntry.lang);

				}
				else
				{
					SyndFeedInput input = new SyndFeedInput();
					rssParser = new RssFileParser(feedEntry.lang);
					String path = feedUrl.getAuthority()+feedUrl.getFile();
					feed = input.build(new XmlReader(new File(path)));
				}

				Logging.Log("Processing feed source (" + currFeedSource + "\\"
						+ feedSettings.feedEntries.size() + ") "
						+ feedEntry.url);

				Iterator it = feed.getEntries().iterator();
				List<String> links = new ArrayList<String>(feed.getEntries()
						.size());

				while (it.hasNext()) {
					SyndEntry entry = (SyndEntry) it.next();

					links.add(entry.getLink());

				}

				// // pozreme, ktore linky uz nie su v aktualnom rss streame,
				// // oznacime
				// // ich ako delete
				// for (String cachedLink : feedCache.keySet()) {
				// if (feedCache.get(cachedLink).equals(FeedState.DELETED))
				// continue;
				//
				// // ak current rss stream neobsahuje kesovanu linku, tak ju
				// // vymaz
				// // z kese
				// if (!links.contains(cachedLink)) {
				// feedCache.put(cachedLink, FeedState.DELETED);
				// Logging.Log("DELETED: " + cachedLink);
				// }
				// }

				// do kese pridame vsetky nove linky + ich spracujeme
				for (String link : links) {
					if (feedCache.get(link) == null) {
						feedInfo = new FeedInfo();
						feedInfo.state = FeedState.NEW;
						feedInfo.ts = tsObject.getCurrTs();
						
						feedCache.put(link, feedInfo);
						Logging.Log("NEW: " + link);

					}
				}

				it = feed.getEntries().iterator();

				// cache mame zosynchronizovanu, takze vsetko co je v kesi tak
				// spracujem

				while (it.hasNext()) {
					SyndEntry entry = (SyndEntry) it.next();

					try {
						String link = entry.getLink();
						FeedInfo currInfo =  feedCache.get(link);
						if (FeedState.NEW.equals(currInfo.state)) {


							parsedFeed = rssParser.parseFeed(entry,feedEntry.translate); 
							
								parsedFeed.ts =currInfo.ts;
								parsedFeed.lang = feedEntry.lang;
							//	parsedFeed.category = feedEntry.category;
								parsedFeed.site = feedEntry.site;
							parsedFeed.articleId = parsedFeed.getArticleId();
								
								if(feedSettings.hbaseImport)
									HBaseProxyManager.getProxy().importRssFeed(parsedFeed);
								 
								topicModel.addDocToStream(parsedFeed.getKey(),parsedFeed.preprocessedBody,parsedFeed.getNormalizedTitle());
								
							

								// saveFeed(csvWriter, parsedFeed);


//								seqFileWriter.write(parsedFeed.getNormalizedTitle(), parsedFeed.preprocessedBody);
//								contentWriter
//										.WriteContent(parsedFeed.getNormalizedTitle(),  //String.valueOf(tsObject.getCurrFeeds()),
//												parsedFeed.preprocessedBody);
//
//								originalContentWriter.WriteContent(String.valueOf(tsObject.getCurrFeeds()),
//										parsedFeed.originalBody);
//
//								
								currInfo.state =  FeedState.PROCESSED;
								feedCache.put(link,currInfo);
								tsObject.increment();


								Logging.Log("PROCESSED: "
										+ currInfo.ts + "/"
										+ tsObject.getCurrFeeds() + ", " + link);
							
						}
						
					}
					catch(ParserException e)
					{
						Logging.Log("ERROR:" +e.getMessage());
						

						
					}
					catch (Exception e) {
						Logging.Log("ERROR: " + tsObject.getCurrTs()
								+ "/" + tsObject.getCurrFeeds() + ", "
								+ entry.getLink());
						e.printStackTrace();
						FeedInfo info = new FeedInfo();
						info.state = FeedState.SKIPED;
						feedCache.put(entry.getLink(), info);
						
					}
				}

				// uz by mali byt vsetky feed bud delete alebo processed
				
				//vypnutie kese
				//if (tsObject.getCurrFeeds() > 0)
					serializeFeedCache();

			} catch (Exception e) {
				Logging.Log(e.getMessage());

			}
		}

	}

//	private void saveFeed(CsvWriter csvWriter, RssFeed feed) throws IOException {
//		csvWriter.writeLine(feed.link, feed.title, String.valueOf(feed.ts));
//
//	}

	public void onTick() {
	
	
		//business logic in main loop
	}
	
	public void onChange() {

	//	topicModel.getCurrentStream().processStreamData();
		
		try {
			topicModel.processStreamData();
		} catch (Exception e) {
			Logging.Log("error processing stream data.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		
		
		topicModel.createStream(tsObject.getCurrTs());
		
		// if (csvWriter != null)
		// csvWriter.finish();
		// csvWriter = CsvWriter
		// .create(seqDirectory + currTs, ";", "UTF8");

		//seqFileWriter.finish();
		//seqFileWriter = SequenceFileWriter.create(contentDirectory+tsObject.getCurrTs()+"-seq");

		
//		contentWriter = ContentWriter.create(
//				contentDirectory + tsObject.getCurrTs(), "UTF8");
//		
//		originalContentWriter = ContentWriter.create(
//				contentDirectory + tsObject.getCurrTs()+"-original", "UTF8");
//		
		
					
	}

	 
	public void onStart()  {
//		contentWriter = ContentWriter.create(
//				contentDirectory + tsObject.getCurrTs()+"-new", "UTF8");
//		
//		originalContentWriter = ContentWriter.create(
//				contentDirectory + tsObject.getCurrTs()+"-original", "UTF8");
//		
//		seqFileWriter = SequenceFileWriter.create(contentDirectory+tsObject.getCurrTs()+"-seq");
	}

	public void onEnd() {

	}

	
}
