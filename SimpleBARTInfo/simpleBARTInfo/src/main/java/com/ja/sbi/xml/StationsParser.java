package com.ja.sbi.xml;

import com.ja.sbi.beans.Station;
import com.ja.sbi.utils.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * simple rss feed parser that puts data into a table
 * 
 * @author Joseph Acosta
 *
 */
public class StationsParser extends DefaultHandler {
	
	// Used to define what elements we are currently in
	private boolean inStationTag = false;
	private boolean inNameTag = false;
	private boolean inAbbr = false;

	// the storage of the feed in a map for the database insert
	private List<Station> stations = new ArrayList<Station>();
	
	public void startElement(String uri, String name, String qName, Attributes atts) {
		
		if (name.trim().equals("station")) {
            inStationTag = true;
            Station xStation = new Station();
            stations.add(xStation);
        } else if (name.trim().equals("name")) {
        	inNameTag = true;
        } else if ( name.trim().endsWith("abbr") ) {
        	inAbbr = true;
		}
	}
	
	public void endElement(String uri, String name, String qName) {
	
		if (name.trim().equals("station")) {
            inStationTag = false;
        } else if (name.trim().equals("name")) {
        	inNameTag = false;
        } else if ( name.trim().endsWith("abbr") ) {
        	inAbbr = false;
		}
	}
	 
	public void characters(char ch[], int start, int length) {
	
		final String xmlData = String.valueOf(ch).substring(start, start+length);
		
		if ( this.inStationTag && xmlData != null ) {
			// get the station we are working on
			final int size = stations.size();
			Station xStation = stations.get(size - 1);
			if ( this.inNameTag ) {
				xStation.setStationName( XMLUtils.append(xStation.getStationName(), xmlData) );
			} else if ( this.inAbbr ) {
				xStation.setShortName( XMLUtils.append(xStation.getShortName(), xmlData) );
			}
		}
	}	

	public List<Station> parseDocument(String urlContent) 
		throws IOException, SAXException, ParserConfigurationException {

		if ( ! isValidRSS(urlContent) ) {
			throw new IOException("Not valid XML data!");
		}
		
		this.inStationTag = false;
		this.inNameTag = false;
		this.inAbbr = false;
		
		this.stations = new ArrayList<Station>();
		
		final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		final SAXParser saxParser = saxFactory.newSAXParser();
		final XMLReader reader = saxParser.getXMLReader();
		reader.setContentHandler(this);
		reader.parse(new InputSource(new StringReader(urlContent)));
		
		return this.stations;
	}
	
	private boolean isValidRSS(String xmlData) {
        return xmlData != null && xmlData.trim().indexOf("<?xml") == 0 && xmlData.indexOf("<stations") != -1;
    }

}
