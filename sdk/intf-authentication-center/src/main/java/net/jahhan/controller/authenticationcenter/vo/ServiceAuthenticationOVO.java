package net.jahhan.controller.authenticationcenter.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务鉴权信息")
public class ServiceAuthenticationOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "服务code")
	private String serviceCode;
	
	@ApiModelProperty(value = "token类型")
	private String tokenType;

	@ApiModelProperty(value = "秘钥")
	private String secrityKey;

	@ApiModelProperty(value = "过期时长")
	private Long expireIn;
}