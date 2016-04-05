package info.chrismcgee.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sanitizer {

	private static Pattern orderNumPattern = Pattern.compile("\\b(\\d{6,}|P\\d{5,})\\b", Pattern.CASE_INSENSITIVE);
	private static Matcher orderNumMatcher;

	public static boolean checkOrderNum (String orderNum)
	{
		orderNumMatcher = orderNumPattern.matcher(orderNum);
		if (orderNumMatcher.lookingAt())
			return true;
		return false;
	}

	public static boolean checkInitials (String initials)
	{
		if (initials.length() > 0) return true;
		return false;
	}
	
}
