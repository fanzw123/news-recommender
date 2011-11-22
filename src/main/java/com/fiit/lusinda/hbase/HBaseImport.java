package com.fiit.lusinda.hbase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.fiit.lusinda.entities.Keyword;
import com.fiit.lusinda.entities.RssFeed;
import com.fiit.lusinda.utils.Logging;

public class HBaseImport {

	
	 private HTable articles;
	 private HTable words;
	 
	 public HBaseImport() throws IOException
	 {
		 articles =  ConnectionManager.getConnection().getTable("articles");
		 articles.setAutoFlush(true);
		 
		 words =  ConnectionManager.getConnection().getTable("words");
		 words.setAutoFlush(true);
		 
		 
	 }
	 
	public void importRssFeed(RssFeed feed)
	{
		//TODO temporary to file
		try {
			
			//TODO get normalized URL
			
			
			
			Put put = new Put(Bytes.toBytes(feed.getKey()),feed.ts);
			
			put.add(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(feed.title));

			put.add(Bytes.toBytes("info"), Bytes.toBytes("prep_body"), Bytes.toBytes(feed.preprocessedBody));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("body"), Bytes.toBytes(feed.originalBody));

			put.add(Bytes.toBytes("info"), Bytes.toBytes("link"), Bytes.toBytes(feed.link));

			articles.put(put);
			
			
			for(Keyword keyword: feed.keywords)
			{
				put = new Put(Bytes.toBytes(keyword.getNormalizedKeyword()),feed.ts);
				put.add(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(keyword.name));		
				put.add(Bytes.toBytes("info"), Bytes.toBytes("escaped_name"), Bytes.toBytes(keyword.escapedName));		
				
				put.add(Bytes.toBytes("a"), Bytes.toBytes(feed.getKey()), Bytes.toBytes(keyword.getScore()));		

				words.put(put);
			}
			
			
			
			Logging.Log("imported to hbase");
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
