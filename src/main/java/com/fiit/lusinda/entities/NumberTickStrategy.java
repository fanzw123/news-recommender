package com.fiit.lusinda.entities;

public class NumberTickStrategy implements TickStrategy {

	@Override
	public long getTick(long previous) {

		return previous+1;
	}

}
