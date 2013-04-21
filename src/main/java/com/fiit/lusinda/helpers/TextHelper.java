package com.fiit.lusinda.helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TextHelper {

	public static List<String> chunkText(String text,int chunkSize) {
	    if (chunkSize<=1) {
	        throw new IllegalArgumentException("Chunk size must be positive");
	    }
	    if (text==null || text.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<String> chunks= new LinkedList<String>();

	    int index=0;
	    int len = text.length();

	    //guaranteed to succeed at least once since 0 length strings we're taken care of
	    do {
	        chunks.add(text.substring(index, Math.min(index + chunkSize, len)));
	        index+=chunkSize;
	    } while (index<len);

	    return chunks;
	}
	
}
