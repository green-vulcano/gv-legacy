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
package tests.unit.vcl.file.remote;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.file.remote.RemoteManagerCall;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.mockftpserver.fake.FakeFtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Node;

/**
 * Test <code>RemoteManagerCall</code>
 * 
 * @version 4.0.0 Mar, 2017
 * @author GreenVulcano Developer Team
 */
public class RemoteManagerTestCase extends TestCase
{
    //private static final String TEST_FILE_RESOURCES       = System.getProperty("user.dir") + File.separator
    //                                                              + "target" + File.separator + "test-classes";
    //private static final String TEST_FILE_DIR             = "TestFileManager";
    //private static final String TEST_FILE_DIR_RENAMED     = "TestFileManager_Renamed";
    private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
                                                                  + "TestFTP";
    private static final String TEST_FILE_MANAGER         = "fileManager_test.txt";

    private FakeFtpServer       fakeFtpServer;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
    	XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        super.setUp();
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));

        @SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("fakeFTP.xml");
        fakeFtpServer = (FakeFtpServer) context.getBean("FakeFtpServer");
        fakeFtpServer.start();        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        if (fakeFtpServer != null) {
            fakeFtpServer.stop();
        }
        super.tearDown();
    }

    /**
     * Test GVSearch
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVSearch
     * 
     * @throws Exception
     *         if any error occurs
     */
    public void testExistFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVSystems.xml", "//remotemanager-call[@name='test_check_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*\\.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testExistFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(2, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertTrue(result.getProperty("GVRM_FOUND_FILES_LIST").contains("Test0.txt"));
        assertTrue(result.getProperty("GVRM_FOUND_FILES_LIST").contains("Test1.txt"));
    }

    /**
     * Test GVDownload file
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVDownload
     * 
     * @throws Exception
     *         if any error occurs
     */
    public void testDownloadFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*\\.txt");
        rm.perform(gvBuffer);
        assertTrue("Resource Test0.txt not in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "Test0.txt").exists());
        assertTrue("Resource Test1.txt not in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "Test1.txt").exists());
    }

    /**
     * Test GVDownload directory
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVDownload
     * 
     * @throws Exception
     *         if any error occurs
     */
    public void testDownloadDir() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_dir_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        rm.perform(gvBuffer);
        assertTrue("Resource Test0.txt not in " + TEST_FILE_DEST_RESOURCES + File.separator, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + "Test0.txt").exists());
        
    }

    /**
     * Test GVDownload file in gvBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVDownload
     * 
     * @throws Exception
     */
    public void testDownloadFileInGVBuffer() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_download_file_gvbuffer_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", "Test0.txt");
        gvBuffer = rm.perform(gvBuffer);
        System.out.println("testDownloadFileInGVBuffer: " + gvBuffer.getObject());
        assertEquals("Content of resource Test0.txt not in GVBuffer", "abcdefghijklmnopqrstuvwxyz", gvBuffer.getObject());
    }

    /**
     * Test GVUpload file
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVUpload
     * 
     * @throws Exception
     *         if any error occurs
     */
    public void testUploadCheckFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setProperty("fileMask", ".*_test\\.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testUploadCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals(TEST_FILE_MANAGER, result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

    /**
     * Test GVUpload file in gvBuffer
     * 
     * @see it.greenvulcano.gvesb.virtual.file.remote.command.GVUpload
     * 
     * @throws Exception
     *         if any error occurs
     */
    public void testUploadGVBufferCheckFile() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//remotemanager-call[@name='test_upload_file_gvbuffer_remote']");
        RemoteManagerCall rm = new RemoteManagerCall();
        rm.init(node);
        GVBuffer gvBuffer = new GVBuffer("TEST", "REMOTEMANAGER-CALL");
        gvBuffer.setObject("1234567890");
        gvBuffer.setProperty("fileMask", "TestGVBuffer.txt");
        GVBuffer result = rm.perform(gvBuffer);
        System.out.println("testUploadGVBufferCheckFile: " + result.getProperty("GVRM_FOUND_FILES_LIST"));
        assertEquals(1, Integer.parseInt(result.getProperty("GVRM_FOUND_FILES_NUM")));
        assertEquals("TestGVBuffer.txt", result.getProperty("GVRM_FOUND_FILES_LIST"));
    }

}
