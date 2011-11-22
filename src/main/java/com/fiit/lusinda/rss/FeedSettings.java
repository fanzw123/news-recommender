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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.entities.RssFeedCategory;

public class FeedSettings {

	public int timer_interval;
	public int ts_interval;
	public int ts_maxFeeds;
	public String outputDirectory;
	List<FeedEntry> feedEntries = new ArrayList<FeedEntry>();

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
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

	// TODO sort feed input XML by counts(_)
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

				settings.feedEntries.add(entry);

			}
		}

		return settings;

	}

}

class FeedEntry {
	public String url;
	public Lang lang;
	public String category = RssFeedCategory.UNKNOWN;
	public String site;
//	public String encoding="UTF-8";
}
