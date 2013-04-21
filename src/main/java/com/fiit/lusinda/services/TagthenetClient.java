package com.fiit.lusinda.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordFactory;
import com.fiit.lusinda.entities.Lang;
import com.fiit.lusinda.hbase.HBaseProxyManager;
import com.fiit.lusinda.mallet.CharSequence2TokenSequencePreserveOriginal;
import com.fiit.lusinda.mallet.TokenSequenceLemmatize;
import com.fiit.lusinda.mallet.TokenSequenceToString;
import com.fiit.lusinda.services.MetallClient.MetallClientException;
import com.fiit.lusinda.textprocessing.SlovakTextProcessing;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.fiit.lusinda.utils.Logging;
import com.google.common.collect.Lists;

public class TagthenetClient {

	public String BASE_URL = "http://tagthe.net/api/";
	int maxAtemps = 3;
	private int timeout = 2000;
	
	private PostMethod createPostMethod() {
		PostMethod post = new PostMethod(BASE_URL);// "/meta");
		post.setRequestHeader("Content-Type",
				PostMethod.FORM_URL_ENCODED_CONTENT_TYPE + "; charset=utf-8");
		post.setRequestHeader("Accept-Charset", "utf-8");

		return post;
	}

	private String getResult(PostMethod post) throws Exception
			 {
		HttpClient client = new HttpClient();

		int i = 0;
		while (i < maxAtemps) {
			try {
				int resp = client.executeMethod(post);
				if (resp == 200) {
					return post.getResponseBodyAsString();
				} else {
					throw new Exception(
							post.getResponseBodyAsString());
				}
			} catch (Exception e) {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e1) {
					
				}
				i++;
				Logging.Log("metall exception occured, waiting...");
			}
		}
		
		return null;

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

	

	public List<String> getTags(String text) throws Exception {
		
		
		
		
		PostMethod post = createPostMethod();

		NameValuePair[] data = { new NameValuePair("text", text),new NameValuePair("view", "json") };
		post.setRequestBody(data);
		StringBuilder topicsString =new StringBuilder();
		
		int time = 0;
		
		String result = getResult(post);
		 
		
		try {
			
			JSONObject json = new JSONObject(result);
			JSONObject m =(JSONObject)json.getJSONArray("memes").get(0);
			JSONObject d = (JSONObject) m.get("dimensions");
			
			

			

		
			
			JSONArray jsonArray =d.getJSONArray("topic"); 
			
		
			
			for (int i = 0; i < jsonArray.length(); i++) {
				{
					
					String kw = (String) jsonArray.get(i);
					if(text.contains(" ".concat(kw).concat(" ")))
						topicsString.append(kw).append(",");
				}
				
				
				//	topics.add(o);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		String translated = StandardTextProcessing.translateBack(topicsString.toString());
		
		String[] splited = translated.split(",");
		
		return Lists.newArrayList(splited);
	}

	
	
	
	
	public static void main(String[] args) throws Exception {
		TagthenetClient client = new TagthenetClient();
		String originalText = "farba jeseň cestička medza štrbským popradským letecký pohľad popradské pleso úpätie ostrva chata-morgána chata podhľad egyptský obloha vzťah personál vysoko položenej chata občasný príchod otravný vegetarián zrejme výška prestávať ohŕňať guláš párok vyjadrovať tento plagátik tento strom vybrať symbióza život sedieť balvan objímať mohutný koreniť nijaký smršť premôcť možno preháňať tento kamzík nazývať črieda podozrivo krotký zblízka zvyknuté častý prítomnosť človek porovnaní hranica výskyt tráva posunúť vysoko ďalší dôkaz globálny otepľovania mesačný krajina smokovcom pováľané obhorené smrek padnúť všetok sem-tam odolať smrekovec listnáč detail vyzerať víchrica váľať strom koreniť vyrábať cigánsky turistický značka orientovať ťažko tuto-hľa zachovať tatranskou lesný priliehavý názov napríklad tatranská púštna nezvykle odhalené mnohý miesto bývať ukryté orientovať ťahšie pracovať odvážaní kalamitný drevo pracovať roky-rokúce autor jahňací povedať prechod téryho chata zbojnícky priečne sedlo náročný skutočnosť najťažsia značkovaná trasa vysoký tatra bohužiaľ zostať nezdokumentovaná obava globálny otepľovania divoký výstavba znižovania stupeň ochrana príroda povoľovania výnimka hroziacej premena krásny tatra disneyland sliezskom hrebienok vyskytovať podozrivo začiatok asfaltka vedúca sliezsky tatranskou polianka zmiznúť rampa možno navždy";
		String text = StandardTextProcessing.preprocessUsingMalletPipes(originalText);
		
		
		
		
		//InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
		
		
		
		
	
	
	
		
	
		
		List<String> result =  StandardTextProcessing.getTags(text);
for(String s:result)
	System.out.println(s);
		System.out.println("done");
	}
	
	
}
