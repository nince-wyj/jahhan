package com.frameworkx.cache;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;

@Singleton
public class ProtocolConfigHolder {

	private List<URL> urlList = new ArrayList<>();

	public void addProtocolUrl(String protocol, String host, int port) {
		URL url = new URL(protocol,host,port);
		urlList.add(url);
	}

	public List<URL> getProtocolUrl(){
		return urlList;
	}
}
