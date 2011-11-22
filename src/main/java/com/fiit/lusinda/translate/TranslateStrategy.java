package com.fiit.lusinda.translate;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslateStrategy {

	protected int numRequests;
	protected int numCharsPerRequest;
	protected String apiKey;

	public abstract String translateText(String inputText, String source,
			String target) throws Exception;

	public abstract String translateText(String inputText) throws Exception;// default
																			// SK
																			// -
																			// EN

	public static List<String> getChunks(final String text, final int chunkSize) {
		int textLength = text.length();

		final int numChunks = 0 == (textLength % chunkSize) ? textLength
				/ chunkSize : 1 + (textLength / chunkSize);
		final List<String> chunks = new ArrayList<String>(numChunks);
	//	int endIndex = 0;
		int lastIndex = 0;
		for (int startIndex = 0; startIndex < textLength; startIndex += lastIndex) {
			lastIndex = Math.min(textLength, startIndex + chunkSize);
			String tmp = text.substring(startIndex, lastIndex);
			if (lastIndex != textLength) {
				lastIndex = tmp.lastIndexOf(".") + 1;
				if (lastIndex == 0)
					lastIndex = tmp.lastIndexOf(" ") + 1;
				tmp = text.substring(startIndex, startIndex + lastIndex);
			}

			chunks.add(tmp);
		}
		return chunks;
	}

	/*
	 * 
	 * public static List<String> getChunks(String text, int maxChars) {
	 * 
	 * int div = text.length() / maxChars; int numChunks = div + 1; List<String>
	 * chunks = new ArrayList<String>(); int lastEndOfSentence = 0; String tmp =
	 * null; int startIndex = 0; int currentIndex = startIndex + maxChars; int
	 * lastIndex = text.length() < maxChars ? text.length() : currentIndex; for
	 * (int i = 1; i <= numChunks; i++) { tmp = text.substring(startIndex,
	 * lastIndex);
	 * 
	 * if (lastIndex < text.length()) { lastEndOfSentence =
	 * tmp.lastIndexOf(". "); if (lastEndOfSentence > 0) tmp =
	 * text.substring(startIndex, startIndex+ lastEndOfSentence + 1); lastIndex
	 * = lastEndOfSentence; } startIndex = lastIndex + 1;
	 * 
	 * currentIndex = startIndex + maxChars;
	 * 
	 * lastIndex = text.length() < currentIndex ? text.length() : currentIndex;
	 * 
	 * chunks.add(tmp+" "); }
	 * 
	 * return chunks; }
	 */

}
