package net.jahhan.validation.annotation;


/**
 * Created by linwb on 2018/1/18 0018.
 */


import net.jahhan.validation.impl.StringInValidatiorImpl;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 该注解是判断字符串类型的参数值在配置的字符串中，存在则验证通过，不存在则验证不通过
 *
 * @author linweibin
 *
 * @apiExample
 * 简单用法：
 *     @StringIn(valueIn = {"男","女"},message = "sex 值不为："男","女"中的值")
 *     private String sex;
 *
 * 高级用法，采用不同分组的验证不同值的用法：
 *      @StringIn.List({
 *         @StringIn(valueIn = {"男","女"},groups = {GroupA.class},message = "sex 值不为："男","女"中的值"),
 *         @StringIn(valueIn = {"男","女","人妖","未知"},groups = {GroupB.class},message = "sex 值不为："男","女","人妖","未知"中的值")
 *     })
 *     private String sex;
 */
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
