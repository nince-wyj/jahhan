package net.jahhan.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatDateTime {
	private static Logger logger = LoggerFactory.getLogger(FormatDateTime.class);
	public static final String DATE_YMD = "yyyy-MM-dd";
	public static final String DATE_YMDH = "yyyy-MM-dd HH";
	public static final String DATETIME_YMDHMS = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_YMDHMSS_S = "yyyy-MM-dd HH:mm:ss.S";
	public static final String DATETIME_14 = "yyyyMMddHHmmss";
	public static final String TIME_HMS = "HH:mm:ss";
	public static final String TIME_HM = "HH:mm";
	public static final String DATE_YMD_STR = "yyyyMMdd";
	public static final String DATETIME_YMDHMS_STR = "yyyyMMddHHmmss";

	private FormatDateTime() {
	}

	/**
	 * 
	 * @param dateStr
	 *            传入的日期字符
	 * @param src_date_format
	 *            传入的日期格式
	 * @param des_date_format
	 *            传出的日期格式
	 * @return
	 */
	public static String formatDateString(String dateStr,
			String src_date_format, String des_date_format) {
		Date date = string2Date(dateStr, src_date_format);
		String returnValue = date2String(date, des_date_format);
		return returnValue;
	}

	/**
	 * 
	 * @description 获取当前日期
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDate() {
		return SimpleDateFormat.getDateInstance().format(new Date());
	}

	/**
	 * 
	 * @description 获取当前时间
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrTime() {
		return SimpleDateFormat.getTimeInstance().format(new Date());
	}

	/**
	 * 
	 * @description 获取当前日期时间
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDateTime() {
		return SimpleDateFormat.getDateTimeInstance().format(new Date());
	}

	/**
	 * 
	 * @description 获取当前日期时间,精确到毫秒级
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDateTime_YMDHMS_S() {
		return new SimpleDateFormat(DATETIME_YMDHMSS_S).format(new Date());
	}

	/**
	 * 
	 * @description 获取当前日期时间,精确到妙
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDateTime_YMDHMS() {
		return new SimpleDateFormat(DATETIME_YMDHMS).format(new Date());
	}

	/**
	 * 
	 * @description 获取当前时间,精确到妙
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDateTime_HMS() {
		return new SimpleDateFormat(TIME_HMS).format(new Date());
	}

	/**
	 * 
	 * @description 根据制定的format格式,获取当前日期时间
	 * @author JianWeiChen
	 * @return
	 */
	public static String getCurrDateTime(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	/**
	 * 
	 * @description 字符型日期转换成日期型
	 * @author JianWeiChen
	 * @param ref_date
	 * @return
	 */
	public static Date string2Date(String ref_date) {
		try {
			return new SimpleDateFormat(DATE_YMD).parse(ref_date);
		} catch (ParseException pe) {
			logger.error("error : ", pe);
			return null;
		}
	}

	/**
	 * 
	 * @description 字符型日期时间转换成日期型
	 * @author JianWeiChen
	 * @param ref_dt
	 * @return
	 */
	public static Date string2DateTime(String ref_dt) {
		try {
			return new SimpleDateFormat(DATETIME_YMDHMS).parse(ref_dt);
		} catch (ParseException pe) {
			logger.error("error : ", pe);
			return null;
		}
	}

	public static Date string2DateTime(String ref_dt, String ref_format) {
		try {
			return new SimpleDateFormat(ref_format).parse(ref_dt);
		} catch (ParseException pe) {
			logger.error("error : ", pe);
			return null;
		}
	}

	/**
	 * @description 字符型日期时间转换成日期型
	 * 
	 * @author JianWeiChen
	 * @param ref_date
	 * @param ref_format
	 * @return
	 */
	public static Date string2Date(String ref_date, String ref_format) {
		try {
			return new SimpleDateFormat(ref_format).parse(ref_date);
		} catch (Exception e) {
			logger.error("error : ", e);
			return null;
		}
	}

	/**
	 * 
	 * @description 日期型日期转换成字符型
	 * @author JianWeiChen
	 * @param ref_date
	 * @return
	 */
	public static String date2String(Date ref_date) {
		return new SimpleDateFormat(DATE_YMD).format(ref_date);
	}

	/**
	 * 
	 * @description 日期型日期转换成指定格式字符型
	 * @author JianWeiChen
	 * @param ref_date
	 * @param ref_format
	 * @return
	 */
	public static String date2String(Date ref_date, String ref_format) {
		try {
			return new SimpleDateFormat(ref_format).format(ref_date);
		} catch (Exception e) {
			logger.error("error : ", e);
			return date2String(ref_date);
		}
	}

	/**
	 * 
	 * @description 日期型日期时间转换成字符型
	 * @author JianWeiChen
	 * @param ref_date
	 * @return
	 */
	public static String dateTime2String(Date ref_date) {
		try {
			return new SimpleDateFormat(DATETIME_YMDHMS).format(ref_date);
		} catch (Exception e) {
			logger.error("error : ", e);
			return new SimpleDateFormat(DATETIME_YMDHMS).format(new Date());
		}
	}

	/**
	 * 
	 * @description 日期型日期时间转换成指定格式字符型
	 * @author JianWeiChen
	 * @param ref_date
	 * @param ref_format
	 * @return
	 */
	public static String dateTime2String(Date ref_date, String ref_format) {
		try {
			return new SimpleDateFormat(ref_format).format(ref_date);
		} catch (Exception e) {
			logger.error("error : ", e);
			return dateTime2String(ref_date);
		}
	}

	/**
	 * 日期相加
	 * 
	 * @param date
	 *            日期
	 * @param day
	 *            天数
	 * @return 返回相加后的日期
	 */
	public static java.util.Date addDate(java.util.Date date, double hour) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(getMillis(date) + (long)( hour * 3600 * 1000));
		return c.getTime();
	}

	/**
	 * 返回毫秒
	 * 
	 * @param date
	 *            日期
	 * @return 返回毫秒
	 */
	public static long getMillis(java.util.Date date) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(date);
		return c.getTimeInMillis();
	}

	public static String addDate(String date, int hour) {
		Date dateS = string2Date(date, "yyyyMMddHHmmss");
		dateS = addDate(dateS, hour);
		return dateTime2String(dateS, "yyyyMMddHHmmss");
	}

	public static String getToday(String pattern) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	/**
	 * 获取月份的天数
	 * 
	 * @param source
	 * @return
	 */
	public static int getMonthDay(String source) {

		int count = 30;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		try {
			Date date = format.parse(source);
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			count = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			logger.error("error : ", e);
		}
		return count;
	}

	public static String getFormatDate(String datestr) {
		// datestr 201504
		String formatDate = datestr.substring(0, 4) + "-"
				+ datestr.substring(4, datestr.length());

		return formatDate;
	}
	
	
	/**
	 * String转String
	 * @param date
	 * @return
	 */
	public static String string2String(String date){
		
		try {
			return new SimpleDateFormat(TIME_HM).format(new SimpleDateFormat(DATETIME_YMDHMS).parse(date));
		} catch (ParseException e) {
			logger.error("error : ", e);
			return null;
		} 
	}
	
	public static String settleDate(String date, double hour) {
		Date dateS = string2Date(date, "yyyy-MM-dd HH:mm:ss");
		dateS = addDate(dateS, hour);
		return dateTime2String(dateS, "yyyy-MM-dd HH:mm:ss");
	}
}
