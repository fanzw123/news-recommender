package com.fiit.lusinda.translate;

import java.io.IOException;
import java.sql.SQLException;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.utils.Logging;

public class ArticleTranslator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
TextTranslator translator = new TextTranslator(new GoogleTranslateStrategy());

SmeDataSource smeDs = new SmeDataSource();

smeDs.connect();

smeDs.translateArticles(new String[] {"title","body","perex"},"time_slice_id=9" ,translator);


smeDs.disconnect();


	}

}
