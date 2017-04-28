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
package  it.greenvulcano.birt.report.internal.field;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 4.0.0 Mar,2017
 * @author GreenVulcano Developer Team
 */

public abstract class Source {

    String type;

    /**
     * 
     * @param source
     */
    abstract public void init(Node source);

    /**
     * 
     * @return String
     */
    public String getType()
    {
        return type;
    }

    /**
     * 
     * @param type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * 
     * @param params
     * @return Object
     * @throws Exception
     */
    abstract public Object getData(Map<String, String> params) throws Exception;
}
