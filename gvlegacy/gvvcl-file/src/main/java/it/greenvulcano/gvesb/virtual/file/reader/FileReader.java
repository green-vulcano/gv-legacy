/*
 * Copyright (c) 2009-2017 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.virtual.file.reader;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
//import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Read a file contents as a byte array and put it in GVBuffer.object.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class FileReader implements CallOperation
{
    private static final Logger logger   = LoggerFactory.getLogger(FileReader.class);

    /**
     * The instance name.
     */
    private String              name     = null;

    /**
     * The pathname for the source directory. Can contain placeholders that will
     * be replaced at call time. Must evaluate to an absolute pathname.
     */
    private String              srcPath  = null;

    /**
     * Source file name. Can contain placeholders that will be expanded at call
     * time.
     */
    private String              filename = null;

    /**
     * The configured operation's key.
     */
    protected OperationKey      key      = null;

    /**
     * If true parsed the file as an XML. Default is false.
     */
    private boolean             asXML;

    /**
     * If true the XML representing the file is validated. 
     * This option applies only if <code>asXML</code> is true. Default is false.
     */
    private boolean             isValidating;

    /**
     * If true the DOM keeps informations about the declared namespaces on the file.
     * This option applies only if <code>asXML</code> is true. Default is false.
     */
    private boolean             isNamespaceAware;

	/**
	 * If true uses AXIOM instead of DOM to parse the file. Default is false.
	 */
    private boolean             useAXIOM;

    /**
     * Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     * needs initialization.<br>
     *
     * @param node
     * 			The configuration node containing all informations.
     *
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     * 
     * @throws InitializationException
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            srcPath = XMLConfig.get(node, "@srcPath");
            filename = XMLConfig.get(node, "@fileName");
            asXML = XMLConfig.getBoolean(node, "xml-processor/@as-xml", false);
            useAXIOM = XMLConfig.getBoolean(node, "xml-processor/@use-axiom", false);
            isValidating = XMLConfig.getBoolean(node, "xml-processor/@validating", false);
            isNamespaceAware = XMLConfig.getBoolean(node, "xml-processor/@namespace-aware", false);

            logger.debug("srcPath  : " + srcPath);
            logger.debug("filename : " + filename);

            logger.debug("FileReader " + name + " configured");
        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred while configuring", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * @param gvBuffer 
     * 			The GVBuffer to be used within the service
     * @return the GVBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     * 
     * @throws ConnectionException, CallException, InvalidDataException
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            File srcFile = buildSourcePath(gvBuffer);

            if (!srcFile.exists()) {
                throw new IllegalArgumentException("Source file " + srcFile.getAbsolutePath()
                        + " does not exist on local filesystem");
            }

            if (!srcFile.isFile()) {
                throw new IllegalArgumentException("Source file " + srcFile.getAbsolutePath() + " is not a normal file");
            }
            
            if (asXML) {
                logger.debug("Preparing to read from source file as XML: " + srcFile.getAbsolutePath());
                FileInputStream stream = new FileInputStream(srcFile);
                if (useAXIOM) {
                    // create the parser
                    //final XMLInputFactory xmlif = StAXUtils.getXMLInputFactory();
                	XMLInputFactory xmlif = XMLInputFactory.newFactory();
                    xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
                    xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
                    xmlif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, isNamespaceAware);
                    xmlif.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
                    XMLStreamReader streamReader = xmlif.createXMLStreamReader(stream);
                    
                    //StAXOMBuilder builder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(), streamReader);
                    OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), streamReader);
                    // get the root element
                    System.out.println("!!!!!!!!!!!!!!!!! builder: " + builder); //TODO
                    gvBuffer.setObject(builder.getDocumentElement());
                }
                else {
                    Document dom = XMLUtils.parseDOM_S(stream, isValidating, isNamespaceAware);
                    gvBuffer.setObject(dom);
                }
            }
            else {
                logger.debug("Preparing to read from source file: " + srcFile.getAbsolutePath());
                byte[] fileContent = BinaryUtils.readFileAsBytes(srcFile);
                gvBuffer.setObject(fileContent);
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while reading file", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * do nothing
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * do nothing
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // do nothig
    }

    /**
     * Set the configured operation key
     * 
     * @param key
     * 			the configured operation key
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * Return the configured operation key
     * 
     * @return key
     * 			the configured operation key
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Return the services of gvBuffer
     * 
     * @return the service of gvBuffer
     * 
     * @param gvBuffer
     * 			The GVBuffer to be used within the service
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    private File buildSourcePath(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

            String srcDirectory = gvBuffer.getProperty("GVFR_DIRECTORY");
            if (srcDirectory == null) {
                if (srcPath != null) {
                    srcDirectory = srcPath;
                }
                else {
                    throw new IllegalArgumentException("Source parent directory pathname NOT available");
                }
            }

            srcDirectory = PropertiesHandler.expand(srcDirectory, params, gvBuffer);

            String srcFilename = gvBuffer.getProperty("GVFR_FILE_NAME");
            if (srcFilename == null) {
                if (filename != null) {
                    srcFilename = filename;
                }
                else {
                    throw new IllegalArgumentException("Source file name NOT available");
                }
            }

            srcFilename = PropertiesHandler.expand(srcFilename, params, gvBuffer);

            File srcPathname = "".equals(srcDirectory) ? new File(srcFilename) : new File(srcDirectory, srcFilename);
            if (srcPathname.isAbsolute()) {
                if (srcPathname.isFile()) {
                    return srcPathname;
                }
                throw new IllegalArgumentException("Source file " + srcPathname.getPath() + " is not a file");
            }
            throw new IllegalArgumentException("Source file path " + srcPathname.getPath() + " is not absolute");
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }
}