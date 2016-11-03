package net.jahhan.web.action;

import java.util.Map;

/**
 * @author nince
 */
public interface FieldTypeHandler {
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value,String version);

	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, Class<?> clazz,String version);
}
