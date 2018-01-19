package net.jahhan.validation.annotation;


/**
 * Created by linwb on 2018/1/18 0018.
 */


import net.jahhan.validation.impl.StringInValidatiorImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = StringInValidatiorImpl.class
)
public @interface StringIn {
    String message() default "输入的值不在列表中";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] valueIn();
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        StringIn[] value();
    }
}
