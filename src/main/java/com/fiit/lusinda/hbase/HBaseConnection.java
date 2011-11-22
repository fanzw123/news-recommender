package com.fiit.lusinda.hbase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Put;
public class HBaseConnection {

	private HBaseConfiguration conf;
	private Map<String, HTable> tables;
	private HBaseAdmin admin;

	public HBaseConnection(){
		conf = new   HBaseConfiguration();
	//	admin = new HBaseAdmin(conf);
	//	conf.addResource(new Path("/Users/teo/DP/hbase/conf/hbase-site.xml"));
		
		tables = new HashMap<String, HTable>();
	}

	public HTable getTable(String tableName) throws IOException {
		HTable table = tables.get(tableName);
	
		
		if (table == null) {
			
			table = new HTable(conf, tableName);
			tables.put(tableName, table);
		}
		return table;
	}
	
//	public void Put(String tableName,String key,String column,byte[] value,long ts) throws IOException
//	{
//		HTable table = getTable(tableName);
//
//	Put put = new Put(Bytes.toBytes(key),ts);
//	put.
//		
//		
//	}
}
