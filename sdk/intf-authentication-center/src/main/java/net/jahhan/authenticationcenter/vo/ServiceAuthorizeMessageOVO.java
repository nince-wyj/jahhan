package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务授权信息")
public class ServiceAuthorizeMessageOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "服务名称")
	private String serviceName;
	
	@ApiModelProperty(value = "第三方名称")
	private String clientName;
}