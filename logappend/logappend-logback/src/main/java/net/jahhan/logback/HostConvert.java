package net.jahhan.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class HostConvert extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		try {
			return NetUtils.getLocalHost();
		} catch (Exception e) {
			return "";
		}

	}
}
