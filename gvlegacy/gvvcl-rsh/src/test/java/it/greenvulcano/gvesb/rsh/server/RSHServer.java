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

package it.greenvulcano.gvesb.rsh.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHServiceImpl;
import it.greenvulcano.util.ArgsManager;

/**
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */
public class RSHServer implements ShutdownEventListener {
	private static final Logger logger = LoggerFactory.getLogger(RSHServer.class);

	/**
	 * The instance default null
	 */
	private static RSHServer instance = null;

	/**
	 * The registry: default null
	 */
	private Registry registry = null;

	/**
	 * The service name: default null
	 */
	private RSHService srvc = null;

	/**
	 * The port: default 1099
	 */
	private int port = 1099;

	public RSHServer(int port) throws RemoteException {
		this.port = port;
	}

	/**
	 * Start the RSHServer
	 * 
	 * @throws RemoteException
	 */
	public void startUp() throws RemoteException {
		ShutdownEventLauncher.addEventListener(this);
//TODO: trycatch commented
		try {
//			
			registry = LocateRegistry.createRegistry(port);
		} catch (Exception exc) {
			// do nothing
			exc.printStackTrace();
		}
//		RSHService bla = (RSHService) UnicastRemoteObject.exportObject(srvc, port);
		if (registry == null) {
			registry = LocateRegistry.getRegistry(port);
		}
		srvc = new RSHServiceImpl("RSHService");
		registry.rebind(RSHService.class.getName(), srvc);
	}

	/**
	 * Shut Down the RSHServer
	 */
	public void shutDown() {
		ShutdownEventLauncher.removeEventListener(this);

		try {
			logger.info("Unregistering " + srvc);
			if (registry != null) {
				registry.unbind(RSHService.class.getName());
			}
			UnicastRemoteObject.unexportObject(srvc, true);
		} catch (Exception exc) {
			logger.warn("Error unregistering " + srvc, exc);
		}
	}

	@Override
	public void shutdownStarted(ShutdownEvent event) {
		shutDown();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Starting RSH Server");
		try {
			ArgsManager am = new ArgsManager("p:", args);
			int port = am.getInteger("p", 1099);

	        if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }  
			instance = new RSHServer(port);
			instance.startUp();
		} catch (Exception exc) {
			logger.error("Starting RSH Server failed", exc);
			System.exit(-1); // can't just return, rmi threads may not exit
		}
		logger.info("Started RSH Server");
	}

}
