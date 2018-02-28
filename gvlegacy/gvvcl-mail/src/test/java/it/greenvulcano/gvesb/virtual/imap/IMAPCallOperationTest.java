/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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
package it.greenvulcano.gvesb.virtual.imap;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;

public class IMAPCallOperationTest extends TestCase {

	private static final String TEST_SYSTEM  = "TEST_SYSTEM";
    private static final String TEST_SERVICE = "TEST_SERVICE";
    private static final String TEST_SUBJECT = "Notifica SendEmail";
    private static final String TEST_MESSAGE = "Test Body";

    private Context             context;

    private static GreenMail    server       = null;
    private static ServerSetup  SMTP         = new ServerSetup(11025, null, ServerSetup.PROTOCOL_SMTP);
    private static ServerSetup  IMAP         = new ServerSetup(11143, null, ServerSetup.PROTOCOL_IMAP);

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    	XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
    	System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.processor.TransformerFactoryImpl");
        server = new GreenMail(new ServerSetup[]{SMTP, IMAP});
        server.setUser("test@gv.com", "password");
        server.setUser("test1@gv.com", "password");
        server.setUser("test2@gv.com", "password");
        server.setUser("test3@gv.com", "password");
        server.start();

        context = new InitialContext();
    }

    /**
     * Tests email receive
     * 
     * @throws Exception
     *         if any error occurs
     */
    public final void testReadEmail() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='ReadEmailsImap']");
        CallOperation op = new IMAPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT, TEST_MESSAGE, SMTP);
        GreenMailUtil.sendAttachmentEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT, TEST_MESSAGE, 
                TEST_MESSAGE.getBytes(), "text/plain", "test.txt", "simple attachment", SMTP);
        
        gvBuffer = op.perform(gvBuffer);
        
        //assertTrue(server.waitForIncomingEmail(5000, 2));
        
        assertEquals("2", gvBuffer.getProperty("IMAP_MESSAGE_COUNT"));

        Document doc = (Document) gvBuffer.getObject();
        System.out.println("Received messages:\n" + XMLUtils.serializeDOM_S(doc, "UTF-8", false, true));

        XMLUtils xml = null;
        try {
            xml = XMLUtils.getParserInstance();
            Node msg1 = xml.selectSingleNode(doc, "/MailMessages/Message[1]"); 
            Node msg2 = xml.selectSingleNode(doc, "/MailMessages/Message[2]");
            
            assertEquals(TEST_SUBJECT, xml.get(msg1,"Subject"));
            assertTrue(xml.get(msg1, ".//PlainMessage").startsWith(TEST_MESSAGE));
            assertEquals("test@gv.com", xml.get(msg1,"From"));
            assertEquals("test1@gv.com", xml.get(msg1,"To"));

            assertEquals(TEST_SUBJECT, xml.get(msg2,"Subject"));
            assertTrue(xml.get(msg2,".//PlainMessage").startsWith(TEST_MESSAGE));
            assertEquals("test@gv.com", xml.get(msg2,"From"));
            assertEquals("test1@gv.com", xml.get(msg2,"To"));
            Node att = xml.selectSingleNode(msg2, ".//EncodedContent");
            assertTrue(xml.get(att,"@content-type").indexOf("TEXT/PLAIN") != -1);
            assertEquals("test.txt", xml.get(att,"@file-name"));
            assertEquals("simple attachment", xml.get(att,"@description"));
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    public final void testReadEmailCount() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='ReadEmailsImapCount']");
        CallOperation op = new IMAPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 1", TEST_MESSAGE + " 1", SMTP);
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 2", TEST_MESSAGE + " 2", SMTP);
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 3", TEST_MESSAGE + " 3", SMTP);
        
        XMLUtils xml = null;
        try {
            xml = XMLUtils.getParserInstance();
            gvBuffer = op.perform(gvBuffer);
            assertEquals("1", gvBuffer.getProperty("IMAP_MESSAGE_COUNT"));
            Document doc = (Document) gvBuffer.getObject();
            System.out.println("Received message 1:\n" + xml.serializeDOM(doc, "UTF-8", false, true));
            Node msg1 = xml.selectSingleNode(doc, "/MailMessages/Message[1]"); 
            assertEquals(TEST_SUBJECT + " 1", xml.get(msg1,"Subject"));
            assertTrue(xml.get(msg1, ".//PlainMessage").startsWith(TEST_MESSAGE + " 1"));
            assertEquals("test@gv.com", xml.get(msg1,"From"));
            assertEquals("test1@gv.com", xml.get(msg1,"To"));
            op.cleanUp();
            /*
            gvBuffer = op.perform(gvBuffer);
            assertEquals("1", gvBuffer.getProperty("POP_MESSAGE_COUNT"));
            doc = (Document) gvBuffer.getObject();
            System.out.println("Received message 2:\n" + xml.serializeDOM(doc, "UTF-8", false, true));
            msg1 = xml.selectSingleNode(doc, "/MailMessages/Message[1]"); 
            assertEquals(TEST_SUBJECT + " 2", xml.get(msg1,"Subject"));
            assertTrue(xml.get(msg1, ".//PlainMessage").startsWith(TEST_MESSAGE + " 2"));
            assertEquals("test@gv.com", xml.get(msg1,"From"));
            assertEquals("test1@gv.com", xml.get(msg1,"To"));
            op.cleanUp();

            gvBuffer = op.perform(gvBuffer);
            assertEquals("1", gvBuffer.getProperty("POP_MESSAGE_COUNT"));
            doc = (Document) gvBuffer.getObject();
            System.out.println("Received message 3:\n" + xml.serializeDOM(doc, "UTF-8", false, true));
            msg1 = xml.selectSingleNode(doc, "/MailMessages/Message[1]"); 
            assertEquals(TEST_SUBJECT + " 3", xml.get(msg1,"Subject"));
            assertTrue(xml.get(msg1, ".//PlainMessage").startsWith(TEST_MESSAGE + " 3"));
            assertEquals("test@gv.com", xml.get(msg1,"From"));
            assertEquals("test1@gv.com", xml.get(msg1,"To"));
            op.cleanUp();
            */
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (context != null) {
            try {
                context.close();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        if (server != null) {
            server.stop();
        }
        super.tearDown();
    }

}
