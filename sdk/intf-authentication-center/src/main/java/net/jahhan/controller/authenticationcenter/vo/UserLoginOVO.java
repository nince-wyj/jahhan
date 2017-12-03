package net.jahhan.controller.authenticationcenter.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.service.service.bean.TokenOVO;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "用户登陆结果")
public class UserLoginOVO extends TokenOVO {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "秘钥")
	private String secrityKey;
}