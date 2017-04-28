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
package tests.unit.birt;

import it.greenvulcano.birt.report.Report;
import it.greenvulcano.birt.report.ReportManager;
import it.greenvulcano.configuration.XMLConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class BIRTTestCase extends TestCase
{
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
    	XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
    }

    public void testSimple() throws Exception
    {
        File outputTarget = new File("target" + File.separator + "output");
        outputTarget.mkdirs();
        
        /*
        File outputPDF = new File(outputTarget, "hello_world.pdf");
        r = ReportManager.instance().getReport("TestGRP", "Hello World");  // TODO richiede hello_world.rptdesign
        os = new FileOutputStream(outputPDF);
        r.generate(os, null, "pdf");
        os.close();
		*/
        
        /*
        r = ReportManager.instance().getReport("TestGRP", "Test");
        os = new FileOutputStream("target" + File.separator + "output" + File.separator + "test.html"); // TODO richiede test.rptdesign
        r.generate(os, null, "html");
        os.close(); */
        
        
        Report r;
        OutputStream os;
        HashMap<String, Object> p = new HashMap<String, Object>();
        p.put("ORDER_NUMBER", "10201");
        r = ReportManager.instance().getReport("TestGRP", "Sales Invoice Expr");
        os = new FileOutputStream("target" + File.separator + "output" + File.separator + "SalesInvoiceExpr.html");
        r.generate(os, p, "html");
        os.close();
        
        p = new HashMap<String, Object>();
        p.put("OrderNumber", "10201");
        r = ReportManager.instance().getReport("TestGRP", "Sales Invoice Param");
        os = new FileOutputStream("target" + File.separator + "output" + File.separator + "SalesInvoiceParam.pdf");
        r.generate(os, p, "pdf");
        os.close();
        
        p = new HashMap<String, Object>();
        p.put("ORDER_NUMBER", "10201");
        r = ReportManager.instance().getReport("TestGRP", "Sales Invoice Excel");
        os = new FileOutputStream("target" + File.separator + "output" + File.separator + "SalesInvoiceExcel.xls");
      //The interface org.eclipse.birt.report.engine.api.IRenderOption has only the options "pdf" and "html" not excel.
        r.generate(os, p, "html"); 
        os.close(); 

        assertTrue(true);
    }

    public void testMultiThreadPDF() throws Exception
    {
        File outputTarget = new File("target" + File.separator + "output");
        outputTarget.mkdirs();

        int n = 100;
        final Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            final Integer j = i;
            threads[i] = new Thread() {
                public void run()
                {
                    OutputStream os = null;
                    try {
                        Map<String, Object> p = new HashMap<String, Object>();
                        p.put("OrderNumber", Integer.toString(10200 + j));
                        Report r = ReportManager.instance().getReport("TestGRP", "Sales Invoice Param");
                        os = new FileOutputStream("target" + File.separator + "output" + File.separator
                                + "SalesInvoiceParam_" + j + ".pdf");
                        r.generate(os, p, "pdf");
                    }
                    catch (Exception exc) {
                        synchronized (j) {
                            System.err.println(j + " *++++++++++++++++++++++++++++++++++++++");
                            exc.printStackTrace();
                            System.err.println(j + " *++++++++++++++++++++++++++++++++++++++");
                        }
                    }
                    finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                        }
                        catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
        for (int i = 0; i < n; i++) {
            threads[i].start();
        }
        for (int i = 0; i < n; i++) {
            threads[i].join();
        }
        assertTrue(true);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();        
    }
    
}
