package com.integralblue.callerid;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

/** MappingJacksonHttpMessageConverter doesn't have a getObjectMapper() method - so we add one.
 * Requested that Spring add such a method so we can drop this hacky class:
 * https://jira.springsource.org/browse/SPR-8605
 * @author candrews
 *
 */
public class ExposedObjectMapperMappingJacksonHttpMessageConverter extends
		MappingJacksonHttpMessageConverter {
	
	private ObjectMapper objectMapper;

	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		super.setObjectMapper(objectMapper);
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
