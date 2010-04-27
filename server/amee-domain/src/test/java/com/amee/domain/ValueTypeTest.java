package com.amee.domain;

import org.apache.xerces.dom.DocumentImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ValueTypeTest {

    // Map of Enum name => label
    private static final Map<String, String> pairs = new HashMap<String, String>();

    {
        pairs.put("INTEGER", "Integer");
        pairs.put("BOOLEAN", "Boolean");
        pairs.put("DECIMAL", "Decimal");
        pairs.put("TEXT", "Text");
        pairs.put("DATE", "Date");
        pairs.put("UNSPECIFIED", "Unspecified");
    }

    @Test
    public void getChoices() {
        assertEquals(pairs, ValueType.getChoices());
    }

    @Test
    public void getJSONObject() {
        try {
            JSONObject JsonObject = ValueType.getJSONObject();
            assertJsonObject(pairs, JsonObject);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    private void assertJsonObject(Map<String, String> pairs, JSONObject JsonObject) {
        try {
            for (Map.Entry<String, String> e : pairs.entrySet()) {
                assertEquals("JSON object values differ.", e.getValue(), JsonObject.getString(e.getKey()));
            }
        } catch (JSONException e) {

            // The key wasn't there.
            fail(e.getMessage());
        }
    }

    @Test
    public void getElement() {
        Document dom = new DocumentImpl();
        Element element = ValueType.getElement(dom);
        dom.appendChild(element);
        assertElement(dom);
    }

    private void assertElement(Document dom) {
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a NodeList of  ValueType elements
        NodeList nl = docEle.getElementsByTagName("ValueType");
        assertEquals("Number of elements not equal to number of enum values.", ValueType.values().length, nl.getLength());

        // Check we have the correct name, label attributes
        for (int i = 0; i < nl.getLength(); i++) {
            Node valueTypeNode = nl.item(i);
            assertTrue("ValueType node has no attributes.", valueTypeNode.hasAttributes());

            // Check the name/label attributes.
            NamedNodeMap attributes = valueTypeNode.getAttributes();
            Node nameNode = attributes.getNamedItem("name");
            Node labelNode = attributes.getNamedItem("label");
            assertEquals(pairs.get(nameNode.getNodeValue()), labelNode.getNodeValue());
        }
    }

    @Test
    public void getValueType() {
        String s = "Some string";
        assertEquals("Should return ValueType.TEXT", ValueType.TEXT, ValueType.getValueType(s));

        BigDecimal bd = new BigDecimal("123.45");
        assertEquals("Should return ValueType.DECIMAL", ValueType.DECIMAL, ValueType.getValueType(bd));

        Integer i = new Integer(12345);
        assertEquals("Should return ValueType.INTEGER", ValueType.INTEGER, ValueType.getValueType(i));

        Boolean b = new Boolean(true);
        assertEquals("Should return ValueType.BOOLEAN", ValueType.BOOLEAN, ValueType.getValueType(b));

        Date date = new Date();
        assertEquals("Should return ValueType.DATE", ValueType.DATE, ValueType.getValueType(date));

        Float f = new Float(123.45);
        assertEquals("Should return ValueType.UNSPECIFIED", ValueType.UNSPECIFIED, ValueType.getValueType(f));

        Double d = new Double(123.45);
        assertEquals("Should return ValueType.UNSPECIFIED", ValueType.UNSPECIFIED, ValueType.getValueType(d));

        Object o = new Object();
        assertEquals("Should return ValueType.UNSPECIFIED", ValueType.UNSPECIFIED, ValueType.getValueType(o));
    }
}
