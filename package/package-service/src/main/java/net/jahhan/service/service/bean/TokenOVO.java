package net.jahhan.service.service.bean;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "token信息")
public class TokenOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "token")
	private String token;

	@ApiModelProperty(value = "刷新token")
	private String refreshToken;

	@ApiModelProperty(value = "过期时长")
	private Long expireIn;
}