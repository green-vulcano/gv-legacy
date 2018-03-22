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
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
            Path sourceFile = Objects.isNull(filename) ? Paths.get(PropertiesHandler.expand(srcPath, gvBuffer)) 
            		                                   : Paths.get(PropertiesHandler.expand(srcPath, gvBuffer) , PropertiesHandler.expand(filename, gvBuffer));

            if (!Files.exists(sourceFile)) {
                throw new IllegalArgumentException("Source file " + sourceFile.toAbsolutePath()
                        + " does not exist on local filesystem");
            }

            if (!Files.isRegularFile(sourceFile)) {
                throw new IllegalArgumentException("Source file " + sourceFile.toAbsolutePath() + " is not a normal file");
            }
            
            byte[] fileContent = Files.readAllBytes(sourceFile);
            if (asXML) {
                logger.debug("Reading source file as XML: " + sourceFile.toAbsolutePath());        
                Document dom = XMLUtils.parseDOM_S(fileContent, isValidating, isNamespaceAware);
                gvBuffer.setObject(dom);
            } else {
                logger.debug("Reading source file: " + sourceFile.toAbsolutePath());               
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

   
}