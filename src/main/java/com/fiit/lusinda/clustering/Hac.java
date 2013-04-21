package com.fiit.lusinda.clustering;

import java.io.IOException;

import javax.naming.OperationNotSupportedException;

import com.fiit.lusinda.similarity.JackardSimilarity;
import com.fiit.lusinda.similarity.JsSimilarityMeasure;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.agglomeration.CompleteLinkage;
import ch.usi.inf.sape.hac.agglomeration.SingleLinkage;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramBuilder;
import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class Hac {

	public static void main(String[] args) throws IOException
	{
Dataset documents = new Dataset();
documents.loadDataset(";", "/var/lusinda/solr/mallet/9/topics-in-documents.csv");
		
		Hac hac = new Hac();
	//	hac.runHac(documents);
	
	}
	
	public void runHac(ResultsDocuments experiment,DissimilarityMeasure measure,AgglomerationMethod method)
	{
		
		DissimilarityMeasure dissimilarityMeasure = measure;
		AgglomerationMethod agglomerationMethod = method;
		MyDendogramBuilder dendrogramBuilder = new MyDendogramBuilder(experiment.getNumberOfObservations(),experiment);
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
		
		clusterer.cluster(dendrogramBuilder);
		MyDendogram dendrogram = dendrogramBuilder.getDendrogram();	
		
		dendrogram.dump();
	}
	
	public void runHac(ResultsDocuments dataset) throws OperationNotSupportedException
	{
		
		
		Experiment experiment = dataset;
		DissimilarityMeasure dissimilarityMeasure = new JsSimilarityMeasure();
		AgglomerationMethod agglomerationMethod = new CompleteLinkage();
		DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
		clusterer.cluster(dendrogramBuilder);
		Dendrogram dendrogram = dendrogramBuilder.getDendrogram();	
		
		dendrogram.dump();
	}
	
}
