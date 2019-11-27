/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.util.remotefs.sftp;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.remotefs.RemoteManager;
import it.greenvulcano.util.remotefs.RemoteManagerException;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public abstract class SSHManager extends RemoteManager {

	private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

	private Session sshClient;

	private String knownHostFilePath = null;

	private String authMethod = null;

	private boolean isConnected = false;

	private JSch jsch;

	private String privateKey;

	private String passphrase;
	

	/**
	 * @see it.greenvulcano.util.remotefs.RemoteManager#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node configNode) throws RemoteManagerException {
		super.init(configNode);
		try {
			knownHostFilePath = XMLConfig.get(configNode, "@knownHostFilePath");
			if (knownHostFilePath != null) {
				if (!PropertiesHandler.isExpanded(knownHostFilePath)) {
					knownHostFilePath = PropertiesHandler.expand(knownHostFilePath);
				}
			}
			authMethod = XMLConfig.get(configNode, "@authMethod", "password");
			if (authMethod.equals("password") && ((getPassword() == null) || (getPassword().length() == 0))) {
				throw new RemoteManagerException("Choosen 'password' authentication method but password not set.");
			}
			if (authMethod.equals("publicKey")) {
				privateKey =  Optional.ofNullable(XMLConfig.get(configNode, "IdentityInfo/@privateKey")).orElseThrow(IllegalArgumentException::new);
				passphrase = XMLConfig.getDecrypted(configNode, "IdentityInfo/@passphrase");
			}
		} catch (IllegalArgumentException e) {
		    throw new RemoteManagerException(
                            "Required parameter 'privateKey' or 'passphrase' missing from configuration.");
		} catch (XMLConfigException exc) {
			throw new RemoteManagerException("Initialization error", exc);
		} catch (PropertiesHandlerException exc) {
			throw new RemoteManagerException("Properties expansion error", exc);
		}

		jsch = new JSch();
		try {
			if (knownHostFilePath != null) {
				jsch.setKnownHosts(knownHostFilePath);
			}
		} catch (JSchException exc) {
			throw new RemoteManagerException("Cannot load known hosts file", exc);
		}
	}

	/**
	 * @see it.greenvulcano.util.remotefs.RemoteManager#connect(Map<String, String>)
	 */
	@Override
	public void connect(Map<String, String> optProperties) throws RemoteManagerException {
		if (!isConnected) {
			try {
				sshClient = getSession(optProperties);
				sshClient.connect(connectTimeout);
				sshClient.setTimeout(dataTimeout);
				isConnected = sshClient.isConnected();
				logger.debug("Connected to SFTP server " + sshClient.getHost() + " and logged in as SFTP user "
						+ sshClient.getUserName());
			} catch (JSchException exc) {
				throw new RemoteManagerException("Cannot connect to host", exc);
			}
		}
	}

	/**
	 * @see it.greenvulcano.util.remotefs.RemoteManager#disconnect(Map<String,
	 *      String>)
	 */
	@Override
	public void disconnect(Map<String, String> optProperties) {
		String localHostname = hostname;
		try {
			if ((sshClient != null) && isConnected && sshClient.isConnected()) {
				Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
				String localUsername = PropertiesHandler.expand(username, localProps);
				localHostname = PropertiesHandler.expand(hostname, localProps);

				logger.debug("Logging out SFTP user " + localUsername + " from SFTP server " + localHostname + "...");

				sshClient.disconnect();
			}
		} catch (Exception exc) {
			logger.warn("Disconnection from SFTP server " + localHostname + " failed", exc);
		} finally {
			isConnected = false;
		}
	}

	private Session getSession(Map<String, String> optProperties) throws RemoteManagerException {
		Session session = null;
		try {
			Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
			String localUsername = PropertiesHandler.expand(username, localProps);
			String localHostname = PropertiesHandler.expand(hostname, localProps);
			String localPort = PropertiesHandler.expand(port, localProps);
			logger.debug("Creating session to SFTP server " + localHostname + ":" + localPort + ". Logging in as SFTP user "
					+ localUsername + "...");

			if (authMethod.equals("password")) {
				String localPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, localProps));
				session = jsch.getSession(localUsername, localHostname, Integer.valueOf(localPort));
				session.setPassword(localPassword);
			} else {
				
				String localPrivateKey = PropertiesHandler.expand(privateKey, localProps);
				
				if (passphrase!=null) {				    
				    String localPassphrase = XMLConfig.getDecrypted(PropertiesHandler.expand(passphrase, localProps));
				    jsch.addIdentity(localPrivateKey, localPassphrase);
				} else {
				    jsch.addIdentity(localPrivateKey);
				}
				session = jsch.getSession(localUsername, localHostname, Integer.valueOf(localPort));
			}

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			return session;
		} catch (Exception exc) {
			throw new RemoteManagerException("Connection error. " + getManagerKey(optProperties), exc);
		}
	}

	/**
	 * @return the isConnected
	 */
	protected boolean isConnected() {
		return this.isConnected;
	}

	/**
	 * @return the current SSH client
	 * @throws RemoteManagerException
	 */
	protected Session getSSHClient() throws RemoteManagerException {
		checkConnected();
		return sshClient;
	}

	/**
	 * @throws RemoteManagerException
	 *             if not connected
	 */
	protected void checkConnected() throws RemoteManagerException {
		if (isAutoconnect()) {
			connect();
		}
		if (!isConnected) {
			throw new RemoteManagerException("NOT connected to SSH server.");
		}

		if (!sshClient.isConnected()) {
			isConnected = false;
			throw new RemoteManagerException("Connection to SSH server expired. Please reconnect.");
		}
	}

}
