package nl.ipo.cds.nagios.parser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Lexer {

	private LexerContext lexerContext;
	private LineNumberReader reader;
	private LinkedList<Token> lookahead;
	private int line = 0;
	private int column = 0;
	private char currentCharacter = 0;
	
	private static Set<Character> punctuationCharacters = new HashSet<Character> ();
	private static Map<String, TokenType> punctuationTokenTypes = new HashMap<String, TokenType> ();

	// Construct lookup tables for fast processing of punctuation characters:
	static {
		for (TokenType tt: TokenType.getPunctuationTokenTypes ()) {
			final String punctuation = tt.getPunctuation ();
			
			punctuationTokenTypes.put (punctuation, tt);
			for (char c: punctuation.toCharArray()) {
				punctuationCharacters.add (c);
			}
		}
	}
	
	/**
	 * Constructs a lexer using the given context that reads characters from the given reader. The reader will be wrapped
	 * in a BufferedReader if it is not already an instance of BufferedReader.
	 * 
	 * @param lexerContext
	 * @param reader
	 */
	public Lexer (final LexerContext lexerContext, final Reader reader) {
		if (!(reader instanceof LineNumberReader)) {
			this.reader = new LineNumberReader (reader, 1024);
		} else {
			this.reader = (LineNumberReader)reader;
		}
		this.lookahead = new LinkedList<Token> ();
	}
	
	/**
	 * Returns the next token in the stream. Always returns EOF tokens after the stream has been exhausted.
	 * 
	 * @return The next token in the stream.
	 * @throws LexerException
	 */
	public Token la () throws LexerException {
		return la (0);
	}

	/**
	 * Performs a lookahead on the token stream by the given amount (0 <= la). Intermediate tokens are buffered. If the lookahead operation scans past
	 * EOF, this method always returns an EOF token.
	 *
	 * @param la The amount of lookahead (>= 0).
	 * @return The token at the given position in the stream.
	 * @throws LexerException
	 */
	public Token la (int la) throws LexerException {
		if (la < 0) {
			throw new IllegalArgumentException ("lookahead must be positive");
		}
		
		setLookaheadSize (la + 1);
		
		if (la == 0) {
			return lookahead.getFirst ();
		} else if (la == lookahead.size () - 1) {
			return lookahead.getLast ();
		} else {
			return lookahead.get (0);
		}
	}

	/**
	 * Accepts and returns the next token in the stream. The token is removed from the lookahead buffer and can no longer
	 * be accessed using the 'la' methods.
	 * 
	 * @return The accepted token.
	 * @throws LexerException
	 */
	public Token accept () throws LexerException {
		setLookaheadSize (1);
		return lookahead.poll ();
	}

	private void setLookaheadSize (int n) throws LexerException {
		while (lookahead.size () < n) {
			nextToken ();
		}
	}
	
	private void nextToken () throws LexerException {
		
		if (line == 0) {
			line = 1;
			currentCharacter = getNextChar ();
		}
		
		while (true) {
			
			char c = acceptChar ();
			
			// Skip whitespace:
			while (c > 0 && c <= ' ') {
				c = acceptChar ();
			}
			
			// Skip comments (single line):
			if (c == '#') {
				while (c > 0 && c != '\n') {
					c = acceptChar ();
				}
				
				continue;
			}
			
			// EOF:
			if (c == 0) {
				lookahead.addLast (new Token (lexerContext, TokenType.EOF, "<EOF>", line, column));
				return;
			}
			
			// Punctuation tokens:
			if (punctuationCharacters.contains (c)) {
				String punctuation = new String (new char[] { c });
				
				while (punctuationCharacters.contains (peekChar ())) {
					punctuation += acceptChar ();
				}

				if (!punctuationTokenTypes.containsKey (punctuation)) {
					throw new LexerException (lexerContext, line, column, String.format ("Invalid punctuation type: `%s`", punctuation));
				}
				
				lookahead.addLast (new Token (lexerContext, punctuationTokenTypes.get (punctuation), punctuationTokenTypes.get (punctuation).getPunctuation (), line, column));
				
				// Parse a value until end of line after an '=':
				if (lookahead.getLast ().getTokenType() == TokenType.ASSIGN) {
					final StringBuilder valueBuilder = new StringBuilder ();
					
					while (peekChar () > 0 && peekChar () != '#' && peekChar () != '\n') {
						valueBuilder.append (acceptChar ());
					}
					
					lookahead.addLast (new Token (lexerContext, TokenType.VALUE, valueBuilder.toString ().trim (), line, column));
				}
				
				return;
			}
			
			// Name tokens:
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
				final StringBuilder name = new StringBuilder ();
				
				name.append (c);
				
				char next = peekChar ();
				while ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z') || next == '_' || (next >= '0' && next <= '9')) {
					name.append (acceptChar ());
					next = peekChar ();
				}
				
				lookahead.addLast (new Token (lexerContext, TokenType.NAME, name.toString (), line, column));
				return;
			}
			
			throw new LexerException (lexerContext, line, column, String.format ("Invalid token starting with `%c`", c));
		}
	}
	
	private char peekChar () throws LexerException {
		return currentCharacter;
	}
	
	private char acceptChar () throws LexerException {
		final char ch = currentCharacter;
		currentCharacter = getNextChar ();
		return ch;
	}
	
	private char getNextChar () throws LexerException {
		try {
			int ch = reader.read ();
			
			// Return 0 on EOF:
			if (ch < 0) {
				return 0;
			}
			
			if (ch == (int)'\n') {
				++ line;
				column = 0;
			}
			
			++ column;
			return (char)ch;
		} catch (IOException e) {
			throw new LexerException (lexerContext, line, column, "Error reading from datasource", e);
		}
	}
}
