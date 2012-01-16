package com.integralblue.callerid.inject;

import java.util.Iterator;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.integralblue.callerid.ExposedObjectMapperMappingJacksonHttpMessageConverter;


public class RestTemplateProvider implements Provider<RestTemplate> {
	@Inject @Named("jsonObjectMapper") ObjectMapper jsonObjectMapper;
	
	public RestTemplate get() {
		final RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
		
		//remove the existing MappingJacksonHttpMessageConverter - we're going to be using our own
		final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
		while(iterator.hasNext()){
			final HttpMessageConverter<?> converter = iterator.next();
			if(converter instanceof MappingJacksonHttpMessageConverter){
				iterator.remove();
			}
		}
		
		//handle json data
		final MappingJacksonHttpMessageConverter jsonConverter = new ExposedObjectMapperMappingJacksonHttpMessageConverter();
		jsonConverter.setObjectMapper(jsonObjectMapper);
		restTemplate.getMessageConverters().add(0,jsonConverter);
		
		return restTemplate;
	}

}