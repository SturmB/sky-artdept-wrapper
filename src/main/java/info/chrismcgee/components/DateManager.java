package info.chrismcgee.components;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateManager {

	private static final java.util.Date[] EMPTY_DATE_ARRAY = new java.util.Date[0];
	public static final LocalDate HOLD_DATE = new LocalDate(9999, 12, 31);
	public static final LocalDate PROOF_DATE = new LocalDate(9999, 12, 30);
	private static LocalDate holdDateComparator = new LocalDate();
	private static LocalDate proofDateComparator = new LocalDate();
	private static LocalDate today = LocalDate.now();
	private static DateTimeFormatter displayFmt = DateTimeFormat.forPattern("MM/dd/yy");
	private static DateTimeFormatter sqlFmt = DateTimeFormat.forPattern("yyyy-MM-dd");
	static final Logger log = LogManager.getLogger(DateManager.class.getName());
	
	/**
	 * Turns a date String into a LocalDate object.
	 * 
	 * @param dateStr A parse-able String representation of a date.
	 * @return LocalDate object
	 */
	public static LocalDate getLocalDate(String dateStr)
	{
		if (dateStr.equals("On Hold")) return HOLD_DATE;
		if (dateStr.equals("Proofs")) return PROOF_DATE;
		return LocalDate.parse(dateStr, displayFmt);
	}
	
	public static Date getSqlDate(String dateStr)
	{
		return Date.valueOf(dateStr);
	}
	
	public static Date localDateToSqlDate(LocalDate lDate)
	{
		String sDate = getSqlFormattedDate(lDate);
		return getSqlDate(sDate);
	}
	
	/**
	 * Takes a LocalDate object and converts it into a more human-readable String.
	 * 
	 * @param convertDate The Date to convert.
	 * @return String of formatted LocalDate
	 */
	public static String getDisplayDate(LocalDate convertDate)
	{
		return convertDate.toString(displayFmt);
	}
	
	public static String getDisplayDate(Date convertDate)
	{
		return getDisplayDate(LocalDate.fromDateFields(convertDate));
	}

	public static String getSqlFormattedDate(LocalDate convertDate)
	{
		return convertDate.toString(sqlFmt);
	}
	
	public static String getToday()
	{
		return getDisplayDate(today);
	}
	
	public static String getNextDay(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.plusDays(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String getNextMonth(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.plusMonths(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String getNextYear(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.plusYears(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String getPreviousDay(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.minusDays(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String getPreviousMonth(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.minusMonths(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String getPreviousYear(String oldDateStr)
	{
		LocalDate oldDate = getLocalDate(oldDateStr);
		LocalDate newDate = oldDate.minusYears(1);
		String newDateStr = getDisplayDate(newDate);
		return newDateStr;
	}
	
	public static String jDateToString(java.util.Date jDate)
	{
		LocalDate convertedDate = LocalDate.fromDateFields(jDate);
		return getDisplayDate(convertedDate);
	}
	
	public static java.util.Date stringToJDate (String stringDate)
	{
		return getLocalDate(stringDate).toDate();
	}
	
	public static Date usDateStringToSqlDate (String stringDate)
	{
		return localDateToSqlDate(getLocalDate(stringDate));
	}
	
	public static java.util.Date[] getDateRange(java.util.Date startDate, java.util.Date endDate)
	{
		LocalDate startLocalDate = new LocalDate(startDate);
		LocalDate endLocalDate = new LocalDate(endDate);
		List<java.util.Date> dates = new ArrayList<java.util.Date>();
		int days = Days.daysBetween(startLocalDate, endLocalDate).getDays();
		for (int i = 0; i < days; i++)
		{
			LocalDate d = startLocalDate.withFieldAdded(DurationFieldType.days(), i);
			dates.add(d.toDate());
		}
		return dates.toArray(EMPTY_DATE_ARRAY);
	}
	
	public static boolean isOnHoldDate (Date sqlDate)
	{
		holdDateComparator = LocalDate.fromDateFields(sqlDate);
		return holdDateComparator.equals(HOLD_DATE);
	}
	
	public static boolean isProofingDate (Date sqlDate)
	{
		proofDateComparator = LocalDate.fromDateFields(sqlDate);
		return proofDateComparator.equals(PROOF_DATE);
	}
	
	public static boolean isWeekDay(Date date) {
		return LocalDate.fromDateFields(date).getDayOfWeek() != DateTimeConstants.SUNDAY
				&& LocalDate.fromDateFields(date).getDayOfWeek() != DateTimeConstants.SATURDAY;
	}

}
