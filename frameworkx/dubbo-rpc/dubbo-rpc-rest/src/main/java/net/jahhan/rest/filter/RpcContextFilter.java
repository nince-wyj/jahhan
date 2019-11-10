/**
 * Copyright 1999-2014 dangdang.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.alibaba.dubbo.rpc.RpcContext;

import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.ThreadVariableContext;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.variable.BaseGlobalVariable;
import net.jahhan.variable.BaseThreadVariable;

/**
 * easyrest filter
 * 
 * @author lishen
 */
@Priority(Integer.MIN_VALUE + 1)
public class RpcContextFilter implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter {

	private static final String DUBBO_ATTACHMENT_HEADER = "Attachment";

	// currently we use a single header to hold the attachments so that the
	// total attachment size limit is about 8k
	private static final int MAX_HEADER_SIZE = 8 * 1024;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		BaseContext applicationContext = BaseContext.CTX;
		ThreadVariableContext variableContext = new ThreadVariableContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		BaseThreadVariable base = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		
		HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
		RpcContext.getContext().setRequest(request);

		// this only works for servlet containers
		if (request != null && RpcContext.getContext().getRemoteAddress() == null) {
			RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
		}

		RpcContext.getContext().setResponse(ResteasyProviderFactory.getContextData(HttpServletResponse.class));

		String headers = requestContext.getHeaderString(DUBBO_ATTACHMENT_HEADER);
		if (headers != null) {
			for (String header : headers.split(",")) {
				int index = header.indexOf("=");
				if (index > 0) {
					String key = header.substring(0, index);
					String value = header.substring(index + 1);
					if (!StringUtils.isEmpty(key)) {
						String string = URLDecoder.decode(value.trim(), "UTF-8");
						RpcContext.getContext().setAttachment(key.trim(), string);
					}
				}
			}
		}
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
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
		((BaseGlobalVariable) BaseContext.CTX.getVariable("base")).setChain(chainId, Thread.currentThread());
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		int size = 0;
		for (Map.Entry<String, String> entry : RpcContext.getContext().getAttachments().entrySet()) {
			if (entry.getValue().contains(",") || entry.getValue().contains("=") || entry.getKey().contains(",")
					|| entry.getKey().contains("=")) {
				throw new IllegalArgumentException("The attachments of " + RpcContext.class.getSimpleName()
						+ " must not contain ',' or '=' when using rest protocol");
			}

			// TODO for now we don't consider the differences of encoding and
			// server limit
			size += entry.getValue().getBytes("UTF-8").length;
			if (size > MAX_HEADER_SIZE) {
				throw new IllegalArgumentException(
						"The attachments of " + RpcContext.class.getSimpleName() + " is too big");
			}

			StringBuilder attachments = new StringBuilder();
			attachments.append(entry.getKey());
			attachments.append("=");
			String encode = URLEncoder.encode(entry.getValue(), "UTF-8");
			attachments.append(encode);
			requestContext.getHeaders().add(DUBBO_ATTACHMENT_HEADER, attachments.toString());
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		if(BaseConfiguration.IS_DEBUG){
			headers.add("Access-Control-Allow-Origin", "*");  
			headers.add("Access-Control-Allow-Headers","x-requested-with, ssi-token");  
			headers.add("Access-Control-Max-Age", "3600");  
			headers.add("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");  
		}
		int size = 0;
		for (Map.Entry<String, String> entry : RpcContext.getContext().getAttachments().entrySet()) {
			if (entry.getValue().contains(",") || entry.getValue().contains("=") || entry.getKey().contains(",")
					|| entry.getKey().contains("=")) {
				throw new IllegalArgumentException("The attachments of " + RpcContext.class.getSimpleName()
						+ " must not contain ',' or '=' when using rest protocol");
			}

			// TODO for now we don't consider the differences of encoding and
			// server limit
			size += entry.getValue().getBytes("UTF-8").length;
			if (size > MAX_HEADER_SIZE) {
				throw new IllegalArgumentException(
						"The attachments of " + RpcContext.class.getSimpleName() + " is too big");
			}

			StringBuilder attachments = new StringBuilder();
			attachments.append(entry.getKey());
			attachments.append("=");
			String encode = URLEncoder.encode(entry.getValue(), "UTF-8");
			attachments.append(encode);
			responseContext.getHeaders().add(DUBBO_ATTACHMENT_HEADER, attachments.toString());
		}

	}

}
