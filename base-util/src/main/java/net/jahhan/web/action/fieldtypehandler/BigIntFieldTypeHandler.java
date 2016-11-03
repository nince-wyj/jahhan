package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import net.jahhan.version.ServerCodeVersion;
import net.jahhan.web.action.FieldTypeHandler;

/**
 * long
 * 
 * @author nince
 */
public class BigIntFieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		// 正则匹配
		if (value != null) {
			Long parseValue;
			if (value.getClass().equals(Long.class)) {
				parseValue = (Long) value;
			} else if (value.getClass().equals(String.class)) {
				parseValue = Long.valueOf((String) value);
			} else {
				parseValue = Long.valueOf((Integer) value);
			}

			if (parseValue > -9223372036854775808l && parseValue < 9223372036854775807l) {
				isValidate = true;
				if (ServerCodeVersion.castToString(version)) {
					requestMap.put(fieldName, String.valueOf(value));
				} else {
					requestMap.put(fieldName, parseValue);
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
