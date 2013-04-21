package com.fiit.lusinda.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fiit.lusinda.similarity.JsSimilarityMeasure;

public class Document extends SimilarItem{

	public String title;
	public String content;
	
	

	public Document(long ts,int id, String title, String content) {
		super(ts,id);
		this.title = title;
		this.content = content;
		
		
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return title;
	}

//	public void cumputeSimilarDocs(List<Document> documents, double treeshold) {
//		for (Document doc : documents) {
//			if (doc.id.equals(this.id))
//				continue;
//			if(doc.ts!=this.ts)
//				continue;
//
//			double result = 1 - js.computeSimmilarity(getTopicDist(),
//					doc.getTopicDist());
//
//			if (result > treeshold)
//				similarDocs.add(doc);
//
//		}
//	}

}
