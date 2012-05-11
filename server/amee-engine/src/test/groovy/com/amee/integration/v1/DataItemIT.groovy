package com.amee.integration.v1

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

import com.amee.integration.BaseApiTestV1

class DataItemIT extends BaseApiTestV1 {

    @Test
    void createUpdateDeleteDataItemJson() {
        setAdminUserV1()
        
        // Create a data item
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.dataItem.uid

        // Get the new data item
        def responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert "Test Country" == responseGet.data.dataItem.label

        // Update the data item
        def responsePut = client.put(
            path: "/data/business/energy/electricity/" + uid,
            body: [name: "New Name"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status

        // Check the data item was updated
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert "New Name" == responseGet.data.dataItem.name        

        // Delete the data item
        def responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status

        // Check it's been deleted
        try{
            responseGet = client.get(
                path: "/data/business/energy/electricity/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }

    @Test
    void createDuplicateDataItemJson() {
        setAdminUserV1()
        
        // Create a data item
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.dataItem.uid
        
        // Ensure we cannot create a duplicate
        try{
            responsePost = client.post(
                path: "/data/business/energy/electricity",
                body: [
                    newObjectType: 'DI',
                    country: "Test Country"],
                requestContentType: URLENC,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_BAD_REQUEST.code == e.response.status
        }
        
        // Clean up
        def responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
    }

}
