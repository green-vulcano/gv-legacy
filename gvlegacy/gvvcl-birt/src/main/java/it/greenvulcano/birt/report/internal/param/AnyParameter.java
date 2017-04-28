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
package it.greenvulcano.birt.report.internal.param;

/**
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public class AnyParameter extends BaseParameter {
    
	private String type = TYPE_ANY;

	/**
	 * Returns a String containing "ANY"
	 * 
	 * @return String
	 */
	public String getType() {
        return type;
    }

	/**
	 * Returns an empty String
	 * 
	 * @return String
	 * @see it.greenvulcano.birtreport.config.Parameter#getFormat()
	 */
	@Override
	public String getFormat()
	{
	    return "";
	}
	
	/**
	 * Override toString() method returning
	 * 	"param[type: " + type + " - " + getName() + "]"
	 * 
	 * @return String
	 * @see java.lang.Object#toString()
	 */

	@Override
	public String toString() {
		return "param[type: " + type + " - " + getName() + "]";
	}

	/**
	 * Returns the parameter passed
	 * 
	 * @param val
	 * @return Object
	 * @throws Exception
	 * @see it.greenvulcano.birtreport.config.Parameter#convertToValue()
	 */
	
	@Override
	public Object convertToValue(String val) throws Exception {
		return val;
	}
	
	/**
	 * Returns val.toString()
	 * 
	 * @param val
	 * @return String
	 * @throws Exception
	 * @see it.greenvulcano.birtreport.config.Parameter#convertFromValue()
	 */

	@Override
	public String convertFromValue(Object val) throws Exception {
	    return val.toString();
	}
}