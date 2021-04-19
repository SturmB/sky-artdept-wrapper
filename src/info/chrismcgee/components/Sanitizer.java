package info.chrismcgee.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sanitizer {

    private static final Pattern orderNumPattern = Pattern.compile("\\b(\\d{6,}|P\\d{5,})\\b", Pattern.CASE_INSENSITIVE);

    public static boolean checkOrderNum(String orderNum) {
        Matcher orderNumMatcher = orderNumPattern.matcher(orderNum);
        return orderNumMatcher.lookingAt();
    }

    public static boolean isNotEmpty(String initials) {
        return initials.length() > 0;
    }

    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        return email.matches(EMAIL_PATTERN);
    }

}
