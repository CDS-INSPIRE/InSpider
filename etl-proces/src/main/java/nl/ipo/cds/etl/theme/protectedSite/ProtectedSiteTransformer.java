package nl.ipo.cds.etl.theme.protectedSite;

import static nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteThemeConfig.THEME_NAME;

import java.util.List;

import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.generalization.GeneralizeReader;
import nl.ipo.cds.etl.util.ScriptExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class ProtectedSiteTransformer implements Transformer {
	
	private static final Log logger = LogFactory.getLog(ProtectedSiteTransformer.class);
	
	private ScriptExecutor scriptExecuter;
	private Resource transformScript, deleteScript;
	private GeneralizeReader generalizeReader;
	
	public void setScriptExecuter(ScriptExecutor scriptExecuter) {
		this.scriptExecuter = scriptExecuter;
	}
	
	public void setTransformScript(Resource transformScript) {
		this.transformScript = transformScript;
		
		Assert.isTrue(transformScript.exists());
	}
	
	public void setDeleteScript(Resource deleteScript) {
		this.deleteScript = deleteScript;
		
		Assert.isTrue(deleteScript.exists());
	}
	
	public void setGeneralizeReader(GeneralizeReader generalizeReader) {
		this.generalizeReader = generalizeReader;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
	public void transform(List<String> themes) throws Exception {
		if (themes.contains(THEME_NAME)) {
			logger.debug("running script transformer for theme '" + THEME_NAME + "'");
			logger.debug("deleting generalized datasets");
			generalizeReader.delete();
			logger.debug("deleting transformed dataset");		
			scriptExecuter.executeScript(deleteScript);
			
			logger.debug("executing transform script");		
			scriptExecuter.executeScript(transformScript);
			logger.debug("executing generalizer");
			generalizeReader.populate();	
		} else {
			logger.debug("not running script transformer for theme '" + THEME_NAME + "'");
		}
	}
}
