package net.jahhan.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ch.qos.logback.core.boolex.Matcher;
/**
 *  Date帮助类
 *  @author Jelly
 * */
public final class DateUtils {
	
	/**
	 * 一天毫秒数
	 * */
	public static Long millisecondsForOneDay = 1000L * 60L * 60L * 24L;
	
	/**
	 *  判断两个日期是否在同一天
	 *  @param  date1 需要对比的时间
	 *  @param  date2 需要对比的时间
	 *  @author Jelly
	 *  @return  true: 相同, false: 不同
	 * */
	public static boolean IsSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2 .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear  && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2 .get(Calendar.DAY_OF_MONTH);
        return isSameDate;
    }
	
	/**
	 *  判断两个日期是否在同一月
	 *  @param  date1 需要对比的时间
	 *  @param  date2 需要对比的时间
	 *  @author Jelly
	 *  @return  true: 相同, false: 不同
	 * */
	public static boolean IsSameMonth(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2 .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear  && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        return isSameMonth;
    }
	
	/**
	 * 计算两个时间的间隔天数
	 * @since
	 * date1:  2015-12- 1 23: 00: 00
	 * date2:  2015-12- 2 00: 00: 00
	 * 结果算出来时 0
	 * @author Jelly
	 * @param  date1  :  计算天数1
	 * @param  date2 ： 计算天数2
	 * @param  isRoundingUp :  是否向上取整, 假设 得出的天数是3.1 如果 true： 4 如果 false ： 3
	 * @return  相差的天数，肯定是整数
	 * */
	public  static  long DateDiffForDay(Date date1, Date date2, boolean isRoundingUp) {
		 Long  differenceMillisecnds =  (date1.getTime() - date2.getTime());
		 double  differenceDay=  Math.abs(differenceMillisecnds) * 1.0  / millisecondsForOneDay;
		 long result =  (long)Math.floor(differenceDay);
		 return result;
	}
	
	
	/**
	 *  判断两个日期是否在同一个周
	 *  @param  date1 需要对比的时间
	 *  @param  date2 需要对比的时间
	 *  @author Jelly
	 *  @return  true: 相同, false: 不同
	 * */
	public static boolean IsSameWeek(Date date1, Date date2) {
	    boolean isEqual = GetWeekForYear(date1)==GetWeekForYear(date2);
	    return isEqual;
    }
	
	
	/**
     * 给出指定时间是今年的第几周
     * @param dt
     * @return 2015年11月13日， 返回 46
     * @author Jelly
     */
    public static int GetWeekForYear(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int w =  cal.get(Calendar.WEEK_OF_YEAR);
        return w;
    }
	
	/**
     * 给出当前时间是周数
     * @param dt
     * @return 周一 - 周六： 1- 6， 周日  0
     * @author Jelly
     */
    public static int GetWeekForDate() {
		Date timeDate=new  Date();
		int w =GetWeekForDate(timeDate);
		return w;
	}
    
	/**
     * 给出指定时间是周几
     * @param dt
     * @return 周一 - 周六： 1- 6， 周日  0
     * @author Jelly
     */
    public static int GetWeekForDate(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int w =  cal.get(Calendar.DAY_OF_WEEK);
        return w;
    }
    
	/**
     * 给出当前时间是周几（符合中国需求，周日是最后1天）
     * @param dt
     * @return 周一 - 周六： 1- 6， 周日 原先是 0，现在变成7
     * @author Jelly
     */
    public static int GetChinaWeekForDate() {
    	Date timeDate=new  Date();
        int w =  GetChinaWeekForDate(timeDate);
        return w;
    }
    
	/**
     * 给出指定时间是周几（符合中国需求，周日是最后1天）
     * @param dt
     * @return 周一 - 周六： 1- 6， 周日 原先是 0，现在变成7
     * @author Jelly
     */
    public static int GetChinaWeekForDate(Date time) {
        int w =  GetWeekForDate(time);
        if(w==0) {
        	w=7;
        }
        return w;
    }
    
    /**
     *  给出指定时间在该周内的最一天的最一秒
	 *  @param time 需要处理的时间
	 *  @author Jelly
     */
    public static Date GetFirstSecondForWeek(Date time){
    		int  weekNumber =  GetChinaWeekForDate(time);
    		 Calendar calendar = Calendar.getInstance();
    		 calendar.setTime(time);
    		 if( weekNumber > 1){
    			 calendar.add(Calendar.DAY_OF_MONTH, -(weekNumber-1));
    		 }
    		 return  GetFirstSecondForDay(calendar.getTime());
    }
    
    /**
     *  给出指定时间在该周内的最后一天的最后一秒
	 *  @param time 需要处理的时间
	 *  @author Jelly
     */
    public static Date GetLastSecondForWeek(Date time){
		int  weekNumber =  GetChinaWeekForDate(time);
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(time);
		 if( weekNumber < 7){
			 calendar.add(Calendar.DAY_OF_MONTH, (7 - weekNumber));
		 }
		 return  GetLastSecondForDay(calendar.getTime());
    }
    

	/**
	 *  给出某月的第一天的第一毫秒
	 *  @param time 需要处理的时间
	 *  @return 返回指定日期的月份第一天一毫秒， 例如 2015-11-1 00：00：00 。
	 *  @author Jelly
	 * */
	public static Date GetFirstSecondForMonth(Date time) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(time);
	     calendar.set(Calendar.DAY_OF_MONTH, 1);
	     return GetFirstSecondForDay(calendar.getTime());
	}
    
	/**
	 *  给出某月的最后一天的最后一毫秒
	 *  @param time 需要处理的时间
	 *  @return 返回指定日期的月份最后一毫秒， 例如 2015-11-30 23：59：59 。
	 *  @author Jelly
	 * */
	public static Date GetLastSecondForMonth(Date time) {
		  Calendar calendar = Calendar.getInstance();
		  calendar.setTime(time);
		  //获取某月最大天数
	      int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	      //设置日历中月份的最大天数
	      calendar.set(Calendar.DAY_OF_MONTH, lastDay);
	      return GetLastSecondForDay(calendar.getTime());
	}
	
	/**
	 *  给出某一天的第一毫秒
	 *  例如 2015-11-10, 处理完后得到 2015-11-10 00：00：00 
	 *  @param time 需要处理的时间
	 *  @return 返回指定日期的最后一毫秒， 例如 2015-11-10 00：00：00 。
	 *  @author Jelly
	 * */
	public static Date GetFirstSecondForDay(Date time){
			Calendar calendar = Calendar.getInstance();   
			calendar.setTime(time);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
        	return  calendar.getTime(); 
	}
	
	/**
	 *  在指定时间上加上天数
	 *  @param time 需要处理的时间
	 *  @param value 需要增加的值，如果是减的话， 设置成负数
	 *  @author Jelly
	 * */
	public static Date AddDay(Date time, Integer value) {
		Calendar calendar = Calendar.getInstance();   
		calendar.setTime(time);
		calendar.add(Calendar.DAY_OF_MONTH, value);
		return  calendar.getTime(); 
	}
	
	/**
	 *  在指定时间上加上月份
	 *  @param time 需要处理的时间
	 *  @param value 需要增加的值，如果是减的话， 设置成负数
	 *  @author Jelly
	 * */
	public static Date AddMonth(Date time, Integer value) {
		Calendar calendar = Calendar.getInstance();   
		calendar.setTime(time);
		calendar.add(Calendar.MONTH, value);
		return  calendar.getTime(); 
	}
	
	/**
	 *  在指定时间上加上年份
	 *  @param time 需要处理的时间
	 *  @param value 需要增加的值，如果是减的话， 设置成负数
	 *  @author Jelly
	 * */
	public static Date AddYear(Date time, Integer value) {
		Calendar calendar = Calendar.getInstance();   
		calendar.setTime(time);
		calendar.add(Calendar.YEAR, value);
		return  calendar.getTime(); 
	}
	
	/**
	 *  给出某一天的最后一毫秒
	 *  例如 2015-11-10, 处理完后得到 2015-11-10 23：59：59 
	 *  @param time 需要处理的时间
	 *  @return 返回指定日期的最后一毫秒， 例如 2015-11-10 23：59：59 。
	 *  @author Jelly
	 * */
	public static Date GetLastSecondForDay(Date time){
			Calendar calendar = Calendar.getInstance();   
			calendar.setTime(time);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
        	return  calendar.getTime(); 
	}
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
}
