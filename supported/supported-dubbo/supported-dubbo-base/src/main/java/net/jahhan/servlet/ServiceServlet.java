package net.jahhan.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.dubbo.remoting.http.HttpHandler;

import net.jahhan.remoting.http.AsyncThread;

@WebServlet(name = "dubboServlet", urlPatterns = { "/service/*" }, asyncSupported = true)
public class ServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 5766349180380479888L;
	private static ServiceServlet INSTANCE;

	private static final Map<Integer, HttpHandler> handlers = new ConcurrentHashMap<Integer, HttpHandler>();

	public static void addHttpHandler(int port, HttpHandler processor) {
		handlers.put(port, processor);
	}

	public static void removeHttpHandler(int port) {
		handlers.remove(port);
	}

	public static ServiceServlet getInstance() {
		return INSTANCE;
	}

	public ServiceServlet() {
		ServiceServlet.INSTANCE = this;
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpHandler handler = handlers.get(request.getLocalPort());

		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		AsyncContext asyncCtx = request.startAsync();
		asyncCtx.setTimeout(-1);
		AsyncThread asyncThread = new AsyncThread();
		asyncThread.setRequest(request);
		asyncThread.setResponse(response);
		asyncThread.setHandler(handler);
		asyncThread.setAsyncCtx(asyncCtx);
		ExecutorService executor = (ExecutorService) request.getServletContext().getAttribute("executor");
		executor.execute(asyncThread);
	}

}