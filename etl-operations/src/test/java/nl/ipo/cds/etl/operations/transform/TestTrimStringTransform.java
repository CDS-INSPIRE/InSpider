package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTrimStringTransform {

	@Test
	public void testTrimUnmodified () {
		final TrimStringTransform t = new TrimStringTransform ();

		assertEquals ("Hello, world!", t.execute ("Hello, world!", new TrimStringTransform.Settings ()));
	}
	
	@Test
	public void testTrimWhitespace () {
		final TrimStringTransform t = new TrimStringTransform ();
		
		assertEquals ("Hello, world!", t.execute ("     \t\n\r   Hello, world!", new TrimStringTransform.Settings ()));
		assertEquals ("Hello, world!", t.execute ("Hello, world!   \t  \n \r  ", new TrimStringTransform.Settings ()));
		assertEquals ("Hello, world!", t.execute ("\t   \r \n Hello, world!   \t  \n \r  ", new TrimStringTransform.Settings ()));
	}

	@Test
	public void testTrimCustom () {
		final TrimStringTransform t = new TrimStringTransform ();
		final TrimStringTransform.Settings settings = new TrimStringTransform.Settings ();
		
		settings.setAdditionalCharacters ("}{");
		
		assertEquals ("Hello, world!", t.execute ("Hello, world!", settings));
		assertEquals ("Hello, world!", t.execute ("{Hello, world!}", settings));
		assertEquals ("Hello, world!", t.execute ("{{Hello, world!}}", settings));
		assertEquals ("Hello, world!", t.execute ("}Hello, world!{", settings));
		assertEquals ("Hello, world!", t.execute ("{}{}}{Hello, world!}{}{}{}", settings));
		assertEquals ("Hello, world!", t.execute (" { Hello, world! } ", settings));
		assertEquals ("Hello, world!", t.execute (" { } }{ }}{{   }{  }{ Hello, world! { } }{ }{ {} {{} { }", settings));
		assertEquals ("Hello{, }world!", t.execute ("Hello{, }world!", settings));
	}
}
