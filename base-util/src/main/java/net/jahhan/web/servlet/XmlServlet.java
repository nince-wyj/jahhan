package net.jahhan.web.servlet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.web.servlet.decodehandler.XmlDecodeDataHandler;

/**
 * 支持微信支付回调的servlet
 * 
 * @author nince
 *
 */
@Singleton
@WebServlet(name = "xmlServlet", urlPatterns = { "/xml/*" })
public class XmlServlet extends HttpServlet {

	private static final long serialVersionUID = 7437808236758903291L;

	private final static Logger logger = LoggerFactory.getLogger("XmlServlet.servlet");

	@Inject
	private XmlDecodeDataHandler decodeDataHandler;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType("application/json;charset=utf-8");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.addHeader("P3P", "CP=CAO PSA OUR");
		if(SysConfiguration.getAllowAllOrigin()){
			resp.addHeader("Access-Control-Allow-Origin", "*");
		}else{
			resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		}
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = new InvocationContext(req, resp);
		String uri = req.getRequestURI();
		String[] requestUrl = uri.split("/");
		String verNo = "1.0";
		if (requestUrl.length > 4) {
			verNo = requestUrl[4];
		}
		try {
			req.setCharacterEncoding("utf-8");
			applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
			decodeDataHandler.setAppType(0);
			decodeDataHandler.setServiceName(requestUrl[3]);
			decodeDataHandler.setVerNo(verNo);
			decodeDataHandler.execute();
		} catch (Exception e) {
			logger.error("act:" + requestUrl[3] + "," + e.getMessage(), e);
		} catch (Error e) {
			logger.error("class not find->service_id" + "," + e.getMessage(), e);
		}
	}

}
