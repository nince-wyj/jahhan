package net.jahhan.servlet;

import javax.inject.Singleton;
import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(name = "statViewServlet", urlPatterns = { "/druid/*" })
@Singleton
public class StatViewServlet extends com.alibaba.druid.support.http.StatViewServlet {

}
