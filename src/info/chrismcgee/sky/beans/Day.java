package info.chrismcgee.sky.beans;

import java.sql.Date;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

public class Day {
	
	static final Logger log = LogManager.getLogger(Day.class.getName());
	
	private Date date;
	private long availableScreenCups;
	private long availableScreenNaps;
	private long availablePad;
	private long availableDigital;
	private long availableHotstamp;
	private long availableOffsetCups;
	private long availableOffsetNaps;
	private long availableOutsourced;
	private long remainScreenCups;
	private long remainScreenNaps;
	private long remainPad;
	private long remainHotstamp;
	private long remainDigital;
	private long remainOffsetCups;
	private long remainOffsetNaps;
	private long remainOutsourced;
	private Timestamp dayCompleted;

/*	public Day() {
		super();
	}*/
	
	public Day(Date date) {
		super();
		this.date = date;
		this.availableScreenCups = isWeekDay() ? 50000 : 0;
		this.availableScreenNaps = isWeekDay() ? 20000 : 0;
		this.availablePad = isWeekDay() ? 100000 : 0;
		this.availableHotstamp = isWeekDay() ? 10000 : 0;
		this.availableDigital = isWeekDay() ? 10000 : 0;
		this.availableOffsetCups = isWeekDay() ? 100000 : 0;
		this.availableOffsetNaps = isWeekDay() ? 100000 : 0;
		this.availableOutsourced = isWeekDay() ? 50000 : 0;
		this.remainScreenCups = 0;
		this.remainScreenNaps = 0;
		this.remainPad = 0;
		this.remainHotstamp = 0;
		this.remainDigital = 0;
		this.remainOffsetCups = 0;
		this.remainOffsetNaps = 0;
		this.dayCompleted = null;
	}

	private boolean isWeekDay()
	{
		log.debug("Day of week is: " + LocalDate.fromDateFields(date).getDayOfWeek());
		log.debug("Sunday is: " + DateTimeConstants.SUNDAY);
		log.debug("Saturday is: " + DateTimeConstants.SATURDAY);
		return LocalDate.fromDateFields(date).getDayOfWeek() != DateTimeConstants.SUNDAY
				&& LocalDate.fromDateFields(date).getDayOfWeek() != DateTimeConstants.SATURDAY;
	}
	

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getAvailableScreenCups() {
		return availableScreenCups;
	}

	public void setAvailableScreenCups(long availableScreenCups) {
		this.availableScreenCups = availableScreenCups;
	}

	public long getAvailableScreenNaps() {
		return availableScreenNaps;
	}

	public void setAvailableScreenNaps(long availableScreenNaps) {
		this.availableScreenNaps = availableScreenNaps;
	}

	public long getAvailablePad() {
		return availablePad;
	}

	public void setAvailablePad(long availablePad) {
		this.availablePad = availablePad;
	}

	public long getAvailableHotstamp() {
		return availableHotstamp;
	}

	public void setAvailableHotstamp(long availableHotstamp) {
		this.availableHotstamp = availableHotstamp;
	}

	public long getAvailableOffsetCups() {
		return availableOffsetCups;
	}

	public void setAvailableOffsetCups(long availableOffsetCups) {
		this.availableOffsetCups = availableOffsetCups;
	}

	public long getAvailableOffsetNaps() {
		return availableOffsetNaps;
	}

	public void setAvailableOffsetNaps(long availableOffsetNaps) {
		this.availableOffsetNaps = availableOffsetNaps;
	}

	public long getAvailableDigital() {
		return availableDigital;
	}

	public void setAvailableDigital(long availableDigital) {
		this.availableDigital = availableDigital;
	}

	public long getAvailableOutsourced() {
		return availableOutsourced;
	}

	public void setAvailableOutsourced(long availableOutsourced) {
		this.availableOutsourced = availableOutsourced;
	}

	public long getRemainScreenCups() {
		return remainScreenCups;
	}

	public void setRemainScreenCups(long remainScreenCups) {
		this.remainScreenCups = remainScreenCups;
	}

	public long getRemainScreenNaps() {
		return remainScreenNaps;
	}

	public void setRemainScreenNaps(long remainScreenNaps) {
		this.remainScreenNaps = remainScreenNaps;
	}

	public long getRemainPad() {
		return remainPad;
	}

	public void setRemainPad(long remainPad) {
		this.remainPad = remainPad;
	}

	public long getRemainHotstamp() {
		return remainHotstamp;
	}

	public void setRemainHotstamp(long remainHotstamp) {
		this.remainHotstamp = remainHotstamp;
	}

	public long getRemainOffsetCups() {
		return remainOffsetCups;
	}

	public void setRemainOffsetCups(long remainOffsetCups) {
		this.remainOffsetCups = remainOffsetCups;
	}

	public long getRemainOffsetNaps() {
		return remainOffsetNaps;
	}

	public void setRemainOffsetNaps(long remainOffsetNaps) {
		this.remainOffsetNaps = remainOffsetNaps;
	}

	public long getRemainDigital() {
		return remainDigital;
	}

	public void setRemainDigital(long remainDigital) {
		this.remainDigital = remainDigital;
	}

	public long getRemainOutsourced() {
		return remainOutsourced;
	}

	public void setRemainOutsourced(long remainOutsourced) {
		this.remainOutsourced = remainOutsourced;
	}

	public Timestamp getDayCompleted() {
		return dayCompleted;
	}

	public void setDayCompleted(Timestamp dayCompleted) {
		this.dayCompleted = dayCompleted;
	}

}
