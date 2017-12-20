package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "第三方用户鉴权信息")
public class ThirdAuthenticationOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "是否有权限")
	private boolean hasAuthentication;
	
}