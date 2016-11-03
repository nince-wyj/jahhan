package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import net.jahhan.web.action.FieldTypeHandler;

/**
 * @author nince
 */
public class Char11FieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		String parseValue = (String) value;
		value = parseValue;
		if (value != null && parseValue.length() == 11) {
			isValidate = true;
		}
		return isValidate;
	}

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, Class<?> clazz,
			String version) {
		return validate(requestMap, fieldName, value, version);
	}
}
