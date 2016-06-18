package com.honey.aaron.workoff.util;

import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final String TAG = TimeUtil.class.getSimpleName();

    public static String getYear(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "2016";
    }

    public static String getMonth(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "01";
    }

    public static String getWeek(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("w", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "1";
    }

    public static String getDate(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "01";
    }

    public static String getDay(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "Mon";
    }

    public static String getTime(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time.");
        }
        return "12:00";
    }

    /**
     * gap을 계산하여 휴식시간 계산 후 반환하는 로직
     */
    public static long getGapFromTimestamps(long fromTimestamp, long toTimestamp) {
        // 만일 자정이 넘었다면 하루를 더해줌.
        if(fromTimestamp > toTimestamp) toTimestamp += 24 * 60 * 60 * 1000;

        // 초를 제거함. 분단위로만 근무시간이 계산됨
        fromTimestamp -= (fromTimestamp % (60 * 60 * 1000)) % (60 * 1000);

        long gap = toTimestamp - fromTimestamp;

        // 8시간 이상일 경우 1시간 휴식 적용
        if(gap / (60 * 60 * 1000) >= 8 && gap / (60 * 60 * 1000) <= 12) {
            gap -= 60 * 60 * 1000;
        } else if(gap / (60 * 60 * 1000) == 4 && (gap % (60 * 60 * 1000)) / (60 * 1000) <= 30) { // 4시간 ~ 4시간 반 근무 4시간 적용
            gap = 4 * 60 * 60 * 1000;
        } else if(gap / (60 * 60 * 1000) < 8  && gap / (60 * 60 * 1000) >= 4) { // 4시간 반 이상일 경우 30분 휴식 적용
            gap -= 30 * 60 * 1000;
        } else if(gap / (60 * 60 * 1000) >= 13) { // 12시간 이상일 경우 최대값 12시간 적용
            gap = 12 * 60 * 60 * 1000;
        }

        return gap;
    }

    /**
     * @param totalTimestamp
     * gap 의 합으로 주단위 총 시간을 계산할때 사용함
      */
    public static String getWeeklyTotalWorkTime(long totalTimestamp) {
        // 초로 변환
//        int sec = (int)(((gap % (60 * 60 * 1000)) % (60 * 1000)) / 1000);
        int min = (int)((totalTimestamp % (60 * 60 * 1000)) / (60 * 1000));
        int hour = (int)(totalTimestamp / (60 * 60 * 1000));

        return hour < 10 ? "0" + hour : hour + ":" + (min < 10 ? "0" + min : min);
    }

    /**
     * @param fromTimestamp 시작 시간
     * @param toTimestamp 종료 시간
     * 시작과 끝의 timestamp 로 시간을 계산할때 사용함
     */
    public static String getTotalWorkTime(long fromTimestamp, long toTimestamp) {
        long gap = getGapFromTimestamps(fromTimestamp, toTimestamp);

        // 초로 변환
//        int sec = (int)(((gap % (60 * 60 * 1000)) % (60 * 1000)) / 1000);
        int min = (int)((gap % (60 * 60 * 1000)) / (60 * 1000));
        int hour = (int)(gap / (60 * 60 * 1000));

        try{
            SimpleDateFormat fromFormat = new SimpleDateFormat("H:m", Locale.KOREA);
            SimpleDateFormat toFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
            String timeDiff = hour + ":" + min;

            return toFormat.format(fromFormat.parse(timeDiff, new ParsePosition(0)));
        }catch (Exception e) {
            Log.e(TAG, "Failed to calculate time.");
        }
        return "04:00";
    }

    public static long getMillisecondsFromString(String year, String month, String date, String time) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        String format = year + "-" + month + "-" + date + " " + time;
        Date toTime = fromFormat.parse(format, new ParsePosition(0));
        Calendar cal = Calendar.getInstance();
        cal.setTime(toTime);
        return cal.getTimeInMillis();
    }

    public static String getDatePeriodForThisWeek() {
        StringBuilder period = new StringBuilder();
        Calendar calendar = Calendar.getInstance();

        // 일요일 날짜 계산..
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        period.append(getDisplayDateFormat(calendar));
        // 날짜 구분 기호 삽입
        period.append(" ~ ");
        // 토요일 날짜 계산..  ** 변경 - 일~월로 보여주기로 함.. 차후 변경 될 시 참고 --> calendar 는 일요일부터 시작이므로 월~ 일은 7일을 더해서 다음주 일요일을 계산해야 함
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        period.append(getDisplayDateFormat(calendar));

        return period.toString();
    }

    public static String getDisplayDateFormat(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
        Date currentTimeZone = calendar.getTime();
        return sdf.format(currentTimeZone);
    }

    public static boolean isToday(long targetTimestamp) {
        Calendar calendar = Calendar.getInstance();
        long todayTimestamp = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, -1);
        long yesterdayTimestamp = calendar.getTimeInMillis();
        return (getYear(targetTimestamp).equals(getYear(todayTimestamp)) && getMonth(targetTimestamp).equals(getMonth(todayTimestamp))
                && getDate(targetTimestamp).equals(getDate(todayTimestamp))) ||
                // 하루가 지나 새벽근무를 할 경우 어제의 날짜와 같으며 시간이 6시 이전일 경우 어제의 근무와 합산하기 때문에 오늘로 본다.
                (getYear(targetTimestamp).equals(getYear(yesterdayTimestamp)) && getMonth(targetTimestamp).equals(getMonth(yesterdayTimestamp))
                && getDate(targetTimestamp).equals(getDate(yesterdayTimestamp)) && calendar.get(Calendar.HOUR_OF_DAY) < 6);
    }
}
