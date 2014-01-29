package nl.ipo.cds.etl.theme.watercourselink;

import nl.ipo.cds.validation.DefaultValidatorContext;
import nl.ipo.cds.validation.ValidationReporter;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

public class Context extends DefaultValidatorContext<Message, Context> {

	public Context (
			final CodeListFactory codeListFactory,
			final ValidationReporter<Message, Context> reporter) {
		super (codeListFactory, reporter);
	}

}
