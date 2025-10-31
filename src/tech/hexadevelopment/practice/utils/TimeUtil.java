package tech.hexadevelopment.practice.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import tech.hexadevelopment.practice.LegionPractice;

public class TimeUtil {

	
	public static String getCurrentTime() {
		LegionPractice plugin = LegionPractice.getInstance();
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(getCurrentDisplayTime());
		SimpleDateFormat sdf = new SimpleDateFormat(plugin.getConfig().getString("current-time-format"));
		return sdf.format(cal.getTime());
	}
	
	public static long getCurrentDisplayTime() {
		return System.currentTimeMillis()+(LegionPractice.getInstance().getConfig().getInt("server-time-increase")*1000*60*60);
	}

	public static Calendar getCurrentCalendarTime() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeInMillis(getCurrentDisplayTime());
		return calendar;
	}
}
