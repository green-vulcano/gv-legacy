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
import it.greenvulcano.expression.ognl.OGNLExpressionEvaluator;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Upload a local file/directory, or the GVBuffer body, on a remote file system.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVUpload implements GVRemoteCommand
{
    private static final Logger logger = LoggerFactory.getLogger(GVUpload.class);

    /**
     *  Pathname of the local directory where the file is transferred. 
     *  Must be an absolute pathname.
     */
    private String              sourcePath;
    
    /**
     *  The name of the file to transfer.
     */
    private String              sourceFilePattern;
    
    /**
     *  Pathname of remote directory where upload the file. It can be an absolute pathname or relative, 
     *  in the second case it is relative to home directory of ftp user. 
     */
    private String              remotePath;
    
    /**
     *  OGNL Expression applied to the current GVBuffer that specifies how to build the file content. 
     *  In the expression, the GVBuffer is associated with #object, and must return a byte[]. 
     */
    private String              fromGVBufferExpression;
    private boolean             isCritical;
    private Map<String, String> optProperties = new HashMap<String, String>();


    /**
     * do nothing
     */
    public GVUpload()
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
        sourcePath = XMLConfig.get(node, "@sourcePath");
        sourceFilePattern = XMLConfig.get(node, "@sourceFilePattern");
        remotePath = XMLConfig.get(node, "@remotePath");
        fromGVBufferExpression = XMLConfig.get(node, "@fromGVBufferExpression");
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
        if (fromGVBufferExpression != null) {
            return ((isCritical ? "[CRITICAL] " : "") + "UPLOAD GVBuffer body to file '" + sourceFilePattern
                    + "' in directory " + remotePath);
        }
        return ((isCritical ? "[CRITICAL] " : "") + "UPLOAD  file(s) '" + sourceFilePattern + "' from directory "
                + sourcePath + " to " + remotePath);
    }


    /**
     *  Invoked from <code>RemoteManagerCall</code> when the Command
     *  needs to be execute.<br>
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
            String currSourcePath = PropertiesHandler.expand(sourcePath, params, gvBuffer);
            String currSourceFile = PropertiesHandler.expand(sourceFilePattern, params, gvBuffer);
            String currRemotePath = PropertiesHandler.expand(remotePath, params, gvBuffer);

            Map<String, String> localOptProperties = new HashMap<String, String>();
            for (String prop : optProperties.keySet()) {
                localOptProperties.put(prop, PropertiesHandler.expand(optProperties.get(prop), params, gvBuffer));
            }
            
            boolean result = false;
            if ((fromGVBufferExpression != null) && (fromGVBufferExpression.length() > 0)) {
                OGNLExpressionEvaluator ognl = new OGNLExpressionEvaluator();
                ognl.addToContext("gvbuffer", gvBuffer);
                Object obj = ognl.getValue(fromGVBufferExpression, gvBuffer);
                InputStream is = new ByteArrayInputStream((byte[]) obj);
                result = manager.put(is, currRemotePath, currSourceFile, localOptProperties);
            }
            else {
                result = manager.put(currSourcePath, currSourceFile, currRemotePath, localOptProperties);
            }
            if (result) {
                logger.debug("File(s) " + currSourceFile + " successfully uploaded from local directory "
                        + currSourcePath + " to remote directory " + currRemotePath);
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
