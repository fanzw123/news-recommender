package com.fiit.lusinda.carrot;

import java.net.URISyntaxException;
import java.util.List;

import morfologik.stemming.PolishStemmer;
import morfologik.stemming.WordData;

import org.carrot2.text.linguistic.IStemmer;

import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.textprocessing.SlovakTextProcessing;
import com.fiit.lusinda.textprocessing.StandardTextProcessing;

public class SlovakStemmerAdapter  implements IStemmer
{

    public SlovakStemmerAdapter()
    {
    }

    
    
    public CharSequence stem(CharSequence word)
    {
    	try {
			return Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(word.toString());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    	
    }
} 


