package net.jahhan.authenticationcenter.vo;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户鉴权信息")
public class UserAuthenticationOVO implements Serializable {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "用户id")
	private Long userId;
	
	@ApiModelProperty(value = "权限列表")
	private List<String> authList;

	@ApiModelProperty(value = "秘钥")
	private String secrityKey;

	@ApiModelProperty(value = "过期时长")
	private Long expireIn;
	
	@ApiModelProperty(value = "随机数")
	private String nonce;
}