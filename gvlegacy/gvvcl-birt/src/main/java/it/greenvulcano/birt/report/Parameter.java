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
package  it.greenvulcano.birt.report;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 * This is the interface that represents the parameters of a report.
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public interface Parameter {
    static String TYPE_STRING         = "STRING";
    static String TYPE_INTEGER        = "INTEGER";
    static String TYPE_DATE           = "DATE";
    static String TYPE_DATE_TIME      = "DATE_TIME";
    static String TYPE_TIME           = "TIME";
    static String TYPE_DECIMAL        = "DECIMAL";
    static String TYPE_FLOAT          = "FLOAT";
    static String TYPE_BOOLEAN        = "BOOLEAN";
    static String TYPE_ANY            = "ANY";
    static String CONTROL_TYPE_TEXT   = "TEXT";
    static String CONTROL_TYPE_SELECT = "SELECT";
    static String CONTROL_TYPE_RADIO  = "RADIO";
    static String CONTROL_TYPE_CHECK  = "CHECK";

    static String SOURCE_TYPE_NONE    = "NONE";
    static String SOURCE_TYPE_FIXED   = "FIXED";
    static String SOURCE_TYPE_STRING  = "STRING";
    static String SOURCE_TYPE_DH      = "DATA_HANDLER";


    /**
     * Receive a node of configuration file xml and initialize the {@link Parameter}
     * depending on parameters of such file. The behavior is differents depending
     * on implementation of this interface.
     * @param node
     *            The configuration node containing all informations.
     * @param task
     * @param scalar
     * @param report
     * @throws Exception
     */
    void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception;

    
    String getName();

    String getLabel();

    String getType();

    String getControlType();

    String getDefValue();

    String getFormat();

    List<String> getValueList();

    void setData(HttpSession session, Map<String, String> params) throws Exception;

    boolean isRequired();

    boolean isDefaultParam();

    String getExpression();

    Object convertToValue(String val) throws Exception;

    String convertFromValue(Object val) throws Exception;
}
