package com.amee.integration.v2

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

import com.amee.integration.BaseApiTestV2

class DataCategoryIT extends BaseApiTestV2 {

    @Test
    void getRootDataCategoryJson() {
        // Get DataCategory list
        def responseGet = client.get(
            path: "/data",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert 'Root' == responseGet.data.dataCategory.name
        assert responseGet.data.children.dataCategories.size() > 0
    }
    
    @Test
    void getRootDataCategoryXML() {
        // Get DataCategory list
        def responseGet = client.get(
            path: "/data",
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert 'Root' == responseGet.data.DataCategoryResource.DataCategory.Name.text()
        assert responseGet.data.DataCategoryResource.Children.DataCategories.size() > 0
    }
    
    @Test
    void createUpdateDeleteCategory() {
        setAdminUserV2()
        
        // Create a DataCategory.
        def responsePost = client.post(
                path: "/data/transport/plane",
                body: [
                        newObjectType: 'DC',
                        itemDefinitionUid: '311B6D2F0363',
                        path: 'testPath',
                        name: 'Test Name'],
                requestContentType: URLENC,
                contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        // Get the new DataCategory
        def responseGet = client.get(
            path: "/data/transport/plane/testPath",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.dataCategory.name == 'Test Name'
        
        // Ensure we cannot make a duplicate
        try{
            responsePost = client.post(
                path: "/data/transport/plane",
                body: [
                        newObjectType: 'DC',
                        itemDefinitionUid: '311B6D2F0363',
                        path: 'testPath',
                        name: 'Test Name'],
                requestContentType: URLENC,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_BAD_REQUEST.code == e.response.status  
        }
        
        // Update the DataCategory
        def responsePut = client.put(
            path: "/data/transport/plane/testPath",
            body: [name: 'New Test Name'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status
        
        // Check the update worked
        responseGet = client.get(
            path: "/data/transport/plane/testPath",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.dataCategory.name == 'New Test Name' 
        
        // Delete the DataCategory
        def responseDelete = client.delete(
            path: "/data/transport/plane/testPath",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        
        // Check the new DataCategory was deleted
        try{
            responseGet = client.get(
                path: "/data/transport/plane/testPath",
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }   
    
    /**
     * Tests that Data Items are correctly included in the JSON response and behave 
     * appropriately with pagination
     */
    @Test
    void getPaginatedDataItemsJson() {
        // Get a DataCategory
        def responseGet = client.get(
            path: "/data/business/energy/electricity",
            query: ['itemsPerPage': 10],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.children.dataItems.rows.size() == 10
        
        responseGet = client.get(
            path: "/data/business/energy/electricity",
            query: ['itemsPerPage': 2],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.children.dataItems.rows.size() == 2
    }
    
    /**
    * Tests that Data Items are correctly included in the XML response and behave
    * appropriately with pagination
    */
   @Test
   void getPaginatedDataItemsXML() {
       def responseGet = client.get(
           path: "/data/business/energy/electricity",
           query: ['itemsPerPage': 10],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       def allItems = responseGet.data.DataCategoryResource.Children.DataItems.DataItem
       assert allItems.size() == 10
       
       responseGet = client.get(
           path: "/data/business/energy/electricity",
           query: ['itemsPerPage': 2],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       allItems = responseGet.data.DataCategoryResource.Children.DataItems.DataItem
       assert allItems.size() == 2
   }
}
