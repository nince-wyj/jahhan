package net.jahhan.rest.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
@Consumes({ "application/json", "application/*+json", "text/json" })
@Produces({ "application/json", "application/*+json", "text/json" })
public class JSONProvider extends ResteasyJackson2Provider {

	@Override
	public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType json,
			MultivaluedMap<String, Object> headers, OutputStream body) throws IOException {

		ObjectMapper mapper = locateMapper(type, json);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// objectMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
//		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setTimeZone(TimeZone.getDefault());
		super.writeTo(value, type, genericType, annotations, json, headers, body);
	}

	@Override
	public Object readFrom(Class<Object> type, final Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

		ObjectMapper mapper = locateMapper(type, mediaType);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// objectMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
//		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setTimeZone(TimeZone.getDefault());
		return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}
}
