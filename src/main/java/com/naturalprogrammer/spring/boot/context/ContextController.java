//package com.naturalprogrammer.spring.boot.context;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.naturalprogrammer.spring.boot.ContextDto;
//import com.naturalprogrammer.spring.boot.SaUtil;
//
//@RestController
//public class ContextController {
//	
//	private Log log = LogFactory.getLog(getClass());
//
//	@Autowired
//	private ContextService contextService;
//	
//	@RequestMapping("/context")
//	public ContextDto contextDto() {
//		log.info("userDto: " + SaUtil.getUserDto());
//		return contextService.getContext();
//	}
//	
//}
