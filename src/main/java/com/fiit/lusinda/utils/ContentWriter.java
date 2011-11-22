package com.fiit.lusinda.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class ContentWriter {

	
	String encoding;
	String path;


	public static ContentWriter create(String path,String encoding) {
		

		
		try {
			ensureExists(path);
		
		
		} catch (IOException e1) {

			e1.printStackTrace();
		
		}
		

		ContentWriter writer = new ContentWriter();
writer.encoding = encoding;
writer.path = path;

		return writer;

	}
	
	public void WriteContent(String id,String content) throws IOException
	{
		OutputStream os;
		OutputStreamWriter osw = null;
		File f;
		
			f= new File(path+"/"+id);
			
		os = (OutputStream) new FileOutputStream(f);

		osw = new OutputStreamWriter(os, encoding);
		
		BufferedWriter bw = new BufferedWriter(osw);
		
		bw.write(content);
		bw.flush();
		bw.close();
	}
	
	


	private static File ensureExists(String path) throws IOException {
		File f = new File(path);
		if (!f.exists())
			f.mkdirs();
		
		return f;

	}

}
