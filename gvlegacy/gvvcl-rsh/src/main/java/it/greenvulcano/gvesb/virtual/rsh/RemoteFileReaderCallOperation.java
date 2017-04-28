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

package it.greenvulcano.gvesb.virtual.rsh;

import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.rsh.RSHServiceClient;
import it.greenvulcano.gvesb.rsh.client.RSHServiceClientManager;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * Read a (remote) file contents as a byte array and put it in GVBuffer.object.
 *
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */
public class RemoteFileReaderCallOperation implements CallOperation {
	private static final Logger logger = LoggerFactory.getLogger(RemoteFileReaderCallOperation.class);

	/**
	 * The instance name.
	 */
	private String name = null;

	/**
	 * Source file path name. Can contain placeholders that will be expanded at
	 * call time.
	 */
	private String filePathName = null;

	/**
	 * The rsh-client-name
	 */
	private String clientName = "";

	/**
	 * The configured operation's key.
	 */
	protected OperationKey key = null;

	/**
	 * if true is parsed as an XML
	 */
	private boolean asXML;

	/**
	 * if true the XML representing the file is validated
	 */
	private boolean isValidating;

	/**
	 * if true the DOM keeps informations about the declared namespaces on the
	 * file
	 */
	private boolean isNamespaceAware;

	/**
	 * if true use AXION instead of DOM to parse the file
	 */
	private boolean useAXIOM;

	/**
	 * @param node
	 *            The configuration node containing all informations.
	 *            
	 * @throws InitializationException
	 * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
	 */
	@Override
	public void init(Node node) throws InitializationException {
		try {
			name = XMLConfig.get(node, "@name");
			clientName = XMLConfig.get(node, "@rsh-client-name");
			filePathName = XMLConfig.get(node, "@filePathName");
			asXML = XMLConfig.getBoolean(node, "xml-processor/@as-xml", false);
			useAXIOM = XMLConfig.getBoolean(node, "xml-processor/@use-axiom", false);
			isValidating = XMLConfig.getBoolean(node, "xml-processor/@validating", false);
			isNamespaceAware = XMLConfig.getBoolean(node, "xml-processor/@namespace-aware", false);

			logger.debug("clientName: " + clientName + " - filePathName : " + filePathName);

			logger.debug("RemoteFileReaderCallOperation " + name + " configured");
		} catch (XMLConfigException exc) {
			logger.error("An error occurred while configuring", exc);
			throw new InitializationException("GV_CONFIGURATION_ERROR",
					new String[][] { { "message", exc.getMessage() } }, exc);
		} catch (Exception exc) {
			logger.error("A generic error occurred while initializing", exc);
			throw new InitializationException("GV_CONFIGURATION_ERROR",
					new String[][] { { "message", exc.getMessage() } }, exc);
		}
	}

	/**
	 * @param gvBuffer
	 *            for transport data in GreenVulcano
	 * 
	 * @return GVBuffer
	 * @throws ConnectionException
	 * @throws CallException
	 * @throws InvalidDataException
	 * 
	 * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
	 */
	@Override
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {
		RSHServiceClient svcClient = null;
		try {
			String srcFile = buildSourcePath(gvBuffer);

			svcClient = RSHServiceClientManager.instance().getRSHServiceClient(clientName);

			if (asXML) {
				logger.debug("Preparing to read from source file as XML: " + srcFile);
				InputStream stream = RemoteInputStreamClient.wrap(svcClient.getFile(srcFile));
				if (useAXIOM) {
					// create the parser
					//final XMLInputFactory xmlif = StAXUtils.getXMLInputFactory();
					
					XMLInputFactory xmlif = XMLInputFactory.newFactory();
					xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
					xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
					xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, isNamespaceAware);
					xmlif.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
					XMLStreamReader streamReader = xmlif.createXMLStreamReader(stream);
					gvBuffer.setObject(OMXMLBuilderFactory
							.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), streamReader).getDocumentElement());
					
				} else {
					Document dom = XMLUtils.parseDOM_S(stream, isValidating, isNamespaceAware);
					gvBuffer.setObject(dom);
				}
			} else {
				logger.debug("Preparing to read from source file: " + srcFile);
				byte[] fileContent = svcClient.getFileB(srcFile);
				gvBuffer.setObject(fileContent);
			}

			return gvBuffer;
		} catch (Exception exc) {
			logger.error("An error occurred while reading file", exc);
			throw new CallException("GV_CALL_SERVICE_ERROR",
					new String[][] { { "service", gvBuffer.getService() }, { "system", gvBuffer.getSystem() },
							{ "id", gvBuffer.getId().toString() }, { "message", exc.getMessage() } },
					exc);
		} finally {
			try {
				RSHServiceClientManager.instance().releaseRSHServiceClient(svcClient);
			} catch (Exception exc) {
				// do nothing
			}
		}
	}

	/**
	 * do nothing
	 * 
	 * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// do nothing
	}

	/**
	 * do nothing
	 * 
	 * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
	 */
	@Override
	public void destroy() {
		// do nothing
	}

	/**
	 * Sets the Operation key.
	 * 
	 * @param key
	 * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
	 */
	@Override
	public void setKey(OperationKey key) {
		this.key = key;
	}

	/**
	 * Return the operation's key.
	 * 
	 * @return OperationKey
	 * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
	 */
	@Override
	public OperationKey getKey() {
		return key;
	}

	/**
	 * Return the alias for the given service
	 * 
	 * @return String
	 * @param gvBuffer
	 * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
	 */
	@Override
	public String getServiceAlias(GVBuffer gvBuffer) {
		return gvBuffer.getService();
	}

	/**
	 * Set the Path of the file to read
	 * 
	 * @param gvBuffer
	 * @return String
	 * @throws Exception
	 */
	private String buildSourcePath(GVBuffer gvBuffer) throws Exception {
		try {
			PropertiesHandler.enableExceptionOnErrors();
			Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

			String srcFilePathName = gvBuffer.getProperty("GVFR_FILE_NAME");
			if (srcFilePathName == null) {
				if (filePathName != null) {
					srcFilePathName = filePathName;
				} else {
					throw new IllegalArgumentException("Source file path name NOT available");
				}
			}

			srcFilePathName = PropertiesHandler.expand(srcFilePathName, params, gvBuffer);
			return srcFilePathName;
		} finally {
			PropertiesHandler.disableExceptionOnErrors();
		}
	}
}