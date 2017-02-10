package it.greenvulcano.gvesb.virtual.pop;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.pop.POPCallOperation;
import it.greenvulcano.util.xml.XMLUtils;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class POPCallOperationTest extends TestCase {

	private static final String TEST_SYSTEM  = "TEST_SYSTEM";
    private static final String TEST_SERVICE = "TEST_SERVICE";
    private static final String TEST_SUBJECT = "Notifica SendEmail";
    private static final String TEST_MESSAGE = "Test Body";

    private Context             context;

    private static GreenMail    server       = null;
    private static ServerSetup  SMTP         = new ServerSetup(11025, null, ServerSetup.PROTOCOL_SMTP);
    private static ServerSetup  POP3         = new ServerSetup(11110, null, ServerSetup.PROTOCOL_POP3);

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    	System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.processor.TransformerFactoryImpl");
        server = new GreenMail(new ServerSetup[]{SMTP, POP3});
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
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='ReadEmails']");
        CallOperation op = new POPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT, TEST_MESSAGE, SMTP);
        GreenMailUtil.sendAttachmentEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT, TEST_MESSAGE, 
                TEST_MESSAGE.getBytes(), "text/text", "test.txt", "simple attachment", SMTP);
        
        gvBuffer = op.perform(gvBuffer);
        
        //assertTrue(server.waitForIncomingEmail(5000, 2));
        
        assertEquals("2", gvBuffer.getProperty("POP_MESSAGE_COUNT"));

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
            assertTrue(xml.get(att,"@content-type").indexOf("text/plain") != -1);
            assertEquals("test.txt", xml.get(att,"@file-name"));
            assertEquals("simple attachment", xml.get(att,"@description"));
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    public final void testReadEmailCount() throws Exception
    {
        Node node = XMLConfig.getNode("GVCore.xml", "//*[@name='ReadEmailsCount']");
        CallOperation op = new POPCallOperation();
        op.init(node);

        GVBuffer gvBuffer = new GVBuffer(TEST_SYSTEM, TEST_SERVICE);
        
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 1", TEST_MESSAGE + " 1", SMTP);
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 2", TEST_MESSAGE + " 2", SMTP);
        GreenMailUtil.sendTextEmail("test1@gv.com", "test@gv.com", TEST_SUBJECT + " 3", TEST_MESSAGE + " 3", SMTP);
        
        XMLUtils xml = null;
        try {
            xml = XMLUtils.getParserInstance();
            gvBuffer = op.perform(gvBuffer);
            assertEquals("1", gvBuffer.getProperty("POP_MESSAGE_COUNT"));
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
