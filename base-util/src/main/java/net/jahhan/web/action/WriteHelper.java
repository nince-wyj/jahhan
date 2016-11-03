package net.jahhan.web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.inject.Inject;

import net.jahhan.api.ResponseFile;
import net.jahhan.api.ResponseMessage;
import net.jahhan.cache.ThirdEncryptKeyCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.ContentTypeEnum;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.BussinessException;
import net.jahhan.factory.crypto.ICrypto;
import net.jahhan.utils.Assert;
import net.jahhan.version.CustomVersion;
import net.jahhan.web.ws.WSSessionManager;

/**
 * 写操作帮助类
 */
@Singleton
public class WriteHelper {
	protected static Logger logger = LoggerFactory.getLogger("framework.Response");
	private static Map<?, ?> defaultContent = Collections.unmodifiableMap(new HashMap<>());

	@Inject
	private WSSessionManager wsSessionManager;

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void toWriteJson(ResponseMessage responseMessage, RequestMethodEnum requestMethodEnum,
			CryptEnum responseSecurityType, String[] returnParameters) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		HttpServletResponse response = invocationContext.getResponse();
		String verNo = invocationContext.getRequestMessage().getVerNo();
		boolean useSession = CustomVersion.useSession(invocationContext.getRequestMessage().getAppType(), verNo);
		if (requestMethodEnum == RequestMethodEnum.SIU && null != responseMessage.getLastModify()
				&& null != responseMessage.getCache()) {
			Date now = new Date();
			Long expires = now.getTime() + responseMessage.getCache() * 1000;
			response.addHeader("Last-Modified", responseMessage.getLastModify().toGMTString());
			response.addHeader("Cache-Control", "max-age=" + responseMessage.getCache());
			response.addHeader("Expires", new Date(expires).toGMTString());
			response.addHeader("Date", now.toGMTString());
			String modifySince = invocationContext.getRequestMessage().getModifySince();
			if (null != modifySince) {
				Date since = new Date(modifySince);
				if (!since.before(new Date(responseMessage.getLastModify().toGMTString()))) {
					response.setStatus(304);
				}
			}
		}
		try {
			String jsonStr = "";
			Object content = responseMessage.getResponseMap();
			if (responseMessage.getResponseMap() instanceof Map) {
				Map<String, Object> filterMap = new HashMap<>();
				Map<String, Object> srcMap = (Map<String, Object>) responseMessage.getResponseMap();
				if (returnParameters != null && returnParameters.length > 0) {
					for (String key : returnParameters) {
						if (srcMap.containsKey(key)) {
							filterMap.put(key, srcMap.get(key));
						}
					}
				}
				content = filterMap;
			}

			Map<String, Object> outputMap = new LinkedHashMap<>();
			outputMap.put("r_msg", responseMessage.getMessageInfo());
			outputMap.put("r_content", content != null ? content : defaultContent);
			outputMap.put("r_code", responseMessage.getErrorCode());
			if (responseMessage.getErrorCode() != 0) {
				outputMap.put("r_content", defaultContent);
			}
			jsonStr = JSON.toJSONString(outputMap, SerializerFeature.DisableCircularReferenceDetect);
			logger.debug("response string:" + jsonStr);
			ICrypto icrypto = applicationContext.getCrypto(responseSecurityType);
			String encryptKey;

			if (useSession && responseSecurityType == CryptEnum.LOGIN) {
				String thirdName = invocationContext.getRequestMessage().getThirdName();
				if (null == thirdName) {
					encryptKey = SysConfiguration.getEncryptkey();
				} else {
					encryptKey = ThirdEncryptKeyCache.getInstance().getEncryptKey(thirdName);
				}
				jsonStr = icrypto.encrypt(jsonStr, encryptKey);
				response.setContentType("text/plain; charset=utf-8");
				logger.info("jsonStr：" + jsonStr);
			} else if (useSession && responseSecurityType == CryptEnum.AES) {
				encryptKey = applicationContext.getUserEntity().getToken();
				jsonStr = icrypto.encrypt(jsonStr, encryptKey);
				response.setContentType("text/plain; charset=utf-8");
				logger.info("jsonStr：" + jsonStr);
			}
			if (requestMethodEnum == RequestMethodEnum.WS) {
				wsSessionManager.sendMessage(invocationContext.getWsRequest().getWsSessionId(),
						invocationContext.getUserEntity(), jsonStr);
			} else {
				PrintWriter writer = response.getWriter();
				writer.write(jsonStr);
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			logger.error("write json error", e);
		}
	}

	@SuppressWarnings("deprecation")
	public void toWriteCustom(ResponseMessage responseMessage, RequestMethodEnum requestMethodEnum,
			CryptEnum responseSecurityType) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		HttpServletResponse response = invocationContext.getResponse();
		String verNo = invocationContext.getRequestMessage().getVerNo();
		boolean useSession = CustomVersion.useSession(invocationContext.getRequestMessage().getAppType(), verNo);
		if (requestMethodEnum == RequestMethodEnum.SIU && null != responseMessage.getLastModify()
				&& null != responseMessage.getCache()) {
			Date now = new Date();
			Long expires = now.getTime() + responseMessage.getCache() * 1000;
			response.addHeader("Cache-Control", "max-age=" + responseMessage.getCache());
			response.addHeader("Last-Modified", responseMessage.getLastModify().toGMTString());
			response.addHeader("Expires", new Date(expires).toGMTString());
			response.addHeader("Date", now.toGMTString());
			String modifySince = invocationContext.getRequestMessage().getModifySince();
			if (null != modifySince) {
				Date since = new Date(modifySince);
				if (!since.before(new Date(responseMessage.getLastModify().toGMTString()))) {
					response.setStatus(304);
				}
			}
		}
		try {
			ICrypto icrypto = applicationContext.getCrypto(responseSecurityType);
			String messageInfo = responseMessage.getMessageInfo();
			if (useSession && responseSecurityType == CryptEnum.AES) {
				String encryptKey = applicationContext.getUserEntity().getToken();
				messageInfo = icrypto.encrypt(messageInfo, encryptKey);
			}
			if (requestMethodEnum == RequestMethodEnum.WS) {
				wsSessionManager.sendMessage(invocationContext.getWsRequest().getWsSessionId(),
						invocationContext.getUserEntity(),
						null == messageInfo ? SystemErrorCode.MESSAGE_FAILED : messageInfo);
			} else {
				response.setCharacterEncoding("UTF-8");
				PrintWriter writer = response.getWriter();
				writer.write(null == messageInfo ? SystemErrorCode.MESSAGE_FAILED : messageInfo);
				writer.flush();
				writer.close();
			}

		} catch (Exception e) {
			logger.error("write messageInfo error", e);
		}
	}

	public void toWriteFile(ResponseMessage responseMessage) {
		ResponseFile responseFile = responseMessage.getResponseFile();
		try {
			Assert.notNull(responseFile, SystemErrorCode.RESPONSE_ERROR);
			Assert.notNull(responseFile.getContentType(), SystemErrorCode.RESPONSE_ERROR);
			Assert.notNull(responseFile.getIn(), SystemErrorCode.RESPONSE_ERROR);
			ApplicationContext applicationContext = ApplicationContext.CTX;
			InvocationContext invocationContext = applicationContext.getInvocationContext();
			HttpServletResponse response = invocationContext.getResponse();
			OutputStream o = response.getOutputStream();
			response.reset();
			response.setContentType(responseFile.getContentType().getMimeType());
			if (responseFile.getContentType() != ContentTypeEnum.HTML) {
				Assert.notNull(responseFile.getFileLength(), SystemErrorCode.RESPONSE_ERROR);
				Assert.notNull(responseFile.getFileName(), SystemErrorCode.RESPONSE_ERROR);
				String length = String.valueOf(responseFile.getFileLength());
				response.setHeader("Content_Length", length);
				response.setHeader("Content-Disposition", "attachment; filename=" + responseFile.getFileName());
			} else {
				if (isGzipSupport()) {
					try (GZIPOutputStream pw = new GZIPOutputStream(o); InputStream in = responseFile.getIn()) {
						response.setCharacterEncoding("UTF-8");
						response.setHeader("content-encoding", "gzip");
						byte buf[] = new byte[1024 * 5];
						int n;
						while ((n = in.read(buf)) != -1) {
							pw.write(buf, 0, n);
						}
						pw.flush();
					} catch (Exception e) {
						logger.error("io exception", e);
					}
					o.close();
					return;
				}
			}
			InputStream in = responseFile.getIn();
			byte b[] = new byte[1024];
			int n;
			while ((n = in.read(b)) != -1) {
				o.write(b, 0, n);
			}
			in.close();
			o.close();
		} catch (BussinessException e) {
			logger.error("下载设置错误：", e);
		} catch (IOException e) {
			logger.error("下载中断", e);
		}
	}

	private boolean isGzipSupport() {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		HttpServletRequest req = invocationContext.getRequest();
		String headEncoding = req.getHeader("accept-encoding");
		if (headEncoding == null || (headEncoding.indexOf("gzip") == -1)) { // 客户端不支持gzip
			return false;
		} else { // 支持 gzip 压缩
			return true;
		}
	}

	public void redirect(ResponseMessage responseMessage) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		HttpServletResponse response = invocationContext.getResponse();
		try {
			response.sendRedirect(responseMessage.getRedirect());
		} catch (IOException e) {
			logger.error("send redirect error", e);
		}
	}
}
