package net.jahhan.demo.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户信息")
public class UserNameIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "用户id")
	private Long userId;
	
	@NotNull(message = "userName不能为空")
	@ApiModelProperty(value = "用户名")
	@Length(min = 0, max = 32, message = "userName字符串长度需小于32")
	private String userName;
}