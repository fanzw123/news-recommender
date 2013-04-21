package com.fiit.lusinda.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.IllegalDataException;
import org.jsoup.Jsoup;

import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.translate.TranslateStrategy;
import com.fiit.lusinda.utils.Logging;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

public class SmeDataSource {

	public static Connection conn;
	
	private String selectStm;


	public SmeDataSource() {

	}

	public void setSelectCmd(String select)
	{
		this.selectStm = select;
	}
	
	public List<Article> getArticles(int count) throws SQLException
	{
		List<Article> articles = Lists.newArrayList();
		PreparedStatement stm = conn
				.prepareStatement("SELECT title,body FROM articles LIMIT ?");
		stm.setInt(1, count);
		
		ResultSet result = stm.executeQuery();
		


		while (result.next()) {
			Article a =new Article();
			
			a.title = result.getString("title");
			a.content = result.getString("body");
			articles.add(a);

		}
		
		return articles;
	}
	
	private SyndFeed buildRssFeed(String feedName,Date publishedDate) throws SQLException, IllegalArgumentException, FeedException, IOException
	{
SyndFeedInput input = new SyndFeedInput();
		
		SyndFeed feed = new SyndFeedImpl();//input.build( new XmlReader( file) );
		Date publishDate = publishedDate ;
		feed.setTitle(feedName);
		feed.setLink("dd");
		feed.setDescription("Dddd");
		//feed.setDescription( "RSS feeds of blog entries from Techblog.ph" );
		//feed.setLanguage( "en-us" );
		feed.setPublishedDate( publishDate );
		feed.setFeedType( "rss_2.0" ); 
		
		return feed;
		
	}
	
	private SyndEntry buildRssEntry(SyndFeed feed,ResultSet result) throws SQLException
	{
		SyndEntry entry = new SyndEntryImpl(); 
		entry.setTitle(  result.getString("title") );
		entry.setLink(  result.getString("url"));
		entry.setPublishedDate( result.getDate("published_at") );
		SyndContent content = new SyndContentImpl();
		content.setType( "text/plain" );
		String cleaned = Jsoup.parse(result.getString("body")).text();
		//String escaped = StringEscapeUtils.escapeXml(cleaned);
		
		content.setValue(cleaned);
		
				entry.setDescription( content );
		
		return entry;
	}

	public void saveSqlAsRss(String outputDir,int maxArticles) throws SQLException, IOException, IllegalArgumentException, FeedException {
		
				
		PreparedStatement statement = conn.prepareStatement(selectStm);//+String.valueOf(numberOfArticles));

		ResultSet result = statement.executeQuery();
		int ts = 1;
		int i=0;
		

		SyndFeed feed =null;
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		
		while (result.next()) {
			if(i>maxArticles || ts==1)
			{
				
				if(entries!=null && entries.size()>0)
				{
					feed.setEntries( entries );
					
					writeFeed(new File(outputDir+"/"+ts+".xml"), feed);
				}
				
				i=0;
				ts++;
				entries = new ArrayList<SyndEntry>();
				feed = buildRssFeed( String.valueOf(ts), result.getDate("published_at"));

				Logging.Log("fetch ts"+ts);
			}
			
			SyndEntry entry = buildRssEntry(feed, result);
			
			entries.add( entry );
			i++;
			
		}
		
	
	}
	
	private void writeFeed(File f,SyndFeed feed) throws IOException, FeedException
	{
		OutputStream out = new FileOutputStream(f);
		
	
		
		
		  Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		  SyndFeedOutput output = new SyndFeedOutput();
		  try
		  {
		         output.output(feed,writer);
		  }
		  catch(IllegalDataException e)
			{
				Logging.Log("skiped");
				if(writer!=null)
					writer.close();
			}
		         writer.close();
		 
	    
	}
	
	public void connect() {
		connect("root","root", "jdbc:mysql://localhost/sme_sk");

	}

	public void connect(String userName, String userPassword, String databaseUrl) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(databaseUrl, userName,
					userPassword);

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void disconnect() {
		try {
			conn.close();
			Logging.Log("disconnected");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logging.Log(e.getMessage());
		}
	}
	
	public void truncate(String tableName) throws SQLException
	{
		PreparedStatement statement = conn.prepareStatement("truncate "+tableName);
		
		statement.execute();
		
		
	}

	public int getCount(String stm) throws SQLException, IOException {

		PreparedStatement statement = conn.prepareStatement(stm);// +String.valueOf(numberOfArticles));

		ResultSet result = statement.executeQuery();
		int count = -1;

		while (result.next()) {
			count = result.getInt("count");

		}

		return count;

	}

	public void resetTimeSlices() throws SQLException
	{
		PreparedStatement resetStm = conn
				.prepareStatement("UPDATE Articles SET time_slice_id=NULL");

		resetStm.executeUpdate();

	}
	
	public int preprocessTimeSlice(int minSliceSize, int minDateInterval) throws SQLException {

		// get first not processed article
		PreparedStatement getLastSliceStm = conn
				.prepareStatement("SELECT published_at,max(published_at) as last_published  FROM articles where time_slice_id IS NULL order by published_at LIMIT 1");

		ResultSet result = getLastSliceStm.executeQuery();

		result.first();
		Date sliceStart = result.getDate("published_at");
		Date lastPublishedDate = result.getDate("last_published");

		getLastSliceStm.close();

		PreparedStatement getMaxLastSliceIdStm = conn
				.prepareStatement("SELECT max(time_slice_id) as max_slice_id FROM articles");

		result = getMaxLastSliceIdStm.executeQuery();

		result.first();
		
		int timeSliceID = result.getInt("max_slice_id");
		timeSliceID++; //get next slice id
		
		getMaxLastSliceIdStm.close();
		
		PreparedStatement getSliceSizeStm = conn
				.prepareStatement("select if(ADDDATE(?, ?) >= ?,-1, count(published_at)) from articles where time_slice_id IS NULL and published_at between ? and  ADDDATE(?, ?) order by published_at");
		int sliceSize = 0;
		int dateInterval = 0;
		do  {
			dateInterval += minDateInterval; // increment dateIterval size
			
			getSliceSizeStm.setDate(1, sliceStart);
			getSliceSizeStm.setInt(2, dateInterval);
			getSliceSizeStm.setDate(3, lastPublishedDate);
			
			getSliceSizeStm.setDate(4, sliceStart);
			getSliceSizeStm.setDate(5, sliceStart);
			getSliceSizeStm.setInt(6, dateInterval);

			result = getSliceSizeStm.executeQuery();
			result.first();

			sliceSize = result.getInt(1);

		}while(sliceSize < minSliceSize && sliceSize>0);
		
		getSliceSizeStm.close();

		PreparedStatement updateStm = conn
				.prepareStatement("UPDATE Articles SET time_slice_id = ? where time_slice_id IS NULL and  published_at between ? and  ADDDATE(?, ?) order by published_at");

		updateStm.setInt(1, timeSliceID);
		updateStm.setDate(2, sliceStart);
		updateStm.setDate(3, sliceStart);
		updateStm.setInt(4, dateInterval);
		
		int effected =  updateStm.executeUpdate();
		
		updateStm.close();
		
		return effected;

	}

	public void translateArticles(String[] columns, String where,
			TranslateStrategy translator) throws SQLException {

		this.translateArticles(columns, where, -1, translator);
	}
	
	public void insertToEvaluatedArticles(int article_id) throws SQLException
	{
		String insertStm = new String(
				"INSERT INTO evaluated_articles(article_id) VALUES(?)");
		
		PreparedStatement insertStatement = conn.prepareStatement(insertStm
				.toString());
		
		insertStatement.setInt(1, article_id);
		
		insertStatement.execute();
	}

	public void translateArticles(String[] columns, String where, int limit,
			TranslateStrategy translator) throws SQLException {

		StringBuilder insertStm = new StringBuilder(
				"INSERT INTO Articles_translated (Id_Article, ");

		StringBuilder selectStm = new StringBuilder();

		selectStm.append("SELECT ");
		for (String col : columns) {
			selectStm.append(col);
			selectStm.append(",");

			// insert
			insertStm.append(col);
			insertStm.append(",");

		}

		insertStm.deleteCharAt(insertStm.length() - 1);
		insertStm.append(") ");

		insertStm.append("VALUES(?, ");

		for (String col : columns) {

			// insert
			insertStm.append("?");
			insertStm.append(",");

		}
		insertStm.deleteCharAt(insertStm.length() - 1);
		insertStm.append(") ");

		selectStm.append("id");
		selectStm.append(" FROM ");
		selectStm.append("Articles ");
		if(where!=null &&!where.isEmpty())
		{
			selectStm.append("WHERE ");
			selectStm.append(where);
		}
		if (limit > 0) {
			selectStm.append(" LIMIT ");
			selectStm.append(Integer.toString(limit));
		}

		Logging.Log("Select stm:" + selectStm.toString());
		Logging.Log("Insert stm:" + insertStm.toString());

		PreparedStatement selectStatement = conn.prepareStatement(selectStm
				.toString());

		PreparedStatement insertStatement = conn.prepareStatement(insertStm
				.toString());

		PreparedStatement checkStatement = conn
				.prepareStatement("select count(*) FROM Articles_Translated where id_Article=?");

		ResultSet result = selectStatement.executeQuery();

		while (result.next()) {

			int id = result.getInt("id");

			checkStatement.setInt(1, id);
			ResultSet translatedResult = checkStatement.executeQuery();

			translatedResult.next();

			if (translatedResult.getInt(1) == 0) {

				int param = 2;

				insertStatement.setInt(1, id);

				for (String col : columns) {

					// Logging.Log(result.getString(col));

					try {
						String translated = translator.translateText(result
								.getString(col));

						insertStatement.setString(param, translated);

						param++;

					} catch (Exception ex) {
						Logging.Log(String.format(
								"article id: %d. Error msg: %s", id,
								ex.getMessage()));
					}

				}

				insertStatement.executeUpdate();
				Logging.Log(String.format("inserted article with id:%d", id));
			} else
				Logging.Log(String.format("skipping article with id:%d", id));

		}

	}
}
