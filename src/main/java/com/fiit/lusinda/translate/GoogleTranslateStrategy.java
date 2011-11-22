package com.fiit.lusinda.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.api.GoogleAPI;
import com.google.api.translate.Translate;

public class GoogleTranslateStrategy extends TranslateStrategy {

	public GoogleTranslateStrategy()
	{
		//TODO use properties file
		
		this.numCharsPerRequest = 5000;
		this.numRequests=2000;
		this.apiKey="AIzaSyCidV1DlrBzAIPAm1_QTH4pgQoanEw1Fqc";
				
		Translate.setHttpReferrer("fiit.stuba.sk");

		
	}
	
	@Override
	public
	String translateText(String inputText, String source, String target) throws Exception {
		
		
		 return Translate.execute(inputText,findLanguage(source),findLanguage(target));
		
	}

	
	private com.google.api.translate.Language findLanguage(String lang)
	{
		return null;
	}

	@Override
	public String translateText(String inputText) throws Exception {
		 return Translate.execute(inputText,com.google.api.translate.Language.SLOVAK,com.google.api.translate.Language.ENGLISH);

	}

	
}
