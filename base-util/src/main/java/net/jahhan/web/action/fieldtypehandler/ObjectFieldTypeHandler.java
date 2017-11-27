//package net.jahhan.web.action.fieldtypehandler;
//
//import java.util.Map;
//
//import com.alibaba.fastjson.JSONObject;
//
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.context.BaseContext;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.web.action.FieldHandlerHolder;
//import net.jahhan.web.action.FieldTypeHandler;
//import net.jahhan.web.action.annotation.Field;
//
///**
// * @author nince
// */
//public class ObjectFieldTypeHandler implements FieldTypeHandler {
//
//	@Override
//	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, String version) {
//		FrameworkException.throwException(SystemErrorCode.CODE_ERROR, "代码错误");
//		return false;
//	}
//
//	@Override
//	public boolean validate(Map<String, Object> requestMap, String fieldName, Object value, Class<?> clazz,
//			String version) {
//		boolean isValidate = true;
//		try {
//			JSONObject paraObject = (JSONObject) value;
//			value = paraObject;
//			java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
//			for (int i = 0; i < fields.length; i++) {
//				java.lang.reflect.Field field = fields[i];
//				Field fieldAnnotation = field.getAnnotation(Field.class);
//				if (null != fieldAnnotation) {
//					String objectFieldName = field.getName();
//					if (!fieldAnnotation.fieldName().equals("")) {
//						objectFieldName = fieldAnnotation.fieldName();
//					}
//					Object paraValue = paraObject.get(objectFieldName);
//					validateObjectValue(requestMap, fieldName + "." + objectFieldName, paraValue, version);
//				} else {
//					FrameworkException.throwException(SystemErrorCode.CODE_ERROR, clazz.getName() + "存在未指定验证注解的字段");
//				}
//			}
//			Object o = JSONObject.toJavaObject(paraObject, clazz);
//			// Object o = GsonUtil.parseObject(value, clazz);
//			BaseContext applicationContext = BaseContext.CTX;
//			applicationContext.getInvocationContext().getRequestMessage().getRequestMap().put(fieldName, o);
//		} catch (FrameworkException e) {
//			throw e;
//		} catch (Exception e) {
//			isValidate = false;
//		}
//		return isValidate;
//	}
//
//	private void validateObjectValue(Map<String, Object> requestMap, String parameter, Object value,
//			String version) {
//		BaseContext applicationContext = BaseContext.CTX;
//		String act = applicationContext.getRequestMessage().getServiceName();
//		Map<String, FieldHandlerHolder> fieldMap = applicationContext.getFieldManager().getFieldMap(act);
//		FieldHandlerHolder fieldHandler = fieldMap.get(parameter);
//		if (fieldHandler == null) {
//			FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, parameter + "字段不存在");
//		}
//		FieldTypeHandler fieldTypeHandler = fieldHandler.getFieldTypeHandler();
//		Class<?> fieldClass = applicationContext.getFieldManager().getFieldClass(act, parameter);
//		if (null == fieldClass)
//			fieldClass = void.class;
//		if (!fieldTypeHandler.validate(requestMap, parameter, value, fieldClass, version)) {
//			FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, parameter + "验证不通过");
//		}
//	}
//}
