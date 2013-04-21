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

public class CsvWriter {

	String delimeter = ";";

	 BufferedWriter writer;

	public static CsvWriter create(String path, String delimeter, String encoding,boolean append) {
		File f;

		OutputStream os;
		OutputStreamWriter osw = null;

		try {
			f = ensureExists(path,!append);
		
			os = (OutputStream) new FileOutputStream(f,append);

			osw = new OutputStreamWriter(os, encoding);
			
		} catch (IOException e1) {

			e1.printStackTrace();
		
		}
		

		CsvWriter writer = new CsvWriter();
writer.writer = new BufferedWriter(osw);
		writer.delimeter = delimeter;

		return writer;

	}
	
	public void writeLine(String...params) throws IOException
	{
		int i=0;
		for(String str:params)
		{
			writer.write(str);
			if(i<params.length)
				writer.write(delimeter);
			i++;
		}
		writer.newLine();
		
	}
	
	public void finish() throws IOException
	{
		writer.flush();
		writer.close();
	}

	private static File ensureExists(String path,boolean delete) throws IOException {
		File f = new File(path);
		if(f.exists() && delete)
			f.delete();
		return f;

	}

}
