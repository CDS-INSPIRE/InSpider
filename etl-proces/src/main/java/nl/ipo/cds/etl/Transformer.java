package nl.ipo.cds.etl;

import java.util.List;

public interface Transformer {
	void transform(List<String> themeNames) throws Exception;
}
