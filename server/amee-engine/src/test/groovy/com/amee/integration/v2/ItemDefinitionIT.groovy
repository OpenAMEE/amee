package com.amee.integration.v2

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.junit.Assert.fail
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

class ItemDefinitionIT extends BaseApiTest {

    /**
     * Test getting the root list of item definitions as JSON
     */
    @Test
    void getItemDefinitionListJson() {
        setAdminUser()
        
        // Get ItemDefinition list
        def responseGet = client.get(
            path: "/definitions/itemDefinitions",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemDefinitions.size() > 0
    }
    
    /**
     * Test getting the root list of item definitions as XML
     */
    @Test
    void getItemDefinitionListXML() {
        setAdminUser()
        
        // Get ItemDefinition list
        def responseGet = client.get(
            path: "/definitions/itemDefinitions",
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.ItemDefinitionsResource.ItemDefinitions.ItemDefinition.size() > 0
    }
    
    /**
     * Test getting a paginated list of item defintions as JSON
     */
    @Test
    void getPaginatedItemDefinitionListJson() {
        setAdminUser()
        
        // Get ItemDefinition list
        def responseGet = client.get(
            path: "/definitions/itemDefinitions",
            query: ['itemsPerPage': 10],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemDefinitions.size() == 10
        
        responseGet = client.get(
            path: "/definitions/itemDefinitions",
            query: ['itemsPerPage': 2],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemDefinitions.size() == 2
    }
   
    /**
     * Test getting a paginated list of item definitions as XML
     */
    void getPaginatedItemDefinitionListXML() {
        setAdminUser()
        
        // Get ItemDefinition list
        def responseGet = client.get(
            path: "/definitions/itemDefinitions",
            query: ['itemsPerPage': 10],
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.ItemDefinitionsResource.ItemDefinitions.ItemDefinition.size() == 10
        
        responseGet = client.get(
            path: "/definitions/itemDefinitions",
            query: ['itemsPerPage': 2],
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.ItemDefinitionsResource.ItemDefinitions.ItemDefinition.size() == 2
    }
    
    /**
     * Test creating, updating and deleting an item definition.  Confirms that the
     * operations are successful by performing the appropriate GET request after each
     * one.
     */
    @Test
    void createUpdateDeleteItemDefinition() {
        setAdminUser()

        // Create new ItemDefinition
        def responsePost = client.post(
            path: "/definitions/itemDefinitions",
            body: ['name': 'test'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.itemDefinition.uid
        assert uid != null

        // Get the new ItemDefinition
        def responseGet = client.get(
            path: "/definitions/itemDefinitions/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert 'application/json' == responseGet.contentType
        assert responseGet.data instanceof net.sf.json.JSON
        assert 'test' == responseGet.data.itemDefinition.name

        uid = responseGet.data.itemDefinition.uid

        // Update it
        def responsePut = client.put(
            path: "/definitions/itemDefinitions/" + uid,
            body: ['name': 'New name'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status

        // Check the update worked
        responseGet = client.get(
            path: "/definitions/itemDefinitions/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert "New name" == responseGet.data.itemDefinition.name

        // Delete it
        def responseDelete = client.delete(
            path: "/definitions/itemDefinitions/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status

        // Check it's been deleted
        try{
            responseGet = client.get(
                path: "/definitions/itemDefinitions/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
    
    /**
     * Test that an invalid locale specification is correctly rejected as an update to
     * an item definition.
     */
    @Test
    void invalidUpdateItemDefinition() {
        setAdminUser()
        
        def uid = '11D3548466F2'
        
        // Get an ItemDefinition
        def responseGet = client.get(
            path: "/definitions/itemDefinitions/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert uid == responseGet.data.itemDefinition.uid 
        
        // Update with invalid locale name
        try{
            def responsePut = client.put(
                path: "/definitions/itemDefinitions/" + uid,
                body: ['name_zz': 'New name'],
                requestContentType: URLENC,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_BAD_REQUEST.code == e.response.status
        }
        
        // Check no modifications were made
        responseGet = client.get(
            path: "/definitions/itemDefinitions/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
    }
}
