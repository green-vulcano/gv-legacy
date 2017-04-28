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
package it.greenvulcano.gvesb.virtual.file.remote.command;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Delete file/directory on remote file system.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 *         REVISON OK
 */
public class GVDelete implements GVRemoteCommand
{
    private static Logger logger = LoggerFactory.getLogger(GVDelete.class);

    /**
     *  Absolute path of the file/directory to remove.
     */
    private String              targetPath;
    
    /**
     * File name to remove. Might contain a regular expression. 
     * It is used only if <code>targetPath</code> is a directory, and if empty the filter is ignored.
     */
    private String              filePattern;
    
    /**
     * If true an internal error interrupt the command sequence. Default true.
     */
    private boolean             isCritical;
    private Map<String, String> optProperties = new HashMap<String, String>();

    /**
     * do nothing
     */
    public GVDelete()
    {
        // do nothing
    }

    /**
     * Invoked from <code>RemoteManagerCall</code> when the Command
     * needs initialization.<br>
     *
     * @param node
     * 			The configuration node containing all informations.
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#init(org.w3c.dom.Node)
     * 
     * @throws Exception
     */
    @Override
    public void init(Node node) throws Exception
    {
        targetPath = XMLConfig.get(node, "@targetPath");
        filePattern = XMLConfig.get(node, "@filePattern", "");
        isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
        
        NodeList nl = XMLConfig.getNodeList(node, "PropertyDef");
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                String name = XMLConfig.get(nl.item(i), "@name");
                String value = XMLConfig.get(nl.item(i), "@value", "");
                optProperties.put(name, value);
            }
        }
    }

    /**
     * check <code>filePattern</code>
     * 
     * @see java.lang.Object#toString()
     * 
     * @return String
     */
    @Override
    public String toString()
    {
        if (filePattern.equals("")) {
            return ((isCritical ? "[CRITICAL] " : "") + "DELETE directory/file " + targetPath);
        }
        return ((isCritical ? "[CRITICAL] " : "") + "DELETE files '" + filePattern + "' from directory " + targetPath);
    }

    /**
     *  Invoked from <code>RemoteManagerCall</code> when the Command
     * needs to be execute.<br>
     * 
     * @param gvBuffer
     * 			The GVBuffer to be used within the service
     * 
     * @param manager
     * 			 A private instance of <code>RemoteManager</code> class to perform FTP
     * 			 operations.
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#executeOperation(it.greenvulcano.util.remotefs.RemoteManager,
     *      it.greenvulcano.gvesb.buffer.GVBuffer)
     *      
     * @throws Exception
     */
    @Override
    public void execute(RemoteManager manager, GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currTargetPath = PropertiesHandler.expand(targetPath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(filePattern, params, gvBuffer);

            Map<String, String> localOptProperties = new HashMap<String, String>();
            for (String prop : optProperties.keySet()) {
                localOptProperties.put(prop, PropertiesHandler.expand(optProperties.get(prop), params, gvBuffer));
            }
            
            boolean result = manager.rm(currTargetPath, currFile, localOptProperties);

            if (result) {
                if (filePattern.equals("")) {
                    logger.debug("Directory/file " + currTargetPath + " successfully deleted");
                }
                else {
                    logger.debug("File '" + currFile + "' successfully deleted from directory " + currTargetPath);
                }
            }
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVRemoteCommand#isCritical()
     * 
     * @return boolean <code>isCritical</code>
     */
    @Override
    public boolean isCritical()
    {
        return isCritical;
    }
}
