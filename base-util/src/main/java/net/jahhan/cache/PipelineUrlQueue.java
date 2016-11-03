package net.jahhan.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import net.jahhan.factory.httpclient.PipeLineCallbackHold;

public class PipelineUrlQueue {
	private static PipelineUrlQueue instance = new PipelineUrlQueue();

	private PipelineUrlQueue() {

	}

	public static PipelineUrlQueue getInstance() {
		return instance;
	}

	private Map<String, LinkedBlockingQueue<PipeLineCallbackHold>> pipelineUrlQueueList = new ConcurrentHashMap<>();

	public LinkedBlockingQueue<PipeLineCallbackHold> getPipelineUrlQueue(String host) {
		return pipelineUrlQueueList.get(host);
	}

	public void registerPipelineUrlQueue(String host) {
		pipelineUrlQueueList.put(host, new LinkedBlockingQueue<PipeLineCallbackHold>(8 * 1024));
	}
}
