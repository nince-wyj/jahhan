package net.jahhan.filter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(filterName = "webStatFilter", urlPatterns = { "/druid/*" }, initParams = {
		@WebInitParam(name = "exclusions", value = "/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*") })
public class WebStatFilter extends com.alibaba.druid.support.http.WebStatFilter {

}
