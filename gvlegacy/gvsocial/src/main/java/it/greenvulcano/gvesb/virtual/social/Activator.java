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
package it.greenvulcano.gvesb.virtual.social;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterDisableNotificationCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterEnableNotificationCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterGetFollowersIDsCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterGetFriendsIDsCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterGetUserTimelineCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterRetweetStatusCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterSendDirectMessageCallOperation;
import it.greenvulcano.gvesb.virtual.social.twitter.TwitterUpdateStatusCallOperation;

/*
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public class Activator implements BundleActivator {
	
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	/**
	 * 
	 * @param context 
	 * 			The execution context of the bundle being started.
	 */
    public void start(BundleContext context) throws Exception {
    	OperationFactory.registerSupplier("Twitter-update-status", TwitterUpdateStatusCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-retweet-status", TwitterRetweetStatusCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-enable-notification", TwitterEnableNotificationCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-disable-notification", TwitterDisableNotificationCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-get-followers-ids", TwitterGetFollowersIDsCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-get-friends-ids", TwitterGetFriendsIDsCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-send-direct-message", TwitterSendDirectMessageCallOperation::new);
    	OperationFactory.registerSupplier("Twitter-get-user-timeline", TwitterGetUserTimelineCallOperation::new);
		LOG.debug("*********** GV Social Up&Runnig");
    }

    /**
     * 
     * @param context
     * 			The execution context of the bundle being stopped.
     */
    public void stop(BundleContext context) throws Exception {
    	OperationFactory.unregisterSupplier("Twitter-update-status");
    	OperationFactory.unregisterSupplier("Twitter-retweet-status");
    	OperationFactory.unregisterSupplier("Twitter-enable-notification");
    	OperationFactory.unregisterSupplier("Twitter-disable-notification");
    	OperationFactory.unregisterSupplier("Twitter-get-followers-ids");
    	OperationFactory.unregisterSupplier("Twitter-get-friends-ids");
    	OperationFactory.unregisterSupplier("Twitter-send-direct-message");
    	OperationFactory.unregisterSupplier("Twitter-get-user-timeline");
		LOG.debug("*********** GV Social Stopped");
    }

}