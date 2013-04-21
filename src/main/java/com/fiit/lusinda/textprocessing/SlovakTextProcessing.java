package com.fiit.lusinda.textprocessing;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SlovakTextProcessing extends StandardTextProcessing{
	
	@Override
	public String processText(String text) throws IOException, Exception {
	
		return null;
	}
	


	private List<String> stopWords;
	private int maxWordLength;
	private String stopWordsPath = "resources/stopWords.txt";
	Map<String, Integer> wordsCount;
	List<String> dictionary;
	private int lastIndex;
	private Lemmatizer lemmatizer;

	public SlovakTextProcessing() {
		lemmatizer = null;//new Lemmatizer();
	}

	private void readStopWords() {

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					new FileInputStream(stopWordsPath), "UTF-8");
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = null;

			while ((line = reader.readLine()) != null)
				this.stopWords.add(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void init(int maxWordLength) {
		this.maxWordLength = maxWordLength;
		this.stopWords = new ArrayList<String>();
		wordsCount = new HashMap<String, Integer>();
		this.lastIndex = 0;
		dictionary = new ArrayList<String>();
		readStopWords();

	}

	public String lemmatizeString(String str) {

		List<String> lemmas = lemmatizer.lemmatize(str);
		if (lemmas != null && lemmas.size() > 0)
			return lemmas.get(0);
		else
			return str;
	}

	public boolean shouldRemove(String word) {
		
		if (stopWords.contains(word) || word.length() <= maxWordLength)
			return true;
		else
			return false;

	}

//	public String lemmatizeStrings(String str)
//	{
//		String[] tokens = tokenize(str);
//
//		
//	}
	
	public void writelemmatizedStrings(String filePath, String source)
			throws IOException {
		File f = new File(filePath);

		OutputStream os = (OutputStream) new FileOutputStream(f);
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);

		BufferedWriter bw = new BufferedWriter(osw);

		String[] tokens = tokenize(source);

		for (int i = 0; i < tokens.length; i++) {
			
			if (shouldRemove(tokens[i]))
				continue;
			else {

				String lemma = null;
				try {
					lemma = lemmatizeString(tokens[i]);

					if (shouldRemove(lemma))
						lemma = null;
				} catch (NoSuchElementException ex) {
					System.out.print("nepodarilo sa spracovat slovo "
							+ tokens[i]);
				}
				if (lemma == null)
					continue;

				bw.write(lemma);
				bw.write("  ");

			}
		}

		bw.flush();
		bw.close();

	}

	public List<String> lemmatizeStrings(String str) {
		String[] tokens = tokenize(str);
		List<String> result = new ArrayList<String>();

		for (int i = 0; i < tokens.length; i++) {
			if (stopWords.contains(tokens[i])
					|| tokens[i].length() <= maxWordLength)
				continue;
			else {

				String lemma = null;
				try {
					lemma = lemmatizeString(tokens[i]);

					if (stopWords.contains(lemma)
							|| tokens[i].length() <= maxWordLength)
						lemma = null;
				} catch (NoSuchElementException ex) {
					System.out.print("nepodarilo sa spracovat slovo "
							+ tokens[i]);
				}
				if (lemma == null)
					continue;

				result.add(lemma);

			}
		}

		return result;
	}

	public String html2PlainText(String str) {

		return Jsoup.parse(str).text().toLowerCase();

	}

	public String[] tokenize(String str) {

		// str = str.replaceAll("[^-a-zA-Z0-9]", "");

		str = html2PlainText(str);
		str = str.replaceAll("[\\\\\\/.,-_'\"!():\\*\\?<>|]", "");

		return str.split("\\s");
	}

	public int getTotalWordsCount() {
		return wordsCount.size();
	}

	public int getWordTotalCounts(String word) {
		return wordsCount.get(word);
	}

	public Map<String, Integer> getWordsCount() {
		return this.wordsCount;
	}

	public Integer getWordPos(String word) {
		return dictionary.indexOf(word);
		// return ArrayUtils.indexOf(wordsCount.keySet().toArray(
		// new String[0]), word);
	}

	public String getWord(Integer pos) {
		return wordsCount.keySet().toArray(new String[0])[pos];
	}

}

