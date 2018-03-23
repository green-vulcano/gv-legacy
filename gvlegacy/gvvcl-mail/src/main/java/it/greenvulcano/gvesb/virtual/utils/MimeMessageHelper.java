/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class MimeMessageHelper {

	private enum MessageType {TEXT, HTML};

	private final MimeMessage mimeMessage;	
	private final Set<BodyPart> attachments = new LinkedHashSet<>();

	private String messageBody = "";
	private MessageType messageType = MessageType.TEXT;

	private MimeMessageHelper(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}
	
	public static MimeMessage decode(byte[] message) throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(message));
		
		return email;
	}
	
	public static MimeMessage decode(String message) throws MessagingException {
		return decode(Base64.decodeBase64(message));
	}
	
	public static Map<String, List<String>> getMessageRecipients(byte[] mimeMessage) throws MessagingException {
		return getMessageRecipients(decode(mimeMessage));
	}
	
	public static Map<String, List<String>> getMessageRecipients(String mimeMessage) throws MessagingException {
		return getMessageRecipients(decode(mimeMessage));
	}	
	
	public static Map<String, List<String>> getMessageRecipients(MimeMessage mimeMessage) throws MessagingException {
		
		
		Map<String, List<String>> recipients = new LinkedHashMap<>();
		
		Optional.ofNullable(mimeMessage.getRecipients(RecipientType.TO))
		        .ifPresent(a -> recipients.put("TO", Stream.of(a).map(Address::toString).collect(Collectors.toList())));
		
		Optional.ofNullable(mimeMessage.getRecipients(RecipientType.CC))
        .ifPresent(a -> recipients.put("CC", Stream.of(a).map(Address::toString).collect(Collectors.toList())));
		
		Optional.ofNullable(mimeMessage.getRecipients(RecipientType.BCC))
        .ifPresent(a -> recipients.put("BCC", Stream.of(a).map(Address::toString).collect(Collectors.toList())));
		
		return recipients;
		
	}
	
	
	public static Body getMessageBody(MimeMessage message) {
		try {
			
			Multipart multipartMessage = (Multipart) message.getContent();
			
			for (int i = 0; i< multipartMessage.getCount(); i++ ) {
				BodyPart bodyPart = multipartMessage.getBodyPart(i);
				
				if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase(Part.INLINE)) {					
					
					return new Body(bodyPart.getContentType(), bodyPart.getContent().toString());
									
				}
			}
			
		} catch (Exception e) {
			// do nothing
		}
		
		return null;
	}
		
	public static List<Attachment> getMessageAttachments(String message) {
		try {
			return getMessageAttachments(decode(message));
		} catch (Exception e) {
			return new LinkedList<>();
		}
		
		
	}
	
	public static List<Attachment> getMessageAttachments(byte[] message) {
		try {
			return getMessageAttachments(decode(message));
		} catch (Exception e) {
			return new LinkedList<>();
		}
		
	}
	
	
	public static List<Attachment> getMessageAttachments(MimeMessage message) {
		
		List<Attachment> attachments = new LinkedList<>();
		
		try {
			
			Multipart multipartMessage = (Multipart) message.getContent();
			
			for (int i = 0; i< multipartMessage.getCount(); i++ ) {
				BodyPart bodyPart = multipartMessage.getBodyPart(i);
				
				if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {					
					byte[] content = IOUtils.toByteArray(bodyPart.getInputStream());					
					attachments.add(new Attachment(bodyPart.getContentType(), bodyPart.getFileName(), Base64.encodeBase64String(content)));				
				}
			}
			
		} catch (Exception e) {
			// do nothing
		}
		
		return attachments;
		
		
	}

	public static MimeMessageHelper createEmailMessage(String from, String to) throws AddressException, MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		for (String recipient : to.split(";")) {
			email.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}

		return new MimeMessageHelper(email);
	}	
	
	
	public MimeMessageHelper setSubject(String subject) throws MessagingException {
		mimeMessage.setSubject(subject);
		return this;
	}

	public MimeMessageHelper addTo(String to) throws AddressException, MessagingException {
		for (String recipient : to.split(";")) {
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}		
		return this;
	}

	public MimeMessageHelper addCC(String cc) throws AddressException, MessagingException {
		for (String recipient : cc.split(";")) {
			mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(recipient));
		}
		return this;
	}

	public MimeMessageHelper addBCC(String bcc) throws AddressException, MessagingException {
		for (String recipient : bcc.split(";")) {
			mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
		}
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

	public MimeMessageHelper addAttachment(String name, String type, String content) throws MessagingException {
		return addAttachment(name, type, Base64.decodeBase64(content));	
	}
	
    public MimeMessageHelper addAttachment(String name, String type, byte[] content) throws MessagingException {
		
		BodyPart attachmentPart = new MimeBodyPart();
		ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(content, type);
		
		attachmentPart.setDataHandler(new DataHandler(byteArrayDataSource));
		attachmentPart.setFileName(name);
		
		attachmentPart.setDisposition(Part.ATTACHMENT);		
		attachments.add(attachmentPart);
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
				mimeMessage.addHeader("Content-Transfer-Encoding",	"quoted-printable");
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
				mimeMessage.addHeader("Content-Transfer-Encoding",	"quoted-printable");
				break;
			}
			
			mimeBodyPart.setDisposition(Part.INLINE);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			for (BodyPart attachmentPart : attachments) {				
				multipart.addBodyPart(attachmentPart);
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
	
	public static class Body {
		private final String contentType, content;

		public Body(String contentType, String content) {			
			this.contentType = contentType;
			this.content = content;
		}

		public String getContentType() {
			return contentType;
		}

		public String getContent() {
			return content;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Body other = (Body) obj;
			if (content == null) {
				if (other.content != null)
					return false;
			} else if (!content.equals(other.content))
				return false;
			if (contentType == null) {
				if (other.contentType != null)
					return false;
			} else if (!contentType.equals(other.contentType))
				return false;
			return true;
		}
		
		
		
	}
	
	public static class Attachment {
		
		private final String contentType, fileName, content;

		public Attachment(String contentType, String fileName, String content) {			
			this.contentType = contentType;
			this.fileName = fileName;
			this.content = content;
		}

		public String getContentType() {
			return contentType;
		}

		public String getFileName() {
			return fileName;
		}

		public String getContent() {
			return content;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
			result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Attachment other = (Attachment) obj;
			if (content == null) {
				if (other.content != null)
					return false;
			} else if (!content.equals(other.content))
				return false;
			if (contentType == null) {
				if (other.contentType != null)
					return false;
			} else if (!contentType.equals(other.contentType))
				return false;
			if (fileName == null) {
				if (other.fileName != null)
					return false;
			} else if (!fileName.equals(other.fileName))
				return false;
			return true;
		}
		
		
		
	}
}
