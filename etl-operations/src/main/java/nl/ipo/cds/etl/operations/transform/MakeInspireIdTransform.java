package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class MakeInspireIdTransform {
	
	@Execute
	public String execute (
			final @Input ("countryCode") String countryCode, 
			final @Input ("bronhouderId") String bronhouderId,
			final @Input ("datasetCode") String datasetCode,
			final @Input ("uuid") String uuid) {

		final StringBuilder builder = new StringBuilder ();
		
		builder.append (countryCode);
		builder.append (".");
		builder.append (bronhouderId);
		builder.append (".");
		builder.append (datasetCode);
		builder.append (".");
		builder.append (uuid);
		
		return builder.toString ();
	}
}
