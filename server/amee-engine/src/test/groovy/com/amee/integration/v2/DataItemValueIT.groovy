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

    @Test
    void createUpdateDeleteDataItemValueJson() {
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
        
        // Create a new Data Item Value History by POSTing to Data Item
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                energyPerTime: '100',
                startDate: '2000-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        
        // Check new history item was created
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/energyPerTime",
            query: [
                startDate: '2000-01-01T00:00:00Z',
                endDate: '2001-01-01T00:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.value == '100'
        
        // Create a new Data Item Value History by PUTing to Data Item Value
        def responsePut = client.put(
            path: "/data/business/energy/electricity/" + uid + "/energyPerTime",
            body: [
                value: "42",
                startDate: '2001-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status
        
        // Check new history item was created
        responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid + "/energyPerTime",
            query: [
                startDate: '2001-01-01T00:00:00Z',
                endDate: '2002-01-01T00:00:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.value == '42'
        
        // TODO: update an existing data item value, delete a data item value
        
        // Delete the data Item
        def responseDelete = client.delete(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check the Data Item was deleted
        try{
            responseGet = client.get(
                path: "/data/business/energy/electricity/" + uid + "/country",
                contentType: JSON)
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
}
