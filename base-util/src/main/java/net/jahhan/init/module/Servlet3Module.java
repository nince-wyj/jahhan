package net.jahhan.init.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.google.inject.servlet.ServletModule;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ScanUtils;
import net.jahhan.web.servlet.ServiceIdInContentServlet;

@InitAnnocation(isLazy = false, initSequence = 9000, onlyWeb = true)
public class Servlet3Module extends ServletModule {

	private final List<Package> packages = new ArrayList<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void configureServlets() {
		Package packs = ServiceIdInContentServlet.class.getPackage();
		Set<Class> classes = ScanUtils.findClassInPackage(".+", packs);
		Set<Class> companyClasses = ScanUtils.findClassInPath(".+Servlet",
				SysConfiguration.getCompanyName().replace(".", "/") + "/servlet/");
		classes.addAll(companyClasses);
		for (Class<?> servletClass : classes) {
			if (HttpServlet.class.isAssignableFrom(servletClass)) {
				Class<HttpServlet> servletClazz = (Class<HttpServlet>) servletClass;
				WebServlet webServlet = servletClass.getAnnotation(WebServlet.class);
				String[] urlPatterns = webServlet.urlPatterns();
				if (urlPatterns.length > 1) {
					serve(urlPatterns[0], Arrays.copyOfRange(urlPatterns, 1, urlPatterns.length)).with(servletClazz,
							getInitParams(webServlet));
				} else {
					if (urlPatterns.length == 1) {
						serve(urlPatterns[0]).with(servletClazz, getInitParams(webServlet));
					} else {
						addError("Guice found a WebServlet %s with no urlPatterns defined.",
								webServlet.getClass().getCanonicalName());
					}
				}
			}
		}
	}

	private Map<String, String> getInitParams(WebServlet webServlet) {
		final WebInitParam[] params = webServlet.initParams();
		final Map<String, String> initParams = new HashMap<>(params.length);
		for (int i = 0; i < params.length; i++) {
			WebInitParam w = params[i];
			initParams.put(w.name(), w.value());
		}
		return initParams;
	}

	protected void scanServlets(Package pack) {
		packages.add(pack);
	}

}
