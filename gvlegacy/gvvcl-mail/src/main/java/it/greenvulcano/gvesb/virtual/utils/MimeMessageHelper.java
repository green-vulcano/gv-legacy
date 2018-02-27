package it.greenvulcano.gvesb.virtual.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;

public class MimeMessageHelper {

	private enum MessageType {TEXT, HTML};

	private final MimeMessage mimeMessage;	
	private final List<String> attachments = new LinkedList<>();

	private String messageBody = "";
	private MessageType messageType = MessageType.TEXT;

	private MimeMessageHelper(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}

	public static MimeMessageHelper createEmailMessage(String from, String to) throws AddressException, MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		return new MimeMessageHelper(email);
	}
	
	public MimeMessageHelper setSubject(String subject) throws MessagingException {
		mimeMessage.setSubject(subject);
		return this;
	}

	public MimeMessageHelper addTo(String to) throws AddressException, MessagingException {
		mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		return this;
	}

	public MimeMessageHelper addCC(String cc) throws AddressException, MessagingException {
		mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
		return this;
	}

	public MimeMessageHelper addBCC(String bcc) throws AddressException, MessagingException {
		mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
		return this;
	}

	public MimeMessageHelper setTextBody(String body) throws MessagingException {
		messageBody = body;
		messageType = MessageType.TEXT;
		return this;
	}

	public MimeMessageHelper setHtmlBody(String body) throws MessagingException {
		messageBody = body;
		messageType = MessageType.HTML;
		return this;
	}

	public MimeMessageHelper addAttachment(String filePath) {
		attachments.add(filePath);
		return this;		
	}

	public MimeMessage getMimeMessage() throws MessagingException {

		if (attachments.isEmpty()) {
			switch (messageType) {
			case HTML:
				mimeMessage.setContent(messageBody, "text/html; charset=utf-8");	
				break;

			case TEXT:
				mimeMessage.setText(messageBody, "UTF-8");
				break;
			}
		} else {

			MimeBodyPart mimeBodyPart = new MimeBodyPart();

			switch (messageType) {
			case HTML:
				mimeBodyPart.setContent(messageBody, "text/html; charset=utf-8");	
				break;

			case TEXT:
				mimeBodyPart.setText(messageBody, "UTF-8");
				break;
			}


			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			for (String filePath : attachments) {
				File file = new File(filePath);

				mimeBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(file);

				mimeBodyPart.setDataHandler(new DataHandler(source));
				mimeBodyPart.setFileName(file.getName());

				multipart.addBodyPart(mimeBodyPart);
			}

			mimeMessage.setContent(multipart);
		}

		return mimeMessage;
	}

	public String getEncodedMimeMessage() throws IOException, MessagingException {

		ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
		getMimeMessage().writeTo(buffer);

		String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());

		return encodedEmail;
	}
}
