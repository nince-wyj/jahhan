package net.jahhan.controller.authenticationcenter.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "exchangeToken信息")
public class ExchangeTokenIVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "跳转url")
	private String redirect_url;

	@ApiModelProperty(value = "code")
	private String code;
	
	@ApiModelProperty(value = "类型")
	private String grantType;
}