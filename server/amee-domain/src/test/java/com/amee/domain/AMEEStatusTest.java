package com.amee.domain;

import org.apache.xerces.dom.DocumentImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AMEEStatusTest {
    // Map of Enum name => label
    private static final Map<String, String> pairs = new HashMap<String, String>();
    {
        pairs.put("TRASH", "Trash");
        pairs.put("ACTIVE", "Active");
        pairs.put("DEPRECATED", "Deprecated");
    }

    @Test
    public void getChoices() {
        assertEquals(pairs, AMEEStatus.getChoices());
    }

    @Test
    public void getJSONObject() {
        try {
            JSONObject JsonObject = AMEEStatus.getJSONObject();
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
        Element element = AMEEStatus.getElement(dom);
        dom.appendChild(element);
        assertElement(dom);
    }

    private void assertElement(Document dom) {
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a NodeList of  AMEEStatus elements
        NodeList nl = docEle.getElementsByTagName("Status");
        assertEquals("Number of elements not equal to number of enum values.", AMEEStatus.values().length, nl.getLength());

        // Check we have the correct name, label attributes
        for(int i = 0 ; i < nl.getLength(); i++) {
            Node AMEEStatusNode = nl.item(i);
            assertTrue("AMEEStatus node has no attributes.", AMEEStatusNode.hasAttributes());

            // Check the name/label attributes.
            NamedNodeMap attributes = AMEEStatusNode.getAttributes();
            Node nameNode = attributes.getNamedItem("name");
            Node labelNode = attributes.getNamedItem("label");
            assertEquals(pairs.get(nameNode.getNodeValue()), labelNode.getNodeValue());
        }
    }
}
