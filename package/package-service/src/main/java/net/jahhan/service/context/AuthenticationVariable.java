package net.jahhan.service.context;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.context.BaseContext;
import net.jahhan.context.Variable;
import net.jahhan.service.service.bean.Service;
import net.jahhan.service.service.bean.User;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthenticationVariable extends Variable {
	private User user;
	private Service service;
	@ApiParam("消息体秘钥")
	private String key;
	@ApiParam("是否需要加密")
	private boolean crypt = false;
	@ApiParam("是否普通请求模式")
	private boolean commonRequest = true;
	@ApiParam("是否检查请求模式")
	private boolean checkMode = false;
	@ApiParam("请求串签名")
	private String sign;
	@ApiParam("是否默认登陆模式的single token")
	private boolean isFirstSingleToken = false;
	@ApiParam("是否无需token访问")
	private boolean isNoneToken = false;
	@ApiParam("是否doc请求")
	private boolean docRequest = false;
	
	public static AuthenticationVariable getAuthenticationVariable() {
		AuthenticationVariable variable = (AuthenticationVariable) BaseContext.CTX.getVariableContext()
				.getVariable("authentication");
		if (null == variable) {
			variable = new AuthenticationVariable();
			BaseContext.CTX.getVariableContext().putVariable("authentication", variable);
		}
		return variable;
	}
}
