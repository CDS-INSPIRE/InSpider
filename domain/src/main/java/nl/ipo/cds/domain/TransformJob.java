package nl.ipo.cds.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue ("TRANSFORM")
public class TransformJob extends EtlJob {

	private final static int PRIORITY = 100;
	
	public TransformJob () {
		super (PRIORITY);
	}
}
