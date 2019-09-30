package info.chrismcgee.sky.artdept;

import info.chrismcgee.util.SendMail;

public class MailTest {

	public static void main(String[] args) {
		SendMail.send("skyartdept@main.skyunlimitedinc.com",
				"itdept@skyunlimitedinc.com",
				"Test",
				"This is a test to see if the email works.");
		System.out.println("Email sent?");
	}

}
