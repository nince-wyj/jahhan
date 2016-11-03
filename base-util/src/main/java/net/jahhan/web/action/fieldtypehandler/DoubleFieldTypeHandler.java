package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import org.slf4j.Logger;

import net.jahhan.factory.LoggerFactory;
import net.jahhan.version.ServerCodeVersion;
import net.jahhan.web.action.FieldTypeHandler;

/**
 * @author nince
 */
public class DoubleFieldTypeHandler implements FieldTypeHandler {

	private final Logger logger = LoggerFactory.getInstance().getLogger(DoubleFieldTypeHandler.class);

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = false;
		if (value != null) {
			try {
				Double parseValue = Double.parseDouble(String.valueOf(value));
				isValidate = true;
				if (ServerCodeVersion.castToString(version)) {
					requestMap.put(fieldName, String.valueOf(value));
				} else {
					requestMap.put(fieldName, parseValue);
				}
			} catch (Exception e) {
				logger.warn("验证double类型出现异常", e);
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
