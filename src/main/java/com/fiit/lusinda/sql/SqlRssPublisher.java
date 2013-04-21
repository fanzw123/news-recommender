package com.fiit.lusinda.sql;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.io.FeedException;

public class SqlRssPublisher {

	public String outputPath;
	public int maxArticles;
	public int maxPerTs;
	public String cmd;
	SmeDataSource smeDs;
	String userName;
	String pwd;
	String db="jdbc:mysql://localhost/sme_sk";

	public SqlRssPublisher() {

	}

	public void doImport() throws IllegalArgumentException, SQLException,
			IOException, FeedException {
		File importDir = new File(outputPath);
		if (!importDir.exists())
			importDir.mkdirs();

		smeDs = new SmeDataSource();

		smeDs.connect(userName,pwd,db);

		smeDs.setSelectCmd(cmd);

		smeDs.saveSqlAsRss(outputPath, maxPerTs);

	}

	public static void main(String[] args) throws IllegalArgumentException,
			SQLException, IOException, FeedException {
		
		SqlRssPublisher sqlImport = new SqlRssPublisher();
		if(args.length!=5)
		{
			sqlImport.outputPath = "/master/sme_rss/zahranicie";
			sqlImport.cmd = "SELECT * FROM sme_sk.articles where category like 'Zahran%ie' order by published_at LIMIT 8000";
			sqlImport.maxPerTs = 300;
			sqlImport.userName = "root";
			sqlImport.pwd = "root";
		}
		else
		{
			sqlImport.outputPath = args[0];
			sqlImport.cmd = args[1];
			sqlImport.maxPerTs=Integer.parseInt(args[2]);
			sqlImport.userName = args[3];
			sqlImport.pwd = args[4];
		}
		Logging.Log("import started");
		
		
		sqlImport.maxArticles = 88494;
		
		
		sqlImport.doImport();

		Logging.Log("import end");
	}

}
