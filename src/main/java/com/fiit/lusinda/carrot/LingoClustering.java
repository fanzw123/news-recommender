package com.fiit.lusinda.carrot;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithmDescriptor;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.LanguageCode;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
import org.carrot2.text.linguistic.DefaultLexicalDataFactoryDescriptor;
import org.carrot2.text.linguistic.LexicalDataLoaderDescriptor;
import org.carrot2.text.preprocessing.pipeline.BasicPreprocessingPipelineDescriptor;
import org.carrot2.util.resource.DirLocator;
import org.carrot2.util.resource.ResourceLookup;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.NGram;
import com.fiit.lusinda.entities.Query;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.rss.FeedSettings;
import com.fiit.lusinda.textprocessing.Lemmatizer;
import com.fiit.lusinda.topicmodelling.Lda;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.fiit.lusinda.utils.FileUtils;
import com.google.common.collect.Lists;

public class LingoClustering {

	private ArrayList<org.carrot2.core.Document> documents = new ArrayList<org.carrot2.core.Document>();
	private String queryString;
	private boolean summarize;
	Controller controller;
	Map<String, Object> attributes ;
	private static File carrotResourceTmp;
	
//	static{
//		carrotResourceTmp = new File("/tmp/carrot");
//		if(!carrotResourceTmp.exists() && carrotResourceTmp.length()>0)
//		{
//			
//			InputStream templateStream = LingoClustering.class.getResourceAsStream("/carrot");
//			try {
//				FileUtils.copyStreams(templateStream, new FileOutputStream(carrotResourceTmp));
//			} catch (FileNotFoundException e) {
//				// 
//				e.printStackTrace();
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//		
//		}
//	}
	
	
	
	public LingoClustering(String q,boolean summarize)
	{
		this.queryString = q;
		this.summarize =summarize;
		attributes = new HashMap<String, Object>();
		controller = ControllerFactory.createSimple();
	}
	
	public void addDocument(String body,String title)
	{
	 	
		documents.add(new org.carrot2.core.Document(title,
				body,
				LanguageCode.SLOVAK));
	}
	
	public void build() 
	{
		 
		 //BasicPreprocessingPipelineDescriptor.Keys.
		 CommonAttributesDescriptor.attributeBuilder(attributes).query(queryString);
		 CommonAttributesDescriptor.attributeBuilder(attributes).documents(documents);
		 	 BasicPreprocessingPipelineDescriptor.attributeBuilder(attributes).stemmerFactory(StemmerFactory.class);

	
		 	 // File resourcesDir = new File("/Users/teo/Documents/workspace-new/news-recommender/src/main/resources/carrot");
		 	 
		 	 
		 	File resourcesDir= new File("/tmp/carrot");
		 	
		 	System.out.println(resourcesDir.getAbsolutePath());
		 	
		 //	File resourcesDir = new File("main/resources/carrot");
		 	
		 	DefaultLexicalDataFactoryDescriptor.attributeBuilder(attributes)
	        .mergeResources(true);
		        ResourceLookup resourceLookup = new ResourceLookup(new DirLocator(resourcesDir));
		        LexicalDataLoaderDescriptor.attributeBuilder(attributes)
	            .resourceLookup(resourceLookup);
		        DefaultLexicalDataFactoryDescriptor.attributeBuilder(attributes).resourceLookup(resourceLookup);
		        
		       LingoClusteringAlgorithmDescriptor.attributeBuilder(attributes).query(queryString);
		 	controller.init(attributes);
	}
	
	public List<Cluster> cluster()
	{
		Cluster c;
		
		ProcessingResult byTopicClusters = controller.process(attributes,LingoClusteringAlgorithm.class);
		List<Cluster> clustersByTopic = byTopicClusters.getClusters();
		return clustersByTopic;
		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		
		FeedSettings settings = 		FeedSettings.getSmeExperimentFeedSettings(7);
		String rootDir = settings.outputDirectory+"/lda/";
		
		File dir = new File(rootDir);
		
		
		File[] dirs = dir.listFiles(new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		});
		
		
		Lda lda = (Lda) Lda.read(new File(dirs[0],"model.mallet"));
		lda.calculatePhi();
		lda.calculateDiagnostics();
		lda.findBigrams3();
		lda.convertDocumentsToArticles();
		
		List<Lda> models = Lists.newArrayList();
		models.add(lda);
		int top = 20;
		
		
		List<Article> articles = lda.getDocumentsAsArticles();
		///	int i=0;
			for(Article a:articles)
			{
				TreeSet<Sorter<NGram>> allTopWords = new TreeSet<Sorter<NGram>>();
				TreeSet<Sorter<Article>> recomendations = new TreeSet<Sorter<Article>>();
				
					
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
						
						a.summarizedText =  LdaModel.getSummary(a.content, q,settings.queryLimit, settings.summarizedSentencesLimit,settings.summarize);
						LdaModel.getSummaries(recomendations,q,settings.queryLimit,settings.summarizedSentencesLimit,settings.summarize);
		
		
	
		
						LingoClustering lingo = new LingoClustering(q.getQueryString(),false);
					
						Iterator<Sorter<Article>> it = recomendations.iterator();
						int i=0;
					while(it.hasNext() && i<top)
					{
						Sorter<Article> sd = it.next();
						lingo.addDocument(sd.data.summarizedText, sd.data.getNiceTitle());
						
					}
	
					lingo.build();
					
					List<Cluster> clusters =  lingo.cluster();
					LdaModel.printClusteredResults(a.title, clusters, top, 0);
					
		
	
	
	
		
			}
		System.out.print("done");
	}
	
	
		
		
		
	

}
