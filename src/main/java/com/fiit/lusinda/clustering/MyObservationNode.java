package com.fiit.lusinda.clustering;

import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;

public final class MyObservationNode implements DendrogramNode {

	private final int observation;


	public MyObservationNode(final int observation) {
		this.observation = observation;
	}

	public final DendrogramNode getLeft() {
		return null;
	}

	public final DendrogramNode getRight() {
		return null;
	}

	public int getObservationCount() {
		return 1;
	}

	public final int getObservation() {
		return observation;
	}
	
	public String toString()
	{
		return Integer.toString(this.observation);
	}

}