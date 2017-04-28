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

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public class BooleanParameter extends BaseParameter {

    private String        type   = TYPE_BOOLEAN;

    /**
     * Calls the super.init(node, task, scalar, report)
     * 
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
            throws Exception {
        super.init(node, task, scalar, report);
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
     * Returns type
     * 
     * @return String
     * 
     * @see it.greenvulcano.birtreport.config.Parameter#getType()
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Returns an empty String
     * 
     * @return String
     * 
     * @see it.greenvulcano.birtreport.config.Parameter#getFormat()
     */
    @Override
    public String getFormat()
    {
        return "";
    }

    /**
     * Returns the String representation of (Boolean) val
     * 
     * @param val
     * @return String
     * @throws Exception
     * 
     * @see it.greenvulcano.birtreport.config.Parameter#convertFromValue(Object val)
     */
   @Override
   public String convertFromValue(Object val) throws Exception {
        return ((Boolean)val).toString();
    }

   /**
    * If val.equals("1") the returns the boolean object True,
    * otherwise parses the string argument as a boolean and then returns it.
    * 
    * @param val
    * @return Object
    * @throws Exception
    * 
    * @see it.greenvulcano.birtreport.config.Parameter#convertToValue(String val)
    */
    @Override
	public Object convertToValue(String val) throws Exception {

		/*
		 * da aggiustare una volta decisi i formati che usiamo (??)
		 */
		Boolean True = new Boolean(true);
		if(val.equals("1")) return True;
		return Boolean.parseBoolean(val);
	}
}