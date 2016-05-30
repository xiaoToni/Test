package com.lianlian.mbop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zhuxl
 * 
 */
public class DateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

	public static String getFormatedDate(Date date, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(date);
	}

	public static Date toDate(String dateStr, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		Date date;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			LOGGER.error("ÈÕÆÚ[{}]½âÎö´íÎó[{}]", dateStr, pattern);
			date = new Date();
		}
		return date;
	}

}
