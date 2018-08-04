package net.jahhan.extension.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.common.JacksonObjectMapperProvider;

import java.util.TimeZone;

import javax.inject.Singleton;

/**
 * @author dylan
 */
@Extension("jackson")
@Singleton
public class DefaultJacksonObjectMapperProvider implements JacksonObjectMapperProvider {
	@Override
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// objectMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.setTimeZone(TimeZone.getDefault());
		return objectMapper;
	}
}
