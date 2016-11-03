package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import net.jahhan.version.ServerCodeVersion;
import net.jahhan.web.action.FieldTypeHandler;

/**
 * byte
 * 
 * @author nince
 */
public class TinyIntFieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		// 正则匹配
		if (value != null) {
			Integer parseValue = null;
			if (value.getClass().equals(Integer.class)) {
				parseValue = (Integer) value;
			} else if (value.getClass().equals(String.class)) {
				parseValue = Integer.valueOf((String) value);
			}
			if (parseValue > -128 && parseValue < 127) {
				isValidate = true;
				if (ServerCodeVersion.castToString(version)) {
					requestMap.put(fieldName, String.valueOf(value));
				} else {
					requestMap.put(fieldName, parseValue.byteValue());
				}
			}
		}
		return isValidate;
	}

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, Class<?> clazz,
			String version) {
		return validate(requestMap, fieldName, value, version);
	}
}
