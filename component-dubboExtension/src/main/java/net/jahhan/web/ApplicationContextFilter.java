package net.jahhan.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;

@WebFilter(urlPatterns = { "/*" })
public class ApplicationContextFilter extends GuiceFilter implements Filter {
	private final static Logger logger = LoggerFactory.getLogger("ApplicationContextFilter");

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		try {
			super.doFilter(request, response, filterChain);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} catch (Error e) {
			logger.error(e.getMessage(), e);
		}
	}
}