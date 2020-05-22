package com.naturalprogrammer.spring.lemon.commonsweb.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Used for handling exceptions that can't be handled by
 * <code>DefaultExceptionHandlerControllerAdvice</code>,
 * e.g. exceptions thrown in filters.
 */
public class LemonErrorController extends BasicErrorController {
	
    private static final Log log = LogFactory.getLog(LemonErrorController.class);

    public LemonErrorController(ErrorAttributes errorAttributes,
			ServerProperties serverProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		
		super(errorAttributes, serverProperties.getError(), errorViewResolvers);
		log.info("Created");
	}

    /**
     * Overrides the base method to add our custom logic
     */
	@Override	
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		
		Map<String, Object> body = getErrorAttributes(request,
			getErrorAttributeOptions(request, MediaType.ALL));
		
		// if a status was put in LemonErrorAttributes, fetch that
		Object statusObj = body.get(LemonErrorAttributes.HTTP_STATUS_KEY);
				
		HttpStatus status;

		if (statusObj == null)             // if not put,
			status = getStatus(request);   // let the superclass make the status
		else {
			status = HttpStatus.valueOf((Integer) body.get(LemonErrorAttributes.HTTP_STATUS_KEY));
			body.remove(LemonErrorAttributes.HTTP_STATUS_KEY); // clean the status from the map
		}
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(body, headers, status);
	}
}
