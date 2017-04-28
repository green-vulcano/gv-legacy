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
package it.greenvulcano.gvesb.virtual.file.command;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.util.file.FileManager;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 * Delete file/directory
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVDelete implements GVFileCommand
{
    private static Logger logger = LoggerFactory.getLogger(GVDelete.class);

    /**
     * Absolute path of the file/directory to remove.
     */
    private String        targetPath;
    
    /**
     * File name to remove. Can be a regular expression. 
     * Is used only if <code>targetPath</code> is a directory, and if empty the filter is ignored
     */
    private String        filePattern;
    
    /**
     * If true an internal error interrupts the command sequence. Default true
     */
    private boolean       isCritical;

    /**
     * do nothing
     */
    public GVDelete()
    {
        // do nothing
    }

    /**
     * Invoked from <code>FileManagerCall</code> when the Command
     * needs initialization.<br>
     * 
     * @param node
     * 			The configuration node containing all informations.
     * 
     * @see it.greenvulcano.gvesb.virtual.file.command.GVFileCommand#init(org.w3c.dom.Node)
     * 
     * @throws Exception
     */
    @Override
    public void init(Node node) throws Exception
    {
        targetPath = XMLConfig.get(node, "@targetPath");
        filePattern = XMLConfig.get(node, "@filePattern", "");
        isCritical = XMLConfig.getBoolean(node, "@isCritical", true);
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
     * Invoked from <code>FileManagerCall</code> when the Command
     * needs to be execute.<br>
     * 
     * @param gvBuffer
     * 			The GVBuffer to be used within the service
     * 
     * @see it.greenvulcano.gvesb.virtual.file.command.GVFileCommand#execute(it.greenvulcano.gvesb.buffer.GVBuffer)
     * 
     * @throws Exception
     */
    @Override
    public void execute(GVBuffer gvBuffer) throws Exception
    {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currTargetPath = PropertiesHandler.expand(targetPath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(filePattern, params, gvBuffer);

            FileManager.rm(currTargetPath, currFile);
            if (filePattern.equals("")) {
                logger.debug("Directory/file " + currTargetPath + " successfully deleted");
            }
            else {
                logger.debug("File '" + currFile + "' successfully deleted from directory " + currTargetPath);
            }
        }
        finally {
            PropertiesHandler.disableExceptionOnErrors();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.file.command.GVFileCommand#isCritical()
     * 
     * @return boolean <code>isCritical</code>
     */
    @Override
    public boolean isCritical()
    {
        return isCritical;
    }
}
