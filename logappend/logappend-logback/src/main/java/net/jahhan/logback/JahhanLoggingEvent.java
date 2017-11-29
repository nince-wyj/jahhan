package net.jahhan.logback;

import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import lombok.Getter;
import lombok.Setter;

public class JahhanLoggingEvent extends LoggingEvent {
	@Getter
	@Setter
	private String threadId;
	@Getter
	@Setter
	private String requestId;
	@Getter
	@Setter
	private String chainId;
	@Getter
	@Setter
	private int port;
	@Getter
	@Setter
	private int pid;
	private ILoggingEvent loggingEvent;

	public JahhanLoggingEvent(ILoggingEvent loggingEvent) {
		this.loggingEvent = loggingEvent;
	}

	@Override
	public String getThreadName() {
		return loggingEvent.getThreadName();
	}

	@Override
	public Level getLevel() {
		return loggingEvent.getLevel();
	}

	@Override
	public String getMessage() {
		return loggingEvent.getMessage();
	}

	@Override
	public Object[] getArgumentArray() {
		return loggingEvent.getArgumentArray();
	}

	@Override
	public String getFormattedMessage() {
		return loggingEvent.getFormattedMessage();
	}

	@Override
	public String getLoggerName() {
		return loggingEvent.getLoggerName();
	}

	@Override
	public LoggerContextVO getLoggerContextVO() {
		return loggingEvent.getLoggerContextVO();
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		return loggingEvent.getThrowableProxy();
	}

	@Override
	public StackTraceElement[] getCallerData() {
		return loggingEvent.getCallerData();
	}

	@Override
	public boolean hasCallerData() {
		return loggingEvent.hasCallerData();
	}

	@Override
	public Marker getMarker() {
		return loggingEvent.getMarker();
	}

	@Override
	public Map<String, String> getMDCPropertyMap() {
		return loggingEvent.getMDCPropertyMap();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String, String> getMdc() {
		return loggingEvent.getMdc();
	}

	@Override
	public long getTimeStamp() {
		return loggingEvent.getTimeStamp();
	}

	@Override
	public void prepareForDeferredProcessing() {
		loggingEvent.prepareForDeferredProcessing();
	}
}
