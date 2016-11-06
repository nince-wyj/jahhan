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
import net.jahhan.web.servlet.decodehandler.SICDecodeDataHandler;

/**
 * @author nince
 */
@Singleton
@WebServlet(name = "serviceInContentServlet", urlPatterns = { "/sic" })
public class ServiceIdInContentServlet extends HttpServlet {

	private static final long serialVersionUID = 7437808236758903291L;

	private final static Logger logger = LoggerFactory.getLogger("serviceInContentServlet.servlet");

	@Inject
	private SICDecodeDataHandler decodeDataHandler;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		resp.setContentType("application/json;charset=utf-8");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.addHeader("P3P", "CP=CAO PSA OUR");
		if(SysConfiguration.getAllowAllOrigin()){
			resp.addHeader("Access-Control-Allow-Origin", "*");
			resp.addHeader("P3P","CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
//			resp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		}else{
			resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		}
		

		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = new InvocationContext(req, resp);
		try {
			req.setCharacterEncoding("utf-8");
			applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
			decodeDataHandler.execute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} catch (Error e) {
			logger.error(e.getMessage(), e);
		}
	}
}
