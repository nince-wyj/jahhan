package net.jahhan.controller.authenticationcenter.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户登陆")
public class UserLoginIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@NotNull(message = "login不能为空")
	@ApiModelProperty(value = "登陆账号")
	private String login;

	@NotNull(message = "password不能为空")
	@ApiModelProperty(value = "密码")
	private String password;

	@Pattern(regexp = "bearer|single", message = "type错误")
	@ApiModelProperty(value = "登陆类型(bearer：客户端登陆，single：浏览器端登陆)默认值：bearer")
	private String type = "bearer";
	
	@ApiModelProperty(value = "服务器名")
	private String service;
}