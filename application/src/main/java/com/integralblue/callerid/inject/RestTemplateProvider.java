package com.integralblue.callerid.inject;

import java.util.Iterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;


public class RestTemplateProvider implements Provider<RestTemplate> {
	@Inject @Named("jsonObjectMapper") ObjectMapper jsonObjectMapper;
	
	public RestTemplate get() {
		final RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		
		//remove the existing MappingJacksonHttpMessageConverter - we're going to be using our own
		final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
		while(iterator.hasNext()){
			final HttpMessageConverter<?> converter = iterator.next();
			if(converter instanceof MappingJackson2HttpMessageConverter){
				iterator.remove();
			}
		}
		
		//handle json data
		final MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setObjectMapper(jsonObjectMapper);
		restTemplate.getMessageConverters().add(0,jsonConverter);
		
		return restTemplate;
	}

}