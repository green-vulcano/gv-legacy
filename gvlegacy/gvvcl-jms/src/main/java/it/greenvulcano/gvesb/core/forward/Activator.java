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
package it.greenvulcano.gvesb.core.forward;

import java.util.Optional;

import javax.management.ObjectName;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.impl.JMSBytesMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSMapMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSObjectMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSStreamMessageDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.JMSTextMessageDataProvider;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.j2ee.JMSDequeueOperation;
import it.greenvulcano.gvesb.virtual.j2ee.JMSEnqueueOperation;
import it.greenvulcano.jmx.JMXEntryPoint;

public class Activator implements BundleActivator {

	private static final Logger LOG   = org.slf4j.LoggerFactory.getLogger(Activator.class);
	
	private static Optional<ObjectName> jmxObjectName = Optional.empty();
	
	private final static ConfigurationListener configurationListener = event-> {
		
		
		LOG.debug("GV JMS Plugin - handling configuration event");
		
		if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(JMSForwardManager.JMS_FORWARD_FILE_NAME)) {
			LOG.info("Calling JMSForwardManager destroy on configuration event "+ event.getDescription());
			JMSForwardManager.destroy();
		}
		
		if ((event.getCode() == ConfigurationEvent.EVT_FILE_LOADED) && event.getFile().equals(JMSForwardManager.JMS_FORWARD_FILE_NAME)) {
			LOG.info("Calling JMSForwardManager init on configuration event "+ event.getDescription());
			JMSForwardManager.init();
		}
	};
	
	@Override
	public void start(BundleContext context) throws Exception {
			
		DataProviderManager.registerSupplier("JMSBytesMessageDataProvider", JMSBytesMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSMapMessageDataProvider", JMSMapMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSObjectMessageDataProvider", JMSObjectMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSStreamMessageDataProvider",JMSStreamMessageDataProvider::new);
		DataProviderManager.registerSupplier("JMSTextMessageDataProvider", JMSTextMessageDataProvider::new);
		
		OperationFactory.registerSupplier("jms-enqueue", JMSEnqueueOperation::new);
		OperationFactory.registerSupplier("jms-dequeue", JMSDequeueOperation::new);
		
		JMSForwardManager.init();
		
		//jmxObjectName = Optional.ofNullable(JMXEntryPoint.getInstance().registerObject(jmsForwardManager, JMSForwardManager.DESCRIPTOR_NAME));
		
		XMLConfig.addConfigurationListener(configurationListener, JMSForwardManager.JMS_FORWARD_FILE_NAME);
		
		LOG.debug("*********** VCL JMS Up&Running ");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
		XMLConfig.removeConfigurationListener(configurationListener);
		
		DataProviderManager.unregisterSupplier("JMSBytesMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSMapMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSObjectMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSStreamMessageDataProvider");
		DataProviderManager.unregisterSupplier("JMSTextMessageDataProvider");
		
		OperationFactory.unregisterSupplier("jms-enqueue");
		OperationFactory.unregisterSupplier("jms-dequeue");
		
		try {
			jmxObjectName.ifPresent(JMXEntryPoint.getInstance()::unregisterObject);
			
		} catch (Exception e) {
			LOG.error("Fail to remove JMSForwardManager object name", e);
		}
		
		JMSForwardManager.destroy();		
		
		LOG.debug("*********** VCL JMS Stopped ");

	}

}
