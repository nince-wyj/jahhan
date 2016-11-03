package net.jahhan.web.action.fieldtypehandler;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import net.jahhan.version.ServerCodeVersion;
import net.jahhan.web.action.FieldTypeHandler;

/**
 * @author nince
 */
public class MapFieldTypeHandler implements FieldTypeHandler {

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
		boolean isValidate = true;
		try {
			JSONObject parseValue = (JSONObject) value;
			if (ServerCodeVersion.castToString(version)) {
				requestMap.put(fieldName, parseValue.toJSONString());
			} else {
				requestMap.put(fieldName, parseValue);
			}
		} catch (Exception e) {
			isValidate = false;
		}
		return isValidate;
	}

	@Override
	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, Class<?> clazz,
			String version) {
		return validate(requestMap, fieldName, value, version);
	}
}
