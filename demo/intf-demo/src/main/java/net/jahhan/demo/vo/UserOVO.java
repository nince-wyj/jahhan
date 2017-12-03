package net.jahhan.demo.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户信息")
public class UserOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "用户id")
	private Long userId;
	
	@NotNull(message = "userName不能为空")
	@ApiModelProperty(value = "用户名")
	@Length(min = 0, max = 32, message = "userName字符串长度需小于32")
	private String userName;

	@NotNull(message = "email不能为空")
	@ApiModelProperty(value = "email")
	@Email(message = "email格式不正确")
	@Length(min = 0, max = 32, message = "email字符串长度需小于32")
	private String email;

	@NotNull(message = "gender不能为空")
	@ApiModelProperty(value = "性别")
	@Pattern(regexp = "男|女", message = "输入值必须为男或女")
	@Length(min = 0, max = 4, message = "gender字符串长度需小于4")
	private String gender;
}