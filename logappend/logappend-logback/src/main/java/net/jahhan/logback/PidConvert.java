package net.jahhan.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class PidConvert extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		try {
			JahhanLoggingEvent frameWorkXLoggingEvent = (JahhanLoggingEvent) event;
			return String.valueOf(frameWorkXLoggingEvent.getPid());
		} catch (Exception e) {
			return "";
		}
	}
}
