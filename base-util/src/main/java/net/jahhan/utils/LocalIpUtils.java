package net.jahhan.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalIpUtils {

	private static Logger logger = LoggerFactory.getLogger(LocalIpUtils.class);
	private static HashMap<String, String> ipMap = new HashMap<String, String>();

	static {
		Enumeration<NetworkInterface> allNetInterfaces = null;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			logger.error("get network interface error!");
		}
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					ipMap.put(netInterface.getName(), ip.getHostAddress());
				}
			}
		}
	}

	public static HashMap<String, String> getIpMap() {
		return ipMap;
	}

	public static String getFirstIp() {
		return ipMap.values().iterator().next();
	}

	public static String getIpByName(String interfaceName) {
		return ipMap.get(interfaceName);
	}
}
