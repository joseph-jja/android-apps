package com.ja.sbi.utils;

public class XMLUtils {

	public static String append(String initialString, String xmlData) {
		
	    if ( initialString != null && initialString.length() > 0 ) {
		    return initialString + xmlData;
	    }
        
    	return xmlData;		
    }

}
