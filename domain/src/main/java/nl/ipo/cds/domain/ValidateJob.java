package nl.ipo.cds.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue ("VALIDATE")
public class ValidateJob extends EtlJob {

	private final static int PRIORITY = 300;
	
	public ValidateJob () {
		super (PRIORITY);
	}
}
