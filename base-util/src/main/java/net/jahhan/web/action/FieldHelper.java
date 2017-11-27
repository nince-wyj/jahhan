//package net.jahhan.web.action;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.inject.Singleton;
//
///**
// * 字段处理帮助类
// */
//@Singleton
//public final class FieldHelper {
//
//	private final Map<String, Map<String, FieldHandlerHolder>> fieldHandlerMap = new HashMap<>();
//	private final Map<String, Map<String, Class<?>>> fieldClassMap = new HashMap<>();
//	private final Map<String, Map<String, Boolean>> fieldImportMap = new HashMap<>();
//
//	public void register(String act, Map<String, FieldHandlerHolder> fieldMap) {
//		fieldHandlerMap.put(act, fieldMap);
//	}
//	
//	public void registerImportMap(String act, Map<String, Boolean> importMap) {
//		this.fieldImportMap.put(act, importMap);
//	}
//
//	public Map<String, FieldHandlerHolder> getFieldMap(String act) {
//		return fieldHandlerMap.get(act);
//	}
//	
//	public Map<String, Boolean> getImportMap(String act) {
//		return this.fieldImportMap.get(act);
//	}
//
//	public Class<?> getFieldClass(String act, String field) {
//		Map<String, Class<?>> classMap = fieldClassMap.get(act);
//		if (null == classMap) {
//			return void.class;
//		}
//		return classMap.get(field);
//	}
//
//	public void register(String act, String fieldName, FieldHandlerHolder fieldHandler) {
//		Map<String, FieldHandlerHolder> fieldMap = fieldHandlerMap.get(act);
//		if (fieldMap == null) {
//			fieldMap = new HashMap<String, FieldHandlerHolder>(2, 1);
//			fieldMap.put(fieldName, fieldHandler);
//			fieldHandlerMap.put(act, fieldMap);
//		} else {
//			fieldMap.put(fieldName, fieldHandler);
//		}
//	}
//
//	public void setFieldClass(String act, String fieldName, Class<?> fieldClass) {
//		Map<String, Class<?>> fieldMap = this.fieldClassMap.get(act);
//		if (fieldMap == null) {
//			fieldMap = new HashMap<String, Class<?>>(2, 1);
//			fieldMap.put(fieldName, fieldClass);
//			this.fieldClassMap.put(act, fieldMap);
//		} else {
//			fieldMap.put(fieldName, fieldClass);
//		}
//	}
//}
