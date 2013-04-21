package com.fiit.lusinda.topicmodelling;

public class DocumentProbability implements Comparable{

DocumentAttributes docAttributes = new DocumentAttributes();
	public double prob;
	
	public DocumentProbability()
	{
		prob = 0.0;
	
	}
	public DocumentProbability(String docId,double prob) 
	{
		this.docAttributes.docId = docId;
		this.prob = prob;
	}
	
	public DocumentAttributes getDocAttributes()
	{
		return docAttributes;
	}
	public void setDocAttributes(DocumentAttributes docAtts)
	{
	 docAttributes = docAtts;
	}
	 public int compareTo(Object o1) {
	        if (this.docAttributes.docId == ((DocumentProbability) o1).docAttributes.docId)
	            return 0;
	        else if ((this.prob) > ((DocumentProbability) o1).prob)
	            return 1;
	        else
	            return -1;
	    }
	
}
