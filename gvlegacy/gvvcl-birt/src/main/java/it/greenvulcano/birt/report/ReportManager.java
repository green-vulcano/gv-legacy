/*******************************************************************************
 * Copyright (c) 2009, 2017 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.birt.report;

import it.greenvulcano.birt.report.exception.BIRTException;
import it.greenvulcano.birt.report.internal.ReportRenderOptions;
import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 */
public class ReportManager implements ShutdownEventListener, ConfigurationListener
{
    private static ReportManager             instance         = null;
    public static String                     BIRT_CFG_FILE    = "GVBIRTReport-Configuration.xml";

    private IReportEngine                    engine           = null;
    private String                           reportEngineHome = null;
    private String                           logLevel         = null;
    private Map<String, ReportRenderOptions> renders          = new HashMap<String, ReportRenderOptions>();

    private Map<String, Group>               groups           = new HashMap<String, Group>();
    private boolean                          isReportsInit    = false;

    /**
     * Constructor. Calls his method init()
     * 
     * @throws BIRTException
     * @see {@link it.greenvulcano.birt.report.ReportManager#init()}
     */
    private ReportManager() throws BIRTException
    {
        init();
    }

    /**
     * If there is already an instance of ReportManager, returns it.
     * If there isn't, make a new instance and returns it.
     * 
     * @return ReportManager
     * @throws BIRTException
     */
    public static synchronized ReportManager instance() throws BIRTException
    {
        if (instance == null) {
            instance = new ReportManager();
            ShutdownEventLauncher.addEventListener(instance);
            XMLConfig.addConfigurationListener(instance, BIRT_CFG_FILE);
        }
        return instance;
    }

    /**
     * 
     * @throws BIRTException
     */
    private void init() throws BIRTException
    {
        try { // inizializzo l'engine
            Node engNode = XMLConfig.getNode(BIRT_CFG_FILE, "/GVBIRTReportConfiguration/Engine");
            reportEngineHome = PropertiesHandler.expand(XMLConfig.get(engNode, "@reportEngineHome",
                    "sp{{gv.app.home}}/BIRTReportEngine"));
            logLevel = XMLConfig.get(engNode, "@logLevel", "FINEST");
            NodeList rnl = XMLConfig.getNodeList(engNode, "Renders/*[@type='report-render']");
            if ((rnl != null) && (rnl.getLength() > 0)) {
                for (int i = 0; i < rnl.getLength(); i++) {
                    Node n = rnl.item(i);
                    System.out.println("---" +  Class.forName(XMLConfig.get(n, "@class")).newInstance());
                    ReportRenderOptions opt = (ReportRenderOptions) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                   
                    System.out.println("---" + opt);
                    opt.init(n);
                    renders.put(opt.getType(), opt);
                }
            }

            EngineConfig config = new EngineConfig();
            //config.setBIRTHome(reportEngineHome);
            config.setLogConfig(reportEngineHome + File.separator + "log", Level.parse(logLevel));
            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT Engine", exc);
        }

        initReport();
    }

    /**
     * Get the configuration from the birt configuration file.
     * If already call this method, do nothing
     * 
     * @throws BIRTException
     */
    private void initReport() throws BIRTException
    {
        if (isReportsInit) {
            return;
        }

        try {// inizializzo i gruppi e di conseguenza i report e i parametri
            NodeList nl = XMLConfig.getNodeList(BIRT_CFG_FILE, "/GVBIRTReportConfiguration/ReportGroups/ReportGroup");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                Group g = new Group();
                g.init(n);

                groups.put(g.getName(), g);
            }
            isReportsInit = true;
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing reports", exc);
        }
    }

    /**
     * Returns a List contains the names of Group withing ReportManager.
     * If there aren't groups, return null
     * 
     * @return List<String>
     * @throws Exception
     */
    public List<String> getGroupsName() throws Exception
    {
        initReport();

        List<String> l = new ArrayList<String>(0);
        String key = null;

        for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(key);
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Returns a List contains Group instances
     * 
     * @return List<Group>
     * @throws Exception
     */
    public List<Group> getGroups() throws Exception
    {
        initReport();

        List<Group> l = new ArrayList<Group>(0);
        String key = null;

        for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(groups.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * 
     * Returns a List contains the names of reports within the group's name
     * 
     * @return List<String>
     * @param group
     * @throws Exception
     */
    public List<String> getReportsName(String group) throws Exception
    {
        initReport();

        List<String> l = new ArrayList<String>(0);
        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        l = g.getReportsNames();

        return l;
    }

    /**
     * 
     * Returns a List contains the names of reports within the group
     * 
     * @return List<String>
     * @param g
     */
    public List<String> getReportsName(Group g)
    {
        List<String> l = new ArrayList<String>(0);
        String name = null;
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            name = it.next();
            l.add(name);
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Returns a List<Report> contains reports instances depending on group's name
     * 
     * @return List<Report>
     * @param group
     * @throws Exception
     */
    public List<Report> getReports(String group) throws Exception
    {
        initReport();

        List<Report> l = new ArrayList<Report>(0);
        String key = null;
        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(reports.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Returns a List<Report> contains reports instances depending on group instance
     * 
     * @return List<Report>
     * @param g
     */
    public List<Report> getReports(Group g)
    {
        List<Report> l = new ArrayList<Report>(0);
        String key = null;
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(reports.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Returns a report instance depending on the parameters
     * 
     * @param group
     * @param report
     * @return Report
     * @throws Exception
     */
    public Report getReport(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        reportChosen(r);
        return r;
    }

    /**
     * Returns the parameters of report, within the group, depending on parameters of this method
     * 
     * @return List<Parameter>
     * @param group
     * @param report
     * @throws Exception
     */
    public List<Parameter> getParams(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        if (r == null) {
            return null;
        }
        return getParams(r);
    }

    /**
     * Returns a List contains the parameters within the report passed
     * 
     * @return List<Parameter>
     * @param r
     * @throws Exception
     */
    public List<Parameter> getParams(Report r) throws Exception
    {
        reportChosen(r);
        return r.getParamsList();
    }

    /**
     * Returns a List<String> about the parameters of a report , within a group, 
     * depending on the parameters passed to this method
     * 
     * @return List<String>
     * @param group
     * @param report
     * @throws Exception
     */
    public List<String> getParamsNames(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        if (r == null) {
            return null;
        }
        return getParamsNames(r);
    }

    /**
     * Returns a List<String> contains the names of parameters about the report instance passed
     * 
     * @return List<String>
     * @param r
     * @throws Exception
     */
    public List<String> getParamsNames(Report r) throws Exception
    {
        reportChosen(r);
        return r.getParamsNames();
    }

    /**
     * Returns a ordered List<String> about the parameters of a report , within a group, 
     * depending on the parameters passed to this method
     * 
     * @return List<String>
     * @param group
     * @param report
     * @throws Exception
     */
    public List<String> getOrderedParamsNames(String group, String report) throws Exception
    {
        List<String> l = getParamsNames(group, report);
        if (l == null) {
            return null;
        }
        Collections.sort(l);

        return l;
    }

    /**
     * Returns a ordered List<String> contains the names of parameters about the report instance passed
     * 
     * @return List<String>
     * @param r
     * @throws Exception
     */
    public List<String> getOrderedParamsNames(Report r) throws Exception
    {
        List<String> l = getParamsNames(r);
        if (l == null) {
            return null;
        }
        Collections.sort(l);

        return l;
    }

    /**
     * Returns engine task for running and rendering report directly to
	 * output format
     * 
     * @param reportConfig
     * @return IRunAndRenderTask
     * @throws BIRTException
     */
    public IRunAndRenderTask getTask(String reportConfig) throws BIRTException
    {
        try {
            IReportRunnable design = engine.openReportDesign(reportEngineHome + File.separator + "reports"
                    + File.separator + reportConfig);
            return engine.createRunAndRenderTask(design);
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT ReportRender for [" + reportConfig + "]", exc);
        }
    }

    /**
     * Returns the ReportRenderOptions depending on type passed
     * 
     * @param type
     * @return ReportRenderOptions
     */
    public ReportRenderOptions getDefaultReportRender(String type)
    {
        return renders.get(type);
    }

    /**
     * Prepares the report
     * 
     * @param report
     * @throws Exception
     */
    private synchronized void reportChosen(Report report) throws Exception
    {
        if (report == null) {
            return;
        }

        if (!report.isInitialized()) {
            IReportRunnable design = null;
            try {
                // Open a report design
                design = engine.openReportDesign(reportEngineHome + File.separator + "reports" + File.separator
                        + report.getReportConfig());
            }
            catch (Exception exc) {
                throw new BIRTException("Error initializing BIRT ReportDesign for [" + report.getReportConfig() + "]",
                        exc);
            }
            IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(design);

            report.init(renders, engine, design, task);
        }

        return;
    }

    /**
     * Deletes all setup
     */
    private void destroy()
    {
        renders.clear();
        groups.clear();
        try {
            if (engine != null) {
                engine.destroy();
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        finally {
            engine = null;
        }
        try {
            Platform.shutdown();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        ShutdownEventLauncher.removeEventListener(instance);
        XMLConfig.removeConfigurationListener(instance);
        instance = null;
    }

    /**
     * Calls destroy method
     * 
     * @param event
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    /**
     * 
     * @param event
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(BIRT_CFG_FILE)) {
            groups.clear();
            isReportsInit = false;
        }
    }
}