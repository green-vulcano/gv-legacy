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

package it.greenvulcano.gvesb.virtual.imap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.SortTerm;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.commons.BaseReceiveMailOperation;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * Check for emails on IMAP server.
 *
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 *
 *
 */
public class IMAPCallOperation extends BaseReceiveMailOperation
{

    private static final Logger logger        = LoggerFactory.getLogger(IMAPCallOperation.class);
   
    private String              sortField     = null;
    private boolean             sortAscending = false;
    private List<SortTerm>      sortingTerms  = new ArrayList<SortTerm>();
    
    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @param node
     * 			The configuration node containing all informations.
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
            preInit(node);

            sortField = XMLConfig.get(node, "@sort-field", "");
            //logger.info("---@sort-field: " + sortField);

            sortAscending = XMLConfig.getBoolean(node, "@sort-ascending", false);

            if (!sortAscending) {
                sortingTerms.add(SortTerm.REVERSE);
            }
            if (sortField.equals("CC")){
                sortingTerms.add(SortTerm.CC);
            } else if (sortField.equals("DATE")){
                sortingTerms.add(SortTerm.DATE);
            } else if (sortField.equals("FROM")){
                sortingTerms.add(SortTerm.FROM);
            } else if (sortField.equals("SIZE")){
                sortingTerms.add(SortTerm.SIZE);
            } else if (sortField.equals("SUBJECT")){
                sortingTerms.add(SortTerm.SUBJECT);
            } else if (sortField.equals("TO")){
                sortingTerms.add(SortTerm.TO);
            } else {
                //The default value
                sortingTerms.add(SortTerm.ARRIVAL);
            }
            
            //logger.info("sortingTerms: " + sortingTerms.toString());
        }
        catch (Exception exc) {
            logger.error("Error initializing IMAP call operation", exc);
            throw new InitializationException("GVVCL_IMAP_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
    }

    /**
     * Return the protocol
     * 
     * @return String
     */
    @Override
    protected String getProtocol() {
        return Optional.ofNullable(serverProps)
                .orElseGet(Properties::new)
                .getProperty("mail.store.protocol", "imap");
    }
    
    /**
     * 
     * @param locStore
     * @param data
     */
    @Override
    protected void postStore(Store locStore, GVBuffer data) throws Exception {
        // do nothing
    }
    
    /**
     * Receives e-mails.
     *
     * @param data
     *        the input GVBuffer.
     * @return the GVBuffer.
     * @throws Exception
     * @throws Exception
     * 
     */
    protected GVBuffer receiveMails(GVBuffer data) throws Exception
    {
        Store localStore = getStore(data);
        if (performLogin) {
            localStore.connect(serverHost, loginUser, loginPassword);
        }
        else {
            localStore.connect();
        }

        boolean sortable = false;
        if (localStore instanceof IMAPStore){
            //Test if sortable store
            if (((IMAPStore) localStore).hasCapability("SORT")){
                sortable = true;
            } else {
                logger.debug("UNSORTABLE Store");
            }
        } else {
            //Store is NOT sortable
            //sortable = false;
            logger.debug("UNSORTABLE Store");
        }
        XMLUtils xml = null;
        try {
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

            try {
                folder.open(Folder.READ_WRITE);
            }
            catch (MessagingException ex) {
                folder.open(Folder.READ_ONLY);
            }
            int totalMessages = folder.getMessageCount();
            int messageCount = 0;

            if (totalMessages == 0) {
                logger.debug("Empty folder " + mbox);
            }
            else {
                List<Message> seen = new ArrayList<Message>();
                Message[] msgs;
                Flags f = new Flags();
                f.add(Flag.SEEN);
                if (sortable){
                    SearchTerm onlyUnread = new FlagTerm(new Flags(f), false);
                    SortTerm[] terms = sortingTerms.toArray(new SortTerm[sortingTerms.size()]);
                    msgs = ((IMAPFolder)folder).getSortedMessages(terms, onlyUnread);
                } else {
                    msgs = folder.search(new FlagTerm(f, false));
                }
                
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add("X-Mailer");
                folder.fetch(msgs, fp);
                
                xml = XMLUtils.getParserInstance();
                Document doc = xml.newDocument("MailMessages");
                for (int i = 0; i < msgs.length; i++) {
                    if ((maxReadMessages != -1) && (messageCount == maxReadMessages)) {
                        break;
                    }
                    Element msg = xml.insertElement(doc.getDocumentElement(), "Message");
                    dumpPart(msgs[i], msg, xml);
                    if (exportEML) {
                        Element eml = xml.insertElement(msg, "EML");
                        xml.setAttribute(eml, "encoding", "base64");
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        
                        msgs[i].writeTo(os);
                        
                        xml.insertText(eml, Base64.getEncoder().encodeToString(os.toByteArray()));
                        os.flush();
                        os.close();
                    }
                    
                    msgs[i].setFlag(Flags.Flag.SEEN, true);
                    seen.add(msgs[i]);
                    messageCount++;
                }
                if (messageCount > 0) {
                    data.setObject(doc);
                }

                if (delete_messages) {
                    folder.setFlags(seen.toArray(new Message[seen.size()]), new Flags(Flags.Flag.DELETED), true);
                }
            }
            data.setRetCode(0);
            data.setProperty("IMAP_MESSAGE_COUNT", String.valueOf(messageCount));
            folder.close(expunge);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
            if (localStore != null) {
                localStore.close();
            }
        }

        return data;
    }
}
