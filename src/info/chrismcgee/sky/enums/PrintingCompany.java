package info.chrismcgee.sky.enums;

public enum PrintingCompany {
	AMERICAN_ACCENTS(0), AMERICAN_CABIN_SUPPLY(1), AMERICAN_YACHT_SUPPLY(2);
	private int value;
	// Use this line below in a class that uses this enum. (if the Switch method isn't used anymore.)
//	private final PrintingCompany[] printingCompanyValues = PrintingCompany.values();
	
	private PrintingCompany(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public static PrintingCompany getPrintingCompany (int x)
	{
		switch (x)
		{
			case 1: return AMERICAN_CABIN_SUPPLY;
			case 2: return AMERICAN_YACHT_SUPPLY;
			default: return AMERICAN_ACCENTS;
		}
	}
}
