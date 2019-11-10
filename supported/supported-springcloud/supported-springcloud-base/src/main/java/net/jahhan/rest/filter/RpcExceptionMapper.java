package net.jahhan.rest.filter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.common.extension.exception.HttpException;
import net.jahhan.common.extension.exception.JahhanException;

public class RpcExceptionMapper implements ExceptionMapper<JahhanException> {

    public Response toResponse(JahhanException e) {
    	int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
		if (e instanceof HttpException) {
			status = ((HttpException) e).getHttpStatus();
		}
	
        return Response.status(status).entity(e.getExceptionMessage()).type(ContentType.APPLICATION_JSON_UTF_8).build();
    }
}
