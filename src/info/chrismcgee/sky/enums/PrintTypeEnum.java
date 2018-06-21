package info.chrismcgee.sky.enums;

public enum PrintTypeEnum {
	SCREEN_CUPS("Screen Cups"), SCREEN_NAPKINS("Screen Napkins"), PAD("Pad"),
	HOTSTAMP("Hotstamp"), OFFSET_CUPS("Offset Cups"), OFFSET_NAPKINS("Offset Napkins"),
	DIGITAL_CUPS("Digital Cups"), DIGITAL_OTHER("Digital Other"), OUTSOURCED("Outsourced"),
	DIGITAL_NAPKINS("Digital Napkins");

	private String value;
	
	private PrintTypeEnum(String value)
	{
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public static PrintTypeEnum getPrintType (String x)
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
			case "Digital Other": return DIGITAL_OTHER;
			case "Digital Napkins": return DIGITAL_NAPKINS;
			case "Outsourced": return OUTSOURCED;
			default: return null;
		}
	}
	
	public static PrintTypeEnum getPrintType (int x)
	{
		switch (x)
		{
			case 0: return SCREEN_CUPS;
			case 1: return PAD;
			case 2: return HOTSTAMP;
			case 3: return OFFSET_CUPS;
			case 4: return OFFSET_NAPKINS;
			case 5: return DIGITAL_OTHER;
			case 6: return SCREEN_NAPKINS;
			case 7: return OUTSOURCED;
			case 8: return DIGITAL_CUPS;
			case 9: return DIGITAL_NAPKINS;
			default: return PAD;
		}
	}

	public static int getIntValue(PrintTypeEnum printType)
	{
		switch (printType)
		{
			case SCREEN_CUPS: return 0;
			case PAD: return 1;
			case HOTSTAMP: return 2;
			case OFFSET_CUPS: return 3;
			case OFFSET_NAPKINS: return 4;
			case DIGITAL_OTHER: return 5;
			case SCREEN_NAPKINS: return 6;
			case OUTSOURCED: return 7;
			case DIGITAL_CUPS: return 8;
			case DIGITAL_NAPKINS: return 9;
			default: return 1;
		}
	}
	
	public static String getSqlValue(PrintTypeEnum printType) {
		
		switch (printType) {
			case SCREEN_CUPS: return "screen_cups";
			case PAD: return "pad";
			case HOTSTAMP: return "hotstamp";
			case OFFSET_CUPS: return "offset_cups";
			case OFFSET_NAPKINS: return "offset_napkins";
			case DIGITAL_CUPS: return "digital_cups";
			case DIGITAL_OTHER: return "digital_other";
			case DIGITAL_NAPKINS: return "digital_napkins";
			case SCREEN_NAPKINS: return "screen_napkins";
			case OUTSOURCED: return "outsourced";
			default: return "pad";
		}
	}

};
