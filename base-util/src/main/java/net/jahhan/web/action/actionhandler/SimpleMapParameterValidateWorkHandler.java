package net.jahhan.web.action.actionhandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.FieldTypeEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.utils.Assert;
import net.jahhan.utils.BeanTools;
import net.jahhan.utils.JsonUtil;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.FieldHandlerHolder;
import net.jahhan.web.action.FieldTypeHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * @author nince
 */
@HandlerAnnocation(500)
public class SimpleMapParameterValidateWorkHandler extends ActionHandler {
	private final Logger logger = LoggerFactory.getInstance().getLogger(SimpleMapParameterValidateWorkHandler.class);
	private String[] parameters;
	private String[] minorParameters;
	private String act;
	private Class<?> referenceObject;
	private String version;

	public SimpleMapParameterValidateWorkHandler(ActionHandler actionHandler, ActionService actionService) {
		this.nextHandler = actionHandler;
		this.parameters = actionService.importantParameters();
		this.minorParameters = actionService.minorParameters();
		this.act = actionService.act();
		this.referenceObject = actionService.referenceObject();
		this.version = actionService.version();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		Map<String, Object> requestMap = invocationContext.getRequestMessage().getRequestMap();
		if (referenceObject.equals(void.class)) {
			Object value;
			if (parameters.length + minorParameters.length > 0) {
				for (String parameter : parameters) {
					value = null == requestMap.get(parameter) ? null : requestMap.get(parameter);
					validate(requestMap, parameter, value);
				}
				for (String parameter : minorParameters) {
					value = null == requestMap.get(parameter) ? null : requestMap.get(parameter);
					if (value != null) {
						validate(requestMap, parameter, value);
					}
				}
			} else {
				Map<String, Boolean> importMap = applicationContext.getFieldManager().getImportMap(act);
				for (String parameter : importMap.keySet()) {
					boolean isImport = importMap.get(parameter);
					value = null == requestMap.get(parameter) ? null : requestMap.get(parameter);
					if (isImport) {
						validate(requestMap, parameter, value);
					} else {
						if (value != null) {
							validate(requestMap, parameter, value);
						}
					}
				}

			}
		} else {
			Map<String, Object> content = (Map<String, Object>) invocationContext.getRequestMessage().getContent();

			try {
				Field[] fields = referenceObject.getDeclaredFields();
				Constructor<?> declaredConstructor = referenceObject.getDeclaredConstructor(new Class[0]);
				declaredConstructor.setAccessible(true);
				Object o = declaredConstructor.newInstance();
				for (Field field : fields) {
					net.jahhan.web.action.annotation.Field annotationField = field
							.getAnnotation(net.jahhan.web.action.annotation.Field.class);
					String paraName = field.getName();
					boolean isImportant = true;
					if (null != annotationField) {
						if (!annotationField.fieldName().equals("")) {
							paraName = annotationField.fieldName();
						}
						FieldTypeEnum fieldTypeEnum = annotationField.fieldType();
						Assert.isFalse(fieldTypeEnum.equals(FieldTypeEnum.OBJECT), paraName + "无法校验对象类型",
								SystemErrorCode.CODE_ERROR);
						isImportant = annotationField.importantParameter();
					}
					Object contentObject = content.get(paraName);
					try {
						if (isImportant) {
							if (contentObject == null) {
								FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, paraName + "参数没有指定");
							}
							validate(requestMap, paraName, contentObject);
						} else {
							if (contentObject != null) {
								validate(requestMap, paraName, contentObject);
							}
						}
					} catch (FrameworkException e) {
						throw e;
					} catch (Exception e) {
						logger.error("", e);
						FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, paraName + "字段验证不通过");
					}
					field.setAccessible(true);
					if (null != contentObject) {
						if (contentObject.getClass().equals(JSONArray.class)
								|| contentObject.getClass().equals(JSONObject.class)) {
							contentObject = JsonUtil.fromJson(contentObject.toString(), field.getType());
						} else {
							contentObject = BeanTools.convertType(contentObject, contentObject.getClass(),
									field.getType());
						}
					}
					field.set(o, contentObject);
				}
				invocationContext.getRequestMessage().setDefaultObject(o);
			} catch (FrameworkException e) {
				throw e;
			} catch (Exception e) {
				logger.error("", e);
				FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, "字段验证不通过");
			}
		}
		nextHandler.execute();
	}

	private void validate(Map<String, Object> requestMap, String parameter, Object value) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		Map<String, FieldHandlerHolder> fieldMap = applicationContext.getFieldManager().getFieldMap(act);
		FieldHandlerHolder fieldHandler = fieldMap.get(parameter);
		Assert.notNull(fieldHandler, parameter + "字段不存在", SystemErrorCode.PARAMETER_ERROR);
		FieldTypeHandler fieldTypeHandler = fieldHandler.getFieldTypeHandler();
		Class<?> fieldClass = applicationContext.getFieldManager().getFieldClass(act, parameter);
		if (null == fieldClass)
			fieldClass = void.class;
		Assert.isTrue(fieldTypeHandler.validate(requestMap, parameter, value, fieldClass, version), parameter + "验证不通过",
				SystemErrorCode.PARAMETER_ERROR);
	}
}
