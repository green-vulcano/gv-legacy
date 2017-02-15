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
package it.greenvulcano.gvesb.virtual.smtp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.imap.IMAPCallOperation;
import it.greenvulcano.gvesb.virtual.pop.POPCallOperation;


/*
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public class Activator implements BundleActivator {
	
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	/**
	 * 
	 * @param context
	 */
    public void start(BundleContext context) throws Exception {
    	OperationFactory.registerSupplier("smtp-call", SMTPCallOperation::new);
    	OperationFactory.registerSupplier("pop-call", POPCallOperation::new);
    	OperationFactory.registerSupplier("imap-call", IMAPCallOperation::new);
		LOG.debug("*********** GV VCL Mail Up&Runnig");
    }

    /**
     * 
     * @param context
     */
    public void stop(BundleContext context) throws Exception {
    	OperationFactory.unregisterSupplier("smtp-call");
    	OperationFactory.unregisterSupplier("pop-call");
    	OperationFactory.unregisterSupplier("imap-call");
		LOG.debug("*********** GV VCL Mail Stopped");
    }

}