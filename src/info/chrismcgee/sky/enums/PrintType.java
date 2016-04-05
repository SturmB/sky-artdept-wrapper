package info.chrismcgee.sky.enums;

public enum PrintType {
	SCREEN_CUPS("Screen Cups"), SCREEN_NAPKINS("Screen Napkins"), PAD("Pad"),
	HOTSTAMP("Hotstamp"), OFFSET_CUPS("Offset Cups"), OFFSET_NAPKINS("Offset Napkins"),
	DIGITAL("Digital"), OUTSOURCED("Outsourced");

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
			case "Digital": return DIGITAL;
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
			case 5: return DIGITAL;
			case 6: return SCREEN_NAPKINS;
			case 7: return OUTSOURCED;
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
			case DIGITAL: return 5;
			case SCREEN_NAPKINS: return 6;
			case OUTSOURCED: return 7;
			default: return 1;
		}
	}

};
