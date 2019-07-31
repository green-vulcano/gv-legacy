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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.file.monitor.AnalysisReport;
import it.greenvulcano.util.file.monitor.FileSystemMonitor;
import it.greenvulcano.util.file.monitor.FileSystemMonitorFactory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * The class perform a directory analysis and return a report describing it's
 * status.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class AnalyzeDirCall implements CallOperation
{
    private static final Logger logger            = LoggerFactory.getLogger(AnalyzeDirCall.class);

    /**
     * The instance name.
     */
    private String              name              = null;

    /**
     * The configured operation key
     */    
    protected OperationKey      key               = null;

    /**
     * A private instance of <code>FileSystemMonitor</code> class to perform an
     * analysis of a directory
     */
    private FileSystemMonitor   fileSystemMonitor = null;

    private String outputFormat = "JSON";

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
            outputFormat = XMLConfig.get(node, "@reportFormat", "XML").toUpperCase();
            
            Node fsNode = XMLConfig.getNode(node, "*[@type='fs-monitor']");
            fileSystemMonitor = FileSystemMonitorFactory.buildInstance(XMLConfig.get(fsNode, "@class"));
            fileSystemMonitor.init(fsNode);
            
            logger.info("AnalyzeDirCall " + name + " configured");
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
            Map<String, String> props = GVBufferPropertiesHelper.getPropertiesMapSS(gvBuffer, true);
            AnalysisReport report = fileSystemMonitor.analyze(props);
            if (!report.resultsAvailable()) {
                logger.debug("No file detected on target directory [" + report.getAnalysisDirectory() + "]");
                gvBuffer.setProperty("GVFSM-REPORT_CREATED", "true");
            }
            else {
                logger.debug("AnalyzeDirCall " + name + " create an XML report of directory ["
                        + report.getAnalysisDirectory() + "]");
               
                switch (outputFormat) {
                case "JSON":
                    gvBuffer.setObject(report.toJSON());
                    break;

                default:
                    gvBuffer.setObject(report.toXML());
                    break;
                }
                
                gvBuffer.setProperty("GVFSM_REPORT_CREATED", "true");
                if (report.getExistingFilesCount() != -1) {
                    gvBuffer.setProperty("GVFSM_EXISTING_FILES", "" + report.getExistingFilesCount());
                }
                if (report.getCreatedFilesCount() != -1) {
                    gvBuffer.setProperty("GVFSM_CREATED_FILES", "" + report.getCreatedFilesCount());
                }
                if (report.getModifiedFilesCount() != -1) {
                    gvBuffer.setProperty("GVFSM_MODIFIED_FILES", "" + report.getModifiedFilesCount());
                }
                if (report.getDeletedFilesCount() != -1) {
                    gvBuffer.setProperty("GVFSM_DELETED_FILES", "" + report.getDeletedFilesCount());
                }
            }
            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("An error occurred while analysing directory", exc);
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
        // do nothing
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
