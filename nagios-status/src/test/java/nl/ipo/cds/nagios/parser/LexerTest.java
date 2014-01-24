package nl.ipo.cds.nagios.parser;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

public class LexerTest {

	private static class Result {
		private TokenType tokenType;
		private String value;
		
		public Result (TokenType tokenType, String value) {
			this.tokenType = tokenType;
			this.value = value;
		}

		public TokenType getTokenType() {
			return tokenType;
		}

		public String getValue() {
			return value;
		}
	}
	
	@Test
	public void testNameTokens () throws Exception {
		testLexer ("name1 name2 name_underscore_3 ", new Result[] {
			new Result (TokenType.NAME, "name1"),	
			new Result (TokenType.NAME, "name2"),	
			new Result (TokenType.NAME, "name_underscore_3")	
		});
	}
	
	@Test
	public void testPunctuationTokens () throws Exception {
		testLexer ("{ } =   ", new Result[] {
			new Result (TokenType.LCURLY, "{"),
			new Result (TokenType.RCURLY, "}"),
			new Result (TokenType.ASSIGN, "="),
			new Result (TokenType.VALUE, "")
		});
	}
	
	@Test
	public void testComments () throws Exception {
		testLexer ("# Single line comment\n           # Single line comment 2\n   name1 {   # Single line comment", new Result[] {
			new Result (TokenType.NAME, "name1"),
			new Result (TokenType.LCURLY, "{")
		});
	}
	
	@Test
	public void testValues () throws Exception {
		testLexer ("objectName {\n    key=value\nkey2=complex value =\nkey3=  complex trimmed = value  \n}", new Result[] {
			new Result (TokenType.NAME, "objectName"),
			new Result (TokenType.LCURLY, "{"),
			new Result (TokenType.NAME, "key"),
			new Result (TokenType.ASSIGN, "="),
			new Result (TokenType.VALUE, "value"),
			new Result (TokenType.NAME, "key2"),
			new Result (TokenType.ASSIGN, "="),
			new Result (TokenType.VALUE, "complex value ="),
			new Result (TokenType.NAME, "key3"),
			new Result (TokenType.ASSIGN, "="),
			new Result (TokenType.VALUE, "complex trimmed = value"),
			new Result (TokenType.RCURLY, "}")
		});
	}
	
	private void testLexer (final String input, final Result[] expectedResult) throws Exception {
		final LexerContext context = new LexerContext ("unittest");
		final Lexer lexer = new Lexer (context, new StringReader (input));
		
		for (Result result: expectedResult) {
			final Token token = lexer.accept ();
			
			assertEquals (result.getTokenType (), token.getTokenType ());
			assertEquals (result.getValue (), token.getValue ());
		}
		
		assertEquals (TokenType.EOF, lexer.accept ().getTokenType ());
	}
}
