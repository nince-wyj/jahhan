package net.jahhan.common.extension.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jahhan.common.extension.constant.BaseConfiguration;

public abstract class PackageUtil {
	static final List<String> servicePathList;

	static {
		String servicePath = BaseConfiguration.SERVICE_PATH;
		String[] servicePathSplit = servicePath.split(",");
		servicePathList = Arrays.asList(servicePathSplit);
	}

	/**
	 * 获取框架及业务包
	 * 
	 * @param packageName
	 * @return
	 */
	public static String[] packages(String... packageNames) {
		List<String> packageList = new ArrayList<>();
		for (String path : servicePathList) {
			for (String packageName : packageNames) {
				packageList.add(path + "." + packageName);
			}
		}
		for (String packageName : packageNames) {
			packageList.add(BaseConfiguration.FRAMEWORK_PATH + "." + packageName);
		}
		return packageList.toArray(new String[packageList.size()]);
	}
}
