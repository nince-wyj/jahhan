package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import net.jahhan.version.ServerCodeVersion;
import net.jahhan.web.action.FieldTypeHandler;

public class BooleanFieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		// 正则匹配
		if (value != null) {
			Boolean parseValue = (Boolean) value;
			if (parseValue == true || parseValue == false) {
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
