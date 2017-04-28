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
package it.greenvulcano.gvesb.virtual.birt.report;

import it.greenvulcano.gvesb.virtual.birt.report.BIRTReportCallOperation;
import it.greenvulcano.birt.report.Report;
import it.greenvulcano.birt.report.ReportManager;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import org.slf4j.Logger;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * 
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public class BIRTReportCallOperation implements CallOperation
{
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BIRTReportCallOperation.class);

    private OperationKey  key        = null;
    
    //private Report report = null;
    
    /**
     * Reference to group name
     */
    private String        groupName  = null;
    
    /**
     * Reference to report name
     */
    private String        reportName = null;
    
    /**
     * Report format, default to pdf. The attribute's admitted values are:
	 * pdf
     * excel
     * html 
     */
    private String        reportType = null;

    /**
     *  Invoked from <code>OperationFactory</code> when an <code>Operation</code>
     *  needs initialization.<br>
     * 
     * @param node
     * 			The configuration node containing all informations.
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     * @throws InitializationException
     */
    @Override
    public void init(Node node) throws InitializationException
    {
        try {
        	reportType = XMLConfig.get(node, "@reportType", "pdf");
            /*Node rn = XMLConfig.getNode(node, "Report");
            if (rn != null) {
                report = new Report();
                report.init(rn);
                logger.debug("Configured for Local BIRT Report[" + reportType + "]: " + report.getName());
            }
            else {*/
            groupName = XMLConfig.get(node, "@groupName");
            reportName = XMLConfig.get(node, "@reportName");
            logger.debug("Configured for Generic BIRT Report[" + reportType + "]: " + groupName + "/" + reportName);
            //}
        }
        catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /**
     * @param gvBuffer 
     * 			The GVBuffer to be used within the service
     * @return the GVBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     * 
     * @throws ConnectionException, CallException, InvalidDataException
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            Report rep = ReportManager.instance().getReport(groupName, reportName);
            String repType = gvBuffer.getProperty("BIRT_REPORT_TYPE");
            if ((repType == null) || "".equals(repType)) {
                repType = reportType;
            }
            //
            props.put("ORDER_NUMBER", "10201"); //TODO
            //
            byte[] data = rep.generate(props, repType);
            gvBuffer.setObject(data);
            gvBuffer.setProperty("BIRT_REPORT_TYPE", repType);
        }
        catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);

        }
        return gvBuffer;
    }

    /**
     * do nothing
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub

    }

    /**
     * Set the key
     * 
     * @param key
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    @Override
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * Returns the key
     * 
     * @return OperationKey
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    @Override
    public OperationKey getKey()
    {
        return key;
    }

    /**
     * Returns the service
     * 
     * @param gvBuffer 
     * 			The GVBuffer to be used within the service
     * @return String
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @Override
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * do nothing
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    @Override
    public void destroy()
    {
        // TODO Auto-generated method stub

    }
}
