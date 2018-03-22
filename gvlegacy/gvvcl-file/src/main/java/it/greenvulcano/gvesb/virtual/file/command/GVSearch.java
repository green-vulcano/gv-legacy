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
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
public class GVSearch implements GVFileCommand {

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
    
    private boolean recursive,returnCollection;

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
        
        recursive = XMLConfig.getBoolean(node, "@recursive", false);
        returnCollection = XMLConfig.getBoolean(node, "@returnCollection", false);
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
    public void execute(GVBuffer gvBuffer) throws Exception  {
        try {
            PropertiesHandler.enableExceptionOnErrors();
            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            String currSourcePath = PropertiesHandler.expand(sourcePath, params, gvBuffer);
            String currFile = PropertiesHandler.expand(filePattern, params, gvBuffer);

            Set<String> results;
            
            if (recursive) {
            	
            	results= Files.walk(Paths.get(currSourcePath))					        	     
    					     .filter(p -> p.toAbsolutePath().toString().matches(Optional.ofNullable(currFile).orElse(".*")))
    					     .map(p -> returnFullPath? p.toAbsolutePath().toString():p.getFileName().toString())    					     
    					     .collect(Collectors.toSet());
            	
            } else {
            	results = Files.list(Paths.get(currSourcePath))
            			       .filter(p -> p.getFileName().toString().matches(Optional.ofNullable(currFile).orElse(".*")))
					           .map(p -> returnFullPath? p.toAbsolutePath().toString():p.getFileName().toString())    					     
					           .collect(Collectors.toSet());
            }
                      
           
            gvBuffer.setProperty(GVFM_FOUND_FILES_NUM, String.valueOf(results.size()));
            
           
            if (results.size() > 0) {                
                gvBuffer.setProperty(GVFM_FOUND_FILES_LIST, results.stream().collect(Collectors.joining(";")));
            }
            
            if (returnCollection) {
            	gvBuffer.setObject(results);
            }
        }  catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                {"message", exc.getMessage()}}, exc);
        }    finally {
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
