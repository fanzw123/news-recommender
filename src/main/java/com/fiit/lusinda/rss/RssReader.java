package com.fiit.lusinda.rss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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

import org.jruby.ast.EnsureNode;

import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.entities.TsChangeListener;
import com.fiit.lusinda.entities.TsObject;
import com.fiit.lusinda.exceptions.ParserException;
import com.fiit.lusinda.hbase.HBaseImport;
import com.fiit.lusinda.utils.ContentWriter;
import com.fiit.lusinda.utils.CsvWriter;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

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

	public void start() {

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
		boolean ok = false;
		String url_sme = "http://rss.sme.sk/rss/rss.asp?sek=smeonline";
		String url_pravda_sport = "http://servis.pravda.sk/rss.asp?o=sk_sport";
		String url_nyt = "http://feeds.nytimes.com/nyt/rss/Europe";
		int maxFeeds = 10;
		long interval = -1;
		try {

			InputStream settingsIn = null;
			boolean nyt = true;

			if (args.length>0 && args[0] != null)
				settingsIn = new FileInputStream(args[0]);
			else
				settingsIn = RssReader.class
						.getResourceAsStream("/feed-settings-en.xml");

			FeedSettings settings = FeedSettings.read(settingsIn);

			RssReader reader = new RssReader(settings);
			reader.start();

		} catch (Exception ex) {
			System.out.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
		}

	}

}

enum FeedState {
	NEW, PROCESSED, SKIPED, DELETED
}

class RssFetcherTimerTask extends TimerTask implements TsChangeListener {

	FeedFetcher fetcher;
	Map<String, FeedState> feedCache;
	// RssParser rssParser;
	TsObject tsObject;
	String outputDirectory;
	String seqDirectory;
	String contentDirectory;
	String localFeedCachePath;
	FeedSettings feedSettings;
	HBaseImport himport;

	// CsvWriter csvWriter;
	ContentWriter contentWriter;

	public RssFetcherTimerTask(FeedSettings settings) throws IOException {

		this.feedSettings = settings;
		// feedUrl = new URL(settings.feedEntries.get(0).url);

		fetcher = new HttpURLFeedFetcher();
		// this.rssParser = new RssParser(settings.feedEntries.get(0).lang);

		this.outputDirectory = settings.outputDirectory;
		this.seqDirectory = outputDirectory + "seq/";
		this.contentDirectory = outputDirectory + "content/";
		this.localFeedCachePath = outputDirectory + "feedCache";

		this.tsObject = new TsObject(this);
		tsObject.maxFeeds = settings.ts_maxFeeds;
		tsObject.ts_interval = settings.ts_interval;
		tsObject.start();
		
		feedCache = deserializeFeedCache();

		himport = new HBaseImport();

		ensureExistsDirs();

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

			FeedState state = feedCache.get(cachedLink);

			if (state.equals(FeedState.PROCESSED))
				writer.writeLine(cachedLink);
		}
		
		writer.finish();

	}

	private Map<String, FeedState> deserializeFeedCache() {

		Map<String, FeedState> cache = new HashMap<String, FeedState>();

		try {
			File f = new File(localFeedCachePath);
			InputStream is = (InputStream) new FileInputStream(f);
			String encoding = "UTF-8";
			InputStreamReader isw = new InputStreamReader(is, encoding);
			BufferedReader br = new BufferedReader(isw);

			String strLine = "";
			StringTokenizer st = null;

			String separator = ";";

			// read comma separated file line by line
			while ((strLine = br.readLine()) != null) {

				st = new StringTokenizer(strLine, separator);

				cache.put(st.nextToken(), FeedState.PROCESSED);

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

		java.util.Date now = new java.util.Date();
		Logging.Log("Processing starts at: " + now.toString());

		// cleanUpFeedCache();

		for (FeedEntry feedEntry : feedSettings.feedEntries) {

			try {

				currFeedSource++;

				feedUrl = new URL(feedEntry.url);
				rssParser = new RssParser(feedEntry.lang);
				feed = fetcher.retrieveFeed(feedUrl);

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
						feedCache.put(link, FeedState.NEW);
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
						if (FeedState.NEW.equals(feedCache.get(link))) {


							parsedFeed = rssParser.parseFeed(entry); 
							
								parsedFeed.ts = tsObject.getCurrTs();
								parsedFeed.lang = feedEntry.lang;
								parsedFeed.category = feedEntry.category;
								parsedFeed.site = feedEntry.site;

								
								 himport.importRssFeed(parsedFeed);

								// saveFeed(csvWriter, parsedFeed);

								contentWriter
										.WriteContent(String.valueOf(tsObject
												.getCurrFeeds()),
												parsedFeed.preprocessedBody);

								feedCache.put(link, FeedState.PROCESSED);
								tsObject.increment();


								Logging.Log("PROCESSED: "
										+ tsObject.getCurrTs() + "/"
										+ tsObject.getCurrFeeds() + ", " + link);
							
						}
						
					}
					catch(ParserException e)
					{
						Logging.Log("ERROR:" +e.getMessage());
						feedCache.put(entry.getLink(), FeedState.SKIPED);

						
					}
					catch (Exception e) {
						Logging.Log("ERROR: " + tsObject.getCurrTs()
								+ "/" + tsObject.getCurrFeeds() + ", "
								+ entry.getLink());
						e.printStackTrace();
						feedCache.put(entry.getLink(), FeedState.SKIPED);
						
					}
				}

				// uz by mali byt vsetky feed bud delete alebo processed
				if (tsObject.getCurrFeeds() > 0)
					serializeFeedCache();

			} catch (Exception e) {
				Logging.Log(e.getMessage());

			}
		}

	}

	private void saveFeed(CsvWriter csvWriter, RssFeed feed) throws IOException {
		csvWriter.writeLine(feed.link, feed.title, String.valueOf(feed.ts));

	}

	public void onChange() {

		// if (csvWriter != null)
		// csvWriter.finish();
		// csvWriter = CsvWriter
		// .create(seqDirectory + currTs, ";", "UTF8");

		contentWriter = ContentWriter.create(
				contentDirectory + tsObject.getCurrTs(), "UTF8");
	}

	 
	public void onStart() {
		contentWriter = ContentWriter.create(
				contentDirectory + tsObject.getCurrTs(), "UTF8");
	}

	public void onEnd() {

	}

}
