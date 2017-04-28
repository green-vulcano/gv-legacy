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
package it.greenvulcano.gvesb.virtual.file;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.file.command.GVFileCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Executes sequences of Command on local file system.
 *
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 *
 *
 */
public class FileManagerCall implements CallOperation
{
    private static Logger       logger   = LoggerFactory.getLogger(FileManagerCall.class);

    /**
     * The module instance's name.
     */
    private String              name     = null;

    /**
     * A List contains the istances of remote-commands
     */
    private List<GVFileCommand> commands = new ArrayList<GVFileCommand>();

    /**
     * The configured operation's key.
     */
    protected OperationKey      key      = null;

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
    public void init(Node node) throws InitializationException
    {
        try {
            name = XMLConfig.get(node, "@name");
            
            logger.debug("BEGIN FileManagerCall[" + name + "] initialization");
            NodeList nl = XMLConfig.getNodeList(node, "FileCommands/*[@type='file-command']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node cmdNode = nl.item(i);
                GVFileCommand comm = (GVFileCommand) Class.forName(XMLConfig.get(cmdNode, "@class")).newInstance();
                comm.init(cmdNode);
                logger.info("Initialized Command: " + comm);
                commands.add(comm);
            }
            logger.debug("END FileManagerCall[" + name + "] initialization");
        }
        catch (Exception exc) {
            logger.error("ERROR FileManagerCall[" + name + "] initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
    }

    /**
     * @param gvBuffer 
     * 			The GVBuffer to be used within the service
     * 
     * @return the GVBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     * 
     * @throws ConnectionException, CallException, InvalidDataException
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            for (GVFileCommand command : commands) {
                try {
                    command.execute(gvBuffer);
                }
                catch (Exception exc) {
                    if (command.isCritical()) {
                        logger.error("CRITICAL Command " + command.getClass().getSimpleName()
                                + " failed execution, exiting");
                        throw exc;
                    }
                    continue;
                }
            }

            return gvBuffer;
        }
        catch (Exception exc) {
            logger.error("ERROR FileManagerCall[" + name + "] execution", exc);
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
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * do nothing
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public void destroy()
    {
        commands.clear();
    }

    /**
     * Set the configured operation key
     * 
     * @param key
     * 			the configured operation key
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
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
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

}
