package info.chrismcgee.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;

import org.joda.time.LocalDate;

public class InputHelper {

	public static String getInput(String prompt) {
		
		BufferedReader stdin = new BufferedReader(
				new InputStreamReader(System.in));
		
		System.out.print(prompt);
		System.out.flush();
		
		try {
			return stdin.readLine();
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	
	public static double getDoubleInput(String prompt) throws NumberFormatException {
		
		String input = getInput(prompt);
		return Double.parseDouble(input);
		
	}
	
	public static Date getDateInput(String prompt) throws ParseException {
		
		String input = getInput(prompt);
		LocalDate ld = LocalDate.parse(input);
		return Date.valueOf(ld.toString());
	}
	
}
