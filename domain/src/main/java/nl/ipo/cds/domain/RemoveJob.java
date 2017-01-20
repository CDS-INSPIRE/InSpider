package nl.ipo.cds.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue ("REMOVE")
public class RemoveJob extends EtlJob {

	private final static int PRIORITY = 250;
	
	public RemoveJob () {
		super (PRIORITY);
	}
}
