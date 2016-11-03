package net.jahhan.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.google.inject.Injector;

import net.jahhan.api.Action;
import net.jahhan.cache.ActionMapCache;
import net.jahhan.cache.AsyncActionCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.FieldTypeEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.handler.WorkHandler;
import net.jahhan.utils.Assert;
import net.jahhan.web.action.actionhandler.ActionWorkHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.Field;

/**
 * 接口注册帮助类
 */
@Singleton
public class ServiceRegisterHelper {
	private final Logger logger = LoggerFactory.getInstance().getLogger(ServiceRegisterHelper.class);

	private static Map<RequestMethodEnum, Map<String, WorkHandler>> serviceMap = new HashMap<RequestMethodEnum, Map<String, WorkHandler>>();
	@Inject
	private FieldConvertHelper defaultFieldTypeManager;

	public WorkHandler addService(RequestMethodEnum actType, String serviceName, WorkHandler service) {
		Map<String, WorkHandler> actMap = serviceMap.get(actType);
		if (null == actMap) {
			actMap = new HashMap<String, WorkHandler>();
		}
		WorkHandler workHandler = actMap.put(serviceName, service);
		serviceMap.put(actType, actMap);
		return workHandler;
	}

	public WorkHandler getService(RequestMethodEnum actType, String serviceName) {
		if (null == serviceMap.get(actType)) {
			return null;
		}
		return serviceMap.get(actType).get(serviceName);
	}

	public List<String> getAllServiceName() {
		List<String> serviceList = new ArrayList<>();
		Iterator<RequestMethodEnum> typeIt = serviceMap.keySet().iterator();
		while (typeIt.hasNext()) {
			Set<String> serviceNameSet = serviceMap.get(typeIt.next()).keySet();
			serviceList.addAll(serviceNameSet);
		}
		return serviceList;
	}

	public void registerService(ClassLoader classLoader, Injector injector, String className) {
		try {
			Class<?> clazz = classLoader.loadClass(className);
			if (clazz.isAnnotationPresent(ActionService.class) && Action.class.isAssignableFrom(clazz)) {
				ActionService actionService = clazz.getAnnotation(ActionService.class);
				logger.info("加载接口服务：" + actionService.act());
				Map<String, FieldHandlerHolder> fieldMap = null;
				Map<String, Boolean> importMap = null;
				Class<?> clazzReferenceObject = actionService.referenceObject();
				if (!clazzReferenceObject.equals(void.class)) {
					java.lang.reflect.Field[] fields = clazzReferenceObject.getDeclaredFields();
					fieldMap = new HashMap<>(fields.length);
					for (java.lang.reflect.Field field : fields) {
						if (field.getName().contains("this$"))
							break;
						Field annotationField = field.getAnnotation(Field.class);
						String annotationFieldName = "", defaultValue;
						FieldTypeEnum annotationFieldTypeEnum;
						if (null != annotationField) {
							annotationFieldName = annotationField.fieldName();
						}
						if (annotationField.fieldType() == FieldTypeEnum.NULL) {
							if (annotationFieldName.equals("")) {
								annotationFieldName = field.getName();
							}
							defaultValue = "";
							Class<?> filedClass = field.getType();
							annotationFieldTypeEnum = defaultFieldTypeManager.getFieldType(filedClass);
							Assert.notNull(annotationFieldTypeEnum,
									clazzReferenceObject.getName() + annotationFieldName + "校验类型未设置！",
									SystemErrorCode.CODE_ERROR);

						} else {
							if (annotationFieldName.equals("")) {
								annotationFieldName = field.getName();
							}
							defaultValue = annotationField.defaultValue();
							annotationFieldTypeEnum = annotationField.fieldType();
							if (annotationFieldTypeEnum.equals(FieldTypeEnum.OBJECT))
								FrameworkException.throwException(SystemErrorCode.CODE_ERROR,
										clazzReferenceObject.getName() + "校验类型不允许设置object类型");
						}

						FieldHandlerHolder fieldHandlerHolder = new FieldHandlerHolder(annotationFieldName,
								annotationFieldTypeEnum, defaultValue);
						fieldMap.put(annotationFieldName, fieldHandlerHolder);
					}
				}
				if (clazzReferenceObject.equals(void.class)) {
					fieldMap = new HashMap<>(actionService.validateParameters().length);
					importMap = new HashMap<>(actionService.validateParameters().length);

					for (Field field : actionService.validateParameters()) {
						String fieldName = field.fieldName();
						if (fieldName.equals("")) {
							FrameworkException.throwException(SystemErrorCode.CODE_ERROR, clazz.getName() + "字段存在空字符串");
						}
						FieldTypeEnum fieldTypeEnum = field.fieldType();
						Class<?> referenceObject = field.referenceObject();
						if (!referenceObject.equals(void.class) && fieldTypeEnum.equals(FieldTypeEnum.OBJECT)) {
							java.lang.reflect.Field[] fields = referenceObject.getDeclaredFields();
							for (int i = 0; i < fields.length; i++) {
								java.lang.reflect.Field objectField = fields[i];
								Field annotationField = objectField.getAnnotation(Field.class);
								if (null != annotationField) {
									String annotationFieldName = annotationField.fieldName();
									if (null == annotationFieldName) {
										annotationFieldName = objectField.getName();
									}
									String defaultValue = annotationField.defaultValue();
									FieldTypeEnum annotationFieldTypeEnum = annotationField.fieldType();
									if (annotationFieldTypeEnum.equals(FieldTypeEnum.OBJECT))
										FrameworkException.throwException(SystemErrorCode.CODE_ERROR,
												referenceObject.getName() + "校验类型不允许设置object类型");
									FieldHandlerHolder fieldHandler = new FieldHandlerHolder(
											fieldName + "." + annotationFieldName, annotationFieldTypeEnum,
											defaultValue);
									fieldMap.put(fieldName + "." + annotationFieldName, fieldHandler);

								} else {
									FrameworkException.throwException(SystemErrorCode.CODE_ERROR,
											clazz.getName() + "存在未指定验证注解的字段");
								}
							}
						} else {
							if (fieldTypeEnum.equals(FieldTypeEnum.OBJECT))
								FrameworkException.throwException(SystemErrorCode.CODE_ERROR,
										clazz.getName() + "注解头错误");
							String defaultValue = field.defaultValue();
							FieldHandlerHolder fieldHandler = new FieldHandlerHolder(fieldName, fieldTypeEnum,
									defaultValue);

							fieldMap.put(fieldName, fieldHandler);
						}
						FieldHandlerHolder fieldHandler = new FieldHandlerHolder(fieldName, field.fieldType(),
								field.defaultValue());
						fieldMap.put(fieldName, fieldHandler);
						importMap.put(fieldName, field.importantParameter());
						ApplicationContext.CTX.getFieldManager().setFieldClass(actionService.act(), fieldName,
								field.referenceObject());
					}
				}

				ApplicationContext.CTX.getFieldManager().registerImportMap(actionService.act(), importMap);
				ApplicationContext.CTX.getFieldManager().register(actionService.act(), fieldMap);
				Action action = (Action) injector.getInstance(clazz);
				ActionHandler service = new ActionWorkHandler(action);
				WorkHandlerHelper workHandlerHelper = injector.getInstance(WorkHandlerHelper.class);
				service = workHandlerHelper.registerActionChain(service, clazz);
				if (actionService.async()) {
					AsyncActionCache.getInstance().setAction(actionService.act());
				}
				if (addService(actionService.requestMethod(), actionService.act(), service) != null) {
					logger.error("接口服务重复:" + actionService.act());
					Thread.sleep(1500);
					System.exit(-1);
				} else if (SysConfiguration.getActSave()) {
					Map<String, String> actInfoMap = new HashMap<>();
					actInfoMap.put("act", actionService.act());
					actInfoMap.put("description", actionService.description());
					ActionMapCache.getInstance().setAction(actionService.act(), actInfoMap);
				}
			}
		} catch (Exception ex) {
			logger.error("接口服务启动失败:" + className, ex);
		}
	}
}
