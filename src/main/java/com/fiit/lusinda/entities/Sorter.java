package com.fiit.lusinda.entities;

public class Sorter<T>  implements Comparable<T> {

	public T data;
	public double weight;
	public int modelId;
	
	public Sorter(int modelId,T data,double weight)
	{
		this.modelId= modelId;
		this.data = data;
		this.weight = weight;
	}
	
	
	
	 public final int compareTo (Object o2) {

		 
		 if (data.equals(((Sorter<T>) o2).data))
			 return 0;
		 else if (weight > ((Sorter<T>) o2).weight) {
			 	return -1;
			}
			else if (weight < ((Sorter) o2).weight) {
				return 1;
			}
			
			
			
			return -1;
			

		}
}
