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

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.remotefs.RemoteManager;

import org.w3c.dom.Node;

/**
 * Defines methods for remote file commands classes.
 *
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 */
public interface GVRemoteCommand
{
    public static final String GVRM_FOUND_FILES_NUM  = "GVRM_FOUND_FILES_NUM";
    public static final String GVRM_FOUND_FILES_LIST = "GVRM_FOUND_FILES_LIST";

    /**
     *
     * @param node
     * 			The configuration node containing all informations.
     * 
     * @throws Exception
     */
    void init(Node node) throws Exception;

    /**
     * @param manager
     * 			A private instance of <code>RemoteManager</code> class to perform FTP
     * 			 operations.
     * 
     * @param gvBuffer
     * 			The GVBuffer to be used within the service
     * 
     * @throws Exception
     */
    void execute(RemoteManager manager, GVBuffer gvBuffer) throws Exception;

    /**
     * @return true if should abort on error.
     */
    boolean isCritical();
}
