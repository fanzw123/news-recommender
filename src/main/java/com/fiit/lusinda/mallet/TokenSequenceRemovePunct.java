/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */





package com.fiit.lusinda.mallet;

import java.io.*;
import java.util.Locale;

import com.fiit.lusinda.textprocessing.StandardTextProcessing;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
/**
 * Convert the text in each token in the token sequence in the data field to lower case.
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

public class TokenSequenceRemovePunct extends Pipe implements Serializable
{
	
	
	

	public Instance pipe(Instance carrier) {
		String currentTerm = null;
		TokenSequence tmpTS = new TokenSequence();
		TokenSequence ts = (TokenSequence) carrier.getData();
		int uppersCount = 0;
		StringBuilder newTerm = new StringBuilder();

		for (int i = 0; i < ts.size(); i++) {
			Token currentToken = ts.get(i);
			currentTerm = currentToken.getText();//.replaceAll("\\p{Punct}|\\d", "");
			uppersCount = 0;
		
			
			Token previousToken = i==0 ? currentToken: ts.get(i -1);
			String previousTerm = previousToken.getText();
			
			if (StandardTextProcessing.startsWithUpper(currentTerm) && !StandardTextProcessing.containsPunct(previousTerm))
				tmpTS.add(StandardTextProcessing.removePunct(currentTerm));
			else if(currentTerm.length()>4)
				tmpTS.add(StandardTextProcessing.toLowerCase(StandardTextProcessing.removePunct(currentTerm)));
			
			
		}

		carrier.setData(tmpTS);

		return carrier;
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
