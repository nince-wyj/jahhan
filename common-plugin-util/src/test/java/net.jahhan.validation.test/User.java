package net.jahhan.validation.test;

import lombok.Data;
import net.jahhan.validation.annotation.NumberIn;

/**
 * Created by linwb on 2018/1/18 0018.
 */
@Data
public class User {

    @NumberIn.List({
            @NumberIn(valueIn = {1,2,3},groups = {GroupA.class},message = "age 值不为1,2,3中的值"),
            @NumberIn(valueIn = {4,5,6},groups = {GroupB.class},message = "age 值不为4,5,6中的值")
    })
    private Integer age;
}
