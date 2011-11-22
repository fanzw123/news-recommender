package com.fiit.lusinda.services;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.fiit.lusinda.utils.Logging;

public class AlchemyClient {

	private static String APIKEY = "67e9ac6736c90d70e7de67cca516ae6e52379fb5";
	private static int timeout = 20000;
	private static int maxAttempts = 10;
	
	private  XPath xpath;
	private  XPathExpression expr;

	AlchemyAPI api;

	public AlchemyClient()  {
		api = AlchemyAPI.GetInstanceFromString(APIKEY);
		xpath = XPathFactory.newInstance().newXPath();
		 try {
			expr = xpath.compile("//text");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public String cleartext(String url) throws XPathExpressionException,
			IOException, SAXException, ParserConfigurationException,
			InterruptedException {
		Document doc = null;
		int i = 0;
		String clearText = null;
		
		while (i < maxAttempts)
			try {
				doc = api.URLGetText(url);
				break;
			} catch (Exception e) {
				Thread.sleep(timeout);
				i++;
				Logging.Log("alchemy exception occured, waiting..."+e.getMessage());
			}

		if (doc != null) {

			
			Object res = expr.evaluate(doc, XPathConstants.NODE);
			Node node = (Node) res;
			if(node!=null)
				clearText = node.getFirstChild().getNodeValue();
		}
		
		
		return clearText;
	}
}
