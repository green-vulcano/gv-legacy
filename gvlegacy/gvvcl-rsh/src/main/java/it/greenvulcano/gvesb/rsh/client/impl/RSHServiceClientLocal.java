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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

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
import it.greenvulcano.gvesb.rsh.server.cmd.ShellCommand;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.log.NMDC;

/**
 * 
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public class RSHServiceClientLocal implements RSHServiceClient {
	
	private static final Logger logger = LoggerFactory.getLogger(RSHServiceClientLocal.class);

	/**
	 * The name of the client
	 */
	private String name = null;
	
	/**
	 * The validation, default false
	 */
	private boolean valid = false;

	public RSHServiceClientLocal() {
		// do nothing
	}

	/**
	 * 
	 * @param node
	 *            The node configuration containing all informations.
	 * 
	 * @throws RSHException
	 * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node node) throws RSHException {
		try {
			name = XMLConfig.get(node, "@name");
			valid = true;
			logger.info("RSHServiceClientLocal[" + name + "] is initialized");
		} catch (Exception exc) {
			throw new RSHException("Error initializing RSHServiceClientLocal", exc);
		}
	}

	/**
	 * Receive file from an inputStream with AXIOM parsing
	 * 
	 * @param fileName
	 * @throws RemoteException,RHSException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(java.lang.String)
	 */
	@Override
	public RemoteInputStream getFile(String fileName) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
		}
		// create a RemoteStreamServer (note the finally block which only
		// releases
		// the RMI resources if the method fails before returning.)
		RemoteInputStreamServer istream = null;

		NMDC.push();
		try {
			logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Read file: " + fileName);
			istream = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			// export the final stream for returning to the client
			RemoteInputStream result = istream.export();
			// after all the hard work, discard the local reference (we are
			// passing
			// responsibility to the client)
			istream = null;
			logger.info("RSHServiceClientLocal[" + name + "] END - Read file: " + fileName);
			return result;
		} catch (Exception exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
			throw new RSHException("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
		} finally {
			// we will only close the stream here if the server fails before
			// returning an exported stream
			if (istream != null) {
				try {
					istream.close();
				} catch (Exception exc) {
					// do nothing
				}
			}
			NMDC.pop();
		}
	}

	/**
	 * Send file with AXIOM parsing
	 * 
	 * @param fileName,ristream
	 * @throws RemoteException,RSHException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#sendFile(java.lang.String,
	 *      com.healthmarketscience.rmiio.RemoteInputStream)
	 */
	@Override
	public void sendFile(String fileName, RemoteInputStream ristream) throws RemoteException, RSHException {
		FileOutputStream ostream = null;
		InputStream istream = null;

		if (!valid) {
			throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
		}

		NMDC.push();
		try {
			logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Write file: " + fileName);
			istream = RemoteInputStreamClient.wrap(ristream);

			File file = new File(fileName);
			ostream = new FileOutputStream(file);

			byte[] buf = new byte[1024];

			int bytesRead = 0;
			while ((bytesRead = istream.read(buf)) >= 0) {
				ostream.write(buf, 0, bytesRead);
			}
			ostream.flush();

			logger.info("RSHServiceClientLocal[" + name + "] END - Write file: " + fileName);
		} catch (Exception exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
			throw new RSHException("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
		} finally {
			try {
				if (istream != null) {
					try {
						istream.close();
					} catch (Exception exc) {
						// do nothing
					}
				}
			} finally {
				if (ostream != null) {
					try {
						ostream.close();
					} catch (Exception exc) {
						// do nothing
					}
				}
			}

			NMDC.pop();
		}
	}

	/**
	 * Receive file with DOM parsing
	 * 
	 * @return byte[]
	 * @param fileName
	 * @throws RemoteException,RSHException
	 */
	@Override
	public byte[] getFileB(String fileName) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
		}

		InputStream istream = null;
		try {
			logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Read file: " + fileName);
			istream = new BufferedInputStream(new FileInputStream(fileName));

			ByteArrayOutputStream ostream = new ByteArrayOutputStream();

			byte[] buf = new byte[1024];

			int bytesRead = 0;
			while ((bytesRead = istream.read(buf)) >= 0) {
				ostream.write(buf, 0, bytesRead);
			}

			logger.info("RSHServiceClientLocal[" + name + "] END - Read file: " + fileName);
			return ostream.toByteArray();
		} catch (IOException exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
			throw new RSHException("RSHServiceClientLocal[" + name + "] Error reading file: " + fileName, exc);
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
	 * Send file with DOM parsing
	 * 
	 * @param fileName
	 * @param content
	 * @throws RemoteException,RSHException
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#sendFileB(String, byte[])
	 */

	@Override
	public void sendFileB(String fileName, byte[] content) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
		}

		OutputStream ostream = null;
		try {
			// LoggerContext.put("RSH_ID", id);
			logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Write file: " + fileName);

			File file = new File(fileName);
			ostream = new FileOutputStream(file);

			ostream.write(content, 0, content.length);

			ostream.flush();
			logger.info("RSHServiceClientLocal[" + name + "] END - Write file: " + fileName);
		} catch (IOException exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
			throw new RSHException("RSHServiceClientLocal[" + name + "] Error writing file: " + fileName, exc);
		} finally {
			if (ostream != null) {
				try {
					ostream.close();
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
	 * @throws RemoteException,RSHException
	 * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#shellExec(it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef)
	 */
	@Override
	public ShellCommandResult shellExec(ShellCommandDef commandDef) throws RemoteException, RSHException {
		if (!valid) {
			throw new RSHException("RSHServiceClientLocal[" + name + "] is invalidated");
		}

		NMDC.push();
		try {
			logger.info("RSHServiceClientLocal[" + name + "] BEGIN - Execute command");
			ShellCommand cmd = new ShellCommand(commandDef);
			ShellCommandResult result = cmd.execute();
			logger.info("RSHServiceClientRMI[" + name + "] END - Execute command");
			return result;
		} catch (RSHException exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error executing command", exc);
			throw exc;
		} catch (Exception exc) {
			logger.error("RSHServiceClientLocal[" + name + "] Error executing command", exc);
			throw new RSHException("RSHServiceClientLocal[" + name + "] Error executing command", exc);
		} finally {
			NMDC.pop();
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
		logger.info("RSHServiceClientLocal[" + name + "] is invalidated");
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
	 * do nothing
	 * @see it.greenvulcano.gvesb.rsh.RSHServiceClient#cleanup()
	 */
	@Override
	public void cleanup() {
		// do nothing
	}

}
