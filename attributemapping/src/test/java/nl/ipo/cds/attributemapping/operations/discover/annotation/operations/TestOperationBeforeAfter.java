package nl.ipo.cds.attributemapping.operations.discover.annotation.operations;

import nl.ipo.cds.attributemapping.operations.annotation.After;
import nl.ipo.cds.attributemapping.operations.annotation.Before;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = TestOperationBeforeAfter.Settings.class)
public class TestOperationBeforeAfter {

	@Before
	public void before (final Settings settings) {
	}
	
	@After
	public void after (final Settings settinsg) {
	}
	
	@Execute
	public String execute (final @Input("a") String a, final @Input("b") String b) {
		return a + b;
	}
	
	public static class Settings {
		
	}
}
