package net.jahhan.authenticationcenter.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.service.service.bean.TokenOVO;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "第三方token")
public class ThirdTokenOVO extends TokenOVO {

	private static final long serialVersionUID = 10000000L;

	@ApiModelProperty(value = "openId")
	private String openId;

}