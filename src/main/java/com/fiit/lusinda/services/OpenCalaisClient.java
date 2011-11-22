package com.fiit.lusinda.services;

import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisException;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordFactory;
import com.fiit.lusinda.services.MetallClient.MetallClientException;
import com.fiit.lusinda.utils.Logging;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OpenCalaisClient {

	private static String APIKEY = "zsrquwtanfyrabvvkxk6kmen";
	private static int timeout = 20000;
	private static int maxAttempts = 10;

	public List<Keyword> getKeywords(String content,
			KeywordFactory keywordFactory) throws IOException,
			InterruptedException {

		CalaisClient calais = new CalaisRestClient(OpenCalaisClient.APIKEY);
		CalaisResponse response = null;
		int i = 0;
		while (i < maxAttempts)
			try {
				response = calais.analyze(content);
				break;
			} catch (Exception e) {
				Thread.sleep(timeout);
				i++;
				Logging.Log("openCalais exception occured, waiting..."
						+ e.getMessage());
			}

		if (response != null)
			return processKeywords(response, keywordFactory);
		else
			return null;
	}

	private List<Keyword> processKeywords(CalaisResponse response,
			KeywordFactory keywordFactory) throws IOException {
		List<Keyword> keywordArray = new ArrayList<Keyword>();

		for (CalaisObject entity : response.getEntities()) {
			Keyword keyword = keywordFactory.getKeyword(
					entity.getField("name"), entity.getField("_type"));
			if (keyword != null)
			{
				keyword.score = Double.parseDouble(entity.getField("relevance"));
				keywordArray.add(keyword);
			}
		}

		return keywordArray;

	}

}