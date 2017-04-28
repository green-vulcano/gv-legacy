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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.utils.dao.DataAccessObject;

/**
 * It is used for manage the source
 * 
 * @version 4.0.0 Mar,2017
 * @author GreenVulcano Developer Team
 */

public class DHSource extends Source {

    private String  service;
    private boolean useStringMap = true;
    private boolean sort         = false;
    private Map<String, String> sourceProps  = new HashMap<String, String>();

    /**
     * Auto-generated constructor stub
     */
    
    public DHSource()
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
        type = Parameter.SOURCE_TYPE_DH;
        try {
            service = XMLConfig.get(source, "@service");
            useStringMap = XMLConfig.getBoolean(source, "@use-string-map", true);
            sort = XMLConfig.getBoolean(source, "@sort", false);
            NodeList nl = XMLConfig.getNodeList(source, "DHVariable");
            for (int i = 0; i < nl.getLength(); i++) {
                Node nv = nl.item(i);
                sourceProps.put(XMLConfig.get(nv, "@name"), XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
            }
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
    
    public Object getData(Map<String, String> params) throws Exception
    {
        Object out = null;
        Map<String, String> localParams = new HashMap<String, String>(params);
        Iterator<String> p = sourceProps.keySet().iterator();
        while (p.hasNext()) {
            String name = p.next();
            if (!localParams.containsKey(name)) {
                localParams.put(name, sourceProps.get(name));
            }
        }
        if (useStringMap) {
            List<LabelValueBean> entries = new ArrayList<LabelValueBean>();
            Map<String, String> values = DataAccessObject.getStringMap(service, localParams);
            Iterator<String> i = values.keySet().iterator();
            while (i.hasNext()) {
                String name = i.next();
                entries.add(new LabelValueBean(name, values.get(name)));
            }
            if (sort) {
                Collections.sort(entries, new FieldValueComparator());
            }
            out = entries;
        }
        else {
            XmlObject doc = DataAccessObject.getDataAsXML(service, localParams);
            XmlObject root = doc.selectPath("/*")[0];
            out = root;
        }
        return out;
    }

    /**
     * Invoked when needs to return sourceProps.
     * 
     * @return Map
     */
    
    public Map<String, String> getSourceProps()
    {
        return sourceProps;
    }
}
