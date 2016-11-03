package net.jahhan.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.PipelineUrlQueue;
import net.jahhan.factory.httpclient.PipeLineCallbackHold;
import net.jahhan.factory.httpclient.PipelineHttpCallBack;

/**
 * pipeline线程方法
 * 
 * @author nince
 */
public class PipelineHttpThread implements Runnable {
	protected final static Logger logger = LoggerFactory.getLogger("PipelineHttpThread");
	private CloseableHttpPipeliningClient httpclient = HttpAsyncClients.createPipelining();
	private String host;

	public PipelineHttpThread(String host) {
		super();
		this.host = host;
		httpclient.start();
	}

	@Override
	public void run() {
		HttpHost targetHost = new HttpHost(host);
		List<HttpRequest> requests = new ArrayList<>();
		List<PipeLineCallbackHold> urlAndCallBackList = new ArrayList<>();
		PipelineUrlQueue.getInstance().getPipelineUrlQueue(host).drainTo(urlAndCallBackList);
		for (PipeLineCallbackHold hold : urlAndCallBackList) {
			requests.add(new HttpGet(hold.getPath()));
		}
		httpclient.execute(targetHost, requests, new PipelineHttpCallBack(urlAndCallBackList));
	}

}
