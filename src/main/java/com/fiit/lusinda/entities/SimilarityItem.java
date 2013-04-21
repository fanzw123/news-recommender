package com.fiit.lusinda.entities;



public class SimilarityItem implements Comparable<SimilarityItem> {

	public SimilarItem item;
	public double weight;
	
	
	public SimilarityItem(SimilarItem i, double weight)
	{
		this.item = i;
		this.weight = weight;
	}
	
	  public int compareTo(SimilarityItem other) {
	      return Double.compare(weight, other.weight);
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	      if (!(o instanceof IntDoublePair)) {
	        return false;
	      }
	      SimilarityItem other = (SimilarityItem) o;
	      return Double.compare(weight,other.weight)==0 && item.id==other.item.id;
	    }
	    
	    @Override
	    public int hashCode() {
	      return (int) Double.doubleToLongBits(weight) ^ item.id;
	    }
	    
}
