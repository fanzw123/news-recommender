package com.fiit.lusinda.carrot;

import java.util.EnumMap;

import org.carrot2.core.LanguageCode;
import org.carrot2.text.linguistic.IStemmer;
import org.carrot2.util.factory.IFactory;
import org.carrot2.util.factory.NewClassInstanceFactory;

import com.google.common.collect.Maps;

public class StemmerFactory extends org.carrot2.text.linguistic.DefaultStemmerFactory
{

	    private final static EnumMap<LanguageCode, IFactory<IStemmer>> stemmerFactories;

	    static
	    {
	    	stemmerFactories = createDefaultStemmers();
	    }
	    
	    private static EnumMap<LanguageCode, IFactory<IStemmer>> createDefaultStemmers()
	    {
	    	
	    	final EnumMap<LanguageCode, IFactory<IStemmer>> map = Maps.newEnumMap(LanguageCode.class);

	    	   map.put(LanguageCode.SLOVAK,     new NewClassInstanceFactory<IStemmer>(SlovakStemmerAdapter.class));

	    	   return map;
	    }

	    
	    @Override
	    public IStemmer getStemmer(LanguageCode languageCode) {
	    
	    	IFactory<IStemmer> stemmerfactory = stemmerFactories.get(languageCode);
	    	if(stemmerfactory==null)
	    		return super.getStemmer(languageCode);
	    	else
	    		return stemmerfactory.createInstance();
	    	
	    	
	    
	    }
}
