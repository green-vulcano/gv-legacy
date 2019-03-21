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
import it.greenvulcano.gvesb.datahandling.utils.dao.DataAccessObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class DataHandlerPoolTestCase {
        
    @Before
    public void setUp() throws Exception  {
        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
      
        Commons.createDB();
    }

    @After
    public void tearDown() throws Exception {
        Commons.clearDB();        
    }

    @Test
    public void testDHCallSelect() throws Exception  {
        String operation = "GVESB::TestSelect";
        DHResult result = DataAccessObject.execute(operation, null, null);
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
    public final void testDHCallInsertOrUpdate() throws Exception  {
        String operation = "GVESB::TestInsert";
        DHResult result = DataAccessObject.execute(operation, Commons.createInsertMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(0, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());

        operation = "GVESB::TestInsertOrUpdate";
        result = DataAccessObject.execute(operation, Commons.createInsertOrUpdateMessage(), null);
        assertEquals(0, result.getDiscard());
        assertEquals(1, result.getUpdate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getInsert());
        assertEquals(0, result.getRead());
        assertEquals("", result.getDiscardCauseListAsString());
    }
}
