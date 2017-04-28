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
package it.greenvulcano.birt.report.exception;

/**
 * Manage the exceptions
 * 
 * @version 4.0.0 Mar,2017
 * @author GreenVulcano Developer Team
 * 
 */
public class BIRTException extends Exception
{

	private static final long serialVersionUID = 1L;

	/**
     * Auto-generated constructor stub
     */
    public BIRTException()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Calls the constructor of superclass with the parameter String
     * 
     * @param message
     */
    public BIRTException(String message)
    {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Calls the constructor of superclass with the parameter Throwable
     * 
     * @param cause
     */
    public BIRTException(Throwable cause)
    {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Calls the constructor of superclass with the parameters String, Throwable
     * 
     * @param message
     * @param cause
     */
    public BIRTException(String message, Throwable cause)
    {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
