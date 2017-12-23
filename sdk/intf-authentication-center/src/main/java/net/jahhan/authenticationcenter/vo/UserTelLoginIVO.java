package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户手机登陆")
public class UserTelLoginIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@NotNull(message = "手机号不能为空")
	@ApiModelProperty(value = "手机号")
	private String tel;

	@NotNull(message = "验证码不能为空")
	@ApiModelProperty(value = "验证码")
	private String code;

}