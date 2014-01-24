package nl.ipo.cds.admin.ba.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nl.ipo.cds.admin.ba.ControllerException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandlers {

	@ExceptionHandler (ControllerException.class)
	@ResponseBody
	public void handleControllerException (final ControllerException ex, final HttpServletResponse response) throws IOException {
		response.sendError (ex.getStatusCode (), ex.getMessage ());
	}
}
