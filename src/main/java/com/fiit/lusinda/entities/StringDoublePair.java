package com.fiit.lusinda.entities;

public class StringDoublePair  implements Comparable {

	public int id;
	public String name;
	public double value;
	
	public StringDoublePair(String name,double value)
	{
		this(-1, name, value);
	}
	
	public StringDoublePair(int id,String name,double value)
	{
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	 public final int compareTo (Object o2) {

			if (value > ((StringDoublePair) o2).value) {
				return -1;
			}
			else if (value < ((StringDoublePair) o2).value) {
				return 1;
			}
			
			String otherString = ((StringDoublePair) o2).name;
			
			return otherString.compareTo(name);
			
					 
		
		}
}
