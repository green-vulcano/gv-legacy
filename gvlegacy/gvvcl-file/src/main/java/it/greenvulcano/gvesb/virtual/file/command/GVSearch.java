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
import it.greenvulcano.util.file.FileNameSorter;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 * Search for files.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVSearch implements GVFileCommand
{
    private static Logger logger = LoggerFactory.getLogger(GVSearch.class);

    /**
     * Absolute path of the directory to search in.
     */
    private String        sourcePath;
    
    /**
     * File name to search for. Can be a regular expression. 
     */
    private String        filePattern;
    
    /**
     * If true the found files are inserted in list with full path. Default false. 
     */
    private boolean       returnFullPath;
    
    /**
     * If true an internal error interrupts the command sequence. Default true.
     */
    private boolean       isCritical;

    /**
     * do nothing
     */
    public GVSearch()
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
        sourcePath = XMLConfig.get(node, "@sourcePath");
        filePattern = XMLConfig.get(node, "@filePattern", "");
        returnFullPath = XMLConfig.getBoolean(node, "@returnFullPath", false);
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
        return ((isCritical ? "[CRITICAL] " : "") + "CHECK for existence of file '" + filePattern + "' in directory " + sourcePath);
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
            String currSourcePath = PropertiesHandler.expand(sourcePath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(filePattern, params, gvBuffer);

            Set<FileProperties> results = FileManager.ls(currSourcePath, currFile);
            int resultsSize = results.size();

            gvBuffer.setProperty(GVFM_FOUND_FILES_NUM, "" + resultsSize);
            
            boolean result = (resultsSize > 0);
            if (result) {
                StringBuilder buf = new StringBuilder();
                List<FileProperties> fm_files = new ArrayList<FileProperties>(resultsSize);
                fm_files.addAll(results);
                Collections.sort(fm_files, new FileNameSorter(true));
                for (FileProperties currFileInfo : fm_files) {
                    String filename = currFileInfo.getName();
                    if (returnFullPath) {
                        buf.append(currSourcePath).append(File.separator);
                    }
                    buf.append(filename).append(";");
                }
                if (buf.length() > 1) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                logger.debug("File (" + resultsSize + ") matching the pattern '" + currFile
                        + "' found within directory " + currSourcePath);
                logger.debug(buf.toString());
                gvBuffer.setProperty(GVFM_FOUND_FILES_LIST, buf.toString());
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
