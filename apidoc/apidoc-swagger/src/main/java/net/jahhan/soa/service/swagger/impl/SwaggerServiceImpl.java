package net.jahhan.soa.service.swagger.impl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.jaxrs.listing.BaseApiListingResource;
import net.jahhan.common.extension.annotation.Controller;
import net.jahhan.soa.service.swagger.SwaggerService;

@Controller
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
