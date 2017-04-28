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
package tests.unit.vcl.file.writer;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;
import junit.framework.TestCase;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.writer.FileWriter;

/**
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 */

public class FileWriterTest extends TestCase {
	
	private static final String TEST_FILE_WRITER         = "fileWrite_test2.txt";
	private static final String TEST_FILE_DIR             = "TestFileManager";
	private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
            + TEST_FILE_DIR;

	/**
     * @see junit.framework.TestCase#setUp()
     */
	
	@Override
	public void setUp() throws Exception {
		
		XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
		super.setUp();
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
		FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
	}

	/**
     * @see junit.framework.TestCase#tearDown()
     */	
	@Override
	public void tearDown() throws Exception {
		
		FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
		super.tearDown();
		
	}

	/**
     * Test FileWriter
     * 
     * @see it.greenvulcano.gvesb.virtual.file.writer.FileWriter
     * 
     * @throws Exception
     *         if any error occurs
     */
	public void testWriteFile() throws Exception
	{
		Node node = XMLConfig.getNode("GVSystems.xml","//filewriter-call[@name='test_write_file']");
		FileWriter fw = new FileWriter();
		fw.init(node);
		
		GVBuffer gvBuffer = new GVBuffer("TEST", "FILEWRITER-CALL");
		gvBuffer.setProperty("filename", TEST_FILE_WRITER);
		gvBuffer.setObject("12345678");
		fw.perform(gvBuffer);
		assertTrue("Resource " + TEST_FILE_WRITER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_WRITER).exists());
		assertTrue("Resource" + TEST_FILE_WRITER + " has a different length ",new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_WRITER).length()==8);
        
	}

}
