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
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

public class MimeMessageHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(MimeMessageHelper.class);

    private enum MessageType {
                              TEXT,
                              HTML,
                              ICALENDAR
    };

    private final MimeMessage mimeMessage;
    private final Set<BodyPart> attachments = new LinkedHashSet<>();

    private String messageBody = "";
    private MessageType messageType = MessageType.TEXT;

    static {
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
        final MimetypesFileTypeMap mimetypes = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
        mimetypes.addMimeTypes("text/calendar ics ICS");
        final MailcapCommandMap mailcap = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
        mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mailcap.addMailcap("application/ics;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    }

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
                .ifPresent(a -> recipients.put("TO", Stream.of(a).map(InternetAddress.class::cast).map(InternetAddress::getAddress).collect(Collectors.toList())));

        Optional.ofNullable(mimeMessage.getRecipients(RecipientType.CC))
                .ifPresent(a -> recipients.put("CC", Stream.of(a).map(InternetAddress.class::cast).map(InternetAddress::getAddress).collect(Collectors.toList())));

        Optional.ofNullable(mimeMessage.getRecipients(RecipientType.BCC))
                .ifPresent(a -> recipients.put("BCC", Stream.of(a).map(InternetAddress.class::cast).map(InternetAddress::getAddress).collect(Collectors.toList())));

        return recipients;

    }

    public static Body getMessageBody(MimeMessage message) {

        return getMessageBody(message, "text/plain");
    }

    public static Body getMessageBody(MimeMessage message, String mimeType) {

        try {

            if (message.getContent() instanceof Multipart) {

                Multipart multipartMessage = (Multipart) message.getContent();

                for (int i = 0; i < multipartMessage.getCount(); i++) {
                    BodyPart bodyPart = multipartMessage.getBodyPart(i);

                    if (bodyPart.isMimeType(mimeType) && (Part.INLINE.equalsIgnoreCase(bodyPart.getDisposition()) || Objects.isNull(bodyPart.getDisposition()))) {

                        return new Body(bodyPart.getContentType(), bodyPart.getContent().toString());

                    }
                }
            } else {

                return new Body(message.getContentType(), message.getContent().toString());
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to read email body of type {}", mimeType, e);
        }

        return null;
    }

    public static CalendarBody getCalendarBody(MimeMessage message) {

        try {

            if (message.getContent() instanceof Multipart) {

                Multipart multipartMessage = (Multipart) message.getContent();
                for (int i = 0; i < multipartMessage.getCount(); i++) {
                    BodyPart bodyPart = multipartMessage.getBodyPart(i);
                    
                    if (bodyPart.isMimeType("text/calendar") || bodyPart.isMimeType("application/ics")) {
                        return new CalendarBody(bodyPart.getContent().toString());
                    }
                }
            } else {

                String content = message.getContent().toString();

                if (content.contains("BEGIN:VCALENDAR")) {
                    return new CalendarBody(content);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to read email body of type text/calendar", e);
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

            for (int i = 0; i < multipartMessage.getCount(); i++) {
                BodyPart bodyPart = multipartMessage.getBodyPart(i);

                if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                    byte[] content = IOUtils.toByteArray(bodyPart.getInputStream());
                    attachments.add(new Attachment(bodyPart.getContentType(), bodyPart.getFileName(), Base64.encodeBase64String(content)));
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to read email attachments ", e);
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

    public MimeMessageHelper setCalendarBody(Date start, Date end, String organizer, String summary, String description, String location) throws UnsupportedEncodingException {

        String eventStart = DateUtils.dateToString(start, "yyyyMMdd'T'HHmmss");
        String eventEnd = DateUtils.dateToString(start, "yyyyMMdd'T'HHmmss");

        return setCalendarBody(eventStart, eventEnd, organizer, summary, description, location);
    }

    public MimeMessageHelper setCalendarBody(String eventStart, String eventEnd, String organizer, String summary, String description, String location)
            throws UnsupportedEncodingException {

        messageBody = new StringBuilder("BEGIN:VCALENDAR").append("\n")
                                                          .append("METHOD:REQUEST")
                                                          .append("\n")
                                                          .append("VERSION:2.0")
                                                          .append("\n")
                                                          .append("BEGIN:VEVENT")
                                                          .append("\n")
                                                          .append("UID:")
                                                          .append(UUID.randomUUID().toString())
                                                          .append("\n")
                                                          .append("DTSTAMP:")
                                                          .append(eventStart)
                                                          .append("\n")
                                                          .append(CalendarBody.DTSTART).append(":")
                                                          .append(eventStart)
                                                          .append("\n")
                                                          .append(CalendarBody.DTEND).append(":")
                                                          .append(eventEnd)
                                                          .append("\n")
                                                          .append(CalendarBody.SUMMARY).append(":")
                                                          .append(summary)
                                                          .append("\n")
                                                          .append(CalendarBody.DESCRIPTION).append(":")
                                                          .append(description)
                                                          .append("\n")
                                                          .append(CalendarBody.LOCATION).append(":")
                                                          .append(TextUtils.urlEncode(location))
                                                          .append("\n")
                                                          .append(CalendarBody.ORGANIZER).append(":")
                                                          .append("MAILTO:")
                                                          .append(organizer)
                                                          .append("\n")
                                                          .append("END:VEVENT")
                                                          .append("\n")
                                                          .append("END:VCALENDAR")
                                                          .append("\n")
                                                          .toString();

        messageType = MessageType.ICALENDAR;

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
                mimeMessage.addHeader("Content-Transfer-Encoding", "quoted-printable");
                break;
            case ICALENDAR:
                mimeMessage.setContent(messageBody, "text/calendar; method=REQUEST; charset=UTF-8");
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
                mimeMessage.addHeader("Content-Transfer-Encoding", "quoted-printable");
                break;

            case ICALENDAR:
                mimeMessage.setContent(messageBody, "text/calendar; method=REQUEST; charset=UTF-8");
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
