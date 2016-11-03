package net.jahhan.web.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.constant.enumeration.FieldTypeEnum;

/**
 * @author nince
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
	public String fieldName() default "";

	public FieldTypeEnum fieldType() default FieldTypeEnum.NULL;

	public String defaultValue() default "";

	public String description() default "";

	public Class<?> referenceObject() default void.class;

	public boolean importantParameter() default true;
}
