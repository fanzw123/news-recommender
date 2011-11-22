package com.fiit.lusinda.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Logging {

	private static final Logger LOG = LoggerFactory
			.getLogger(Logging.class);
	
	public static void Log(String msg)
	{
		LOG.info(msg);
	}
}
