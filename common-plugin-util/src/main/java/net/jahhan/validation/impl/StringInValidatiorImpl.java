package net.jahhan.validation.impl;


import net.jahhan.validation.annotation.StringIn;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 验证注解@StringIn功能实现类
 * Created by linwb on 2018/1/18 0018.
 * @author linweibin
 */
public class StringInValidatiorImpl implements ConstraintValidator<StringIn, String> {

    private String[] valueIn;

    @Override
    public void initialize(StringIn stringIn) {
        valueIn = stringIn.valueIn();
    }

    @Override
    public boolean isValid(String data, ConstraintValidatorContext constraintValidatorContext) {
        if (data != null) {
            for (String v : valueIn) {
                if (v.equals(data)) {
                    return true;
                }
            }
        }
        return false;
    }
}
