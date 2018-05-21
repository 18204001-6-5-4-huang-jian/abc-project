package com.abcft.apes.vitamin.util;

import jdk.nashorn.internal.runtime.ParserException;
import org.bson.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class TimeUtil {

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format  如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Long date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static Long date2TimeStamp(String dateStr) {
        return date2TimeStamp(dateStr, "yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
    }

    public static String timeStamp2Date(long timestamp, String formats) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formats);
        return simpleDateFormat.format(new Date(timestamp));
    }

    public static String date2String(Object date) {
        return date2String(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String date2String(Object date, String format) {
        if (date instanceof Date) {
            return date2String((Date) date, format);
        } else {
            return date.toString();
        }
    }

    public static long getTimeInterval(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()));
    }

    public static String date2String(Date date) {
        return date2String(date, "yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
    }

    public static String date2String(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static String getCurDateTimeString() {
        return getCurDateTime("yyyyMMddHHmmssSSS");
    }

    public static String getCurDateTime() {
        return getCurDateTime("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String getCurDateTime(String format) {
        return timeStamp2Date(System.currentTimeMillis(), format);
    }

    public static Long getTomorowTS() {
        Date today = getCurDate();
        Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
        String tomorrowStr = date2String(tomorrow);

        return date2TimeStamp(tomorrowStr);
    }

    public static Date getCurDate() {
        return convertLocalDate2Date(LocalDate.now());
    }


    public static String getCurTimeStampStr() {
        return Long.toString(System.currentTimeMillis());
    }

    public static Date getRelateDate(int type, int mins) {
        return getRelateDate(new Date(), type, mins);
    }

    public static Date getRelateDate(Date date, int type, int delta) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, delta);

        return calendar.getTime();
    }

    public static Date getStopDate(Date startDate, String termUnit, int termValue) {
        return TimeUtil.convertLocalDate2Date(getStopDate(TimeUtil.convertDate2LocalDate(startDate), termUnit, termValue));
    }

    public static LocalDate getStopDate(LocalDate startDate, String termUnit, int termValue) {
        LocalDate stopDate;
        if (termUnit.equalsIgnoreCase("year") && termValue == 0) {
            stopDate = startDate.plusMonths(6);
            return stopDate;
        }
        switch (termUnit) {
            case "day":
                stopDate = startDate.plusDays(termValue);
                break;
            case "week":
                stopDate = startDate.plusWeeks(termValue);
                break;
            case "month":
                stopDate = startDate.plusMonths(termValue);
                break;
            case "year":
                stopDate = startDate.plusYears(termValue);
                break;
            default:
                stopDate = startDate.plusDays(1);
                break;
        }
        return stopDate;
    }

    public static Date convertLocalDate2Date(LocalDate date) {
        return java.sql.Timestamp.valueOf(date.atStartOfDay());
    }

    public static LocalDate convertDate2LocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date strToDate(String strDate, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(strDate);
    }

    public static Date strToDate(String strDate, String... patterns) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        for (String pattern : patterns) {
            try {
                dateFormat.applyPattern(pattern);
                return dateFormat.parse(strDate);
            } catch (Exception ignored) {
            }
        }
        String stringBuilder = "date string '" + strDate +
                "' not matches any pattens: " +
                Arrays.toString(patterns);
        throw new ParserException(stringBuilder);
    }

    /**
     * 将日期转换成评论下方的格式化日期字符串
     *
     * @param date
     * @return
     */
    public static String date2CommentDate(Date date, String lang) {
        long cTime = date.getTime();
        long midnight = getCurDate().getTime();
        long time1 = date.getTime() - midnight;
        long now = new Date().getTime();
        long time2;
        String timeRes;
        boolean isCN = lang.equalsIgnoreCase(CommentUtil.Langs.ZH_CN.name());
//        System.err.println("lang==" + lang);
//        System.err.println("isCN==" + isCN);
        //今天的评论
        if (time1 > 0) {
            time2 = now - cTime;
            //时间差在5分钟以内显示 刚刚
            if (time2 < 300000) {
                timeRes = (isCN ? "刚刚" : "Just now");
                return timeRes;
            }
            //时间差在一小时之内显示 mm分钟前
            if (time2 < 3600000) {
                timeRes = time2 / 60000 +
                        (isCN ? " 分钟前" : " minutes ago");
                return timeRes;
            }
            //时间差在一天之内显示 今天 hh:mm
            return date2String(date,
                    (isCN ? "'今天' HH:mm" : "'Today' HH:mm"));

        }
        return date2String(date, "yyyy-M-d HH:mm");

    }

    public static String changeTimeZone(Date utc, String pattern, String offset) {
        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("GMT" + offset));
        return df.format(utc);
    }

    public static int strToCalendarUnit(final String unit) {
        String unitL = unit.toLowerCase();
        switch (unitL) {
            case "seconds":
                return Calendar.SECOND;
            case "minutes":
                return Calendar.MINUTE;
            case "hours":
                return Calendar.HOUR_OF_DAY;
            case "days":
                return Calendar.DAY_OF_YEAR;
            case "weeks":
                return Calendar.WEEK_OF_YEAR;
            case "months":
                return Calendar.MONTH;
            case "years":
                return Calendar.YEAR;
            case "second":
                return Calendar.SECOND;
            case "minute":
                return Calendar.MINUTE;
            case "hour":
                return Calendar.HOUR_OF_DAY;
            case "day":
                return Calendar.DAY_OF_YEAR;
            case "week":
                return Calendar.WEEK_OF_YEAR;
            case "month":
                return Calendar.MONTH;
            case "year":
                return Calendar.YEAR;
            default:
                throw new IllegalArgumentException("can't convert '" + unit + "' to any time unit of Calendar");
        }
    }

    public static TimeUnit strToTimeUnit(String unit) {
        String unitL = unit.toLowerCase();
        switch (unitL) {
            case "seconds":
                return TimeUnit.SECONDS;
            case "minutes":
                return TimeUnit.MINUTES;
            case "hours":
                return TimeUnit.HOURS;
            case "days":
                return TimeUnit.DAYS;
            case "second":
                return TimeUnit.SECONDS;
            case "minute":
                return TimeUnit.MINUTES;
            case "hour":
                return TimeUnit.HOURS;
            case "day":
                return TimeUnit.DAYS;
            default:
                throw new IllegalArgumentException("can't convert '" + unit + "' to any TimeUnit");
        }
    }

    public static int getWorkdays(Date begin, Date end) {
        return getSpecialDays(begin, end, DayType.WORKDAY);
    }

    public static int getRestdays(Date begin, Date end) {
        return getSpecialDays(begin, end, DayType.RESTDAY);
    }

    public static int getHolidays(Date begin, Date end) {
        return getSpecialDays(begin, end, DayType.HOLIDAY);
    }

    public static int getNaturedays(Date begin, Date end) {
        return getSpecialDays(begin, end, DayType.NATUREDAY);
    }

    /**
     * 获取指定日期区间内的天数（工作日， 休息日， 节假日， 自然日）
     *
     * @param begin
     * @param end
     * @param dayType
     * @return
     */
    public static int getSpecialDays(Date begin, Date end, DayType dayType) {
        if (dayType.equals(DayType.ALL))
            return getSpecialDays(begin, end).getInteger("total");
        if (dayType.equals(DayType.NATUREDAY))
            return findDates(begin, end).size();
        return (int) getSpecialDays(begin, end).get("list", ArrayList.class).get(dayType.id);
    }

    public static Document getSpecialDays(Date begin, Date end) {
        String httpUrl = "http://tool.bitefu.net/jiari";
        BufferedReader reader;
        //构造query参数
        List<String> dateStr = findDates(begin, end, "yyyyMMdd");
        String httpArg = StringUtils.list2CSV(dateStr, ",");
        httpUrl = httpUrl + "?d=" + httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            //构造json响应体
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            JsonReader jsonReader = Json.createReader(reader);
            JsonObject jsonObject = jsonReader.readObject();
            System.out.println(jsonObject.toString());
            reader.close();
            int work = 0;
            int rest = 0;
            int holi = 0;
            Document document = MongoUtil.json2Document(jsonObject);
            for (Object o : document.values()) {
                if (Integer.valueOf(String.valueOf(o)) == DayType.WORKDAY.id)
                    work++;
                if (Integer.valueOf(String.valueOf(o)) == DayType.RESTDAY.id)
                    rest++;
                if (Integer.valueOf(String.valueOf(o)) == DayType.HOLIDAY.id)
                    holi++;
            }
            //注意添加顺序与DateType的ID顺序一致
            List<Integer> list = new ArrayList<>(Arrays.asList(
                    work, rest, holi
            ));
            return new Document("total", work + rest + holi)
                    .append("list", list);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List lDate = new ArrayList();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        calBegin.add(Calendar.DAY_OF_MONTH, 1);
        while (dEnd.after(calBegin.getTime())) {
            lDate.add(calBegin.getTime());
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
        }
        return lDate;
    }

    public static List<String> findDates(Date dBegin, Date dEnd, String pattern) {
        List lDate = findDates(dBegin, dEnd);
        List<String> list = new ArrayList<>();
        lDate.forEach(d -> list.add(date2String(d, pattern)));
        return list;
    }

    public static int getDays(int calendarUnit) {
        switch (calendarUnit) {
            case Calendar.DAY_OF_YEAR:
                return 1;
            case Calendar.WEEK_OF_YEAR:
                return 7;
            case Calendar.MONTH:
                return 30;
            case Calendar.YEAR:
                return 365;
            default:
                return 0;
        }
    }

    public static long getDaysCount(Date dBegin, Date dEnd) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime lBegin = LocalDateTime.ofInstant(dBegin.toInstant(), zoneId);
        LocalDateTime lEnd = LocalDateTime.ofInstant(dEnd.toInstant(), zoneId);
        return ChronoUnit.DAYS.between(lBegin, lEnd);
    }

    public static Date changeTimeZone(Date rawDate, ZoneId origin, ZoneId target) {
        ZonedDateTime originDate = rawDate.toInstant().atZone(origin);
        return Date.from(ZonedDateTime.of(originDate.toLocalDateTime(), target).toInstant());
    }

    public static boolean isToday(Date date) {
        LocalDate localDate = LocalDate
                .from(date.toInstant().atZone(ZoneId.systemDefault()));
        return localDate.equals(LocalDate.now(ZoneId.systemDefault()));
    }

    public static Date getStartOfCurrentMonth() {
        LocalDate localDate = LocalDate.now(ZoneId.systemDefault());
        int year = localDate.getYear();
        int monthValue = localDate.getMonthValue();
        Instant instant = LocalDate.of(year, monthValue, 1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date strToDate(String date, boolean isCN, String... patterns) {
        date = date.trim();
//        ZoneId.getAvailableZoneIds().stream().sorted().forEach(System.out::println);
        for (String pattern : patterns) {
            pattern = pattern.trim();
            try {
                SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat(pattern, isCN ? Locale.CHINA : Locale.ENGLISH);
                return simpleDateFormat.parse(date);
            } catch (Exception ignored) {
//                System.out.println(isCN);
//                System.out.println(pattern);
            }
        }
        String stringBuilder = "date string '" + date +
                "' not matches any pattens: " +
                Arrays.toString(patterns);
        throw new ParserException(stringBuilder);
    }

    public static int extractTimeUnitFromDate(Date date, int calendarUnit) {
        switch (calendarUnit) {
            case Calendar.YEAR:
                return getYearFromDate(date);
            case Calendar.MONTH:
                return getMonthFromDate(date);
            case Calendar.DAY_OF_MONTH:
                return getDayFromDate(date);
            default:
                return -1;
        }
    }

    private static int getDayFromDate(Date date) {
        LocalDate localDate = LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault()));
        return localDate.getDayOfMonth();
    }

    private static int getMonthFromDate(Date date) {
        LocalDate localDate = LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault()));
        return localDate.getMonthValue();
    }

    private static int getYearFromDate(Date date) {
        LocalDate localDate = LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault()));
        return localDate.getYear();
    }

    public static long getMonthCount(Date startDate, Date stopDate) {
        LocalDate start = LocalDate.from(startDate.toInstant().atZone(ZoneId.systemDefault()));
        LocalDate stop = LocalDate.from(stopDate.toInstant().atZone(ZoneId.systemDefault()));
        return ChronoUnit.MONTHS.between(start, stop);
    }

    public static Date plusDate(Date date, ChronoUnit months) {
        LocalDate localDate = LocalDate.from(date.toInstant().atZone(ZoneId.systemDefault()));
        LocalDate plus = localDate.plus(1, months);
        return Date.from(Instant.from(plus.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
    }

    public static String date2String(Date date, String pattern, ZoneId zoneId) {
        LocalDateTime localDateTime = LocalDateTime.from(date.toInstant().atZone(zoneId));
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public enum DayType {
        WORKDAY(0),     //工作日
        RESTDAY(1),     //休息日
        HOLIDAY(2),     //节假日
        NATUREDAY(3),   //自然日(本地方法获得)
        ALL(4);         //自然日(调用接口获得)

        private int id;

        DayType(int id) {
            this.id = id;
        }

        public static DayType valueOf(int id) {
            for (DayType dayType : values()) {
                if (dayType.id == id)
                    return dayType;
            }
            return ALL;
        }
    }
}
