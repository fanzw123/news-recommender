package com.fiit.lusinda.textprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import com.fiit.lusinda.carrot.LingoClustering;
import com.fiit.lusinda.utils.FileUtils;
import com.fiit.lusinda.utils.Logging;
import com.strangegizmo.cdb.Cdb;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;
/**
 * Lemmatizer of Slovak language using constant database of slovak word forms.
 * Database developed on JULS SAV in Bratislava.
 * 
 * @author simko
 */
public class Lemmatizer {

    Cdb cdb;
    
   private static File lemmaResourceTmp;
    
//    static{
//    	lemmaResourceTmp = new File("/tmp/form2lemma.cdb");
//		if(!lemmaResourceTmp.exists() && lemmaResourceTmp.length()>0)
//		{
//			
//			InputStream templateStream = LingoClustering.class.getResourceAsStream("form2lemma.cdb");
//			try {
//				FileUtils.copyStreams(templateStream, new FileOutputStream(lemmaResourceTmp));
//			} catch (FileNotFoundException e) {
//				// 
//				e.printStackTrace();
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//		
//		}
//    }

      Lemmatizer() throws URISyntaxException {
    	  //InputStream stream = Lemmatizer.class.getClassLoader().getResourceAsStream("form2lemma.cdb");	  
     
    	  
    	  this("/tmp/form2lemma.cdb");
    }

    Lemmatizer(String dbFile) {
    	 try {
             cdb = new Cdb(dbFile);
         } catch (IOException e) {
             System.out.println("error loading cdb file");
             e.printStackTrace();
         }
    }

    private static Lemmatizer lemmatizer;
    
    public static Lemmatizer getLemmatizer() throws URISyntaxException
    {

    	if(lemmatizer == null)
    		lemmatizer = new Lemmatizer();
    	
    	return lemmatizer;
    }
    
  
    public String[] lemmatizeFirst(String[] words)
    {
    	String[] lemmas = new String[words.length];
    	
    	for(int i=0;i<words.length;i++)
    	{
    		lemmas[i] = lemmatizeFirstOrGetOriginal(words[i]);
    	}
    	
    	return lemmas;
    		
    	
    }
  
    public List<String> lemmatize(String word) {
        return lemmatize(word, null);
    }

    public String lemmatizeFirst(String word) {
        List<String> lemmas = lemmatize(word);
        if(lemmas!=null && lemmas.size()>0)
        	return lemmas.get(0);
        else
        	return null;
    }
    
    
    
    
    public String lemmatizeWords(String words,String delimeter,String breakOnWord) throws OperationNotSupportedException
    {
    	StringBuilder result = new StringBuilder();
    	String[] parts = delimeter.split(delimeter);
    	for(int i=0;i<parts.length;i++)
    	{
    		throw new OperationNotSupportedException("not yet implemented");
    	}
    	
    	return result.toString();
    	
    }
    
    public String lemmatizeFirstOrGetOriginal(String word) {
        List<String> lemmas = lemmatize(word);
        if(lemmas!=null && lemmas.size()>0)
        	return lemmas.get(0);
        else
        	return word;
    }

    
    public List<String> lemmatize(String word, List<String> lemmas) {
        if (lemmas == null) {
            lemmas = new ArrayList<String>();
        }
        byte[] response;
        try {
            response = cdb.find(word.getBytes("UTF-8"));

            while ((response != null) && (response.length > 0)) {
                lemmas.add(new String(response, "UTF-8"));
                response = cdb.findnext(word.getBytes("UTF-8"));
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        //if (lemmas.isEmpty()) {
            if (word.startsWith("ne")) {
                return lemmatize(word.substring(2), lemmas);
            } else if (word.startsWith("naj")) {
                return lemmatize(word.substring(3), lemmas);
            }
        //}

        return lemmas;
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException {
    //    Lemmatizer lem = new Lemmatizer();


//        System.out.println(new String(lem.cdb.find(word.getBytes("UTF-8")), "UTF-8"));
//        System.out.println(new String(lem.cdb.findnext(word.getBytes("UTF-8")), "UTF-8"));
//        System.out.println(new String(lem.cdb.findnext(word.getBytes("UTF-8")), "UTF-8"));
//        System.out.println(new String(lem.cdb.findnext(word.getBytes("UTF-8")), "UTF-8"));
//        System.out.println(new String(lem.cdb.findnext(word.getBytes("UTF-8")), "UTF-8"));
//        System.out.println(lem.cdb.find("asdfghj".getBytes("UTF-8")));
//        List<String> lemmatize = lem.lemmatize(word);
//        for (String lemma : lemmatize) {
//            System.out.println(lemma);
//        }
    }


  
}

