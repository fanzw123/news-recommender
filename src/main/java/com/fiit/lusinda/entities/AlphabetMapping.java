package com.fiit.lusinda.entities;

import cc.mallet.types.Alphabet;

public class AlphabetMapping {

	private Alphabet source;
	private Alphabet target;
	public int[] mapping;

	public AlphabetMapping(Alphabet source,Alphabet target)
	{
		this.source = source;
		this.target = target;
		
		mapping = new int[source.size()];
	}
	
	
	
	public int[] map(int[] sourceIndicies,int[] targetIndicies)
	{
		int[] mappedIndicies = new int[sourceIndicies.length];
		
		for(int i=0;i<sourceIndicies.length;i++)
		{
			int mapped = map(sourceIndicies[i]);
			if(mapped==-1)
			{
				mappedIndicies[i] = -1;
				continue;
			}
			for(int j=0;j<targetIndicies.length;j++)
			{
				if(targetIndicies[j]==mapped)
				{
					mappedIndicies[i] = sourceIndicies[i];
					break;
				}
				
			}
			
		}
		
		return mappedIndicies;
	}
	
	public int map(int sourceId) {
		
		if (mapping[sourceId] == 0) {
			mapping[sourceId] = target.lookupIndex(source.lookupObject(sourceId));
			
		}
		
		return mapping[sourceId];
	}
}
