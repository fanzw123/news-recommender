package com.fiit.lusinda.clustering;

import com.fiit.lusinda.similarity.JackardSimilarity;

import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.MergeNode;

public final class MyDendogram {

	private final DendrogramNode root;

	private ResultsDocuments resultsDocuments;

	public MyDendogram(final DendrogramNode root, ResultsDocuments resultsDocuments) {
		this.root = root;
		this.resultsDocuments = resultsDocuments;
	}

	public DendrogramNode getRoot() {
		return root;
	}

	public void dump() {
		dumpNode("  ", root);
	}
	
	private  void getCommonWords(int[] commonWords,final DendrogramNode node)
	{
		if (node == null) {
			return;
		} else if (node instanceof MyObservationNode) {
			
			if(commonWords==null)
				commonWords = resultsDocuments.get(((MyObservationNode) node)
						.getObservation()).keywords;
			
			
			
			commonWords = JackardSimilarity.getCommonWords(commonWords,resultsDocuments.get(((MyObservationNode) node)
					.getObservation()).keywords );
			
		//	return commonWords;
						
		} else if (node instanceof MergeNode) {
			
			
			 getCommonWords(commonWords, ((MergeNode) node).getLeft());
			 getCommonWords(commonWords, ((MergeNode) node).getRight());
		}
	}

	private void dumpNode(final String indent, final DendrogramNode node) {
		if (node == null) {
			System.out.println(indent + "<null>");
		} else if (node instanceof MyObservationNode) {
			System.out
					.println(indent+
							resultsDocuments.get(((MyObservationNode) node)
									.getObservation()).title);
		} else if (node instanceof MergeNode) {
		//	int[] commonWords = resultsDocuments.get(((MyObservationNode) node)
			//		.getObservation()).keywords; 
			//getCommonWords(commonWords, node);
			System.out.println(indent + "---");
			dumpNode(indent + "  ", ((MergeNode) node).getLeft());
			dumpNode(indent + "  ", ((MergeNode) node).getRight());
		}
	}
}
