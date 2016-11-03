package net.jahhan.web.servlet.filter;

import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;

import com.google.inject.servlet.GuiceFilter;

@WebFilter(urlPatterns = { "/*" })
public class ApplicationContextFilter extends GuiceFilter implements Filter {

}