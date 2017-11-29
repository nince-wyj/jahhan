package net.jahhan.cache.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import net.jahhan.common.extension.utils.LogUtil;

/**
 * @author nince
 */
public class DateTimeUtils {

    private static final String weekDays[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

    public static long getTimesMorning() {
        return getTimesMorning(System.currentTimeMillis());
    }
    
    public static long getTimesMorning(long timeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String getFormatTime(Timestamp datetime) {
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String st = sdf.format(datetime);//
        return st.substring(st.length() - 8);
    }

    public static long getTimes(int hour, int second, int minute, int millsecond) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.MILLISECOND, millsecond);
        return cal.getTimeInMillis();
    }

    // 获得当天24点时间
    public static long getTimesNight() {
        return getTimesNight(System.currentTimeMillis());
    }
    
    // 获得当天24点时间
    public static long getTimesNight(long timeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    // 获得当月24点时间

    public static long getTimesMonthNight() {
        // 获取Calendar
        Calendar calendar = Calendar.getInstance();
        // 设置时间,当前时间不用设置
        // calendar.setTime(new Date());
        // 设置日期为本月最大日期
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // 打印

        return calendar.getTimeInMillis();
    }

    public static Timestamp str2Timestamp(String yyyymmddhhmmss) {
        Timestamp ts = Timestamp.valueOf(yyyymmddhhmmss);
        return ts;
    }

    public static String timestamp2Str(Timestamp datetime) {
        String st = "";
        if (datetime != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            st = sdf.format(datetime);//
        }
        if (st.contains(".")) {
            st = st.substring(0, st.lastIndexOf("."));
        }
        return st;
    }

    public static String time2Str(long time, String format) {
        Date datetime = new Date(time);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
        return sdf.format(datetime);
    }

    public static String timestampStr2Str(String timesStampSr) {
        if (timesStampSr != null && !timesStampSr.isEmpty()) {
            if (timesStampSr.contains(".")) {
                timesStampSr = timesStampSr.substring(0, timesStampSr.lastIndexOf("."));
            }
        }
        return timesStampSr;
    }

    public static Timestamp long2Timestamp(long time) {
        return new Timestamp(time);
    }

    public static long getTimeStampSeconds(Timestamp datetime) {
        long millionSeconds = datetime.getTime();
        return millionSeconds / 1000l;
    }

    /**
     * 获取指定时间对应的毫秒数
     * 
     * @param time
     *            "HH:mm:ss"
     * @return
     */
    public static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
        	LogUtil.error("解析日期出现问题", e);
        }
        return 0;
    }

    public static int getDateSeconds(String dateStr) {
        int dateSeconds = 0;
        try {
            DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dayFormat.parse(dateStr);
            dateSeconds = (int) (date.getTime() / 1000l);
        } catch (ParseException ex) {
        	LogUtil.error("解析日期出现问题", ex);
        }
        return dateSeconds;
    }

    public static long getDateMillionSeconds(String dateStr) {
        long dateSeconds = 0;
        try {
            DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dayFormat.parse(dateStr);
            dateSeconds = date.getTime();
        } catch (ParseException ex) {
        	LogUtil.error("解析日期出现问题", ex);
        }
        return dateSeconds;
    }

    public static String getWeekName(String dateStr) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDateMillionSeconds(dateStr));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek < 0) {
            dayOfWeek = 0;
        }
        return weekDays[dayOfWeek];
    }

    public static int getWeekIndex(String dateStr) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDateMillionSeconds(dateStr));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek < 0) {
            dayOfWeek = 0;
        }
        return dayOfWeek;

    }

    /**
     * 返回当前是周几(从星期一开始算起)
     * 
     * @return
     */
    public static int getCurrentWeekIndex() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
        case 1:
            return 7;
        case 2:
            return 1;
        case 3:
            return 2;
        case 4:
            return 3;
        case 5:
            return 4;
        case 6:
            return 5;
        case 7:
            return 6;
        }
        return 0;
    }

    public static boolean isToday(Timestamp timestamp) {
        boolean isToday = false;
        if (timestamp != null) {
            long times = getTimes(23, 59, 59, 0);
            long now = timestamp.getTime();
            long diff = times - now;
            if (diff > 0) {
                long days = diff / (1000l * 60 * 60 * 24);
                if (days == 0) {
                    isToday = true;
                }
            }
        }
        return isToday;
    }

    /**
     * 返回int类型的年月日,比如20140819
     * 
     * @param timestamp
     * @return
     * @author nince
     */
    public static int getDay(Date timestamp) {
        String st = "";
        if (timestamp != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            st = sdf.format(timestamp);
        }
        return Integer.parseInt(st);
    }

    /**
     * 返回int类型的年月日,比如20140819
     * 
     * @param timestamp
     * @return
     * @author nince
     */
    public static String getDayStr(Date timestamp) {
        String st = "";
        if (timestamp != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            st = sdf.format(timestamp);
        }
        return st;
    }

    public static int getLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
        String st = sdf.format(date);
        return Integer.parseInt(st);
    }

    public static Timestamp getLastDayInTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        long time = calendar.getTimeInMillis();
        Timestamp timestamp = new Timestamp(time);
        return timestamp;
    }

    public static Timestamp getDayInTimestamp(int day, long addTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        Timestamp timestamp = new Timestamp(time + addTime);
        return timestamp;
    }

    public static long getTimestampDiff(Timestamp firstTimestatmp, Timestamp secondTimestamp) {
        long firstTime = firstTimestatmp.getTime();
        long secondTime = secondTimestamp.getTime();
        return firstTime - secondTime;
    }

    public static int getDateToInteger(String dateStr) {
        int result = 0;
        if (dateStr != null && !dateStr.isEmpty()) {
            result = Integer.parseInt(dateStr.replace("-", "").trim());
        }
        return result;
    }

    public static String convertDateFormat(Integer dateInteger, String format) {
        String result = "";
        if (dateInteger != null && dateInteger != 0) {
            if (format != null && !format.isEmpty()) {
                try {
                    DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
                    Date date = dayFormat.parse(String.valueOf(dateInteger));
                    SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
                    result = sdf.format(date);
                } catch (ParseException ex) {
                	LogUtil.error("解析日期出现问题", ex);
                }
            }
        }
        return result;

    }

    public static int getDateInSecondes(Integer birthday) {
        int dateSeconds = 0;
        DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        try {
            if (birthday > 0) {
                date = dayFormat.parse(String.valueOf(birthday));
                dateSeconds = (int) (date.getTime() / 1000l);
            }
        } catch (ParseException ex) {
        	LogUtil.error("解析日期出现问题", ex);
        }

        return dateSeconds;

    }

    public static String getWeek(long currentTime, int daysOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DATE, daysOfWeek);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getMonth(long currentTime, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.MONTH, month);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getDate(long currentTime, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.add(Calendar.DATE, days);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getyyyy_mm_ddDate(long currentTime, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.add(Calendar.DATE, days);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getyyyymmddDate(long currentTime, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.add(Calendar.DATE, days);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getHour(long currentTime, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.HOUR_OF_DAY, hours);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static Timestamp getHourTimestamp(long currentTime, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.HOUR_OF_DAY, hours);
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return timestamp;
    }

    public static String getyyyy_mm_ddsssDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getNowyyyy_mm_dd() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    public static String getBeforeThirtyDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -30);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String st = sdf.format(date);
        return st;
    }

    public static long getLeaveTimestampDiff(Timestamp sendTimestatmp, int days) {
        Date curDate = null;
        long LeaveLongTime = 0l;
        long LeaveMillisecond = 0l;
        try {
            String sendStrTime = DateTimeUtils.timestamp2Str(sendTimestatmp);
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            curDate = dateFormat.parse(sendStrTime);
            long sendLongTime = curDate.getTime();
            String confirmdata = getDate(sendLongTime, days);// 确认时间
            java.util.Date dt = new Date();
            System.out.println(dt.toString()); // java.util.Date的含义
            long lSysTime = dt.getTime(); // 得到秒数，Date类型的getTime()返回毫秒数
            curDate = dateFormat.parse(confirmdata);
            LeaveLongTime = curDate.getTime() - lSysTime;
            LeaveMillisecond = LeaveLongTime / 1000;

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return LeaveMillisecond;

    }

    public static long getLeaveAckTimestampDiff(Timestamp AckTimestatmp) {
        Date ackDate = null;
        long LeaveLongTime = 0l;
        long LeaveMillisecond = 0l;
        try {
            String ackStrTime = DateTimeUtils.timestamp2Str(AckTimestatmp);
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            ackDate = dateFormat.parse(ackStrTime);
            long ackLongTime = ackDate.getTime();
            java.util.Date dt = new Date();
            long lSysTime = dt.getTime(); // 得到秒数，Date类型的getTime()返回毫秒数
            LeaveLongTime = ackLongTime - lSysTime;
            LeaveMillisecond = LeaveLongTime / 1000;

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return LeaveMillisecond;

    }

    public static Timestamp getLeaveTimeBysendTime(Timestamp sendTimestatmp, int days) {// 发货后15天确认时间
        Date curDate = null;
        Timestamp confirmdataTimestamp = null;
        try {
            String sendStrTime = DateTimeUtils.timestamp2Str(sendTimestatmp);
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            curDate = dateFormat.parse(sendStrTime);
            long sendLongTime = curDate.getTime();
            String confirmdata = getDate(sendLongTime, days);// 确认时间
            confirmdataTimestamp = Timestamp.valueOf(confirmdata);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return confirmdataTimestamp;
    }

    public static Timestamp getProcessDays(int days) {
        java.util.Date nowdt = new Date();
        long nowLongTime = nowdt.getTime();
        Timestamp confirmdataTimestamp = null;
        String confirmdata = getDate(nowLongTime, days);//
        confirmdataTimestamp = Timestamp.valueOf(confirmdata);
        return confirmdataTimestamp;
    }

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());

        System.out.print(dateFormat.format(timestamp));
    }

    public static String timestamp2StrDate(Timestamp datetime) {
        String st = "";
        if (datetime != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            st = sdf.format(datetime);//
        }
        if (st.contains(".")) {
            st = st.substring(0, st.lastIndexOf("."));
        }
        return st;
    }

    public static String timestamp2StrMonth(Timestamp datetime) {
        String st = "";
        if (datetime != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
            st = sdf.format(datetime);//
        }
        if (st.contains(".")) {
            st = st.substring(0, st.lastIndexOf("."));
        }
        return st;
    }

    public static long timeComparison(Timestamp timestatmp) {
        Date ackDate = null;
        long LeaveLongTime = 0l;
        try {
            String timeStrTime = DateTimeUtils.timestamp2Str(timestatmp);
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            ackDate = dateFormat.parse(timeStrTime);
            long ackLongTime = ackDate.getTime();
            java.util.Date dt = new Date();
            long lSysTime = dt.getTime(); //
            LeaveLongTime = ackLongTime - lSysTime;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return LeaveLongTime;

    }

    /**
     * 时间天数加减加
     */
    public static String addDate(int num) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar nowdate = Calendar.getInstance();
        nowdate.add(Calendar.DATE, num);
        str = sdf.format(nowdate.getTime());
        return str;
    }

    public static Timestamp curTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static long getTimelong(String dateStr) {
        long dateSeconds = 0;
        try {
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            Date date = dayFormat.parse(dateStr);
            dateSeconds = date.getTime();
        } catch (ParseException ex) {
        	LogUtil.error("解析日期出现问题", ex);
        }
        return dateSeconds;
    }

    public static String getcurrent_yyyy_mm_dd() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        return dateFormat.format(timestamp);
    }

    /**
     * 返回页面前端显示传入的预计送达时间格式 yyyy-MM-dd HH:mm-HH:mm,将此格式进行解析
     * 
     * @param expectedTime
     * @return array ( array[0] = begin , array[1] = end )<br/>
     *         begin/end：yyyy-MM-dd HH:mm:ss;
     * @throws Exception
     */
    public static String[] getAddExpectedTime(String expectedTime) throws Exception {
        String time = expectedTime;
        String[] array = new String[2];
        int s = 16;
        if (s != time.lastIndexOf("-")) {
            throw new Exception("预计送达时间格式不正确，请查看格式是否如：yyyy-MM-dd HH:mm-HH:mm。");
        }
        array[0] = time.substring(0, time.lastIndexOf("-")) + ":00";
        String top = time.substring(0, 11);
        array[1] = top + time.substring(time.lastIndexOf("-") + 1, time.length()) + ":00";
        return array;
    }

    /**
     * 返回给APP客户端显示的预计送达时间格式
     * 
     * @param begin
     *            (yyyy-MM-dd HH:mm:ss)
     * @param end
     *            (yyyy-MM-dd HH:mm:ss)
     * @return yyyy-MM-dd HH:mm-HH:mm
     * @throws Exception
     */
    public static String getExpectedTime(String begin, String end) throws Exception {
        if (StringUtils.isEmpty(begin)) {
            throw new Exception("预计送达的开始时间不允许为空。");
        }
        String beginTime = begin.substring(0, begin.lastIndexOf(":"));

        if (StringUtils.isEmpty(end)) {
            throw new Exception("预计送达的结束时间不允许为空。");
        }
        String endTime = end.substring(11, end.length());
        endTime = endTime.substring(0, endTime.lastIndexOf(":"));
        return beginTime + "-" + endTime;
    }

    public static String getHHMM(String time) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");// 定义格式，不显示毫秒
            Date d = df.parse(time);
            return df.format(d);
        } catch (ParseException e) {
        	LogUtil.error("时间转换失败。");
        }
        return "";
    }
}
