package com.fiit.lusinda.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

	public static void copyStreams(InputStream in,OutputStream out) throws IOException
	{
	 byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
    }
    in.close();
    out.close();
	}
}
