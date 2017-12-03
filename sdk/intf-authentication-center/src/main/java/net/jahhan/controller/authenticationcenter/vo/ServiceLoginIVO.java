package net.jahhan.controller.authenticationcenter.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务登陆")
public class ServiceLoginIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@NotNull(message = "serviceCode不能为空")
	@ApiModelProperty(value = "服务代码")
	private String serviceCode;

	@NotNull(message = "password不能为空")
	@ApiModelProperty(value = "服务密码")
	private String password;

	@Pattern(regexp = "inner|third", message = "type错误")
	@ApiModelProperty(value = "登陆类型(inner：内部服务器，third：第三方服务)默认值：third")
	private String type = "third";

}