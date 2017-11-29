package net.jahhan.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ChainIdConvert extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		try {
			JahhanLoggingEvent frameWorkXLoggingEvent = (JahhanLoggingEvent) event;
			return frameWorkXLoggingEvent.getChainId();
		} catch (Exception e) {
			return "";
		}
	}
}
