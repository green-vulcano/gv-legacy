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
package tests.unit.vcl.file.reader;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.reader.AnalyzeDirCall;
import junit.framework.TestCase;

/**
 * 
 * Test <code>AnalyzeDirCall</code>.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */

public class AnalyzeDirTest extends TestCase {
	
	private static final String TEST_FILE_DIR   = "TestFileManager";
	private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
            + TEST_FILE_DIR;
	
	/**
     * @see junit.framework.TestCase#setUp()
     */
	
	@Override
	public void setUp() throws Exception
	{		
		XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
		super.setUp();
		FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
	}

	/**
     * Tests analyze directories
     * 
     * @throws Exception
     *         if any error occurs
     */
	
	public void testAnalyzeDir() throws Exception
	{
		Node node = XMLConfig.getNode("GVSystems.xml","//fsmonitor-call[@name='test_analyze_dir']");
		AnalyzeDirCall ad = new AnalyzeDirCall();
		ad.init(node);
		GVBuffer gvBuffer = new GVBuffer("TEST", "ANALYZEDIR-CALL");
		ad.perform(gvBuffer);
        
	}
	
	/**
     * @see junit.framework.TestCase#tearDown()
     */
	
	@Override
	public void tearDown() throws Exception
	{	
		super.tearDown();
	}

}
