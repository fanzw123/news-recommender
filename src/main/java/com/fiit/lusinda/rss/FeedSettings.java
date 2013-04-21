package com.fiit.lusinda.rss;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.RssFeedCategory;
import com.fiit.lusinda.utils.Logging;

public class FeedSettings {

	public int timer_interval;
	public int window_length;
	public boolean hbaseImport;
	public int ts_interval;
	public int ts_maxFeeds;
	public String outputDirectory;
	public String settingsPath;
	public String feedEntries_location;
	public int feedEntries_max;
	
	public int maxRecommendations;
	public int queryLimit;
	public int summarizedSentencesLimit;
	public int maxWordsPerTopic;
	public int topics = 70;
	public int averageNumRuns = 1;
	public boolean evaluate = false;
	public boolean estimateTopicCountsUsingHLDA = true; 
	public boolean onlyUpperKeywords= false;
	public boolean experimental = true;
	public boolean summarize = false;
//	public boolean localStore= false; 
	
	
	List<FeedEntry> feedEntries = new ArrayList<FeedEntry>();

	private static String getTagValue(String sTag, Element eElement) {
		try
		{
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		
		
		Node nValue = (Node) nlList.item(0);
		
		

		return nValue.getNodeValue();
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}

	private static List<FeedEntry> readNytRssFeeds()
			throws XPathExpressionException {
		List<FeedEntry> result = new ArrayList<FeedEntry>();

		InputStream in = RssReader.class.getResourceAsStream("/nyt.xml");

		InputSource inputSource = new InputSource(in);
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath
				.compile("//outline[@xmlUrl and not(contains(@title,'Job'))]/@xmlUrl");
		Object res = expr.evaluate(inputSource, XPathConstants.NODESET);
		NodeList list = (NodeList) res;
		FeedEntry entry;

		for (int i = 0; i < list.getLength(); i++) {
			entry = new FeedEntry();
			entry.lang = Lang.ENGLISH;
			entry.url = list.item(i).getNodeValue();
			result.add(entry);
		}
		return result;
	}

	private static InputStream getSettingsAsStream(String settingsPath) {

		InputStream stream = null;
				
		try {
			stream = new FileInputStream(settingsPath);
		} catch (FileNotFoundException e) {
			
			Logging.Log("could not find "+settingsPath+" using default settings.xml on classPath");
			stream = RssReader.class
					.getResourceAsStream(settingsPath);
		}
		return stream;
		
	}
	
	public void reload() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
	{
		this.feedEntries = read(this.settingsPath).feedEntries;
	}

	
	public static FeedSettings getSmeExperimentFeedSettings(int max)
	{
		FeedSettings settings = new FeedSettings();
		settings.timer_interval = 1;
		
		
		settings.ts_interval = -1;
		settings.window_length = -1;
		
		
		settings.feedEntries_location="/master/sme_rss/zahranicie/";
		settings.feedEntries_max= max;
		settings.hbaseImport = false;
		settings.maxRecommendations=10;
		settings.queryLimit=20;
		settings.summarizedSentencesLimit = 4;
		settings.maxWordsPerTopic=1000;
		settings.topics = 70;
		settings.averageNumRuns = 1;
		settings.evaluate = false;
		settings.estimateTopicCountsUsingHLDA = true; 
		settings.onlyUpperKeywords = false;
		settings.experimental = false;
		settings.summarize = false;
		//settings.localStore = true;
		
		settings.ts_maxFeeds = 300;
		settings.outputDirectory = "/var/lusinda/solr/rss/itsrc/";

		readFeedEntries(settings);

		return settings;
	}
	
	public static void readFeedEntries(FeedSettings settings)
	{
		String protocol = "file://";
		for(int i=1;i<=settings.feedEntries_max;i++)
		{
			FeedEntry entry = new FeedEntry();
			entry.url =protocol+ settings.feedEntries_location+"/"+i+".xml";
			entry.lang = Lang.SLOVAK;
			entry.category = RssFeedCategory.UNKNOWN;
			entry.site = "sme";
			
			entry.translate = false;

			settings.feedEntries.add(entry);
		}
	}
	
	public static FeedSettings read(InputStream in)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		FeedSettings settings = new FeedSettings();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(in);
		doc.getDocumentElement().normalize();

		Element el = (Element) doc.getElementsByTagName("common").item(0);

		settings.timer_interval = Integer.parseInt(getTagValue(
				"timer-interval", el));
		settings.ts_interval = Integer.parseInt(getTagValue("ts-interval", el));

		settings.ts_maxFeeds = Integer.parseInt(getTagValue("ts-maxFeeds", el));
		settings.outputDirectory = getTagValue("outputDirectory", el);

		settings.feedEntries_location=getTagValue("feedEntries-location", el);
		settings.feedEntries_max= Integer.parseInt(getTagValue("feedEntries-max", el));
		settings.hbaseImport = Boolean.parseBoolean(getTagValue("hbaseImport", el));
		settings.maxRecommendations=Integer.parseInt(getTagValue("maxRecommendations", el));
		settings.queryLimit=Integer.parseInt(getTagValue("queryLimit", el));
		settings.summarizedSentencesLimit=Integer.parseInt(getTagValue("summarizedSentencesLimit", el));
		settings.maxWordsPerTopic=Integer.parseInt(getTagValue("maxWordsPerTopic", el));
		settings.topics = Integer.parseInt(getTagValue("topics", el));
		settings.averageNumRuns = Integer.parseInt(getTagValue("averageNumRuns", el));
		settings.evaluate = Boolean.parseBoolean(getTagValue("evaluate", el));
		settings.estimateTopicCountsUsingHLDA = Boolean.parseBoolean(getTagValue("estimateTopicCountsUsingHLDA", el));
		settings.onlyUpperKeywords = Boolean.parseBoolean(getTagValue("onlyUpperKeywords", el));
		settings.experimental = Boolean.parseBoolean(getTagValue("experimental", el));
		settings.summarize = Boolean.parseBoolean(getTagValue("summarize", el));
		//settings.localStore = Boolean.parseBoolean(getTagValue("localStore", el));
		
		
		
		if(StringUtils.isBlank(settings.feedEntries_location))
		{

		NodeList nList = doc.getElementsByTagName("feed");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				FeedEntry entry = new FeedEntry();
				entry.url = getTagValue("url", eElement);
				entry.lang = Lang.valueOf(getTagValue("lang", eElement));
				entry.category = RssFeedCategory.resolveCategory(getTagValue(
						"category", eElement));
				entry.site = getTagValue("site", eElement);
				String value = getTagValue("language", eElement); 
				entry.translate = value==null?false:Boolean.getBoolean(value);

				settings.feedEntries.add(entry);

			}
		}
		}
		else
			readFeedEntries(settings);

		return settings;

	}

	// TODO sort feed input XML by counts(_)
	public static FeedSettings read(String settingsPath)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {

		FeedSettings settings = read(getSettingsAsStream(settingsPath));
		settings.settingsPath = settingsPath;

		return settings;

	}
}

class FeedEntry {
	public String url;
	public Lang lang;
	public String category = RssFeedCategory.UNKNOWN;
	public String site;
	
	public boolean translate = true;
	// public String encoding="UTF-8";
}
