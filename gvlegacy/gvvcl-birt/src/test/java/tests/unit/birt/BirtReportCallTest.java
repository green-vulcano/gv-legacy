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


import org.junit.Ignore;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.birt.report.BIRTReportCallOperation;
import junit.framework.TestCase;

/**
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */

@Ignore("not teady yet")
public class BirtReportCallTest extends TestCase {
	@Ignore
	@Override
	public void setUp() throws Exception
	{		
		XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
		super.setUp();
	}
	@Ignore
	public void test() throws Exception{
		
		Node node = XMLConfig.getNode("GVSystems.xml","//birt-report-call[@name='TestBirt']");
		BIRTReportCallOperation birt = new BIRTReportCallOperation();
		birt.init(node);
		GVBuffer gvBuffer = new GVBuffer("TEST", "BIRTREPORT-CALL");
		birt.perform(gvBuffer);
		
		assertTrue(true);
		
	}
	@Ignore
	@Override
	public void tearDown() throws Exception
	{	
		super.tearDown();
	}

}
