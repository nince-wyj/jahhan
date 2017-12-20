package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "用户注册信息")
public class UserRegistIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "用户名")
	@Length(min = 0, max = 32, message = "userName字符串长度需小于32")
	private String userName;

	@ApiModelProperty(value = "用户头像")
	@Length(min = 0, max = 128, message = "imageUrl字符串长度需小于128")
	private String imageUrl;

	@ApiModelProperty(value = "手机号")
	@Length(min = 11, max = 11, message = "mobilePhone字符串长度需等于11")
	private String mobilePhone;

	@ApiModelProperty(value = "email")
	@Email
	@Length(min = 0, max = 64, message = "email字符串长度需小于64")
	private String email;

	@ApiModelProperty(value = "登陆账号")
	@Length(min = 0, max = 64, message = "login字符串长度需小于64")
	private String login;

	@ApiModelProperty(value = "性别")
	@Length(min = 0, max = 4, message = "gender字符串长度需小于4")
	private String gender;

	@NotNull(message = "password不能为空")
	@ApiModelProperty(value = "密码")
	@Length(min = 0, max = 32, message = "password字符串长度需小于32")
	private String password;
}