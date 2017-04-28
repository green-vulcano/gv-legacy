/*******************************************************************************
 * Copyright (c) 2009, 2017 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package it.greenvulcano.gvesb.rsh;

import java.rmi.RemoteException;

import org.w3c.dom.Node;

import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;

/**
 * 
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */
public interface RSHServiceClient extends RSHService
{

	/**
	 * 
	 * @param node
	 * @throws RSHException
	 */
    public void init(Node node) throws RSHException;

    /**
     * 
     * @param fileName
     * @return byte[]
     * @throws RemoteException
     * @throws RSHException
     */
    public byte[] getFileB(String fileName) throws RemoteException, RSHException;

    /**
     * 
     * @param fileName
     * @param content
     * @throws RemoteException
     * @throws RSHException
     */
    public void sendFileB(String fileName, byte[] content) throws RemoteException, RSHException;

    /**
     * 
     * @return String
     */
    public String getName();

    public void invalidate();

    /**
     * 
     * @return boolean
     */
    public boolean isValid();

    public void cleanup();
}
