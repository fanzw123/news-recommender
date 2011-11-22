package com.fiit.lusinda.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.memetix.mst.translate.Translate;


public class BingTranslateStrategy extends TranslateStrategy {

	public BingTranslateStrategy()
	{
		//TODO use properties file
		
		this.numCharsPerRequest = 5000;
		this.numRequests=2000;
		this.apiKey="E516F8907E960E41A323E88227E237E079709456";
				
		Translate.setHttpReferrer("fiit.stuba.sk");
		Translate.setKey(apiKey);
		
	}
	
	@Override
	public
	String translateText(String inputText, String source, String target) throws Exception {
		
		
		 return Translate.execute(inputText,findLanguage(source),findLanguage(target));
		
	}

	
	

	
	private com.memetix.mst.language.Language findLanguage(String lang)
	{
		return null;
	}
	
	@Override
	public String translateText(String inputText) throws Exception {
		return Translate.execute(inputText,com.memetix.mst.language.Language.SLOVAK,com.memetix.mst.language.Language.ENGLISH);
	}

	
}
