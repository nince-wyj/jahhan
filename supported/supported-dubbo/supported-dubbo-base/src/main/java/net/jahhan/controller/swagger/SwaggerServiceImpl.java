package net.jahhan.controller.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.alibaba.dubbo.config.annotation.Service;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.jaxrs.listing.BaseApiListingResource;
import net.jahhan.intf.swagger.SwaggerService;

@Service
public class SwaggerServiceImpl extends BaseApiListingResource implements SwaggerService {
	@Context
	ServletContext context;

	@Override
	public Response getListingJson(Application app, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo) {

		Response rsp = null;
		try {
			rsp = getListingJsonResponse(app, context, sc, headers, uriInfo);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return rsp;
	}
}
