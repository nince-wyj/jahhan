package net.jahhan.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class RequestIdConvert extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		try {
			JahhanLoggingEvent frameWorkXLoggingEvent = (JahhanLoggingEvent) event;
			return frameWorkXLoggingEvent.getRequestId();
		} catch (Exception e) {
			return "";
		}
	}
}
