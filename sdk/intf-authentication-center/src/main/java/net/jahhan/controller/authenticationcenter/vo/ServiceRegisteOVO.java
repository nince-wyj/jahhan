package net.jahhan.controller.authenticationcenter.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务注册结果")
public class ServiceRegisteOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "服务ID")
	private String serviceCode;

	@ApiModelProperty(value = "服务密码")
	private String password;


}