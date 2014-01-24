package nl.ipo.cds.nagios.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import nl.ipo.cds.nagios.ast.ContactStatusNode;
import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.InfoNode;
import nl.ipo.cds.nagios.ast.KVPNode;
import nl.ipo.cds.nagios.ast.ObjectNode;
import nl.ipo.cds.nagios.ast.ProgramStatusNode;
import nl.ipo.cds.nagios.ast.ServiceCommentNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;

public class Parser implements Iterable<ObjectNode> {
	
	private ParserContext parserContext;
	private Lexer lexer;

	private class ObjectIterator implements Iterator<ObjectNode> {
		private boolean hasNextObject = false;
		private ObjectNode nextObject = null;
		
		public boolean hasNext() {
			if (!hasNextObject) {
				nextObject = object ();
				hasNextObject = true;
			}
			
			return nextObject != null;
		}

		public ObjectNode next() {
			if (!hasNextObject) {
				nextObject = object ();
			}
			
			if (nextObject == null) {
				throw new NoSuchElementException ();
			}
			hasNextObject = false;
			
			return nextObject;
		}

		public void remove() {
			throw new UnsupportedOperationException ();
		}
	}
	
	public Parser (final ParserContext parserContext, final Lexer lexer) {
		this.parserContext = parserContext;
		this.lexer = lexer;
	}
	
	/**
	 * 
	 * @return
	 */
	public ObjectNode object () {
		while (true) {
			try {
				if (check (TokenType.EOF)) {
					return null;
				} else if (check ("contactstatus")) {
					return contactStatus ();
				} else if (check ("hoststatus")) {
					return hostStatus ();
				} else if (check ("info")) {
					return info ();
				} else if (check ("programstatus")) {
					return programStatus ();
				} else if (check ("servicecomment")) {
					return serviceComment ();
				} else if (check ("servicestatus")) {
					return serviceStatus ();
				} else if (!check (TokenType.NAME)) {
					error (getLine (), getColumn (), String.format ("Expected object type, found `%s`", getValue ()));
				} else {
					error (getLine (), getColumn (), String.format("Unknown object type `%s`", getValue ()));
				}
				
			} catch (ParserException e) {
				parserContext.reportError (e.getLine (), e.getColumn (), e.getMessage ());

				// Skip until the end of the object:
				try {
					while (!check (TokenType.EOF) && !check (TokenType.RCURLY)) {
						accept ();
					}
					accept ();
				} catch (ParserException ex) {
					// Ignore further parser exceptions until EOF.
				}
				
				// Continue parsing the next object:
				continue;
			}
			
			break;
		}
		
		return null;
	}
	
	public ContactStatusNode contactStatus () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("contactstatus");
		
		return new ContactStatusNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
	}
	
	public HostStatusNode hostStatus () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("hoststatus");
		
		final HostStatusNode status = new HostStatusNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
		
		expectValue (status, "host_name");
		expectValue (status, "current_state");
		expectValue (status, "last_hard_state");
		expectValue (status, "plugin_output");
		expectValue (status, "plugin_output");
		expectValue (status, "long_plugin_output");
		expectValue (status, "performance_data");
		expectValue (status, "last_check");
		expectValue (status, "last_state_change");
		expectValue (status, "last_hard_state_change");
		expectValue (status, "is_flapping");
		expectValue (status, "scheduled_downtime_depth");
		
		return status;
	}
	
	public InfoNode info () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("info");
		
		return new InfoNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
	}
	
	public ProgramStatusNode programStatus () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("programstatus");
		
		return new ProgramStatusNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
	}
	
	public ServiceCommentNode serviceComment () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("servicecomment");
		
		return new ServiceCommentNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
	}
	
	public ServiceStatusNode serviceStatus () throws ParserException {
		final int line = getLine ();
		final int column = getColumn ();
		
		expect ("servicestatus");
		
		final ServiceStatusNode status = new ServiceStatusNode (
				parserContext,
				line,
				column,
				objectValues ()
			);
		
		expectValue (status, "host_name");
		expectValue (status, "service_description");
		expectValue (status, "current_state");
		expectValue (status, "last_hard_state");
		expectValue (status, "plugin_output");
		expectValue (status, "long_plugin_output");
		expectValue (status, "performance_data");
		expectValue (status, "is_flapping");
		expectValue (status, "scheduled_downtime_depth");
		
		return status;
	}
	
	public List<KVPNode> objectValues () throws ParserException {
		final List<KVPNode> kvps = new ArrayList<KVPNode> ();
		
		expect (TokenType.LCURLY);
		
		while (check (TokenType.NAME)) {
			kvps.add (kvp ());
		}
		
		expect (TokenType.RCURLY);
		
		return kvps;
	}
	
	public KVPNode kvp () throws ParserException {
		final Token key, value;
		final int line = getLine ();
		final int column = getColumn ();
		
		key = expect (TokenType.NAME);
		expect (TokenType.ASSIGN);
		value = expect (TokenType.VALUE);
		
		return new KVPNode (parserContext, line, column, key, value);
	}
	
	private boolean check (final TokenType tokenType) throws ParserException {
		try {
			return lexer.la ().getTokenType ().equals (tokenType);
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private boolean check (final TokenType tokenType, final String value) throws ParserException {
		try {
			return lexer.la ().getTokenType ().equals (tokenType) && lexer.la ().getValue ().equals (value);
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}

	private boolean check (final String value) throws ParserException {
		return check (TokenType.NAME, value);
	}
	
	private Token expect (final TokenType tokenType) throws ParserException {
		try {
			if (!check (tokenType)) {
				error (String.format ("Expected %s, but found `%s`", tokenType, lexer.la ().getValue ()));
			}

			return lexer.accept ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private Token expect (final TokenType tokenType, final String value) throws ParserException {
		try {
			if (!check (tokenType, value)) {
				error (String.format ("Expected `%s`, but found `%s`", value, lexer.la ().getValue ()));
			}
		
			return lexer.accept ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private Token expect (final String value) throws ParserException {
		return expect (TokenType.NAME, value);
	}
	
	private void expectValue (final ObjectNode object, final String key) throws ParserException {
		if (!object.hasValue (key)) {
			error (object.getLine (), object.getColumn (), String.format ("Object must have a value for `%s`", key));
		}
	}
	
	private Token accept () throws ParserException {
		try {
			return lexer.accept ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private void error (final int line, final int column, final String message) throws ParserException {
		throw new ParserException (parserContext, line, column, message);
	}
	
	private void error (final String message) throws ParserException {
		try {
			error (lexer.la ().getLine (), lexer.la ().getColumn (), message);
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private int getLine () throws ParserException {
		try {
			return lexer.la ().getLine ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private int getColumn () throws ParserException {
		try {
			return lexer.la ().getColumn ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}
	
	private String getValue () throws ParserException {
		try {
			return lexer.la ().getValue ();
		} catch (LexerException e) {
			throw new ParserException (parserContext, e);
		}
	}

	public Iterator<ObjectNode> iterator () {
		return new ObjectIterator ();
	}
}
