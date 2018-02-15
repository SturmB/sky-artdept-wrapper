package info.chrismcgee.sky.beans;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.stream.Format;

import info.chrismcgee.enums.OSType;

@Root(name="maximumsAvailable")
public class ShipDate {
	
	private static final String FILE_SYSTEM_PREFIX = OSType.getOSType() == OSType.MAC
			? "/Volumes"
			: "//SKYFS";
	private static final String XML_LOCATION = FILE_SYSTEM_PREFIX + "/ArtDept/ArtDept/Scripts";
	private static final String XML_FILENAME = "maxes.xml";
	private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	private static final Format XML_FORMAT = new Format(XML_PROLOG);
	private static File xmlFile = new File(XML_LOCATION + "/" + XML_FILENAME);
	
	private Date id;
	@Element
	private long availableScreenCups = 50000L;
	@Element
	private long availableScreenNaps = 20000L;
	@Element
	private long availablePad = 100000L;
	@Element
	private long availableHotstamp = 10000L;
	@Element
	private long availableDigitalCups = 12000L;
	@Element
	private long availableDigitalFlats = 12000L;
	@Element
	private long availableOffsetCups = 100000L;
	@Element
	private long availableOffsetNaps = 100000L;
	@Element
	private long availableOutsourced = 50000L;

	private long remainScreenCups = 0L;
	private long remainScreenNaps = 0L;
	private long remainPad = 0L;
	private long remainHotstamp = 0L;
	private long remainDigitalCups = 0L;
	private long remainDigitalFlats = 0L;
	private long remainOffsetCups = 0L;
	private long remainOffsetNaps = 0L;
	private long remainOutsourced = 0L;
	private Timestamp dayCompleted = null;

	public ShipDate() {
	}
	
	public ShipDate(Date date) {
		this.id = date;
	}
	

	public Date getId() {
		return id;
	}

	public void setId(Date id) {
		this.id = id;
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

	public long getAvailableDigitalCups() {
		return availableDigitalCups;
	}

	public void setAvailableDigitalCups(long availableDigitalCups) {
		this.availableDigitalCups = availableDigitalCups;
	}

	public long getAvailableDigitalFlats() {
		return availableDigitalFlats;
	}
	
	public void setAvailableDigitalFlats(long availableDigitalFlats) {
		this.availableDigitalFlats = availableDigitalFlats;
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

	public long getRemainDigitalCups() {
		return remainDigitalCups;
	}

	public void setRemainDigitalCups(long remainDigitalCups) {
		this.remainDigitalCups = remainDigitalCups;
	}

	public long getRemainDigitalFlats() {
		return remainDigitalFlats;
	}
	
	public void setRemainDigitalFlats(long remainDigitalFlats) {
		this.remainDigitalFlats = remainDigitalFlats;
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
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder("This Day bean consists of: ");
		sb.append('\n');
		sb.append("Screen Cups: ");
		sb.append(availableScreenCups);
		sb.append('\n');
		sb.append("Screen Napkins: ");
		sb.append(availableScreenNaps);
		sb.append('\n');
		sb.append("Pad Print: ");
		sb.append(availablePad);
		sb.append('\n');
		sb.append("Hotstamp: ");
		sb.append(availableHotstamp);
		sb.append('\n');
		sb.append("Digital Cups: ");
		sb.append(availableDigitalCups);
		sb.append('\n');
		sb.append("Screen Flats: ");
		sb.append(availableDigitalFlats);
		sb.append('\n');
		sb.append("Offset Cups: ");
		sb.append(availableOffsetCups);
		sb.append('\n');
		sb.append("Offset Napkins: ");
		sb.append(availableOffsetNaps);
		sb.append('\n');
		sb.append("Outsourced: ");
		sb.append(availableOutsourced);
		sb.append('\n');
		
		return sb.toString();
	}

	public static File getXmlFile() {
		return xmlFile;
	}

	public static Format getXmlFormat() {
		return XML_FORMAT;
	}

}
