package info.chrismcgee.enums;

public enum OSType {
	WINDOWS, MAC, UNIX, SOLARIS;
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static OSType getOSType () {
		return getOS();
	}

	private static OSType getOS() {
		
		if (isWindows())
			return WINDOWS;
		else if (isMac())
			return MAC;
		else if (isUnix())
			return UNIX;
		else if (isSolaris())
			return SOLARIS;
		else
			return null;
	}
	
	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
	
	private static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
	
	private static boolean isUnix() {
		return (OS.indexOf("nix") >= 0
				|| OS.indexOf("nux") >= 0
				|| OS.indexOf("aix") >= 0);
	}
	
	private static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}
	
}
