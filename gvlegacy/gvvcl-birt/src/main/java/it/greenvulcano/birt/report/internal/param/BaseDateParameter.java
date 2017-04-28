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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public abstract class BaseDateParameter extends BaseParameter
{
    protected String defaultFormat = "";

    private String format = null;

    
    /**
     * @param node
     * 			The configuration node containing all informations.
     * @param task
     * @param scalar
     * @param report
     * 
     * @throws Exception
     * @see
     * it.greenvulcano.birtreport.config.Parameter#init(org.eclipse.birt.report
     * .engine.api.IGetParameterDefinitionTask,
     * org.eclipse.birt.report.engine.api.IScalarParameterDefn,
     * org.eclipse.birt.report.engine.api.IReportRunnable)
     */
    @Override
    public void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception
    {
        super.init(node, task, scalar, report);
        this.format = scalar.getDisplayFormat();
        if (this.format == null) {
            this.format = defaultFormat;
        }
        if (node != null) {
            if (XMLConfig.exists(node, "@format")) {
                format = XMLConfig.get(node, "@format", this.format);
            }
        }
    }

    /**
     * Returns format
     * 
     * @return String
     * @see it.greenvulcano.birtreport.config.Parameter#getFormat()
     */
    @Override
    public String getFormat()
    {
        return format;
    }
    
    /**
	 * Override toString() method returning
	 * 	"param[type: " + getType() + " - " + getName() + " - " + format + "]"
	 * 
	 * @return String
	 * @see java.lang.Object#toString()
	 */

    @Override
    public String toString()
    {
        return "param[type: " + getType() + " - " + getName() + " - " + format + "]";
    }

    /**
     * Returns the conversion of String passed to a java.util.Date instance
     * 
     * @param val
     * @return Object
     * @throws Exception
     * @see it.greenvulcano.birtreport.config.Parameter#convertToValue(String val)
     */
    
    public Object convertToValue(String val) throws Exception
    {
        return DateUtils.stringToDate(val, format);
    }
    
    /**
     * Returns the conversion of Object passed to a java.lang.String representation.
     * 
     * @param val
     * @return String
     * @throws Exception
     * @see it.greenvulcano.birtreport.config.Parameter#convertFromValue(String val)
     */

    public String convertFromValue(Object val) throws Exception
    {
        return DateUtils.dateToString((Date) val, format);
    }
}