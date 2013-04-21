package com.fiit.lusinda.mallet;

import java.util.regex.Pattern;

import cc.mallet.extract.StringSpan;
import cc.mallet.extract.StringTokenization;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.CharSequenceLexer;

public class CharSequence2TokenSequencePreserveOriginal extends CharSequence2TokenSequence {
	
	CharSequenceLexer originalLexer;
	
	public CharSequence2TokenSequencePreserveOriginal(Pattern original,Pattern preprocessed)
	{
		super(preprocessed);
		originalLexer = new CharSequenceLexer(original);
		

	}
	
	@Override
	public Instance pipe(Instance carrier) {
	
			
		//set source original
		CharSequence string = (CharSequence) carrier.getData();
		originalLexer.setCharSequence (string);
		TokenSequence ts = new StringTokenization (string);
		while (originalLexer.hasNext()) {
			originalLexer.next();
		ts.add (new StringSpan (string, originalLexer.getStartOffset (), originalLexer.getEndOffset ()));
		}
		carrier.setSource(ts);
		
		//process in standard way
		return super.pipe(carrier);

	}
	

}
