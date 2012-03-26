package com.amee.integration.v2

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

class ProfileItemIT extends BaseApiTest {

    @Test
    void createUpdateDeleteProfileItemJson() {
        // Create new profile item
        def responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                responsibleArea: 100,
                totalArea: 100],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[8]

        // Get the new profile item
        def responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.profileItem.uid == uid
        assert responseGet.data.profileItem.itemValues.size() > 0
        def energyConsumptionItem = responseGet.data.profileItem.itemValues.find{ it.path == 'energyPerTime' }
        assert energyConsumptionItem.value == '100'
        assert responseGet.data.profileItem.amount.unit == 'kg/year'
        assert responseGet.data.profileItem.amount.value == 200

        // Update the profile item
        def responsePut = client.put(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            body: [energyPerTime: 10],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status

        // Get the updated profile item
        responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        def data = responseGet.data
        energyConsumptionItem = responseGet.data.profileItem.itemValues.find{ it.path == 'energyPerTime' }
        assert energyConsumptionItem.value == '10'
        assert responseGet.data.profileItem.amount.value == 20

        // Delete the profile item
        def responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status

        // Check profile item was deleted
        try{
            responseGet = client.get(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }

    @Test
    void getProfileItemUnauthorisedJson() {
        // Create profile item as standard user
        setStandardUser()
        def responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                responsibleArea: 100,
                totalArea: 100],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[8]

        // Try to retrieve as Ecoinvent user
        setEcoinventUser()
        try{
            def responseGet = client.get(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_FORBIDDEN.code == e.response.status
        }
    }

    @Test
    void getProfileItemUnauthorisedXML() {
        // Create profile item as standard user
        setStandardUser()
        def responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                responsibleArea: 100,
                totalArea: 100],
            requestContentType: URLENC,
            contentType: XML)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[8]

        // Try to retrieve as Ecoinvent user
        setEcoinventUser()
        try{
            def responseGet = client.get(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
                contentType: XML)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_FORBIDDEN.code == e.response.status
        }
    }
    

}
