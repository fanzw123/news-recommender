/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package com.fiit.lusinda.mallet;

import java.io.*;


import com.fiit.lusinda.textprocessing.StandardTextProcessing;
import com.fiit.lusinda.utils.Logging;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * Convert the token sequence in the data field to a token sequence of ngrams.
 * 
 * @author Don Metzler <a
 *         href="mailto:metzler@cs.umass.edu">metzler@cs.umass.edu</a>
 */

public class TokenSequenceNGramsFiltered extends Pipe implements Serializable {

	private   long ts;
	public TokenSequenceNGramsFiltered(long ts) {
		this.ts = ts;
	}

	private boolean canBeFirstPartOfBiGram(String token) {
	
		return !StandardTextProcessing.containsPunct(token);
				}
	
	private String removePunct(String str)
	{
		return str.replaceAll("\\p{Punct}|\\d", "");
	}

	public Instance pipe(Instance carrier) {
		String currentTerm = null;
		TokenSequence tmpTS = new TokenSequence();
		TokenSequence ts = (TokenSequence) carrier.getData();
		int uppersCount = 0;
		StringBuilder newTerm = new StringBuilder();
		


		//currentTerm = ts.get(0).getText().replaceAll("\\p{Punct}|\\d", "");
		//tmpTS.add(currentTerm);
		
		for (int i = 0; i < ts.size(); i++) {
			Token currentToken = ts.get(i);
			currentTerm = currentToken.getText();//.replaceAll("\\p{Punct}|\\d", "");
			uppersCount = 0;
			
			Token nextToken = i==ts.size()-1?currentToken: ts.get(i + 1);
			String nextTerm = removePunct(nextToken.getText());
			
			Token previousToken = i==0 ? currentToken: ts.get(i -1);
			String previousTerm = previousToken.getText();
			
			
			
			if (canBeFirstPartOfBiGram(currentTerm)) {
				
				newTerm.setLength(0);

				newTerm.append("b_"); //for bigrams
				
				if (StandardTextProcessing.startsWithUpper(nextTerm))
					uppersCount++;

				if (StandardTextProcessing.startsWithUpper(currentTerm) && canBeFirstPartOfBiGram(previousTerm))
					uppersCount++;

				if (uppersCount > 0)
				{
//					newTerm.append("u_");
//					newTerm.append(uppersCount);
//					newTerm.append("_");
				}
				
				//add bigram
				if (uppersCount > 0 && (currentTerm.length() > 4 && nextTerm.length() > 4)) {
					currentTerm = lookupWord(currentTerm);
					nextTerm = lookupWord(nextTerm);
					
					newTerm.append(currentTerm);
					newTerm.append("_");
					newTerm.append(nextTerm);
					
					tmpTS.add(StandardTextProcessing.toLowerCase(newTerm.toString()));
				} 
				
				
				
			} 
			
			//add current 
			newTerm.setLength(0);
//			if (StandardTextProcessing.startsWithUpper(currentTerm)  && canBeFirstPartOfBiGram(previousTerm))
//				newTerm.append("u_1_");
//			
			
			if (newTerm.length()>0	|| currentTerm.length() > 4)
			{
				currentTerm = lookupWord(removePunct(currentTerm));
				
				newTerm.append(currentTerm);
				tmpTS.add(StandardTextProcessing.toLowerCase(newTerm.toString()));
			}
			

		}

		carrier.setData(tmpTS);

		return carrier;
	}
	
	private String lookupWord(String token) 
	{
		
		return token;
//		
//		String key = null;
//		try {
//			key = HBaseProxyManager.getProxy().putWord(token, token.length()/2,ts,true,4);
//		} catch (IOException e) {
//			Logging.Log("error put to hbase");
//			e.printStackTrace();
//		}
//		if(key==null)
//			key = token;
//		
//		return key;
	}

	// Serialization

	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);
		out.writeLong(ts);
		
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		
		ts = in.readLong();
	
		

	}

}
