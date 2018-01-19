package net.jahhan.validation.annotation;

/**
 * Created by linwb on 2018/1/19 0019.
 */


import net.jahhan.validation.impl.NumberInValidatiorImpl;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 该注解是判断int或long类型的参数值在配置的数值中，存在则验证通过，不存在则验证不通过
 * @author linweibin
 *
 * @apiExample
 * 简单用法：
 *     @NumberIn(valueIn = {1,2,3},message = "age 值不为1,2,3中的值")
 *     private Integer age;
 *
 * 高级用法，采用不同分组的验证不同值的用法：
 *      @NumberIn.List({
 *         @NumberIn(valueIn = {1,2,3},groups = {GroupA.class},message = "age 值不为1,2,3中的值"),
 *         @NumberIn(valueIn = {4,5,6},groups = {GroupB.class},message = "age 值不为4,5,6中的值")
 *     })
 *     private Integer age;
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = NumberInValidatiorImpl.class
)
public @interface NumberIn {
    String message() default "输入的值不在列表中";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long[] valueIn();

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        NumberIn[] value();
    }
}
