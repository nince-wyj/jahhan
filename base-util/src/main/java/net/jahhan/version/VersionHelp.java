package net.jahhan.version;

/**
 * @author nince
 */
public abstract class VersionHelp {

	/**
	 * 
	 * @param version 要比较版本
	 * @param cVersion 被比较版本
	 * @return
	 */
	public static boolean noSamllerThan(String version, String cVersion) {
		String[] ver = version.split("\\.");
		String[] cver = cVersion.split("\\.");
		for (int i = 0; i < ver.length; i++) {
			Integer iv = Integer.valueOf(ver[i]);
			Integer civ = Integer.valueOf(cver[i]);
			if (iv < civ)
				return false;
			if (iv > civ)
				return true;
		}
		return true;
	}
}
