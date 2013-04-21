/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */





package com.fiit.lusinda.mallet;

import java.io.*;


import com.fiit.lusinda.utils.Logging;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
/**
 * Convert the token sequence in the data field each instance to a feature sequence.
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

public class TokenSequenceNormalize extends Pipe
{
	public TokenSequenceNormalize (Alphabet dataDict)
	{
		super (dataDict, null);
	}

	long ts;
	public TokenSequenceNormalize (long ts)
	{
		super(new Alphabet(), null);
		
		this.ts= ts;
	}
	
	private String addKey(String token) throws IOException
	{
		return null;// HBaseProxyManager.getProxy().putWord(token, token.length()/2,ts,true,4);
	}
	
	
	
	public Instance pipe (Instance carrier)
	{
		
		TokenSequence ts = (TokenSequence) carrier.getData();
		TokenSequence newTS = new TokenSequence();
		
		for (int i = 0; i < ts.size(); i++) {
			String key = null;
			try {
				key = addKey(ts.get(i).getText());
				
			} catch (IOException e) {
				Logging.Log("error put to hbase");
				e.printStackTrace();
			}
			if(key!=null)
				newTS.add (key);
			else
				newTS.add (ts.get(i).getText());
			
		}
		carrier.setData(newTS);
		return carrier;
	}

}
