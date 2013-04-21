package com.fiit.lusinda.hbase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.KeywordsCluster;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.entities.Sorter;
import com.fiit.lusinda.entities.StringDoublePair;
import com.fiit.lusinda.utils.Logging;

public class HBaseProxyManager {

	
	private HBaseProxyManager()
	{
		
	}
	 
	 private static HBaseProxy proxy;
	 
	 public static HBaseProxy getProxy() throws IOException
	 {
		 if(proxy==null)
			 proxy = HBaseProxy.create();
		 
		 return proxy;
	 }
	 
//	 public void importTopics()
//	 {
//		 
//	 }
//	 
//	 public void importWords()
//	 {
//		 
//	 }
//	 
//	 public void importDocTopics()
//	 {
//		 
//	 }
//	 
//	 public void importSimilarTopics()
//	 {
//		 
//	 }
//	 
//	 public void importSimilarWords()
//	 {
//		 
//	 }
//	 
//	 public void importSimilarDocuments()
//	 {
//		 
//	 }
//	 
	 
	 
	 public static void main(String[] args) throws IOException, URISyntaxException 
	 {
		 
	//	 HBaseProxyManager.getProxy().dumpArticles();
		 
		// String key = "sk_sme_Štefan_Skrúcaný_dostal_dva_roky_podmienečne";
		 String key = "sk_sme_Ako_doviezť_auto_z_Českej_republiky?";
		 Map<String,KeywordsCluster> clusters  = HBaseProxyManager.getProxy().getKeywordsClusters(key);
		 
		 TreeSet<StringDoublePair> sorted = new TreeSet<StringDoublePair>();
		 
		 
		 for(Entry<String,KeywordsCluster> e:clusters.entrySet())
		 {
			 double weight = e.getValue().sum;//* e.getValue().documents.size();
			// int count = e.getKey().split(" ").length;
			 //weight = weight * count*count;
			 sorted.add(new StringDoublePair(e.getKey(), weight));
			
			// System.out.print(e.getKey());
			 //System.out.print(" "+e.getValue().sum);
			 //System.out.println();
//			 List<org.carrot2.core.Document> docs = e.getValue().documents;
//			 for(org.carrot2.core.Document a:docs)
//					 System.out.println(a.getTitle());
			
			 
		 }
		 
		 Iterator<StringDoublePair> it = sorted.iterator();
		 while(it.hasNext())
		 {
			 StringDoublePair sd= it.next();
			 
			 System.out.print(sd.name);
			 System.out.print(" "+sd.value);
			 System.out.println();
			 
		 }
	 }
	
	
	
	
	
}
