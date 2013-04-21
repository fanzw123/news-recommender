package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Bigram {

	public String bigram;

	private String first;
	private String second;

	public List<Bigram> successors = new ArrayList<Bigram>();

//	private List<Bigram> ancestors = new ArrayList<Bigram>();

	
	public boolean isRoot(Map<String, Bigram> bigrams_dict) {
		
		for (Bigram bgram : bigrams_dict.values()) {
			if (!this.bigram.equals(bgram.bigram)
					&& this.getFirst().equals(bgram.getSecond())) {
				return false;
			}
		}
			
		return true;
		
	//	return ancestors.size() == 0;
	}

	public List<String> getPhrases() {
		List<String> phrases = new ArrayList<String>();
		Stack<Bigram> notVisited = new Stack<Bigram>();
	

		this.getPhrasesInternal(notVisited, phrases,this.getFirst()+"_",0);

		return phrases;
	}

	public void getPhrasesInternal(Stack<Bigram> notVisited, List<String> phrases,String phrase,int i) {
		
		i++;
		if(i==20)
		{
			int a=3;
			a++;
		
			
		}
		if(successors.size()==0)
		{
			if(!notVisited.empty())
				notVisited.pop().getPhrasesInternal(notVisited, phrases, phrase,i);
			
			phrases.add(phrase+this.getSecond()+"_");
		}
		else
		{
			
			for (Bigram bgram : successors) {
				notVisited.push(bgram);
		
			}
			
			Bigram next = notVisited.pop();
			next.getPhrasesInternal(notVisited, phrases, phrase+next.getFirst() + "_",i);
			
		}
	
		
	
	

	}

	public void buildLinks(Map<String, Bigram> bigrams_dict) {
	//	findAncestors(bigrams_dict);
		findSuccesors(bigrams_dict,0);
	}

	public void findSuccesors(Map<String, Bigram> bigrams_dict,int i) {
		i++;
		if(i==20)
		{
			int a=3;
			a++;
		
			
		}
		for (Bigram bgram : bigrams_dict.values()) {
			if (!this.bigram.equals(bgram.bigram)
					&& this.getSecond().equals(bgram.getFirst()) &&
					!this.getFirst().equals(bgram.getSecond()) && !successors.contains(bgram)  ) {
				successors.add(bgram);
			}
		}

		for (Bigram bgram : successors)
			bgram.findSuccesors(bigrams_dict,i);
	}
//
//	public void findAncestors(Map<String, Bigram> bigrams_dict) {
//		for (Bigram bgram : bigrams_dict.values()) {
//			if (this.bigram.equals(bgram.bigram)
//					&& this.getFirst().equals(bgram.getSecond())) {
//				ancestors.add(bgram);
//			}
//		}
//
//		for (Bigram bgram : ancestors)
//			bgram.findAncestors(bigrams_dict);
//	}

	public Bigram(String bigram) {
		this.bigram = bigram.replaceAll("b_", "");
		String[] parts = this.bigram.split("_");
		this.first = parts[0];
		this.second = parts[1];
	}

	public String getFirst() {
		return first;
	}

	public String getSecond() {
		return second;
	}
}
