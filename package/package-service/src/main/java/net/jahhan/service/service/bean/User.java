package net.jahhan.service.service.bean;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.jahhan.service.service.constant.UserTokenType;

@Data
@ApiModel(value = "用户")
public class User implements Serializable {
	private static final long serialVersionUID = -7432953290772366942L;
	@ApiModelProperty(value = "用户id")
	private Long userId;
	@ApiModelProperty(value = "权限列表")
	private List<String> authList;
	@ApiModelProperty(value = "秘钥")
	private String secrityKey;
	@ApiModelProperty(value = "用户token类型")
	private transient UserTokenType userAuthorizationType;
	@ApiModelProperty(value = "是否需要refreshToken")
	private transient boolean needRefreshToken = false;
	@ApiModelProperty(value = "新token")
	private transient String newToken;
	@ApiModelProperty(value = "token更新信息")
	private transient TokenOVO tokenOVO;
}