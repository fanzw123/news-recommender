package com.fiit.lusinda.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.hadoop.mapred.join.Parser.NumToken;

import com.fiit.lusinda.hbase.HBaseProxyManager;
import com.fiit.lusinda.rss.FeedSettings;
import com.fiit.lusinda.similarity.CosineSimilarity;
import com.fiit.lusinda.topicmodelling.HLda;
import com.fiit.lusinda.topicmodelling.Lda;
import com.fiit.lusinda.topicmodelling.LdaModel;
import com.fiit.lusinda.topicmodelling.LdaProperties;
import com.fiit.lusinda.utils.Logging;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;

public class DocumentStream {

	long ts;

	
	LdaProperties properties;

//	private List<String> docContent = new ArrayList<String>();
	private List<String> documents = new ArrayList<String>();
	
	
	private WordTopicMatrix wordTopicMatrix;
	// private List<Topic> topics = new ArrayList<Topic>();

	// private List<String> docId= new ArrayList<String>();
	// private List<double[]> results = new ArrayList<double[]>();
	// private List<double[]> topics= new ArrayList<double[]>();

	InstanceList list;
	InstanceList testing;
	String rootDir;
	LdaModel lda;
//	int maxWordsPerTopic;
	FeedSettings settings;
	
	public LdaModel getModel()
	{
		return lda;
	}
	
	public long getTs()
	{
		return ts;
	}
	
	public WordTopicMatrix getWtMatrix()
	{
		return wordTopicMatrix;
	}

	public DocumentStream(long ts, String rootDir,FeedSettings settings) {
		this.ts = ts;
		this.rootDir = rootDir + "/" + ts + "/";
		//this.maxWordsPerTopic =maxWordsPerTopic;
		this.settings = settings;
		
		list = LdaModel.createInstanceList(this.ts);
		testing = new InstanceList(list.getPipe());
		lda = new LdaModel();
	}

//	public List<String> getDocs() {
//		return docContent;
//	}

//	public String getDocId(int id) {
//		Document doc=  (Document)documents.get(id);
//				return doc.title;
//	}

//	public boolean containsWord(String word) {
//		int index = lda.getAlphabet().lookupIndex(word);
//
//		return index > 0 ? true : false;
//
//	}

	public Alphabet getAlphabet()
	{
		return lda.getAlphabet();
	}
	
	public void addDocument(String key,String body, String title) {
		//this.docContent.add(body);
		
		list.addThruPipe(new Instance(body, key,title, null));
		
		//Document doc = new Document(this.ts,-1,title,body);
		//this.documents.add(doc);
		this.documents.add(body);
		
		//return doc;

	}

	public void processStreamData() {
	//	list = LdaModel.createInstanceList(docContent);

		properties = LdaModel.getDefaultProperties(rootDir,settings);
//		properties.averageNumRuns=3;
//		properties.evaluate = true;
//		properties.maxTopics = 200;
//		properties.topicIncrement = 10;
//		properties.topics = 40;
//		properties.heldOut = false;
		
		properties.topics = 70;
		properties.maxTopics =properties.topics ;
		
//		if(properties.estimateTopicCountsUsingHLDA)
//		{
//			
//			HLda hlda = new HLda();
//		
//			hlda.initialize(list, null, 2, new Randoms());
//			
//			hlda.estimate(5000);
//			properties.topics = hlda.getNumOfChildren();
//			properties.maxTopics = properties.topics;
//			
//		}
		
		lda.init(properties.topics, list, null, rootDir);
		
		try {
			
//			List<String> docs = new ArrayList<String>();
//			
//			for(SimilarItem d:documents)
//			{
//				Document doc = (Document) d;
//				docs.add(doc.content);
//			}
				
			
			
			lda.run(properties);
			lda.getLda().setDocuments(documents);
			
		//	if(!settings.localStore)
		//	{
				lda.getLda().calculateDiagnostics();
				lda.getLda().calculatePhi();
				lda.getLda().findBigrams3();
				lda.getLda().convertDocumentsToArticles();
		//	}
			
		//	computeSimilarities();
	
				//lda.getLda().findBigrams3();
			
			//Lda model = lda.getLda();
			//model.topicPhraseXMLReport(new PrintWriter(System.out),20);
			
		} catch (Exception e) {

			Logging.Log("unable to run lda.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally
		{
		
		//	cleanup();
		}

	}


	public void cleanup()
	{
		
	list = new InstanceList(list.getPipe());
	}

	
	
	public void persistentDocumentStream() throws IOException, URISyntaxException
	{
	//	lda.getLda().write(new File(this.rootDir+"model.mallet"));
	//	lda.getLda().printTopWords(new File(this.rootDir+"words.txt"), 10, false);
		
	//	if(settings.localStore)
			lda.getLda().write(new File(this.rootDir+"model.mallet"));
		
		//persistent words per topic
		if(settings.hbaseImport)
		{
			
			
			
		List<Phrases> phrasesList = lda.getLda().getPhrases();
		
		for(int k=0;k<phrasesList.size();k++)
		{
			Phrases phrases = phrasesList.get(k);
			List<NGram> words = phrases.getSortedNGrams(settings.maxWordsPerTopic, 0);
			
				HBaseProxyManager.getProxy().putWords(words,k,ts);
		}
		}
		
	}
	
	
	
	public void computeSimilarities() throws FileNotFoundException, UnsupportedEncodingException {
		// assign distributions
		//for (int i = 0; i < documents.size(); i++)
			//documents.get(i).addTopicDist(this.ts,lda.getDocumentTopic(i));
//
//		Logging.Log("documents created");
//		
		
	//	wordTopicMatrix = new WordTopicMatrix(lda,lda.getLda().getPhi(),lda.getAlphabet());
		

		//build net


//		for (int i = 0; i < properties.topics; i++)
//			wordTopicMatrix.setTopicDistribution(i,this.ts,
//					lda.getTopicWordDistribution(i));

		
		
	//	Logging.Log("wordTopicMatrix probabilites  assigned");

		
		// compute similarity
//		for (int i = 0; i < documents.size(); i++)
//			documents.get(i).cumputeSimilarItems(documents,10);
//		
//		Logging.Log("similar documents computed");


//		for (int i = 0; i < properties.topics; i++)
//			wordTopicMatrix.getTopics().get(i).cumputeSimilarItems(wordTopicMatrix.getTopics());
//		
	//	wordTopicMatrix.computeSimilarWords(ts, 10, 0.9,new File(rootDir+"/similarWords.txt"));
		
	//	List<List<WordCluster>> clusters = lda.getLda().wordsPruning();
//		wordTopicMatrix.computeSimilarWordsExtended(documents,clusters,2, 0.9,new File(rootDir+"/similarWordsExtended.txt"));
		
	//	Logging.Log("similar words computed");

	//	wordTopicMatrix.printSimilarWords(10);
		

	}

	private Instance getLastInstance()
	{
		return list.get(list.size()-1);
	}
	
	public double[] infer(String content) {

	//	InstanceList testing = new InstanceList(list.getPipe());
	//	testing.addThruPipe(new Instance(doc.content, null, doc.id, null));
		
	return 	this.lda.getLda().inferDist(content);
		
//		testing.addThruPipe(new Instance(doc.content, null,doc.title, null));
//		double[] dist = lda.infer(testing.get(testing.size()-1));
//		doc.addTopicDist(this.ts, dist);
//		
//		
//		//doc.topicDist = lda.infer(testing.get(0));
//
//		return doc;
	}
	
	public void computeSimilarTopics(DocumentStream as) throws Exception
	{
		
	
//	Lda another = (Lda) Lda.read(new File(as.rootDir+"model.mallet"));
	
	//this.lda.getLda().findSimilarTopics(another);
	
		
		//List<TreeSet<IDSorter>> result = this.wordTopicMatrix.computeSimilarTopics2(as.getWtMatrix(),new CosineSimilarity());
	
		
//		for(int i = 0;i<lda.getLda().numTopics;i++)
//		{
//			System.out.println("----------------------");
//			
//			lda.getLda().printTopics(i, result.get(i), 20);
//		}
	}

	public void assignSimilarDocuments(Document doc, double treeshold) {
	//	doc.cumputeSimilarItems(documents,10);

	}

}
