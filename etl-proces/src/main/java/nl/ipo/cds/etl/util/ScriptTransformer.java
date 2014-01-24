package nl.ipo.cds.etl.util;

import java.util.List;

import nl.ipo.cds.etl.Transformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ScriptTransformer implements Transformer {

	private static final Log logger = LogFactory.getLog(ScriptTransformer.class);
	
	private final ScriptExecutor executor;
	private final Resource script;
	private final String themeName; 

	public ScriptTransformer(ScriptExecutor executor, Resource script, String themeName) {
		this.executor = executor;
		this.script = script;
		this.themeName = themeName;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
	public void transform(List<String> themeNames) throws Exception {
		if (themeNames.contains(themeName)) {
			logger.debug("running script transformer for theme '" + themeName + "'");
			executor.executeScript(script);	
		} else {
			logger.debug("not running script transformer for theme '" + themeName + "'");
		}
	}
}
