package com.fiit.lusinda.entities;

public class TsObject {
	
	public int maxFeeds;
	int currFeeds = 1;
	long currTs;
	public long ts_interval;

	TsChangeListener listener;
	TickStrategy strategy;
	
	public TsObject(TsChangeListener listener,TickStrategy strategy)
	{
		this.listener = listener;
		this.strategy =strategy;
		this.currTs =strategy.getTick(currTs);
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
		listener.onTick();
		
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
		//	currTs= new java.util.Date().getTime();
			this.currTs =strategy.getTick(currTs);
			currFeeds = 0;
			return true;
		} else
			return false;
		// currFeeds++;

		// return currTs;

	}
	
}
