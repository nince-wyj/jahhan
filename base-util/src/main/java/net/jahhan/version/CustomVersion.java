package net.jahhan.version;

import java.util.Properties;

import net.jahhan.utils.PropertiesUtil;

/**
 * @author nince
 */
public abstract class CustomVersion {

	public static boolean useSession(int type, String version) {
		Properties property = PropertiesUtil.getProperties("version");
		if (null == property) {
			return true;
		}
		String cVersion = property.getProperty("useSession");
		if (null == cVersion) {
			return true;
		}
		String tVersion = property.getProperty(type % 100 + ".useSession");
		if (null != tVersion) {
			cVersion = tVersion;
		}
		return VersionHelp.noSamllerThan(version, cVersion);
	}
}
