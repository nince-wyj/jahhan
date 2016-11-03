package net.jahhan.web.ws;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

public class WSRequest {
	private String message;
	private String wsSessionId;
	private String httpSessionId;
	private Session session;
	private HttpSession httpSession;
	private Map<String, List<String>> headerMap;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getWsSessionId() {
		return wsSessionId;
	}

	public void setWsSessionId(String wsSessionId) {
		this.wsSessionId = wsSessionId;
	}

	public String getHttpSessionId() {
		return httpSessionId;
	}

	public void setHttpSessionId(String httpSessionId) {
		this.httpSessionId = httpSessionId;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public Map<String, List<String>> getHeaderMap() {
		return headerMap;
	}

	public void setHeaderMap(Map<String, List<String>> headerMap) {
		this.headerMap = headerMap;
	}

}
