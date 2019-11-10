package net.jahhan.service.authenticationcenter;

import java.util.UUID;

import javax.inject.Named;
import javax.inject.Singleton;

import com.frameworkx.annotation.Reference;

import net.jahhan.authenticationcenter.intf.ServiceIntf;
import net.jahhan.authenticationcenter.vo.ServiceLoginIVO;
import net.jahhan.authenticationcenter.vo.ServiceLoginOVO;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.ThreadVariableContext;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.variable.BaseGlobalVariable;
import net.jahhan.variable.BaseThreadVariable;

@Named
@Singleton
public class ServiceLogin {
	@Reference
	private ServiceIntf serviceInft;

	public void loginInit() {
		BaseContext applicationContext = BaseContext.CTX;
		BaseGlobalVariable baseGlobalVariable = (BaseGlobalVariable)applicationContext.getVariable("base");
		ThreadVariableContext variableContext = new ThreadVariableContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).setChainId(UUID.randomUUID().toString());

		ServiceLoginIVO serviceLoginIVO = new ServiceLoginIVO();
		String serviceCode = PropertiesUtil.get("base", "serviceCode");
		String password = PropertiesUtil.get("base", "servicePassword");
		serviceLoginIVO.setServiceCode(serviceCode);
		serviceLoginIVO.setPassword(password);
		serviceLoginIVO.setType("inner");
		ServiceLoginOVO login = serviceInft.login(serviceLoginIVO);
		Assert.notNull(login, JahhanErrorCode.INIT_ERROR);
		baseGlobalVariable.setToken(login.getToken());
		baseGlobalVariable.setAppPubKey(login.getAppPubKey());
		baseGlobalVariable.setThirdPubKey(login.getThirdPubKey());
		baseGlobalVariable.setBrowserSecrityKey(login.getBrowserSecrityKey());
		baseGlobalVariable.setBrowserPubKey(login.getBrowserPubKey());
		baseGlobalVariable.setInnerSecrityKey(login.getInnerSecrityKey());
		baseGlobalVariable.setFirstSingleToken(login.getFirstSingleToken());
	}

}
