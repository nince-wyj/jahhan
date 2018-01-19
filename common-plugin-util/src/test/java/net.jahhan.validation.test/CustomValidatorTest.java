package net.jahhan.validation.test;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Created by linwb on 2018/1/18 0018.
 */
public class CustomValidatorTest {
    @Before
    public void init(){}
    @Test
    public void test(){
        User user = new User();
        user.setAge(2);

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();

        Validator validator = vf.getValidator();

        Set<ConstraintViolation<User>> set= validator.validate(user,GroupB.class);

        for (ConstraintViolation<User> constraintViolation : set) {

            System.out.println(constraintViolation.getMessage());

        }
    }
}
