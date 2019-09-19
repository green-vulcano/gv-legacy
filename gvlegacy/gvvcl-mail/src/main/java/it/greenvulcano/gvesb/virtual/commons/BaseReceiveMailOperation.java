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

package it.greenvulcano.gvesb.virtual.commons;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

public abstract class BaseReceiveMailOperation extends BaseMailOperation {

    private static final Logger logger = LoggerFactory.getLogger(BaseReceiveMailOperation.class);

    protected String loginUser = null;
    protected String loginPassword = null;
    protected String serverHost = null;

    protected String mbox = "INBOX";
    protected boolean delete_messages = false;
    protected boolean expunge = false;
    protected boolean exportEML = false;
    protected boolean validateOnly = false;
    protected Store store = null;

    /**
     * The emails cleaner pattern
     */
    protected Pattern emailRxPattern = null;
    protected int maxReadMessages = -1;

    /**
     * Preliminary initialization operations
     * 
     * @param node
     * The configuration node containing all informations.
     * @return Session
     * The session
     * 
     */
    protected Session preInit(Node node) throws InitializationException {

        try {

            mbox = XMLConfig.get(node, "@folder", "INBOX");
            logger.debug("Messages folder: " + mbox);

            delete_messages = XMLConfig.getBoolean(node, "@delete-messages", false);
            expunge = XMLConfig.getBoolean(node, "@expunge", false);

            exportEML = XMLConfig.getBoolean(node, "@export-EML", false);

            maxReadMessages = XMLConfig.getInteger(node, "@max-read-messages", 10);

            String regex = XMLConfig.get(node, "@email-rx-cleaner", "[A-z][A-z0-9_\\-]*([.][A-z0-9_\\-]+)*[@][A-z0-9_\\-]+([.][A-z0-9_\\-]+)*[.][A-z]{2,4}");
            emailRxPattern = Pattern.compile(regex);

            Session session = super.preInit(node);

            if (!dynamicServer) {
                store = session.getStore(getProtocol());
            }

            return session;
        } catch (Exception exc) {
            logger.error("Error initializing call operation", exc);
            throw new InitializationException("GVVCL_RCV_MAIL_INIT_ERROR", new String[][] { { "node", node.getLocalName() } }, exc);
        }
    }

    /**
     * Abstract method for retrieve the protocol
     * 
     * @return String
     * The protocol
     */
    protected abstract String getProtocol();

    /**
     * 
     * @param gvBuffer
     * The GVBuffer to be used within the service
     * @return the GVBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {

        Store localStore = null;
        try  {

            localStore = getStore(gvBuffer);
            if (performLogin) {
                localStore.connect(serverHost, loginUser, loginPassword);
            } else {
                localStore.connect();
            }

            if (validateOnly || Boolean.valueOf(gvBuffer.getProperty("VALIDATE_MAIL_PROPERTIES"))) {

                Folder folder = localStore.getDefaultFolder();
                if (folder == null) {
                    logger.error("No default folder");
                    throw new Exception("No default folder");
                }
                
                folder = folder.getFolder(mbox);
                if (folder == null) {
                    logger.error("Invalid folder " + mbox);
                    throw new Exception("Invalid folder " + mbox);
                }

                return gvBuffer;
            }

            return receiveMails(localStore, gvBuffer);
        } catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                                    new String[][] { { "service", gvBuffer.getService() },
                                                     { "system", gvBuffer.getSystem() },
                                                     { "id", gvBuffer.getId().toString() },
                                                     { "message", exc.getMessage() } },
                                    exc);
        } finally {
            if (localStore != null) {
                try {
                    localStore.close();
                } catch (MessagingException e) {
                    logger.error("Error closing mail Store", e);
                }
            }
        }
    }

    /**
     * Abstract method for receiving mails
     * 
     * @param data
     * The GVBuffer to be used within the service
     * @return the GVBuffer
     * @throws Exception
     */
    protected abstract GVBuffer receiveMails(Store locStore, GVBuffer data) throws Exception;

    /**
     * 
     * @param locStore
     * javax.mail.Store
     * @param data
     * the GVBuffer
     * @throws Exception
     */
    protected abstract void postStore(Store locStore, GVBuffer data) throws Exception;

    /**
     * Retrieve information about Store
     * 
     * @param data
     * @return Store
     * @throws Exception
     */
    protected Store getStore(GVBuffer data) throws Exception {

        if (!dynamicServer) {
            postStore(store, data);
            return store;
        }

        loginUser = null;
        loginPassword = null;
        serverHost = null;

        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(data, true);

            Properties localProps = new Properties();
            for (Object k : serverProps.keySet()) {
                String name = (String) k;
                String value = PropertiesHandler.expand(serverProps.getProperty(name), params, data);
                if (name.contains(".host")) {
                    logger.debug("Logging-in to host: " + value);
                    serverHost = value;
                } else if (name.contains(".user")) {
                    logger.debug("Logging-in as user: " + value);
                    loginUser = value;
                } else if (name.contains(".password")) {
                    value = XMLConfig.getDecrypted(value);

                    loginPassword = value;
                }
                localProps.setProperty(name, value);
            }

            Session session = Session.getInstance(localProps, null);

            if (session == null) {
                throw new CallException("GVVCL_RCV_MAIL_NO_SESSION", new String[][] { { "properties", "" + localProps } });
            }

            Store locStore = session.getStore(getProtocol());

            postStore(locStore, data);

            return locStore;
        } catch (CallException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new CallException("GVVCL_RCV_MAIL_SESSION_ERROR", new String[][] { { "message", exc.getMessage() } }, exc);
        } finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    /**
     * Generate dump Part of message
     * 
     * @param p
     * @param msg
     * @param xml
     * @throws Exception
     */
    protected void dumpPart(Part p, Element msg, XMLUtils xml) throws Exception {

        if (p instanceof Message) {
            dumpEnvelope((Message) p, msg, xml);
        }

        Element content = null;
        String filename = p.getFileName();
        if (p.isMimeType("text/plain") && (filename == null)) {
            content = xml.insertElement(msg, "PlainMessage");
            xml.insertText(content, (String) p.getContent());
        } else if (p.isMimeType("text/html") && (filename == null)) {
            content = xml.insertElement(msg, "HTMLMessage");
            xml.insertCDATA(content, (String) p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            content = xml.insertElement(msg, "Multipart");
            for (int i = 0; i < count; i++) {
                dumpPart(mp.getBodyPart(i), content, xml);
            }
        } else if (p.isMimeType("message/rfc822")) {
            content = xml.insertElement(msg, "NestedMessage");
            dumpPart((Part) p.getContent(), content, xml);
        } else {
            content = xml.insertElement(msg, "EncodedContent");
            DataHandler dh = p.getDataHandler();
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            dh.writeTo(os);
            xml.insertText(content, Base64.getEncoder().encodeToString(os.toByteArray()));
            os.flush();
            os.close();
        }

        if (filename != null) {
            xml.setAttribute(content, "file-name", filename);
        }
        String ct = p.getContentType();
        if (ct != null) {
            xml.setAttribute(content, "content-type", ct);
        }
        String desc = p.getDescription();
        if (desc != null) {
            xml.setAttribute(content, "description", desc);
        }
    }

    /**
     * Execute dump for envelope
     * 
     * @param m
     * @param msg
     * @param xml
     * @throws Exception
     */
    private void dumpEnvelope(Message m, Element msg, XMLUtils xml) throws Exception {

        dumpSR(m.getFrom(), msg, "From", xml);
        dumpSR(m.getRecipients(RecipientType.TO), msg, "To", xml);
        dumpSR(m.getRecipients(RecipientType.CC), msg, "Cc", xml);
        dumpSR(m.getRecipients(RecipientType.BCC), msg, "Bcc", xml);
        dumpSR(m.getReplyTo(), msg, "ReplyTo", xml);
        Element headers = xml.insertElement(msg, "Headers");
        Enumeration<?> hEnum = m.getAllHeaders();
        while (hEnum.hasMoreElements()) {
            Header h = (Header) hEnum.nextElement();
            Element el = xml.insertElement(headers, h.getName());
            xml.insertText(el, h.getValue());
        }
        Element subject = xml.insertElement(msg, "Subject");
        xml.insertText(subject, m.getSubject());
    }

    /**
     * 
     * @param addr
     * @param msg
     * @param container
     * @param xml
     * @throws Exception
     */
    private void dumpSR(Address[] addr, Element msg, String container, XMLUtils xml) throws Exception {

        Element cont = xml.insertElement(msg, container);
        Matcher mtc = emailRxPattern.matcher("");

        String list = "";
        if (addr != null) {
            for (Address address : addr) {
                mtc.reset(address.toString());
                while (mtc.find()) {
                    list += mtc.group() + " ";
                }
            }
        }
        xml.insertText(cont, list.trim());
    }

}
