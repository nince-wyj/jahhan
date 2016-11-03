package net.jahhan.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步处理的接口信息
 * 
 * @author nince
 *
 */
public class AsyncActionCache {
	private static AsyncActionCache instance = new AsyncActionCache();

	private AsyncActionCache() {

	}

	public static AsyncActionCache getInstance() {
		return instance;
	}

	private List<String> actionList = new ArrayList<>();

	public boolean contains(String actionName) {
		return actionList.contains(actionName);
	}

	public void setAction(String actionName) {
		actionList.add(actionName);
	}

}
