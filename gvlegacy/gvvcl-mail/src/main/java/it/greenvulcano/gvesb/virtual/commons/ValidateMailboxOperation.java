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

import java.util.Optional;
import java.util.Properties;

import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.InitializationException;

public class ValidateMailboxOperation extends BaseReceiveMailOperation {

    private static final Logger logger = LoggerFactory.getLogger(ValidateMailboxOperation.class);

    @Override
    public void init(Node node) throws InitializationException {

        try {
            preInit(node);
            validateOnly = true;
        } catch (Exception exc) {
            logger.error("Error initializing validate mailbox call operation", exc);
            throw new InitializationException("GVVCL_MAIL_INIT_ERROR", new String[][] { { "node", node.getLocalName() } }, exc);
        }

    }

    @Override
    protected String getProtocol() {
        return Optional.ofNullable(serverProps).orElseGet(Properties::new).getProperty("mail.store.protocol", "pop3");
    }

    @Override
    protected GVBuffer receiveMails(Store locStore, GVBuffer data) throws Exception {

        throw new UnsupportedOperationException();
    }

    @Override
    protected void postStore(Store locStore, GVBuffer data) throws Exception {

    }

}