package net.jahhan.utils.apache.convert;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.Converter;

public class DateConverter implements Converter {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Class<T> type, Object value) {
		if (value.getClass().equals(String.class)) {
			String dateString = (String) value;
			try {
				return (T) sdf2.parse(dateString);
			} catch (Exception e) {
			}
			try {
				return (T) sdf.parse(dateString);
			} catch (Exception e) {
			}
		}
		try {
			Long dateString = (Long) value;
			return (T) new Date(dateString);
		} catch (Exception e) {
			try {
				Integer dateString = (Integer) value;
				return (T) new Date(Long.valueOf(dateString));
			} catch (Exception e1) {
			}
		}
		return null;
	}

}
