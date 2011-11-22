package com.fiit.lusinda.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fiit.lusinda.entities.ExtractionSource;
import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordFactory;
import com.fiit.lusinda.utils.Logging;

public class MetallClient {
	private static final String METALL_BASE_URL = "http://peweproxy.fiit.stuba.sk/metall";

	private int maxAtemps = 2;
	private int timeout = 100000;

	public class MetallClientException extends Exception {

		public MetallClientException(String message) {
			super(message);
		}

		public MetallClientException(Exception e) {
			super(e);
		}
	}

	public String cleartext(String url) throws MetallClientException {

		PostMethod post = createPostMethod("readability");

		NameValuePair[] data = { new NameValuePair("url", url) };
		post.setRequestBody(data);

		return getResult(post);

	}

	private PostMethod createPostMethod(String methodName) {
		PostMethod post = new PostMethod(METALL_BASE_URL + "/" + methodName);// "/meta");
		post.setRequestHeader("Content-Type",
				PostMethod.FORM_URL_ENCODED_CONTENT_TYPE + "; charset=utf-8");
		post.setRequestHeader("Accept-Charset", "utf-8");

		return post;
	}

	private String getResult(PostMethod post) throws MetallClientException
			 {
		HttpClient client = new HttpClient();

		int i = 0;
		while (i < maxAtemps) {
			try {
				int resp = client.executeMethod(post);
				if (resp == 200) {
					return post.getResponseBodyAsString();
				} else {
					throw new MetallClientException(
							post.getResponseBodyAsString());
				}
			} catch (Exception e) {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e1) {
					throw new MetallClientException(e1.getMessage());
				}
				i++;
				Logging.Log("metall exception occured, waiting...");
			}
		}
		
		return null;

	}

	public List<Keyword> getKeywords(String content,
			KeywordFactory keywordFactory) throws MetallClientException,
			IOException {

		PostMethod post = createPostMethod("meta");

		NameValuePair[] data = { new NameValuePair("content", content) };
		post.setRequestBody(data);

		String result = getResult(post);

		return processKeywords(result, keywordFactory);

	}

	private List<Keyword> processKeywords(String extractedKeywords,
			KeywordFactory keywordFactory) throws IOException {
		List<Keyword> keywordArray = new ArrayList<Keyword>();

		try {

			JSONArray jsonArray = new JSONArray(extractedKeywords);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				Keyword keyword = keywordFactory.getKeyword(
						json.getString("name"), json.getString("type"));
				if (keyword != null)
					keywordArray.add(keyword);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return keywordArray;

	}

	public static void main(String[] args) {
		MetallClient client = new MetallClient();

	}

}
