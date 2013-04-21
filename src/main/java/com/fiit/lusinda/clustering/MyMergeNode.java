package com.fiit.lusinda.clustering;

import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;

public class MyMergeNode implements DendrogramNode {
	

		private final DendrogramNode left;
		private final DendrogramNode right;
		private final double dissimilarity;
		private final int observationCount;


		public MyMergeNode(final DendrogramNode left, final DendrogramNode right, final double dissimilarity) {
			this.left = left;
			this.right = right;
			this.dissimilarity = dissimilarity;
			observationCount = left.getObservationCount()+right.getObservationCount();
		}

		public int getObservationCount() {
			return observationCount;
		}

		public final DendrogramNode getLeft() {
			return left;
		}

		public final DendrogramNode getRight() {
			return right;
		}

		public final double getDissimilarity() {
			return dissimilarity;
		}

	}

