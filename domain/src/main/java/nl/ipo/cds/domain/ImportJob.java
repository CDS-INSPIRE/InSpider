package nl.ipo.cds.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue ("IMPORT")
public class ImportJob extends EtlJob {
	private static final int PRIORITY = 200;
	
	public ImportJob () {
		super (PRIORITY);
	}
}
