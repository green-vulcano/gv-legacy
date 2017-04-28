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
package it.greenvulcano.birt.report;

import it.greenvulcano.configuration.XMLConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Rapresents logical group of reports. Groups reports with similar features
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public class Group {
    private String              name    = null;
    private Map<String, Report> reports = new HashMap<String, Report>();

    /**
     * Obtain the configuration about ReportGroup
     * 
     * @param node
     * 			The configuration node containing all informations.
     * 
     * @throws Exception
     */
    public void init(Node node) throws Exception {
        name = XMLConfig.get(node, "@name");
        NodeList nl = XMLConfig.getNodeList(node, "Report");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            Report r = new Report(n);
            reports.put(r.getName(), r);
        }

    }

    /**
     * Returns name
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the report instance depending the name of parameter
     * 
     * @param report
     * @return Report
     */
    public Report getReport(String report) {
        return reports.get(report);
    }

    /**
     * Returns the Map<String,Report> if isn't null, otherwise return null.
     * 
     * @return Map<String,Report> || null
     */
    public Map<String, Report> getReports() {
        if (reports.isEmpty())
            return null;
        return reports;
    }

    /**
     * Return a List<String> contains a set of keys about reports map
     * 
     * @return List<String> || null
     */
    public List<String> getReportsNames(){
        List<String> l = new ArrayList<String>(0);
        String name = null;
        if(reports == null) return null;

        for(Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            name = it.next();
            l.add(name);
        }
        if(l.isEmpty()) return null;
        return l;
    }

    /**
	 * Override toString() method returning
	 * 	"Group [" + name + "] - Reports: " + reports
	 * 
	 * @return String
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        return "Group [" + name + "] - Reports: " + reports;
    }

}