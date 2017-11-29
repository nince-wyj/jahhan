package net.jahhan.init.initer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1800)
public class SwaggerIniter implements BootstrapInit {
	@Override
	public void execute() {
		BeanConfig swaggerConfig = new BeanConfig();
		swaggerConfig.setResourcePackage(PropertiesUtil.get("base", "swagger.scan"));
		swaggerConfig.setVersion("1.0.0");
		swaggerConfig.setSchemes(new String[] { "http", "https" });
		swaggerConfig.setHost("localhost");
		swaggerConfig.setBasePath("/service");
		swaggerConfig.setScan(true);
		Info info = new Info();
		info.setTitle(PropertiesUtil.get("base", "serviceCode") + " API");
		info.setDescription(PropertiesUtil.get("base", "serviceDescription"));
		swaggerConfig.setInfo(info);
	}
}
