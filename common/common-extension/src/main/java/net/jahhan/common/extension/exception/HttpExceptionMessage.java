package net.jahhan.common.extension.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HttpExceptionMessage extends ExceptionMessage {
	private static final long serialVersionUID = -4302161171854854646L;
	private int httpStatus;
}
