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

class DataItemIT extends BaseApiTest {

    @Test
    void createUpdateDeleteDataItemJson() {
        setAdminUser()

        // Create a data item
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def location = responsePost.headers['Location'].value
        def uid = location.split("/")[7]

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
        setAdminUser()
        
        // Create a data item
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[7]
        
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
    
    @Test
    void getDataItemJson() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 100, 
                "responsibleArea": 100, 
                "totalArea": 100],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert "/business/energy/electricity/963A90C107FA" == responseGet.data.path
        assert "Albania" == responseGet.data.dataItem.label
        assert responseGet.data.userValueChoices.choices.size() == 3
        assert responseGet.data.amount.unit == "kg/year"
        assert responseGet.data.amount.value == 200
    }
    
    @Test
    void getDataItemXML() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 100,
                "responsibleArea": 100,
                "totalArea": 100],
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert "/business/energy/electricity/963A90C107FA" == responseGet.data.DataItemResource.Path.text()
        assert "Albania" == responseGet.data.DataItemResource.DataItem.Label.text()
        assert responseGet.data.DataItemResource.Choices.Choices.Choice.size() == 3
        assert responseGet.data.DataItemResource.Amount.@unit.text() == "kg/year"
        assert responseGet.data.DataItemResource.Amount.text() == "200.0"
    }
    
    @Test
    void getCustomInputUnitsDataItemJson() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 3000000,
                "responsibleArea": 100,
                "totalArea": 100,
                "energyPerTimeUnit": "J",
                "energyPerTimePerUnit": "month"],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.amount.unit == "kg/year"
        assert responseGet.data.amount.value == 20
    }
    
    @Test
    void getCustomInputUnitsDataItemXML() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 3000000,
                "responsibleArea": 100,
                "totalArea": 100,
                "energyPerTimeUnit": "J",
                "energyPerTimePerUnit": "month"],
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.DataItemResource.Amount.@unit.text() == "kg/year"
        assert responseGet.data.DataItemResource.Amount.text() == "20.0"
    }
    
    @Test
    void getCustomOutputUnitsDataItemJson() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 100,
                "responsibleArea": 100,
                "totalArea": 100,
                "returnUnit": "lb",
                "returnPerUnit": "month"],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.amount.unit == "lb/month"
        assert responseGet.data.amount.value == 36.74371036414626
    }
    
    @Test
    void getCustomOutputUnitsDataItemXML() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/963A90C107FA",
            query: [
                "energyPerTime": 100,
                "responsibleArea": 100,
                "totalArea": 100,
                "returnUnit": "lb",
                "returnPerUnit": "month"],
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.DataItemResource.Amount.@unit.text() == "lb/month"
        assert responseGet.data.DataItemResource.Amount.text() == "36.74371036414626"
    }
    
}
