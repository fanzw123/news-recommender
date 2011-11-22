package com.fiit.lusinda.hbase;

import org.apache.hadoop.hbase.HBaseConfiguration;

public class ConnectionManager {	
	
private static HBaseConnection conn;

public static HBaseConnection getConnection()
{
	if(conn ==null)
		conn = new HBaseConnection();
	
	return conn;
}

	
	
}
