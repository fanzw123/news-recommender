package com.fiit.lusinda.entities;

public class DateTimeTickStrategy implements TickStrategy {

	@Override
	public long getTick(long previous) {

		return new java.util.Date().getTime();
	}

}
