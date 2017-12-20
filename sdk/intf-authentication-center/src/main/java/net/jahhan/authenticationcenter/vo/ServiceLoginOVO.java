package net.jahhan.authenticationcenter.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.service.service.bean.TokenOVO;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "服务登陆结果")
public class ServiceLoginOVO extends TokenOVO {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "权限")
	private String scope;
	
	@ApiModelProperty(value = "内部服务秘钥")
	private String innerSecrityKey;
	
	@ApiModelProperty(value = "app公共秘钥")
	private String appPubKey;

	@ApiModelProperty(value = "第三方服务公共秘钥")
	private String thirdPubKey;

	@ApiModelProperty(value = "浏览器秘钥（不下发）")
	private String browserSecrityKey;
	
	@ApiModelProperty(value = "浏览器公共秘钥（下发）")
	private String browserPubKey;
	
	@ApiModelProperty(value = "浏览器第一次上传token（下发）")
	private String firstSingleToken;
}