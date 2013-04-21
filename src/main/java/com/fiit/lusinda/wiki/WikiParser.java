package com.fiit.lusinda.wiki;

import java.io.FileReader;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

public class WikiParser {

	private WikiParser()
	{
		
	}
	
	MediaWikiParser parser;
	
	public void setParser(MediaWikiParser p)
	{
		this.parser = p;
	}
	
	private static WikiParser getParser()
	{
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		MediaWikiParser parser = pf.createParser();
		WikiParser p = new WikiParser();
		p.setParser(parser);
		return p;
		
		
		
		
	}
	
	public void parse(String text)
	{
		ParsedPage pp = parser.parse(text);
        
		// only the links to other Wikipedia language editions
		for (Link language : pp.getLanguages()) {
		    System.out.println(language.getTarget());
		}
		    
		//get the internal links of each section
		for (Section section : pp.getSections()){
		    System.out.println("Section: " + section.getTitle());

		    for (Link link : section.getLinks(Link.type.INTERNAL)) {
		        System.out.println("  " + link.getTarget());
		    }
		}

	}
	
	
	public static void main(String[] args) throws Exception {
		

		// get a ParsedPage object
			
			WikiParser parser = WikiParser.getParser();
			
				XMLInputFactory f = XMLInputFactory.newInstance();
				InputStream in = WikiParser.class.getResourceAsStream(
				        "/wiki5000.xml");
				XMLStreamReader reader = f.createXMLStreamReader(in);

				while(reader.hasNext()){
				    int eventType = reader.next();

				    
				    
				    if(eventType == XMLStreamReader.CHARACTERS){
				    	String n = reader.getLocalName();
				    	if(n.equals("text"))
				    	{
				    		parser.parse(reader.getText());
				    	}
				    }
				    

				    //... more event types handled here...
				}
				
				
	}
	
	
	
}
