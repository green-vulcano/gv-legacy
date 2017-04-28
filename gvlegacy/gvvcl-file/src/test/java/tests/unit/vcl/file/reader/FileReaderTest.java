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

import junit.framework.TestCase;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.reader.FileReader;

/**
 * 
 * Test <code>FileReader</code>.
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 * 
 * 
 */

public class FileReaderTest extends TestCase {
	
	private static final String TEST_FILE_READER         = "fileRead_test.txt";
	private static final String TEST_FILE_READER_XML      = "fileRead_test.xml";
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
        //FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
		FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
	}
	
	/**
     * @see junit.framework.TestCase#tearDown()
     */
	
	@Override
	public void tearDown() throws Exception {
		
		//FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
		super.tearDown();
		
	}
	
	/**
     * Test <code>FileReader</code> with a txt file
     * 
     * @see it.greenvulcano.gvesb.virtual.file.reader.FileReader
     * 
     * @throws Exception
     *         if any error occurs
     */
	
	public void testReadFile() throws Exception
	{		
		String content = "12345678";
		
	    FileUtils.write(new File(
               TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_READER), content.subSequence(0, content.length()) );
	    
		
		Node node = XMLConfig.getNode("GVSystems.xml","//filereader-call[@name='test_read_file']");
		FileReader fr = new FileReader();
		fr.init(node);
		GVBuffer gvBuffer = new GVBuffer("TEST", "FILEREADER-CALL");
		fr.perform(gvBuffer);
		byte[] data = (byte[]) gvBuffer.getObject();
		
		assertEquals(content,new String(data));
		
		assertTrue("Resource " + TEST_FILE_READER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_READER).exists());
		
	}
	
	/**
     * Test <code>FileReader</code> with a xml file.
     * 
     * @see it.greenvulcano.gvesb.virtual.file.reader.FileReader
     * 
     * @throws Exception
     *         if any error occurs
     */
	
	public void testReadFileXML() throws Exception
	{		
		String content = "<tag>ciao</tag>";
		
	    FileUtils.write(new File(
               TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_READER_XML), content.subSequence(0, content.length()));
	    
		
		Node node = XMLConfig.getNode("GVSystems.xml","//filereader-call[@name='test_read_file_xml']");
		FileReader fr = new FileReader();
		fr.init(node);
		GVBuffer gvBuffer = new GVBuffer("TEST", "FILEREADER-CALL");
		fr.perform(gvBuffer);
		
		assertEquals(gvBuffer.getObject().toString(),content);
		
		assertTrue("Resource " + TEST_FILE_READER_XML + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_READER_XML).exists());
		
	}

}
