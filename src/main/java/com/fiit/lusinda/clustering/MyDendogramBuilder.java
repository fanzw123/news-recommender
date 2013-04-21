package com.fiit.lusinda.clustering;

import com.fiit.lusinda.entities.Article;
import com.fiit.lusinda.similarity.JackardSimilarity;

import ch.usi.inf.sape.hac.ClusteringBuilder;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramNode;
import ch.usi.inf.sape.hac.dendrogram.MergeNode;
import ch.usi.inf.sape.hac.dendrogram.ObservationNode;
import ch.usi.inf.sape.hac.experiment.Experiment;



public final class MyDendogramBuilder implements ClusteringBuilder {

    private final DendrogramNode[] nodes;
    private MergeNode lastMergeNode;
    ResultsDocuments resultsDocuments;


    public MyDendogramBuilder(final int nObservations,ResultsDocuments experiment) {
    	this.resultsDocuments = experiment;
        nodes = new DendrogramNode[nObservations];
        for (int i = 0; i<nObservations; i++) {
            nodes[i] = new MyObservationNode(i);
        }
    }

    public final void merge(final int i, final int j, final double dissimilarity) {
        final MergeNode node = new MergeNode(nodes[i], nodes[j], dissimilarity);
        nodes[i] = node;
        lastMergeNode = node;
        
        if( nodes[i].getLeft().getLeft() instanceof MyObservationNode && nodes[i].getRight() instanceof MyObservationNode)
        {
        int[] commonWords =JackardSimilarity.getCommonWords(resultsDocuments.get(((MyObservationNode) nodes[i].getLeft())
    					.getObservation()).keywords,
    					resultsDocuments.get(((MyObservationNode) nodes[i].getRight())
    	    					.getObservation()).keywords);
    String resukt = resultsDocuments.getWords(commonWords, ", ");
    System.out.println(resultsDocuments.get(((MyObservationNode) nodes[i].getLeft())
			.getObservation()).title);
    System.out.println(resultsDocuments.getWords(resultsDocuments.get(((MyObservationNode) nodes[i].getLeft()).getObservation()).keywords,", "));
    System.out.println(resultsDocuments.get(((MyObservationNode) nodes[i].getRight())
			.getObservation()).title);
    System.out.println(resultsDocuments.getWords(resultsDocuments.get(((MyObservationNode) nodes[i].getRight()).getObservation()).keywords,", "));
    System.out.println(resukt);
    
    Article a1 = resultsDocuments.get(((MyObservationNode) nodes[i].getLeft()).getObservation());
    Article a2 = resultsDocuments.get(((MyObservationNode) nodes[i].getRight()).getObservation());
    JackardSimilarity jack = new JackardSimilarity();
    double score =  jack.computeSimilarity(a1.keywords,a2.keywords);
    System.out.println(score);
    System.out.println();
        }
    			
    }

    public final MyDendogram getDendrogram() {
        if (nodes.length==1) {
            return new MyDendogram(nodes[0],resultsDocuments);
        } else {
            return new MyDendogram(lastMergeNode,resultsDocuments);
        }
    }

}
