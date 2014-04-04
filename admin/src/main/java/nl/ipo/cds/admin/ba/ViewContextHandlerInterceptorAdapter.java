package nl.ipo.cds.admin.ba;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.ipo.cds.admin.security.AuthzImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ViewContextHandlerInterceptorAdapter extends
		HandlerInterceptorAdapter {

	private static String VIEW_NAME = "viewName";

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
		
		if(modelAndView != null){
			Map<String, Object> model = modelAndView.getModel();
			String overriddenViewName = (String)model.get(VIEW_NAME);
			String viewName = StringUtils.isNotBlank(overriddenViewName) ? overriddenViewName : modelAndView.getViewName();
			// add viewName to model
			if(!viewName.startsWith("redirect:")) {
				viewName = StringUtils.replace(viewName, "/", "_");
				model.put(VIEW_NAME, viewName);
			}

			// Add security-context to model
			AuthzImpl authz = new AuthzImpl();
			model.put("authz", authz);
		}
	}

	
}
