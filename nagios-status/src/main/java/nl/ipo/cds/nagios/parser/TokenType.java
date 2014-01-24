package nl.ipo.cds.nagios.parser;

import java.util.ArrayList;
import java.util.List;

public enum TokenType {
	LCURLY ("{"),
	RCURLY ("}"),
	ASSIGN ("="),
	NAME,
	VALUE,
	EOF;
	
	private String punctuation;
	
	private TokenType () {
	}
	
	private TokenType (String punctuation) {
		this.punctuation = punctuation;
	}
	
	public String getPunctuation () {
		return punctuation;
	}
	
	public boolean isPunctuation () {
		return punctuation != null;
	}
	
	public static List<TokenType> getPunctuationTokenTypes () {
		final List<TokenType> list = new ArrayList<TokenType> ();
		
		for (TokenType tt: TokenType.values()) {
			if (tt.isPunctuation ()) {
				list.add (tt);
			}
		}
		
		return list;
	}
}
