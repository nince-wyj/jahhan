package net.jahhan.validation.impl;

import net.jahhan.validation.annotation.NumberIn;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by linwb on 2018/1/18 0018.
 */
public class NumberInValidatiorImpl implements ConstraintValidator<NumberIn, Number> {

    private long[] valueIn;

    @Override
    public void initialize(NumberIn numberIn) {
        this.valueIn = numberIn.valueIn();
    }

    @Override
    public boolean isValid(Number data, ConstraintValidatorContext constraintValidatorContext) {
        if (data != null) {
            for (Long v : valueIn) {
                if (v.equals(data.longValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}
