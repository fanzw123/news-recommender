package com.fiit.lusinda.clustering;

import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.MergeNode;

public final class MyDendogram {

	private final DendrogramNode root;

	private Dataset dataset;

	public MyDendogram(final DendrogramNode root, Dataset dataset) {
		this.root = root;
		this.dataset = dataset;
	}

	public DendrogramNode getRoot() {
		return root;
	}

	public void dump() {
		dumpNode("  ", root);
	}

	private void dumpNode(final String indent, final DendrogramNode node) {
		if (node == null) {
			System.out.println(indent + "<null>");
		} else if (node instanceof MyObservationNode) {
			System.out
					.println(indent
							+ "+("
							+ dataset.get(((MyObservationNode) node)
									.getObservation()).documentAttributes.category
							+ ")"
							+ dataset.get(((MyObservationNode) node)
									.getObservation()).documentAttributes.title);
		} else if (node instanceof MergeNode) {
			System.out.println(indent + "---");
			dumpNode(indent + "  ", ((MergeNode) node).getLeft());
			dumpNode(indent + "  ", ((MergeNode) node).getRight());
		}
	}
}
