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

import java.util.Properties;

import javax.mail.Session;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;

public abstract class BaseMailOperation implements CallOperation {

    private static final Logger logger          = LoggerFactory.getLogger(BaseMailOperation.class);

    /**
     * The configured operation key
     */
    protected OperationKey      key             = null;
    
    /**
     * Mail session JNDI name.
     */
    protected String              jndiName        = null;

    /**
     * The protocol-specific default Mail server. This overrides the mail.host
     * property.
     */
    protected String              protocolHost    = null;

    /**
     * The protocol-specific default user name for connecting to the Mail
     * server. This overrides the mail.user property.
     */
    protected String              protocolUser    = null;
    protected boolean             dynamicServer   = false;
    protected Properties          serverProps     = null;
    protected boolean             performLogin    = false;

    /**
     * Preliminary initialization operations
     * 
     * @param node
     * 			The configuration node containing all informations.
     * @return Session
     * 
     */
    protected Session preInit(Node node) throws InitializationException {
        JNDIHelper initialContext = null;
        
        try {
            jndiName = XMLConfig.get(node, "@jndi-name");
            if (jndiName != null) {
                logger.debug("JNDI name: " + jndiName);
            }

            protocolHost = XMLConfig.get(node, "@override-protocol-host");
            if (protocolHost != null) {
                logger.debug("Override protocol host: " + protocolHost);
            }

            protocolUser = XMLConfig.get(node, "@override-protocol-user");
            if (protocolUser != null) {
                logger.debug("Override protocol user: " + protocolUser);
            }

            Session session = null;
            if (jndiName != null) {
                initialContext = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
                session = (Session) initialContext.lookup(jndiName);
            }

            NodeList nodeList = XMLConfig.getNodeList(node, "mail-properties/mail-property");
            if (nodeList != null && nodeList.getLength() > 0) {
                serverProps = new Properties();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node property = nodeList.item(i);
                    String name = XMLConfig.get(property, "@name");
                    String value = XMLConfig.get(property, "@value");
                    
                    if (name.contains(".password")) {
                        performLogin = true;
                    }
                    if (PropertiesHandler.isExpanded(value)) {
                        dynamicServer = true;
                    }
                    serverProps.setProperty(name, value);
                    
                }
            }

            if ((protocolHost != null) || (protocolUser != null)) {
                if (serverProps == null) {
                    serverProps = session.getProperties();
                }
                if (protocolHost != null) {
                    serverProps.setProperty("mail.protocol.host", protocolHost);
                }
                if (protocolUser != null) {
                    serverProps.setProperty("mail.protocol.user", protocolUser);
                }
            }

            if (!dynamicServer) {
                if (serverProps != null) {
                    session = Session.getDefaultInstance(serverProps, null);
                }

                if (session == null) {
                    throw new InitializationException("GVVCL_MAIL_NO_SESSION", new String[][]{{"node", node.getLocalName()}});
                }
            }

            return session;
        }
        catch (Exception exc) {
            logger.error("Error initializing IMAP call operation", exc);
            throw new InitializationException("GVVCL_MAIL_INIT_ERROR", new String[][]{{"node", node.getLocalName()}},
                    exc);
        }
        finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                }
                catch (NamingException exc) {
                    logger.error("An error occurred while closing InitialContext.", exc);
                }
            }
        }

    }
    
    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     * Set the key
     * 
     * @param key
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * Return the key
     * 
     * @return OperationKey
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

}
