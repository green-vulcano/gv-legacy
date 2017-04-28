/*******************************************************************************
 * Copyright (c) 2009, 2017 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.gvesb.gvbirt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.birt.report.BIRTReportCallOperation;

/*
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */

public class Activator implements BundleActivator {

	/**
	 * Invoked from Karaf to start the bundle
	 * 
	 * @param context 
	 * 			The execution context of the bundle being started.
	 */
    public void start(BundleContext context) {
    
    	OperationFactory.registerSupplier("birt-report-call", BIRTReportCallOperation::new);
    }

    /**
     * Invoked from Karaf to stop the bundle
     *	 
     * @param context 
     * 			The execution context of the bundle being stopped.
     */
    public void stop(BundleContext context) {

    	OperationFactory.unregisterSupplier("birt-report-call");
    	
    }

}