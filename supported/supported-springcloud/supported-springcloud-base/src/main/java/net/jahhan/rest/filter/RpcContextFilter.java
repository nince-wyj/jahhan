package net.jahhan.rest.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.context.VariableContext;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.request.context.RequestVariable;

@Priority(Integer.MIN_VALUE + 1)
public class RpcContextFilter implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter {

	private static final String ATTACHMENT_HEADER = "Attachment";

	// currently we use a single header to hold the attachments so that the
	// total attachment size limit is about 8k
	private static final int MAX_HEADER_SIZE = 8 * 1024;
	@Context
	HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		BaseContext applicationContext = BaseContext.CTX;
		VariableContext variableContext = new VariableContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		BaseVariable base = BaseVariable.getBaseVariable();

		RequestVariable requestVariable = RequestVariable.getVariable();
		requestVariable.setRequest(request);

		// this only works for servlet containers
		if (request != null && requestVariable.getRemoteAddress() == null) {
			requestVariable.setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
		}

		requestVariable.setResponse(response);

		String headers = requestContext.getHeaderString(ATTACHMENT_HEADER);
		if (headers != null) {
			for (String header : headers.split(",")) {
				int index = header.indexOf("=");
				if (index > 0) {
					String key = header.substring(0, index);
					String value = header.substring(index + 1);
					if (!StringUtils.isEmpty(key)) {
						String string = URLDecoder.decode(value.trim(), "UTF-8");
						requestVariable.setAttachment(key.trim(), string);
					}
				}
			}
		}
		Map<String, String> attachments = requestVariable.getAttachments();
		String requestId = attachments.get("request_id");
		String chainId = attachments.get("chain_id");
		String behaviorId = attachments.get("behavior_id");
		if (null == requestId) {
			requestId = UUID.randomUUID().toString();
		}
		if (null == chainId) {
			chainId = UUID.randomUUID().toString();
		}
		if (null == behaviorId) {
			behaviorId = UUID.randomUUID().toString();
		}
		base.setRequestId(requestId);
		base.setChainId(chainId);
		base.setBehaviorId(behaviorId);
		BaseContext.CTX.setChain(chainId, Thread.currentThread());
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		int size = 0;
		RequestVariable requestVariable = RequestVariable.getVariable();
		for (Map.Entry<String, String> entry : requestVariable.getAttachments().entrySet()) {
			if (entry.getValue().contains(",") || entry.getValue().contains("=") || entry.getKey().contains(",")
					|| entry.getKey().contains("=")) {
				throw new IllegalArgumentException("The attachments of " + RequestVariable.class.getSimpleName()
						+ " must not contain ',' or '=' when using rest protocol");
			}

			// TODO for now we don't consider the differences of encoding and
			// server limit
			size += entry.getValue().getBytes("UTF-8").length;
			if (size > MAX_HEADER_SIZE) {
				throw new IllegalArgumentException(
						"The attachments of " + RequestVariable.class.getSimpleName() + " is too big");
			}

			StringBuilder attachments = new StringBuilder();
			attachments.append(entry.getKey());
			attachments.append("=");
			String encode = URLEncoder.encode(entry.getValue(), "UTF-8");
			attachments.append(encode);
			requestContext.getHeaders().add(ATTACHMENT_HEADER, attachments.toString());
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		RequestVariable requestVariable = RequestVariable.getVariable();
		if (BaseConfiguration.IS_DEBUG) {
			headers.add("Access-Control-Allow-Origin", "*");
			headers.add("Access-Control-Allow-Headers", "x-requested-with, ssi-token");
			headers.add("Access-Control-Max-Age", "3600");
			headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
		}
		int size = 0;
		for (Map.Entry<String, String> entry : requestVariable.getAttachments().entrySet()) {
			if (entry.getValue().contains(",") || entry.getValue().contains("=") || entry.getKey().contains(",")
					|| entry.getKey().contains("=")) {
				throw new IllegalArgumentException("The attachments of " + RequestVariable.class.getSimpleName()
						+ " must not contain ',' or '=' when using rest protocol");
			}

			// TODO for now we don't consider the differences of encoding and
			// server limit
			size += entry.getValue().getBytes("UTF-8").length;
			if (size > MAX_HEADER_SIZE) {
				throw new IllegalArgumentException(
						"The attachments of " + RequestVariable.class.getSimpleName() + " is too big");
			}

			StringBuilder attachments = new StringBuilder();
			attachments.append(entry.getKey());
			attachments.append("=");
			String encode = URLEncoder.encode(entry.getValue(), "UTF-8");
			attachments.append(encode);
			responseContext.getHeaders().add(ATTACHMENT_HEADER, attachments.toString());
		}

	}

}
