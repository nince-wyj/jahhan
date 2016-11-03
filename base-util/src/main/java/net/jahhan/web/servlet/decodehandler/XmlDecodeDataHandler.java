package net.jahhan.web.servlet.decodehandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Injector;

import net.jahhan.api.RequestMessage;
import net.jahhan.api.ResponseMessage;
import net.jahhan.cache.AsyncActionCache;
import net.jahhan.cache.DebugActClassCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.handler.WorkHandler;
import net.jahhan.thread.AsyncThread;
import net.jahhan.thread.ResponseConsumeThread;
import net.jahhan.web.action.ServiceRegisterHelper;
import net.jahhan.web.servlet.listener.AppAsyncListener;

public class XmlDecodeDataHandler implements DecodeHandler {
	private final static Logger logger = LoggerFactory.getLogger("XmlDecodeDataHandler");

	private String serviceName;
	private int appType;
	private String verNo;
	@Inject
	private Injector injector;
	@Inject
	private ThreadPoolFactory threadPoolFactory;
	@Inject
	private ServiceRegisterHelper serviceRegisterHelper;

	public XmlDecodeDataHandler() {
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public void setVerNo(String verNo) {
		this.verNo = verNo;
	}

	public void setConsumeTime(long startTime, String actName) {
		long now = System.currentTimeMillis();
		long ms = now - startTime;
		ResponseConsumeThread t = injector.getInstance(ResponseConsumeThread.class);
		t.setActName(actName);
		t.setConsumeTime(ms);
		ExecutorService executeService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
		executeService.execute(t);
	}

	@Override
	public void execute() {
		long startTime = System.currentTimeMillis();
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		Map<String, String> parameterMapChange = new HashMap<>();
		HttpServletRequest request = invocationContext.getRequest();
		try {
			ServletInputStream inputStream = request.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			logger.debug("recive xml=" + sb.toString());
			ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(in);
			Node root = document.getFirstChild();
			NodeList list = root.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				parameterMapChange.put(node.getNodeName(), node.getTextContent());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		String requestJson = JSON.toJSONString(parameterMapChange);
		try {
			requestJson = java.net.URLDecoder.decode(requestJson, "utf-8");
		} catch (UnsupportedEncodingException e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, "报文格式不正确");
		}
		if (StringUtils.isEmpty(requestJson)) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, "报文为空");
		}
		try {
			JSONObject json = JSONObject.parseObject(requestJson);
			RequestMessage requestMessage = new RequestMessage();
			requestMessage.setServiceName(serviceName);
			requestMessage.setAppType(appType);
			requestMessage.setVerNo(verNo);
			Map<String, Object> reqMap = requestMessage.getRequestMap();
			Entry<String, Object> tempEntry = null;
			for (Iterator<Entry<String, Object>> it = json.entrySet().iterator(); it.hasNext();) {
				tempEntry = it.next();
				Object value = tempEntry.getValue();
				if (value != null) {
					value = String.valueOf(value);
				}
				reqMap.put(tempEntry.getKey(), value);
			}
			requestMessage.setContent(json);
			requestMessage.setLocalAddr(request.getLocalAddr());

			invocationContext.setRequestMessage(requestMessage);
			invocationContext.setResponseMessage(new ResponseMessage());

			WorkHandler service = serviceRegisterHelper.getService(RequestMethodEnum.XML,
					requestMessage.getServiceName());
			if (service == null) {
				if (SysConfiguration.getIsDebug()) {
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					String className = DebugActClassCache.getInstance().getClassName(requestMessage.getServiceName());
					serviceRegisterHelper.registerService(classLoader, injector, className);
					service = serviceRegisterHelper.getService(RequestMethodEnum.XML, requestMessage.getServiceName());
				}
				if (service == null) {
					FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口不存在");
				}
			}
			if (AsyncActionCache.getInstance().contains(requestMessage.getServiceName())) {
				request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
				AsyncContext asyncCtx = request.startAsync();
				asyncCtx.addListener(new AppAsyncListener());
				asyncCtx.setTimeout(SysConfiguration.getAsyncTimeOut());

				ExecutorService executeService = (ExecutorService) request.getServletContext()
						.getAttribute("executor");
				AsyncThread asyncThread = injector.getInstance(AsyncThread.class);
				asyncThread.setAsyncCtx(asyncCtx);
				asyncThread.setInvocationContext(invocationContext);
				asyncThread.setActName(requestMessage.getServiceName());
				asyncThread.setService(service);
				executeService.execute(asyncThread);
			} else {
				logger.debug(requestMessage.getServiceName() + " act!!");
				service.execute();
				if (invocationContext.getResponseMessage().getErrorCode() != SystemErrorCode.SUCCESS) {
					logger.error("XML方法：" + serviceName + "失败返回，错误信息："
							+ invocationContext.getResponseMessage().getMessageInfo());
				}
				if (SysConfiguration.getRecordTimeConsume())
					setConsumeTime(startTime, requestMessage.getServiceName());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, e.getMessage());
		}
	}

	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}
}
