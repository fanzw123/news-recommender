package com.fiit.lusinda.topicmodelling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class TopicWordMatrix {

	List<String> dictionary;
	int[] words;
	double[] weights;
	int[] topics;
	
	Map<Integer, HashMap<Integer, Double>> matrix;

	int numTopics;

	public TopicWordMatrix()
	{
		init();
	}
	
	public TopicWordMatrix(String[] wordArray, int numTopics) {
		
		init();
		
		for (int i = 0; i < wordArray.length; i++)
			this.dictionary.add(wordArray[i]);
		this.numTopics = numTopics;


	}

	private void init()
	{
		this.dictionary = new ArrayList<String>();
		this.matrix = new HashMap<Integer, HashMap<Integer, Double>>();

	}
	
	private String getWord(int wordIndex) {
		return dictionary.get(wordIndex);
	}

	private int getWordIndex(String word) {

		return dictionary.indexOf(word);
	}

	public void addWordProbability(int topic, String word, double probability) {
		HashMap<Integer, Double> innerMap = matrix.get(topic);
		if (innerMap == null) {
			innerMap = new HashMap<Integer, Double>();
			for (int i = 0; i < dictionary.size(); i++) {
				innerMap.put(i, 0.0);
			}
			matrix.put(topic, innerMap);
		}

		innerMap.put(getWordIndex(word), probability);

	}

	public void load(String filePath,String separator) throws IOException {

		File f = new File(filePath);
		InputStream is = (InputStream) new FileInputStream(f);
		String encoding = "UTF-8";
		InputStreamReader isw = new InputStreamReader(is, encoding);
		BufferedReader br = new BufferedReader(isw);

		String strLine = "";
		StringTokenizer st = null;
		int lineNumber = 0;
		int columnNumber = 0;

		init();
		
		// read comma separated file line by line
		while ((strLine = br.readLine()) != null) {

			st = new StringTokenizer(strLine, separator);
			if(lineNumber ==0)
			{
				st.nextToken(); //first skip
				while (st.hasMoreTokens()) {
					this.dictionary.add(st.nextToken());
				}
			}
			else
			{
				st.nextToken(); //first skip

				columnNumber = 1;

				while (st.hasMoreTokens()) {
				addWordProbability(lineNumber, getWord(columnNumber), Double.parseDouble(st.nextToken()));
				columnNumber++;
				}
			}
				lineNumber++;

		}

	}

	public void save(String filePath, String separator) throws IOException {
		File f = new File(filePath);
		OutputStream os = (OutputStream) new FileOutputStream(f);
		String encoding = "UTF-8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write("topic" + separator);

		for (String word : dictionary)
			bw.write(word + separator);

		
				bw.newLine();
			for(Map.Entry<Integer, HashMap<Integer,Double>> entry:matrix.entrySet())
			{
				bw.write(entry.getKey() + separator);
				for(Double weight:entry.getValue().values())
					bw.write(weight+separator);
						
						bw.newLine();

			}
				bw.flush();
		bw.close();
	}
}
