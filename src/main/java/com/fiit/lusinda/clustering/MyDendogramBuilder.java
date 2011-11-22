package com.fiit.lusinda.clustering;

import ch.usi.inf.sape.hac.ClusteringBuilder;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.MergeNode;
import ch.usi.inf.sape.hac.dendrogram.ObservationNode;



public final class MyDendogramBuilder implements ClusteringBuilder {

    private final DendrogramNode[] nodes;
    private MergeNode lastMergeNode;
    Dataset dataset;


    public MyDendogramBuilder(final int nObservations,Dataset dataset) {
    	this.dataset = dataset;
        nodes = new DendrogramNode[nObservations];
        for (int i = 0; i<nObservations; i++) {
            nodes[i] = new MyObservationNode(i);
        }
    }

    public final void merge(final int i, final int j, final double dissimilarity) {
        final MergeNode node = new MergeNode(nodes[i], nodes[j], dissimilarity);
        nodes[i] = node;
        lastMergeNode = node;
    }

    public final MyDendogram getDendrogram() {
        if (nodes.length==1) {
            return new MyDendogram(nodes[0],dataset);
        } else {
            return new MyDendogram(lastMergeNode,dataset);
        }
    }

}
