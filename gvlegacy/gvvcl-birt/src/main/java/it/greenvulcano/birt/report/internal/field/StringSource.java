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
package it.greenvulcano.birt.report.internal.field;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * It is used for manage the source
 * 
 * @version 4.0.0 Mar,2017
 * @author GreenVulcano Developer Team
 */

public class StringSource extends Source {

    private String value = "";

    /**
     * Auto-generated constructor stub
     */
    
    public StringSource()
    {
        // do nothing
    }

    /**
     * Invoked from <code>BaseParameter</code>
     *
     * @param source
     * 			The configuration node containing all informations.
     */
    
    public void init(Node source)
    {
        try {
            type = Parameter.SOURCE_TYPE_STRING;
            value = XMLConfig.get(source, ".").trim();
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * @param params
     * @return Object
     * @exception Exception
     */
    
    public Object getData(Map<String, String> params)
    {
        try {
            return PropertiesHandler.expand(value, MapUtils.convertToHMStringObject(params));
        }
        catch (Exception exc) {
            // do nothing
        }
        return "";
    }
}
