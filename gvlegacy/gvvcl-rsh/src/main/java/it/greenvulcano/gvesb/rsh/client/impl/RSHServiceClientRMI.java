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

package it.greenvulcano.gvesb.rsh.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;

/**
 * 
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */
public class RSHServiceClientRMI implements RSHServiceClient {
	private static final Logger logger = LoggerFactory.getLogger(RSHServiceClientRMI.class);

	/**
	 * The name of the service
	 */
	private RSHService service = null;
	/**
	 * The name of the RSH client
	 */
	private String name = null;

	/**
	 * The registry Service Name
	 */
	private String regSvcName = null;

	/**
	 * The registry URL
	 */
	private String regURL = null;

	/**
	 * The registry context Factory
	 */
	private String regCtxFactory = null;

	/**
	 * The validation, default false
	 */
	private boolean valid = false;

	public RSHServiceClientRMI() {

		// do nothing
	}

	/**
	 * 
	 * @param node
	 *            The adapter configuration containing all informations.
	 * 
	 * @throws RSHException
	 * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node node) throws RSHException {
		try {
			name = XMLConfig.get(node, "@name");
			regURL = XMLConfig.get(node, "@regURL");
			regSvcName = XMLConfig.get(node, "@regSvcName", RSHService.class.getName());
			regCtxFactory = XMLConfig.get(node, "@regCtxFactory", "com.sun.jndi.rmi.registry.RegistryContextFactory");
			valid = true;
			logger.info("RSHServiceClientRMI[" + name + "] is initialized: regURL=" + regURL + " - regSvcName="
					+ regSvcName + " - regCtxFactory=" + regCtxFactory);
		} catch (Exception exc) {
			throw new RSHException("Error initializing RSHServiceClientRMI", exc);
		}
	}

	/**
	 * Receive file from an inputStream with AXIOM parsing
	 * 
	 * @param fileName
	 * @return RemoteInputStream
	 * @throws RemoteException, RSHException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(java.lang.String)
	 */
	@Override
	public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
		}
		try {
			logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Read file: " + fileName);
			RemoteInputStream istream = getService().getFile(fileName);
			logger.info("RSHServiceClientRMI[" + name + "] END - Read file: " + fileName);
			return istream;
		} catch (RSHException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
			throw new RSHException("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
		}
	}

	/**
	 * Sends the file
	 * 
	 * @param fileName
	 * @param ristream
	 * @throws RemoteException, RSHException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#sendFile(String, RemoteInputStream)
	 */
	@Override
	public void sendFile(String fileName, RemoteInputStream ristream) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
		}
		try {
			logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Write file: " + fileName);
			getService().sendFile(fileName, ristream);
			logger.info("RSHServiceClientRMI[" + name + "] END - Write file: " + fileName);
		} catch (RSHException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
			throw new RSHException("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
		}

	}

	/**
	 * Receive file with DOM parsing
	 * 
	 * @return byte[]
	 * @param fileName
	 * @throws RemoteException, RSHException
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#getFileB(String)
	 */
	@Override
	public byte[] getFileB(String fileName) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
		}

		InputStream istream = null;
		try {
			logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Read file: " + fileName);
			istream = RemoteInputStreamClient.wrap(getService().getFile(fileName));

			ByteArrayOutputStream ostream = new ByteArrayOutputStream();

			byte[] buf = new byte[1024];

			int bytesRead = 0;
			while ((bytesRead = istream.read(buf)) >= 0) {
				ostream.write(buf, 0, bytesRead);
			}

			logger.info("RSHServiceClientRMI[" + name + "] END - Read file: " + fileName);
			return ostream.toByteArray();
		} catch (RSHException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
			throw exc;
		} catch (RemoteException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
			this.invalidate();
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
			throw new RSHException("RSHServiceClientRMI[" + name + "] Error reading file: " + fileName, exc);
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (Exception exc) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Send file with GZIPRemoteInputStream
	 * 
	 * @param fileName
	 * @param content
	 * @throws RemoteException, RSHException
	 * 
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#sendFileB(String, byte[])
	 */
	@Override
	public void sendFileB(String fileName, byte[] content) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
		}

		RemoteInputStreamServer ristream = null;
		try {
			logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Write file: " + fileName);
			ristream = new GZIPRemoteInputStream(new ByteArrayInputStream(content));
			getService().sendFile(fileName, ristream.export());
			logger.info("RSHServiceClientRMI[" + name + "] END - Write file: " + fileName);
		} catch (RSHException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
			throw exc;
		} catch (RemoteException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
			this.invalidate();
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
			throw new RSHException("RSHServiceClientRMI[" + name + "] Error writing file: " + fileName, exc);
		} finally {
			if (ristream != null) {
				try {
					ristream.close();
				} catch (Exception exc) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Execute a shell command
	 * 
	 * @return ShellCommandResult
	 * @param commandDef
	 * @throws RemoteException, RSHException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#shellExec(ShellCommandDef)
	 */
	@Override
	public ShellCommandResult shellExec(ShellCommandDef commandDef) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientRMI[" + name + "] is invalidated");
		}
		try {
			logger.info("RSHServiceClientRMI[" + name + "] BEGIN - Execute command");
			ShellCommandResult result = getService().shellExec(commandDef);
			logger.info("RSHServiceClientRMI[" + name + "] END - Execute command");
			return result;
		} catch (RSHException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error RSHExeption executing command", exc);
			throw exc;
		} catch (RemoteException exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error RemoteException executing command", exc);
			this.invalidate();
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientRMI[" + name + "] Error Exception executing command", exc);
			throw new RSHException("RSHServiceClientRMI[" + name + "] Error executing command", exc);
		}
	}

	/**
	 * get the name
	 * 
	 * @return String
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * invalidate the service
	 * 
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#invalidate()
	 */
	@Override
	public void invalidate() {
		logger.info("RSHServiceClientRMI[" + name + "] is invalidated");
		valid = false;
		cleanup();
	}

	/**
	 * return boolean: if true is validate
	 * 
	 * @return boolean
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	/**
	 * set service = null
	 * 
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#cleanup()
	 */
	@Override
	public void cleanup() {
		service = null;
	}

	/**
	 * Get the service parameters
	 * 
	 * @return RSHService
	 * @throws RSHException
	 */
	private RSHService getService() throws RSHException {
		Context ctx = null;
		try {
			if (service != null) {
				return service;
			}
			// TODO:added <String, String>
			Hashtable env = new Hashtable();
			env.put(Context.PROVIDER_URL, regURL);
			env.put(Context.INITIAL_CONTEXT_FACTORY, regCtxFactory);

			ctx = new InitialContext(env);
			System.out.println("-----------------" + ctx);
			ClassLoader prev = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				service = (RSHService) ctx.lookup(regSvcName);
			} finally {
				Thread.currentThread().setContextClassLoader(prev);
			}
			System.out.println("SERVICE-------------------------" + service);

			return service;
		} catch (Exception exc) {
			throw new RSHException("Error contacting RMI Registry", exc);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception exc) {
					// do nothing
				}
			}
		}
	}
}
