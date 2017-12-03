package net.jahhan.service.service.bean;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务器")
public class Service implements Serializable {
	private static final long serialVersionUID = -7432953290772366942L;
	@ApiModelProperty(value = "服务器code")
	private String serviceCode;
	@ApiModelProperty(value = "秘钥")
	private String secrityKey;
	@ApiModelProperty(value = "是否内部服务")
	private boolean innerService = false;
}