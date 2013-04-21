package com.fiit.lusinda.sql;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.fiit.lusinda.adapters.SmeDataSource;
import com.fiit.lusinda.hbase.HBaseProxyManager;
import com.fiit.lusinda.utils.Logging;
import com.sun.syndication.io.FeedException;

public class HBaseSqlImport {
//0903188160 stelinka905 k40
	
	
	public int maxArticles;
	public int treshold;
	
	SmeDataSource smeDs;
	String userName;
	String pwd;
	boolean truncate =false;
	String db="jdbc:mysql://localhost/sme_sk";

	public HBaseSqlImport() {

	}

	public void doImport() throws IllegalArgumentException, SQLException,
			IOException, FeedException {
	
		smeDs = new SmeDataSource();

		smeDs.connect(userName,pwd,db);

		smeDs.truncate("evaluated_articles");

		HBaseProxyManager.getProxy().HBaseToSqlImport(smeDs,maxArticles,treshold);

		smeDs.conn.close();


	}

	public static void main(String[] args) throws IllegalArgumentException,
			SQLException, IOException, FeedException {
		
		HBaseSqlImport sqlImport = new HBaseSqlImport();
		if(args.length>=4)
		{
			sqlImport.maxArticles=Integer.parseInt(args[0]);
			sqlImport.treshold=Integer.parseInt(args[1]);
			sqlImport.userName = args[2];
			sqlImport.pwd = args[3];
			if(args[4]!=null && args[4].equals("t"))
				sqlImport.truncate = true;
			else
				sqlImport.truncate = false;

			
			
		}
		else
		{
		
			sqlImport.maxArticles = 100;
			sqlImport.treshold = 2;
			sqlImport.userName = "root";
			sqlImport.pwd = "root";
			sqlImport.truncate = true;
			
		}
		Logging.Log("import started");
		
		
	
		
		
		sqlImport.doImport();

		Logging.Log("import end");
	}

}
