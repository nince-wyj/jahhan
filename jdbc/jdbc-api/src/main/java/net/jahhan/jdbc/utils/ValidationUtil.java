package net.jahhan.jdbc.utils;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;

import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.exception.JahhanException;

public class ValidationUtil {
	private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

	public static void validate(Object validateObject, Class<?> group) {
		Validator validator = factory.getValidator();
		if (null != validator) {
			Set<ConstraintViolation<Object>> constraintViolations = validator.validate(validateObject, group);
			for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
				// 错误信息
				String errorMessage = constraintViolation.getMessage();
				if (!StringUtils.isBlank(errorMessage)) {
					JahhanException.throwException(JahhanErrorCode.VALIATION_EXCEPTION, errorMessage);
				}
			}
		}
	}
}
