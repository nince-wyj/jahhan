package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jahhan.web.action.FieldTypeHandler;

/**
 * @author nince
 */
public class DateOrEmptyFieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		String parseValue = (String) value;
		value = parseValue;
		if (value != null) {
			if (!parseValue.isEmpty()) {
				Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
				Matcher matcher = pattern.matcher(parseValue);
				if (matcher.find()) {
					isValidate = true;
				}
			} else {
				isValidate = true;
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
