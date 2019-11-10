/**
 * Copyright 1999-2014 dangdang.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jahhan.rest.filter;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.alibaba.dubbo.rpc.protocol.rest.RestConstraintViolation;
import com.alibaba.dubbo.rpc.protocol.rest.ViolationReport;

import net.jahhan.common.extension.constant.ContentType;
import net.jahhan.common.extension.exception.HttpException;
import net.jahhan.common.extension.exception.JahhanException;

/**
 * @author lishen
 */
public class RpcExceptionMapper implements ExceptionMapper<JahhanException> {

	public Response toResponse(JahhanException e) {
//        // TODO do more sophisticated exception handling and output
//        if (e.getCause() instanceof ConstraintViolationException) {
//            return handleConstraintViolationException((ConstraintViolationException) e.getCause());
//        }
//        // we may want to avoid exposing the dubbo exception details to certain clients
//        // TODO for now just do plain text output
		int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
		if (e instanceof HttpException) {
			status = ((HttpException) e).getHttpStatus();
		}
		return Response.status(status).entity(e.getExceptionMessage()).type(ContentType.APPLICATION_JSON_UTF_8).build();
	}

	protected Response handleConstraintViolationException(ConstraintViolationException cve) {
		ViolationReport report = new ViolationReport();
		for (ConstraintViolation cv : cve.getConstraintViolations()) {
			report.addConstraintViolation(new RestConstraintViolation(cv.getPropertyPath().toString(), cv.getMessage(),
					cv.getInvalidValue() == null ? "null" : cv.getInvalidValue().toString()));
		}
		// TODO for now just do xml output
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(report).type(ContentType.TEXT_XML_UTF_8)
				.build();
	}
}
