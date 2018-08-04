package net.jahhan.common.extension.utils.apache.convert;

import org.apache.commons.beanutils.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		} else if (value instanceof Date && type.equals(Date.class)) {
			return (T) new Date(((Date) value).getTime());

		} else if (value instanceof Date && type.equals(java.sql.Date.class)) {
			return (T) new java.sql.Date(((Date) value).getTime());
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
