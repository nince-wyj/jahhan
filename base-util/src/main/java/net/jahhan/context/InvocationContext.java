package net.jahhan.context;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jahhan.api.RequestMessage;
import net.jahhan.api.ResponseMessage;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.constant.enumeration.InvocationEnum;
import net.jahhan.db.event.DBEvent;
import net.jahhan.factory.httpclient.AsyncHttpCallBack;
import net.jahhan.web.ws.WSRequest;

/**
 * 线程局部变量
 * 
 */
public class InvocationContext {

	private HttpServletRequest request;

	private HttpServletResponse response;

	private WSRequest wsRequest;

	private RequestMessage requestMessage;

	private ResponseMessage responseMessage;
	// 分布式锁
	private String lock;

	// 数据库连接
	private List<Connection> connList = new ArrayList<Connection>();

	private Connection currentConn = null;
	// 线程类型
	private InvocationEnum invocationEnum;

	// 线程缓存
	private Map<String, Object> writeCache = new HashMap<>();

	private List<String> modifyKeys = new ArrayList<>();
	// bpm
	private List<String> bpmRecessFillList = new ArrayList<>();
	// 异步http
	private boolean asyncClient = false;

	private List<AsyncHttpCallBack> asyncHttpCallBackList;

	private AsyncContext asyncCtx;

	private Map<String, Object> sessions = new HashMap<>();

	private List<DBEvent> events = new ArrayList<>();

	public void setDBEvent(DBEvent event) {
		events.add(event);
	}

	public List<DBEvent> getEvents() {
		return events;
	}

	public InvocationContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.invocationEnum = InvocationEnum.SERVLET;
		this.asyncHttpCallBackList = new ArrayList<>();
	}

	public InvocationContext(WSRequest wsRequest) {
		this.wsRequest = wsRequest;
		this.invocationEnum = InvocationEnum.WEBSOCKET;
		this.asyncHttpCallBackList = new ArrayList<>();
	}

	public InvocationContext() {
		this.invocationEnum = InvocationEnum.THREAD;
		this.asyncHttpCallBackList = new ArrayList<>();
	}

	public InvocationContext(InvocationContext invocationContext) {
		this.invocationEnum = InvocationEnum.HTTPCALLBACK;
		this.asyncHttpCallBackList = invocationContext.getAsyncHttpCallBackList();
		this.asyncCtx = invocationContext.getAsyncCtx();
		this.request = invocationContext.getRequest();
		this.response = invocationContext.getResponse();
		this.requestMessage = invocationContext.getRequestMessage();
		this.responseMessage = invocationContext.getResponseMessage();
		this.wsRequest = invocationContext.getWsRequest();
	}

	public List<AsyncHttpCallBack> getAsyncHttpCallBackList() {
		return asyncHttpCallBackList;
	}

	public Object getSession(String sessionName) {
		return sessions.get(sessionName);
	}

	public void setSession(String sessionName, Object session) {
		this.sessions.put(sessionName, session);
	}

	public int asyncHttpCallBackListSize() {
		return asyncHttpCallBackList.size();
	}

	public AsyncContext getAsyncCtx() {
		return asyncCtx;
	}

	public void setAsyncCtx(AsyncContext asyncCtx) {
		this.asyncCtx = asyncCtx;
	}

	public boolean removeAsyncHttpCallBackList(AsyncHttpCallBack asyncHttpCallBack) {
		synchronized (asyncHttpCallBackList) {
			return asyncHttpCallBackList.remove(asyncHttpCallBack);
		}
	}

	public void addAsyncHttpCallBack(AsyncHttpCallBack asyncHttpCallBack) {
		synchronized (asyncHttpCallBackList) {
			asyncHttpCallBackList.add(asyncHttpCallBack);
		}
	}

	public List<String> getBpmRecessFillList() {
		return bpmRecessFillList;
	}

	public void addBpmRecess(String bpmRecessFill) {
		bpmRecessFillList.add(bpmRecessFill);
	}

	public void clearLocalCache() {
		writeCache.clear();
		modifyKeys.clear();
	}

	public void addPojo(Class<?> pojoClass, String id, Object pojo) {
		writeCache.put(pojoClass.getSimpleName() + id, pojo);
	}

	public void delPojo(Class<?> pojoClass, String id) {
		writeCache.remove(pojoClass.getSimpleName() + id);
		modifyKeys.add(pojoClass.getSimpleName() + id);
	}

	public boolean isDeletePojo(Class<?> pojoClass, String id) {
		return modifyKeys.contains(pojoClass.getSimpleName() + id);
	}

	public Object getLocalCachePojo(Class<?> pojoClass, String id) {
		return writeCache.get(pojoClass.getSimpleName() + id);
	}

	public boolean isAsyncClient() {
		return asyncClient;
	}

	public void setAsyncClient(boolean asyncClient) {
		this.asyncClient = asyncClient;
	}

	public String getLock() {
		return lock;
	}

	public void setLock(String lock) {
		this.lock = lock;
	}

	public WSRequest getWsRequest() {
		return wsRequest;
	}

	public InvocationEnum getInvocationEnum() {
		return invocationEnum;
	}

	private DBConnectionType connectionType = null;

	private DBLogisticsConnectionType DBLogisticsConnType = null;

	public DBLogisticsConnectionType getDBLogisticsConnType() {
		return DBLogisticsConnType;
	}

	public void setDBLogisticsConnType(DBLogisticsConnectionType dBLogisticsConnType) {
		DBLogisticsConnType = dBLogisticsConnType;
	}

	public DBConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(DBConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public RequestMessage getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(RequestMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	public ResponseMessage getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(ResponseMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	public Connection getCurrentConn() {
		return currentConn;
	}

	public void setCurrentConn(Connection currentConn) {
		this.currentConn = currentConn;
	}

	public List<Connection> getConnections() {
		return connList;
	}

	public void addDbCon(Connection con) {
		this.connList.add(con);
	}

	public void removeDbCon(Connection connection) {
		if (this.getCurrentConn() == connection) {
			this.setCurrentConn(null);
		}
		if (this.connList == null) {
			return;
		}
		this.connList.remove(connection);
	}
}
