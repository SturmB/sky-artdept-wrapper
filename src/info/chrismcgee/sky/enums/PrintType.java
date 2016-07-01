package info.chrismcgee.sky.enums;

public enum PrintType {
	SCREEN_CUPS("Screen Cups"), SCREEN_NAPKINS("Screen Napkins"), PAD("Pad"),
	HOTSTAMP("Hotstamp"), OFFSET_CUPS("Offset Cups"), OFFSET_NAPKINS("Offset Napkins"),
	DIGITAL_CUPS("Digital Cups"), DIGITAL_FLATS("Digital Flats"), OUTSOURCED("Outsourced");

	private String value;
	
	private PrintType(String value)
	{
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public static PrintType getPrintType (String x)
	{
		switch (x)
		{
			case "Screen Cups": return SCREEN_CUPS;
			case "Screen Napkins": return SCREEN_NAPKINS;
			case "Pad": return PAD;
			case "Hotstamp": return HOTSTAMP;
			case "Offset Cups": return OFFSET_CUPS;
			case "Offset Napkins": return OFFSET_NAPKINS;
			case "Digital Cups": return DIGITAL_CUPS;
			case "Digital Flats": return DIGITAL_FLATS;
			case "Outsourced": return OUTSOURCED;
			default: return null;
		}
	}
	
	public static PrintType getPrintType (int x)
	{
		switch (x)
		{
			case 0: return SCREEN_CUPS;
			case 1: return PAD;
			case 2: return HOTSTAMP;
			case 3: return OFFSET_CUPS;
			case 4: return OFFSET_NAPKINS;
			case 5: return DIGITAL_FLATS;
			case 6: return SCREEN_NAPKINS;
			case 7: return OUTSOURCED;
			case 8: return DIGITAL_CUPS;
			default: return PAD;
		}
	}

	public static int getIntValue(PrintType printType)
	{
		switch (printType)
		{
			case SCREEN_CUPS: return 0;
			case PAD: return 1;
			case HOTSTAMP: return 2;
			case OFFSET_CUPS: return 3;
			case OFFSET_NAPKINS: return 4;
			case DIGITAL_FLATS: return 5;
			case SCREEN_NAPKINS: return 6;
			case OUTSOURCED: return 7;
			case DIGITAL_CUPS: return 8;
			default: return 1;
		}
	}
	
	public static String getSqlAvailValue(PrintType printType) {
		
		switch (printType) {
			case SCREEN_CUPS: return "avail_screen";
			case PAD: return "avail_pad";
			case HOTSTAMP: return "avail_hotstamp";
			case OFFSET_CUPS: return "avail_offset_cups";
			case OFFSET_NAPKINS: return "avail_offset_naps";
			case DIGITAL_CUPS: return "avail_digital_cups";
			case DIGITAL_FLATS: return "avail_digital_flats";
			case SCREEN_NAPKINS: return "avail_screen_naps";
			case OUTSOURCED: return "avail_outsourced";
			default: return "avail_pad";
		}
	}

};
