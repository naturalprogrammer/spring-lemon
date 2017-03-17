package com.naturalprogrammer.spring.lemon.exceptions;

import static com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler.HTTP_STATUS_KEY;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(ErrorController.class)
public class LemonErrorController extends BasicErrorController {
	
    private static final Log log = LogFactory.getLog(LemonErrorController.class);

    public LemonErrorController(ErrorAttributes errorAttributes,
			ServerProperties serverProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		
		super(errorAttributes, serverProperties.getError(), errorViewResolvers);
		log.info("Created");
	}

	@Override	
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		
		Map<String, Object> body = getErrorAttributes(request,
				isIncludeStackTrace(request, MediaType.ALL));
		
		@SuppressWarnings("unchecked")
		HttpStatus status =	(HttpStatus) body.get(HTTP_STATUS_KEY);
		
		if (status == null)
			status = getStatus(request);
		else
			body.remove(HTTP_STATUS_KEY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		return new ResponseEntity<Map<String, Object>>(body, headers, status);
	}
}
