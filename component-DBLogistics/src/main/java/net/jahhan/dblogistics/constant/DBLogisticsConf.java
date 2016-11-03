package net.jahhan.dblogistics.constant;

import java.util.Properties;

import freemarker.template.Configuration;
import net.jahhan.utils.PropertiesUtil;

public class DBLogisticsConf {
	private static Configuration freeMakerConf;
	
	public static Configuration getFreeMakerConf() {
		return freeMakerConf;
	}

	public static void setFreeMakerConf(Configuration freeMakerConf) {
		DBLogisticsConf.freeMakerConf = freeMakerConf;
	}
	private static boolean useDoc=false;

	public static boolean isUseDoc() {
		return useDoc;
	}

	public static void setUseDoc(boolean useDoc) {
		DBLogisticsConf.useDoc = useDoc;
	}
	static{
		Properties property = PropertiesUtil.getProperties("dblogistic");
		if (null != property.getProperty("useDoc"))
			setUseDoc(Boolean.valueOf(property.getProperty("useDoc")));
	}
}
