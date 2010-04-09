package com.amee.domain.auth;

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

public class UserTypeTest {
    // Map of Enum name => label
    private static final Map<String, String> pairs = new HashMap<String, String>();
    {
        pairs.put("STANDARD", "Standard");
        pairs.put("GUEST", "Guest");
        pairs.put("ANONYMOUS", "Anonymous");
        pairs.put("SUPER", "Super");
    }

    @Test
    public void getChoices() {
        assertEquals(pairs, UserType.getChoices());
    }

    @Test
    public void getJSONObject() {
        try {
            JSONObject JsonObject = UserType.getJSONObject();
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
        Element element = UserType.getElement(dom);
        dom.appendChild(element);
        assertElement(dom);
    }

    private void assertElement(Document dom) {
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a NodeList of  UserType elements
        NodeList nl = docEle.getElementsByTagName("UserType");
        assertEquals("Number of elements not equal to number of enum values.", UserType.values().length, nl.getLength());

        // Check we have the correct name, label attributes
        for(int i = 0 ; i < nl.getLength(); i++) {
            Node UserTypeNode = nl.item(i);
            assertTrue("UserType node has no attributes.", UserTypeNode.hasAttributes());

            // Check the name/label attributes.
            NamedNodeMap attributes = UserTypeNode.getAttributes();
            Node nameNode = attributes.getNamedItem("name");
            Node labelNode = attributes.getNamedItem("label");
            assertEquals(pairs.get(nameNode.getNodeValue()), labelNode.getNodeValue());
        }
    }

}
