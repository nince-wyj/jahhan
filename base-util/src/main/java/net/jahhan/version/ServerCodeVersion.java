package net.jahhan.version;

import net.jahhan.constant.SysConfiguration;

public abstract class ServerCodeVersion {
	public static boolean castToString(String version) {
		if(version.equals("")){
			version = SysConfiguration.getVersion();
		}
		return !VersionHelp.noSamllerThan(version, "1.2");
	}
}
