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
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.context.VariableContext;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.common.extension.utils.PropertiesUtil;

@Named
@Singleton
public class ServiceLogin {
	@Reference
	private ServiceIntf serviceInft;

	public void loginInit() {
		BaseContext applicationContext = BaseContext.CTX;
		VariableContext variableContext = new VariableContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		BaseVariable.getBaseVariable().setChainId(UUID.randomUUID().toString());

		ServiceLoginIVO serviceLoginIVO = new ServiceLoginIVO();
		String serviceCode = PropertiesUtil.get("base", "serviceCode");
		String password = PropertiesUtil.get("base", "servicePassword");
		serviceLoginIVO.setServiceCode(serviceCode);
		serviceLoginIVO.setPassword(password);
		serviceLoginIVO.setType("inner");
		ServiceLoginOVO login = serviceInft.login(serviceLoginIVO);
		Assert.notNull(login, JahhanErrorCode.INIT_ERROR);
		applicationContext.setToken(login.getToken());
		applicationContext.setAppPubKey(login.getAppPubKey());
		applicationContext.setThirdPubKey(login.getThirdPubKey());
		applicationContext.setBrowserSecrityKey(login.getBrowserSecrityKey());
		applicationContext.setBrowserPubKey(login.getBrowserPubKey());
		applicationContext.setInnerSecrityKey(login.getInnerSecrityKey());
		applicationContext.setFirstSingleToken(login.getFirstSingleToken());
	}

}
