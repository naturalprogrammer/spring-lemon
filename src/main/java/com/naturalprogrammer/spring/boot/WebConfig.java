package com.naturalprogrammer.spring.boot;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * https://docs.angularjs.org/api/ng/service/$http
 * http://stackoverflow.com/questions/26384930/how-to-add-n-before-each-spring-json-response-to-prevent-common-vulnerab
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#core.web
 * @author skpat_000
 *
 */
@Configuration
@EnableSpringDataWebSupport
public class WebConfig extends WebMvcConfigurerAdapter {
	
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setJsonPrefix(")]}',\n");
        converters.add(converter);
    }
    
}