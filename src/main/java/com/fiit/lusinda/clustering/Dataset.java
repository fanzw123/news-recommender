package com.fiit.lusinda.clustering;

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
import java.util.StringTokenizer;

import com.fiit.lusinda.topicmodelling.Document;
import com.fiit.lusinda.topicmodelling.DocumentAttributes;
import com.fiit.lusinda.topicmodelling.DocumentProbability;

import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.ObservationNode;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class Dataset extends ArrayList<Document> implements Experiment {

	int numberOfObservations;

	public Dataset() {
	}

	public int getNumberOfObservations() {
		// TODO Auto-generated method stub
		return this.size();

	}

	public void loadDataset(String separator, String filePath)
			throws IOException {
		File f = new File(filePath);
		InputStream is = (InputStream) new FileInputStream(f);
		String encoding = "UTF-8";
		InputStreamReader isw = new InputStreamReader(is, encoding);
		BufferedReader br = new BufferedReader(isw);

		String strLine = "";
		StringTokenizer st = null;
		int lineNumber = 0;
		int tokenNumber = 0;

		// read comma separated file line by line
		while ((strLine = br.readLine()) != null) {
			lineNumber++;

			
			if (lineNumber > 1) {
				// break comma separated line using ","
				st = new StringTokenizer(strLine, separator);
				Document document = new Document();

				document.documentAttributes = new DocumentAttributes();
				document.documentAttributes.title= st.nextToken();
				document.documentAttributes.category= st.nextToken();
				if("0.0".equals(document.documentAttributes.category))
				{
					int a=2;
					a++;
				}
				int tokens = st.countTokens();
				document.probability = new double[tokens];
				while (st.hasMoreTokens()) {
					document.probability[tokenNumber] = Double.parseDouble(st.nextToken());
					tokenNumber++;
					
				}

				this.add(document);
				tokenNumber = 0;
			}

		}

	}

	public void saveDataset(String separator, String filePath)
			throws IOException {

		File f = new File(filePath);
		OutputStream os = (OutputStream) new FileOutputStream(f);
		String encoding = "UTF-8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write("title" + separator);
		bw.write("category" + separator);

		for (int i = 0; i < this.get(0).probability.length; i++) {
			bw.write("topic" + Integer.toString(i) + separator);
		}

		for (int doc = 0; doc < this.size(); doc++) {
			Document document = this.get(doc);
			if(document.documentAttributes.title == null || document.documentAttributes.title.equals(""))
				bw.write("no-title" + separator);
			else
				bw.write(document.documentAttributes.title + separator);
			
			if(document.documentAttributes.category == null || document.documentAttributes.category.equals(""))
				bw.write("no-category" + separator);
			else
				bw.write(document.documentAttributes.category + separator);
			
			for (int topic = 0; topic < document.probability.length; topic++)
				bw.write(document.probability[topic] + separator);

			bw.newLine();

		}

		bw.flush();
		bw.close();
	}
}
