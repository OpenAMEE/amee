package com.amee.integration.v2

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.junit.Assert.assertEquals
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

class DataItemValueIT extends BaseApiTest {

    def paths = [
        "country",
        "energyPerTime",
        "responsibleArea",
        "totalArea",
        "massCH4PerEnergy",
        "massCO2PerEnergy",
        "massN2OPerEnergy"
    ]

    /**
     * Test creating a DataItem and check that:
     * <ul>
     *  <li>DataItemValue defaults are created correctly</li>
     *  <li>Specified DataItemValues are created correctly</li>
     *  <li>A DataItemValueHistory can be created by POST</li>
     *  <li>A DataItemValueHistory can be created by PUT</li>
     *  <li>
     *  
     * </ul> 
     */
    @Test
    void createUpdateDataItemValueJson() {
        setAdminUser()

        // Create a new DataItem
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

        // Check default Data Item Values were created
        def responseGet = client.get(
            path: "/data/business/energy/electricity",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        def dataItemValuePaths = responseGet.data.itemValues.collect{it.path}
        dataItemValuePaths.each { assert paths.contains(it) }

        // Check specified Data Item Value was created
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/country",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.value == 'Test Country'
        
        // Create a new Data Item Value History
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '100',
                startDate: '2000-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        // Check new history item was created
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [
                startDate: '2000-01-01T00:00:00Z',
                endDate: '2001-01-01T00:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.value == '100'
        
        // Update the new Data Item Value History
        def responsePut = client.put(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            body: [
                value: "42",
                startDate: '2000-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status
        
        // Check new history item was created
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [
                startDate: '2000-01-01T00:00:00Z',
                endDate: '2001-01-01T00:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.value == '42'
        
        // Delete the history item
        def responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check the history item was deleted
        try{
            responseGet = client.get(
                path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
                query: [
                    startDate: '2001-01-01T00:00:00Z',
                    endDate: '2002-01-01T00:00:00Z'],
                contentType: JSON)
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
        
        // Delete the Data Item
        responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check the Data Item was deleted
        try{
            responseGet = client.get(
                path: "/data/business/energy/electricity/" + uid,
                contentType: JSON)
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
    
    @Test
    void dataItemSeriesJson() {
        setAdminUser()
        
        // Create a new DataItem
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
        
        // Create a data series with a new value each day
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '1',
                startDate: '2000-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '2',
                startDate: '2000-01-02T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '3',
                startDate: '2000-01-03T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        // Check the history items are all available
        def responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [startDate: '2000-01-01T12:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert '1' == responseGet.data.itemValue.value
        
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
             query: [startDate: '2000-01-02T12:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert '2' == responseGet.data.itemValue.value
        
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
             query: [startDate: '2000-01-03T12:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert '3' == responseGet.data.itemValue.value
        
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
             query: [
                 startDate: '2000-01-01T12:00:00Z',
                 valuesPerPage: '10'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValues.size() == 4 // 3 we made plus default
        
        // Delete one in the middle
        def responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [startDate: '2000-01-02T12:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check the previous result is now returned for that time
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
             query: [startDate: '2000-01-02T12:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert '1' == responseGet.data.itemValue.value
        
        // Delete the Data Item
        responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check the Data Item was deleted
        try{
            responseGet = client.get(
                path: "/data/business/energy/electricity/" + uid,
                contentType: JSON)
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
}
