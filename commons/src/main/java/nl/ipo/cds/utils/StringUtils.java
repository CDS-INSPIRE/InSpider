package nl.ipo.cds.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;

public class StringUtils {

	/**
	 * Place single quotes around the individual entries of a comma seperated String
	 * @param csvString
	 * @return
	 */
	public static String singleQuoatanizeCSVString(String csvString) {
		StringBuffer result = new StringBuffer();
		
		char quoteChar = '\"';
		char surroundWithQuote = '\'';
		
		StrTokenizer csvTokenizer = StrTokenizer.getCSVInstance(csvString);
		csvTokenizer.setIgnoreEmptyTokens(true);
		csvTokenizer.setQuoteChar(quoteChar);

		int i = 0;
		for(String token : csvTokenizer.getTokenArray()){
			if(i>0){
				result.append(",");
			}
			// If necessary remove surroundWithQuote at first position
			if(token.charAt(0) == surroundWithQuote){
				token = token.substring(1, token.length()-1);
			}
			// If necessary remove surroundWithQuote at last position
			if(token.charAt(token.length()-1) == surroundWithQuote){
				token = token.substring(0, token.length()-2);
			}
			
			// Start with surroundWithQuote and LIKE syntax
			result.append(surroundWithQuote).append(token).append(surroundWithQuote);
			
			i++;
		}
		return result.toString();
	}

	/**
	 * Place single quotes around the individual entries in a List of Strings
	 * @param entries
	 * @return
	 */
	public static List<String> singleQuoatanizeStringList(List<String> entries) {
		List<String> quotedEntries = new ArrayList<String>(entries == null ? 0 : entries.size());
		
		if(entries != null){ 
			for (Iterator<String> iterator = entries.iterator(); iterator.hasNext();) {
				String entry = (String) iterator.next();
				quotedEntries.add("'"+entry+"'");
			}
		}
		return quotedEntries;
	}

}
