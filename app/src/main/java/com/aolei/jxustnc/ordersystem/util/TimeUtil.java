package com.aolei.jxustnc.ordersystem.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将时间格式化
 * Created by NewOr on 2016/5/12.
 */
public class TimeUtil {

    public final static String FORMAT_TIME = "HH:mm";
    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
    public final static String FORMAT_DATE_TIME_SECOND = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_MONTH_DAY_TIME = "MM-dd HH:mm";

    public static String getFormatToday(String dateFormat) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(currentTime);
    }

    public static Date stringToDate(String dateStr, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String dateToString(Date date, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);
    }

    public static String getFormatDate(String time) {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = stringToDate(time, FORMAT_DATE_TIME_SECOND);
        int temp = Integer.parseInt(sdf.format(today)) - Integer.parseInt(sdf.format(otherDay));
        switch (temp) {
            case 0:
                result = "今天 " + getHourAndMin(time);
                break;
            case 1:
                result = "昨天" + getHourAndMin(time);
                break;
            case 2:
                result = "前天" + getHourAndMin(time);
                break;
            default:
                result = getTime(time);
                break;
        }
        return result;
    }

    public static String getHourAndMin(String time) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_TIME);
        return format.format(stringToDate(time, FORMAT_DATE_TIME_SECOND));
    }

    public static String getTime(String time) {
        String pattern = FORMAT_DATE_TIME;
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(stringToDate(time, FORMAT_DATE_TIME_SECOND));
    }

}
