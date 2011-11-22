package com.fiit.lusinda.entities;

public class TsObject {
	
	public int maxFeeds;
	int currFeeds = 0;
	long currTs;
	public long ts_interval;

	TsChangeListener listener;
	
	public TsObject(TsChangeListener listener)
	{
		this.listener = listener;
		this.currTs = new java.util.Date().getTime();
	}
	
	public void start()
	{
		listener.onStart();
		
	}
	
	public long getCurrTs()
	{
		return currTs;
	}
	
	public int getCurrFeeds()
	{
		return currFeeds;
	}
	
	public void increment()
	{
		refreshTs();
		if(currFeeds == 0)
			listener.onChange();
		
		currFeeds++;
		
//		boolean refresh = refreshTs();
//		if(refresh)
//			listener.onChange();
	}
	
	private boolean refreshTs() {
		if (currFeeds >= maxFeeds) {
			currTs = new java.util.Date().getTime();
			currFeeds = 0;
			return true;
		} else
			return false;
		// currFeeds++;

		// return currTs;

	}
	
}
