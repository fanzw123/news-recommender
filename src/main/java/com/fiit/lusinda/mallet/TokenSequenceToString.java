package com.fiit.lusinda.mallet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;

import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.utils.Logging;

import cc.mallet.types.Token;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;

public class TokenSequenceToString  extends Pipe implements Serializable {

	public Instance pipe (Instance carrier)
	{
		TokenSequence ts = (TokenSequence) carrier.getData();
	
		TokenSequence tmpTS = new TokenSequence();
		
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			
			try {
				String lemma = Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(t.getText());
				if(lemma.length()>4)
					tmpTS.add(lemma);
				
			} catch (URISyntaxException e) {
				
				Logging.Log("unable to locate lemmatizer cdb");
			}
		}
		
		carrier.setData(tmpTS);
		
		return carrier;
	}
	
	public String getLemmatizedText(Instance carrier)
	{
		TokenSequence ts = (TokenSequence) carrier.getData();
		
		StringBuilder lemmatized = new StringBuilder();
		
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			
			try {
				String lemma = Lemmatizer.getLemmatizer().lemmatizeFirstOrGetOriginal(t.getText());
				if(lemma.length()>4)
					lemmatized.append(lemma).append(" ");
				
			} catch (URISyntaxException e) {
				
				Logging.Log("unable to locate lemmatizer cdb");
			}
		}
		
		
		
		return lemmatized.toString();
	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
	}
	
}
