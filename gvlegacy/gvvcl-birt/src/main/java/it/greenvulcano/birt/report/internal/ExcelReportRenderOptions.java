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
package it.greenvulcano.birt.report.internal;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.w3c.dom.Node;

import it.greenvulcano.birt.report.exception.BIRTException;

/**
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public class ExcelReportRenderOptions implements ReportRenderOptions

{
	  /**
	   * do nothing
	   * 
	   * @param node
	   * 	The configuration node containing all informations.
	   * @throws BIRTException
	   * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#init(Node node)
	   */
	
	  public void init(Node node) throws BIRTException
	    {
	        try {
	            // do nothing
	        }
	        catch (Exception exc) {
	            throw new BIRTException("Error initializing Excel Report Options", exc);
	        }
	    }

	  /**
	     * Returns "excel"
	     * 
	     * @return String
	     * 
	     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getType()
	     */
	    @Override
	    public String getType()
	    {
	        return "excel";
	    }

	    /**
	     * Istance EXCELRenderOption and then set output format as "html". Return the istance
	     * 
	     * @return RenderOption
	     * @see it.greenvulcano.birt.report.internal.ReportRenderOptions#getOptions()
	     */
	    @Override
	    public RenderOption getOptions()
	    {
	        EXCELRenderOption options = new EXCELRenderOption();
	        //options.setOutputFileName("/home/gianluca/tmp/java/eclipse/birt-runtime-2_6_1/ReportEngine/output/test.html");
	        options.setOutputFormat("html");
	        //options.setHtmlRtLFlag(false);
	        //options.setEmbeddable(false);
	        //options.setImageDirectory("C:\\test\\images");

	        return options;
	
	        
//	    /**
//	 * 
//	 */
//	static final long serialVersionUID = 1L; //TODO
//
//	/**
//     *
//     */
//    public ExcelReportRenderOptions()
//    {
//        // TODO Auto-generated constructor stub
//    }
//
//    /**
//     * @param message
//     */
//    public ExcelReportRenderOptions(String message)
//    {
//        super(message);
//        // TODO Auto-generated constructor stub
//    }
//
//    /**
//     * @param cause
//     */
//    public ExcelReportRenderOptions(Throwable cause)
//    {
//        super(cause);
//        // TODO Auto-generated constructor stub
//    }
//
//    /**
//     * @param message
//     * @param cause
//     */
//    public ExcelReportRenderOptions(String message, Throwable cause)
//    {
//        super(message, cause);
//        // TODO Auto-generated constructor stub
//    }

}
	    }
