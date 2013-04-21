package com.fiit.lusinda.entities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.carrot2.core.Cluster;
import org.carrot2.core.LanguageCode;

import com.fiit.lusinda.carrot.LingoClustering;
import com.fiit.lusinda.hbase.HBaseProxyManager;
import com.fiit.lusinda.rss.FeedSettings;
import com.fiit.lusinda.topicmodelling.Lda;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.fiit.lusinda.utils.Logging;

public class StreamTopicModel {

	String rootDir;
//	int windowLength = 1;
//	int maxRecommendations;
//	int maxWordsPerTopic;
//	int queryLimit;
//	StreamTopicModelInfo info;
	FeedSettings settings;
	
	public StreamTopicModel(String rootDir,FeedSettings settings) {
		this.rootDir = rootDir;
//		this.windowLength = info.windowLength;
//		maxRecommendations = info.maxRecommendations;
//		queryLimit = info.queryLimit;
//		maxWordsPerTopic = info.maxWordsPerTopic;
		this.settings = settings;
		
		
		docStreams = new ArrayList<DocumentStream>();
		models = new ArrayList<Lda>();
		
		
		File f = new File(rootDir);
		if (!f.exists())
			f.mkdirs();
	}

	// TODO in future priority quene, to satisfy docStream window
private List<DocumentStream> docStreams;
private List<Lda> models;

	public void createStream(long ts) {
		DocumentStream stream = new DocumentStream(ts, rootDir,settings);

		this.docStreams.add(stream);
	}

	public DocumentStream getCurrentStream() {
		return docStreams.get(docStreams.size() - 1);
	}


	
//	//@@Obsolete
//	public List<Article> infer(String doc,String docName) {
//		
//		List<Article> articles = new ArrayList<Article>();
//		
//		for (int i = 0; i < docStreams.size() - 1; i++) {
//			DocumentStream stream = docStreams.get(i);
//			Article newArticle = new Article(-1,null,docName,doc,0,stream.infer(doc));
//			articles.add(newArticle);	
//			
//		//	stream.assignSimilarDocuments(doc, 0.8);
//			
////			stream.assignSimilarity(doc, 0.8);
////			
////			getCurrentStream().a
//			
//			//Logging.Log("infered..");
//			
//		}
//		return articles;
//	}

	public void addDocToStream(String key,String body, String title) throws URISyntaxException, IOException {
	
		getCurrentStream().addDocument(key,body, title);

		if (docStreams.size() > 1)
		{
			//List<Article> articles = this.infer(body,title);
			//if(!settings.localStore)
				generateRecommendations(key,body, title);
			
		}
	}
	
	public void loadLdaModels() throws Exception
	{
		File dir = new File(rootDir);
		
		
		File[] dirs = dir.listFiles(new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		});
		
		
		for(File f:dirs)
		{
			File model = new File(f,"model.mallet");
			Lda lda = (Lda) Lda.read(model);
			lda.calculateDiagnostics();
			lda.calculatePhi();
			lda.findBigrams3();
			models.add(lda);
		}
		
	
	}
	




	
	public void generateRecommendations() throws URISyntaxException, IOException
	{
		
		Lda lda = getCurrentStream().getModel().getLda();
		
		List<Article> articles = lda.getDocumentsAsArticles();
	///	int i=0;
		for(Article a:articles)
		{
			TreeSet<Sorter<NGram>> allTopWords = new TreeSet<Sorter<NGram>>();
			TreeSet<Sorter<Article>> recomendations = new TreeSet<Sorter<Article>>();
			
				a.ts = getCurrentStream().ts;
			///	i++;
				
					TreeSet<Sorter<NGram>> docWords = lda.getDocumentWordsLikelihood(a,settings.onlyUpperKeywords);
					allTopWords.addAll(docWords);
				//	LdaModel.printTopRecomendedWords(a.title, allTopWords, settings.queryLimit, 0);
					
					TreeSet<Sorter<Article>> partialRecomendations =  lda.getSimilarDocuments(a,
							settings.maxRecommendations,settings.experimental);
					recomendations.addAll(partialRecomendations);
					
			
					Query q =  new Query(allTopWords);	
					q.flush(15);
				//	LdaModel.printTopRecomendedWords(a.title, allTopWords, settings.queryLimit, 0);
					if(settings.summarize)
					{
						a.summarizedText =  LdaModel.getSummary(a.content, q,settings.queryLimit, settings.summarizedSentencesLimit,settings.summarize);
						LdaModel.getSummaries(recomendations,q,settings.queryLimit,settings.summarizedSentencesLimit,settings.summarize);
					}
			//		System.out.println(a.title);
				//	LdaModel.printSortedDocuments(recomendations, 10,0);
					
				
				
					
				
					if(settings.hbaseImport)
					{
						HBaseProxyManager.getProxy().putRecommendedArticles(a,recomendations,settings.maxRecommendations);
						HBaseProxyManager.getProxy().putTopProbWords(a,q, settings.queryLimit);
					}
		}
		
		
		System.out.println("Recommendations for LdaModel generated");
		
	}
	
	public void loadDocumentStreams()
	{
		
	}

	public void generateRecommendations(String key,String body, String title) throws URISyntaxException, IOException
	{
		TreeSet<Sorter<NGram>> allTopWords = new TreeSet<Sorter<NGram>>();
		TreeSet<Sorter<Article>> recomendations = new TreeSet<Sorter<Article>>();
		
		Article newArticle = new Article(-1,key,title,body,0,null);
		newArticle.ts = getCurrentStream().ts;
		
	//	for (int i = 0; i < docStreams.size() - 1; i++) {
			
	//	DocumentStream stream = docStreams.get(i);
	//	Lda lda = stream.getModel().getLda();
		
		for (int i = 0; i < models.size() - 1; i++) {
			Lda lda = models.get(i);
		
	//	newArticle.dist =stream.infer(body);
			newArticle.dist =lda.inferDist(body);
			
		TreeSet<Sorter<NGram>> docWords = lda.getDocumentWordsLikelihood(newArticle);
		allTopWords.addAll(docWords);
		
		TreeSet<Sorter<Article>> partialRecomendations =  lda.getSimilarDocuments(body,
				title,i,
				settings.maxRecommendations,settings.experimental);
		recomendations.addAll(partialRecomendations);
		
		}
		
		Query q =  new Query(allTopWords);	
		
		if(settings.summarize)
		{
			newArticle.summarizedText = LdaModel.getSummary(newArticle.content, q, settings.queryLimit, settings.summarizedSentencesLimit,settings.summarize);
			LdaModel.getSummaries(recomendations,q,settings.queryLimit,settings.summarizedSentencesLimit,settings.summarize);
		}
	
		//System.out.println(title);
		//LdaModel.printSortedDocuments(recomendations, 10,0);
		
//		LingoClustering lingo = new LingoClustering(q.getQueryString(), false);
//		Iterator<Sorter<Article>> it = recomendations.iterator();
//		
//		while(it.hasNext())
//		{
//			Sorter<Article> sd = it.next();
//			lingo.addDocument(sd.data.summarizedText,sd.data.getNiceTitle());
//		}
//		List<Cluster> clustersByTopic =null;
//		try
//		{
//			lingo.build();
//		clustersByTopic = lingo.cluster();
//		}
//		catch(Exception ex){}
//		
//		
//		
//		LdaModel.printClusteredResults(title, clustersByTopic, 30, 0);
//		
		
		if(settings.hbaseImport)
		{
			HBaseProxyManager.getProxy().putRecommendedArticles(newArticle,recomendations,settings.maxRecommendations);
			HBaseProxyManager.getProxy().putTopProbWords(newArticle,q, settings.queryLimit);
		}
	
	}
	
	public void processStreamData() throws IOException, URISyntaxException
	{
		DocumentStream currStream =getCurrentStream(); 
		currStream.processStreamData();
		
	
		
		
		//currStream.computeSimilarities();
		currStream.persistentDocumentStream();
		models.add(currStream.lda.getLda());
		
		//if(!settings.localStore)
			generateRecommendations();
		
		//all but currStream
	//	for(int i = docStreams.size() - windowLength-1;  i>=0; i--) { 
		
		for (int i = 0; i < docStreams.size() - 1; i++) {	
			//DocumentStream stream = docStreams.get(i);
			
			try {
			//	currStream.computeSimilarTopics(stream);
			} catch (Exception e) {
				//Logging.Log("error processing data stream "+stream.rootDir);
				e.printStackTrace();
			} 
		
		}
	}
	
	
	
}
