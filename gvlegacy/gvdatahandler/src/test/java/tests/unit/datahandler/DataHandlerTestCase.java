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

package tests.unit.datahandler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class DataHandlerTestCase {

    private static final String BASE_DIR = "target" + File.separator + "test-classes";
  
    private DHFactory dhFactory;

    @Before
    public void setUp() throws Exception {

        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());

        Commons.createDB();
        dhFactory = new DHFactory();
        dhFactory.initialize(null);
    }

    @After
    public void tearDown() throws Exception {

        if (dhFactory != null) {
            dhFactory.destroy();
        }
        Commons.clearDB();
    }

    @Test
    public void testDHCallSelect() throws Exception {

        String operation = "GVESB::TestSelect";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(4, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(2).getTextContent();
        assertEquals("2000-01-01 12:30:45", field2);
        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
    }

    @Test
    public void testDHCallSelectMulti() throws Exception {

        String operation = "GVESB::TestSelectMulti";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Document output = (Document) result.getData();
        assertNotNull(output);
        assertTrue(output.getDocumentElement().hasChildNodes());
        Node data = output.getDocumentElement().getChildNodes().item(0);
        assertTrue(data.hasChildNodes());
        Node row = data.getChildNodes().item(0);
        assertTrue(row.hasChildNodes());
        NodeList cols = row.getChildNodes();
        assertEquals(11, cols.getLength());
        String id = cols.item(0).getTextContent();
        assertEquals("1", id);
        String field1 = cols.item(1).getTextContent();
        assertEquals("testvalue", field1);
        String field2 = cols.item(2).getTextContent();
        assertEquals("2000-01-01 12:30:45", field2);
        String field3 = cols.item(3).getTextContent();
        assertEquals("123,45", field3);
    }

    @Test
    public void testDHCallThreadSelect() throws Exception {

        String operation = "GVESB::TestThreadSelect";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(2, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

    }

    @Test
    public void testDHCallSelectMerge() throws Exception {

        String operation = "GVESB::TestSelectMerge";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        Document output = (Document) result.getData();
        assertNotNull(output);
        System.out.println("TestSelectMerge: " + XMLUtils.serializeDOM_S(output));
    }

    @Test
    public final void testDHCallInsertOrUpdate() throws Exception {

        String operation = "GVESB::TestInsert";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, Commons.createInsertMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        operation = "GVESB::TestInsertOrUpdate";
        dboBuilder = dhFactory.getDBOBuilder(operation);
        result = dboBuilder.EXECUTE(operation, Commons.createInsertOrUpdateMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(1, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
    }

    @Test
    public final void testDHCallMultiInsertOrUpdate() throws Exception {

        String operation = "GVESB::TestInsertMulti";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, Commons.createInsertMultiMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        operation = "GVESB::TestInsertOrUpdateMulti";
        dboBuilder = dhFactory.getDBOBuilder(operation);
        result = dboBuilder.EXECUTE(operation, Commons.createInsertOrUpdateMultiMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(1, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
    }

    @Test
    public void testDHCallFlatSelect() throws Exception {

        String operation = "GVESB::TestFlatSelect";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = new String((byte[]) out);
        assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
    }

    @Test
    public void testDHCallFlatTZoneSelect() throws Exception {

        String operation = "GVESB::TestFlatTZoneSelect";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = new String((byte[]) out);
        assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
    }

    @Test
    public void testDHCallFlatSelectFile() throws Exception {

        System.setProperty("gv.app.home", BASE_DIR);
        String operation = "GVESB::TestFlatSelectFile";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatSelectFile.csv"));
        assertEquals("1@testvalue.....................@20000101 123045@123,45@\n", output);
    }

    @Test
    public void testDHCallMultiFlatSelectFile() throws Exception {

        System.setProperty("gv.app.home", BASE_DIR);
        String operation = "GVESB::TestMultiFlatSelectFile";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(2, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestMultiFlatSelectFile.csv"));
        assertEquals("id@field1@field2@field3@\n1@testvalue.....................@20000101 123045@123,45@\n", output);
    }

    @Test
    public void testDHCallFlatTZoneSelectFile() throws Exception {

        System.setProperty("gv.app.home", BASE_DIR);
        String operation = "GVESB::TestFlatTZoneSelectFile";
        IDBOBuilder dboBuilder = dhFactory.getDBOBuilder(operation);
        DHResult result = dboBuilder.EXECUTE(operation, null, null);
        assertNotNull(result);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getInsert());
        assertEquals(1, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
        Object out = result.getData();
        assertNotNull(out);
        String output = TextUtils.readFile(PropertiesHandler.expand("sp{{gv.app.home}}/log/TestFlatTZoneSelectFile.csv"));
        assertEquals("1@testvalue.....................@20000101 113045@123,45@\n", output);
    }

}
