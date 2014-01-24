package nl.ipo.cds.nagios.parser;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.nagios.ast.ContactStatusNode;
import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.InfoNode;
import nl.ipo.cds.nagios.ast.ObjectNode;
import nl.ipo.cds.nagios.ast.ProgramStatusNode;
import nl.ipo.cds.nagios.ast.ServiceCommentNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;

import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	private LexerContext lexerContext;
	private DefaultParserContext parserContext;
	
	@Before
	public void before () {
		lexerContext = new LexerContext ("unittest");
		parserContext = new DefaultParserContext ("unittest");
	}
	
	@Test
	public void testEmptyObject () {
		parse ("info { }", 1, 0, new Class<?>[] { InfoNode.class });
	}
	
	@Test
	public void testEmptyObjects () {
		parse (
				"info { }\n" +
				"programstatus { }\n" +
				"contactstatus { }\n" +
				"servicecomment { }\n",
				4, 0,
				new Class<?>[] { InfoNode.class, ProgramStatusNode.class, ContactStatusNode.class, ServiceCommentNode.class }
			);
	}
	
	@Test
	public void testHostStatus () {
		parse ("hoststatus { }", 0, 1, null);
	}
	
	@Test
	public void testServiceStatus () {
		parse ("servicestatus { }", 0, 1, null);
	}
	
	@Test
	public void testInvalidObject () {
		parse ("invalidobject { }", 0, 1, new Class<?>[] { });
	}
	
	@Test
	public void testErrorRecovery () {
		parse (
				"info { }\n" +
				"invalidobject { }\n" +
				"info {\n" +
				"    invalidkvp\n" +
				"    anotherinvalidkvp\n" +
				"    {\n" +
				"}\n" +
				"info {\n" +
				"   invalidpunctuation().!\n" +
				"}\n" +
				"servicecomment { }",
				2, 5,
				new Class<?>[] { InfoNode.class, ServiceCommentNode.class }
			);
	}
	
	@Test
	public void testKvps () {
		parseKvp (
				"servicecomment {\n" +
				"    a=b\n" +
				"    b=1234.5\n" +
				"    c=Hello, world!\n" +
				"}",
				new String[] {
					"a", "b",
					"b", "1234.5",
					"c", "Hello, world!"
				}
			);
	}
	
	@Test
	public void testKvpsMultiple () {
		parseKvp (
				"servicecomment {\n" +
				"    a=b\n" +
				"    b=1234.5\n" +
				"    c=Hello, world!\n" +
				"}\n\n" +
				"servicecomment {" +
				"    d=e\n" +
				"}",
				new String[] {
					"a", "b",
					"b", "1234.5",
					"c", "Hello, world!",
					null, null,
					"d", "e"
				}
			);
	}
	
	private void parse (final String input, final int objectCount, final int errorCount, final Class<?>[] types) {
		final Lexer lexer = new Lexer (lexerContext, new StringReader (input));
		final Parser parser = new Parser (parserContext, lexer);
		final List<ObjectNode> list = new ArrayList<ObjectNode> ();
		
		
		for (final ObjectNode node: parser) {
			list.add (node);
		}
		
		assertEquals (objectCount, list.size ());
		assertEquals (errorCount, parserContext.getErrors ().size ());
		
		if (types != null) {
			assertEquals (types.length, list.size ());
			
			for (int i = 0; i < types.length; ++ i) {
				assertEquals (types[i], list.get(i).getClass ());
			}
		}
	}
	
	private void parseKvp (final String input, final String[] kvps) {
		final Lexer lexer = new Lexer (lexerContext, new StringReader (input));
		final Parser parser = new Parser (parserContext, lexer);
		final List<ObjectNode> list = new ArrayList<ObjectNode> ();
		int objectIndex = 0;
		
		for (final ObjectNode node: parser) {
			list.add (node);
		}

		assertTrue (list.size () > 0);
		
		for (int i = 0; i < kvps.length; i += 2) {
			final String key = kvps[i];
			final String value = kvps[i + 1];
			
			if (key == null && value == null) {
				++ objectIndex;
				assertTrue (list.size () > objectIndex);
				continue;
			}
			
			final ObjectNode object = list.get (objectIndex);
			
			assertTrue (object.hasValue (key));
			assertEquals (value, object.getValue (key));
		}
		
		assertEquals (objectIndex + 1, list.size ());
	}
}
