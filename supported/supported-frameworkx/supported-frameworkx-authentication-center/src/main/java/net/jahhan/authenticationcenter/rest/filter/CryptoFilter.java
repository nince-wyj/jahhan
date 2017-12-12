package net.jahhan.authenticationcenter.rest.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;

import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.context.BaseContext;
import net.jahhan.context.BaseVariable;
import net.jahhan.exception.JahhanException;
import net.jahhan.service.authenticationcenter.Authentication;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.User;
import net.jahhan.service.service.constant.UserTokenType;
import net.jahhan.spi.common.ICrypto;

@Priority(Integer.MIN_VALUE + 100)
@Named
public class CryptoFilter
		implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter, ClientResponseFilter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String DUBBO_ATTACHMENT_HEADER = "Attachment";
	private ICrypto md5Crypto = ExtensionExtendUtil.getExtension(ICrypto.class, "md5");
	private ICrypto aesCrypto = ExtensionExtendUtil.getExtension(ICrypto.class, "aes");

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		try {

			Map<String, String> tokenMap = new HashMap<>();
			String authorizationHeaders = requestContext.getHeaderString(AUTHORIZATION_HEADER);
			if (authorizationHeaders != null) {
				for (String header : authorizationHeaders.split(",")) {
					String[] split = header.split(" ");
					String key = split[0];
					String value = "";
					if (split.length > 1) {
						value = split[1];
					}
					if (!StringUtils.isEmpty(key)) {
						String string = URLDecoder.decode(value.trim(), "UTF-8");
						tokenMap.put(key.trim(), string);
					}
				}
			}
			UriInfo uriInfo = requestContext.getUriInfo();
			String path = uriInfo.getPath();
			boolean docRequest = path.endsWith("/swagger");
			Map<String, Cookie> cookies = requestContext.getCookies();
			if (cookies.containsKey(UserTokenType.SINGLE_TOKEN.getValue())
					&& !tokenMap.containsKey(UserTokenType.SINGLE_TOKEN.getValue())) {
				Cookie cookie = cookies.get(UserTokenType.SINGLE_TOKEN.getValue());
				tokenMap.put(UserTokenType.SINGLE_TOKEN.getValue(), cookie.getValue());
			}
			Authentication authentication = BaseContext.CTX.getInjector().getInstance(Authentication.class);
			URI requestUri = uriInfo.getRequestUri();

			InputStream entityStream = requestContext.getEntityStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(entityStream));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String context = sb.toString();

			String uriMd5Sign = md5Crypto
					.encrypt(requestContext.getMethod() + path + requestUri.getRawQuery() + context, null);
			AuthenticationVariable.getAuthenticationVariable().setSign(uriMd5Sign);
			AuthenticationVariable.getAuthenticationVariable().setDocRequest(docRequest);
			boolean needDecrypt = authentication.decryptToken(tokenMap);
			if (needDecrypt && !(docRequest && BaseConfiguration.IS_DEBUG)) {
				AuthenticationVariable.getAuthenticationVariable().setCrypt(true);
				String key = AuthenticationVariable.getAuthenticationVariable().getKey();
				Assert.notNullString(key, "鉴权秘钥获取失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
				context = aesCrypto.decrypt(context, key);
			}
			String requestMd5Sign = md5Crypto
					.encrypt(requestContext.getMethod() + path + requestUri.getRawQuery() + context, null);
			BaseVariable.getBaseVariable().setSign(requestMd5Sign);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				out.write(context.getBytes());
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				requestContext.setEntityStream(in);
			} catch (Exception e) {
				JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
			} finally {
				out.close();
			}
		} catch (JahhanException e) {
			throw e;
		} catch (Exception e) {
			JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		}
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		String token = BaseContext.CTX.getToken();
		if (token != null) {
			requestContext.getHeaders().add(AUTHORIZATION_HEADER, "BASIC_TOKEN " + token);
		} else {
			requestContext.getHeaders().add(AUTHORIZATION_HEADER, "BASIC_TOKEN login");
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		try {
			boolean crypt = authenticationVariable.isCrypt();
			if (crypt) {
				Object entity = responseContext.getEntity();
				String key = authenticationVariable.getKey();
				String encrypt = aesCrypto.encrypt(JsonUtil.toJson(entity), key);
				responseContext.setEntity(encrypt, new Annotation[] {}, MediaType.TEXT_PLAIN_TYPE);
			}
		} catch (Exception e) {
			responseContext.setEntity("权限或秘钥错误！", new Annotation[] {}, MediaType.TEXT_PLAIN_TYPE);
		}
		User user = authenticationVariable.getUser();
		if (null != user) {
			UserTokenType userAuthorizationType = user.getUserAuthorizationType();
			MultivaluedMap<String, Object> headers = responseContext.getHeaders();
			if (userAuthorizationType.equals(UserTokenType.SINGLE_TOKEN)) {
				headers.remove(AUTHORIZATION_HEADER);
				headers.add(AUTHORIZATION_HEADER, UserTokenType.SINGLE_TOKEN.getValue() + " " + user.getNewToken());
				Map<String, NewCookie> cookies = responseContext.getCookies();
				NewCookie cookie = new NewCookie(UserTokenType.SINGLE_TOKEN.getValue(), user.getNewToken());
				cookies.put(UserTokenType.SINGLE_TOKEN.getValue(), cookie);
			} else if (user.isNeedRefreshToken()) {
				headers.add(DUBBO_ATTACHMENT_HEADER, "refresh_token=need");
			}
		}
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		boolean needDecrypt = AuthenticationVariable.getAuthenticationVariable().isCrypt();
		URI uriInfo = requestContext.getUri();
		String path = uriInfo.getPath();
		boolean docRequest = path.endsWith("/swagger");
		if (needDecrypt && !(docRequest && BaseConfiguration.IS_DEBUG)) {
			InputStream entityStream = responseContext.getEntityStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(entityStream));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String context = sb.toString();
			String key = AuthenticationVariable.getAuthenticationVariable().getKey();
			Assert.notNullString(key, "鉴权秘钥获取失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
			context = aesCrypto.decrypt(context, key);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				out.write(context.getBytes());
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				responseContext.setEntityStream(in);
			} catch (Exception e) {
				JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
			} finally {
				out.close();
			}
		}
	}

}
