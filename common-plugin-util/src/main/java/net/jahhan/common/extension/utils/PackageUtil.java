package net.jahhan.common.extension.utils;

import net.jahhan.common.extension.constant.BaseConfiguration;

public abstract class PackageUtil {
	/**
	 * 获取框架及业务包
	 * 
	 * @param packageName
	 * @return
	 */
	public static String[] packages(String packageName) {
		return new String[] { BaseConfiguration.FRAMEWORK_PATH + "." + packageName,
				BaseConfiguration.COMPANY_PATH + "." + packageName };
	}
}
