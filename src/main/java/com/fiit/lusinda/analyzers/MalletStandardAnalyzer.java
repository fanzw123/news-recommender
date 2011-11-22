package com.fiit.lusinda.analyzers;

import java.io.Reader;
import java.util.ArrayList;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import com.fiit.lusinda.utils.Logging;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;

public class MalletStandardAnalyzer extends org.apache.lucene.analysis.Analyzer{

	InstanceList malletInstance;
	public MalletStandardAnalyzer()
	{
		Pipe instancePipe;
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
	
		
		return new WhitespaceAnalyzer().tokenStream(fieldName, reader);
	}
}