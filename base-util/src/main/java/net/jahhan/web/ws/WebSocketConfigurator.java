package net.jahhan.web.ws;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.utils.Assert;
import net.jahhan.web.ApplicationContextListener;

public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
	@Override
	public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		Assert.notNull(httpSession, "未登陆", SystemErrorCode.SESSION_NO_EXIST);
		Map<String, List<String>> headerMap = request.getHeaders();
		config.getUserProperties().put(HttpSession.class.getName(), httpSession);
		config.getUserProperties().put("headers", headerMap);
	}

	@Override
	public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
		return ApplicationContextListener.injector.getInstance(clazz);
	}
}