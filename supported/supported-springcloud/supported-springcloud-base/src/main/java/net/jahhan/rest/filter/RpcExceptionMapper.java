package net.jahhan.rest.filter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.exception.JahhanException;

public class RpcExceptionMapper implements ExceptionMapper<JahhanException> {

    public Response toResponse(JahhanException e) {
        
        return Response.status(e.getExceptionMessage().getHttpStatus()).entity(e.getExceptionMessage()).type(ContentType.APPLICATION_JSON_UTF_8).build();
    }
}
