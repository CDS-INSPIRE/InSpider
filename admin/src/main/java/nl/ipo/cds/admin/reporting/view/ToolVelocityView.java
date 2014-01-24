package nl.ipo.cds.admin.reporting.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.ipo.cds.admin.BaseConfiguration;
import nl.idgis.commons.velocity.ToolContext;

import org.apache.velocity.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.velocity.VelocityLayoutView;

public class ToolVelocityView extends VelocityLayoutView {

	@Autowired
	private BaseConfiguration baseConfiguration;
	
	@Override
	protected Context createVelocityContext (Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final Context ctx = super.createVelocityContext (model, request, response);
		
		this.baseConfiguration = this.getApplicationContext().getBean(BaseConfiguration.class);
		
		ctx.put("cdsVersion", this.baseConfiguration.getCdsVersion());
		ctx.put("mavenVersion", this.baseConfiguration.getMavenVersion());
		ctx.put("buildVersion", this.baseConfiguration.getBuild());

		return new ToolContext (ctx);
	}
}
