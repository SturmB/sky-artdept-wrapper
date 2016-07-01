package info.chrismcgee.util;

import java.util.Properties;

//import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SendMail {
	
	static final Logger log = LogManager.getLogger(SendMail.class.getName()); // For logging.

	public static boolean send(String from, String to, String subject, String body) {
		
		// Recipient's email ID needs to be mentioned.
//		to = "christophermcgee@mainserver.com"; // For testing.
		
		// Sender's email ID needs to be mentioned.
//		from = "java@mainserver.com"; // For testing.
		
		// Authentication is not working with MS Exchange, so it has been removed.
//		final String username = "christophermcgee@mainserver.com";
//		final String password = "sky241";
		
		// Email server host address.
		String host = "mail.skyunlimitedinc.com";
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "false");
//		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "25");
		
		// Get the Session object.
/*		Session session = Session.getDefaultInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});*/
		Session session = Session.getInstance(props);
		
		try {
			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);
			
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));
			
			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			
			// Set Subject: header field
			message.setSubject(subject);
//			message.setSubject("Testing Subject"); // For testing.
			
			// Now set the actual message
			message.setText(body);
//			message.setText("Hello, this is sample for to check send email using JavaMailAPI"); // For testing.
			
			// Send message
			Transport.send(message);
			
//			System.out.println("Sent message successfully....");
			log.debug("Sent message successfully....");
		
		} catch (MessagingException e) {
			log.error("Could not send email message.", e);
			return false;
		}
		
		return true;
	}

}
