package com.amee.integration.v1

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_OK
import groovyx.net.http.HttpResponseException

import org.junit.Test

import com.amee.integration.BaseApiTestV1

class ProfileItemIT extends BaseApiTestV1 {

    @Test
    void createUpdateDeleteProfileItemJson() {
        
        // Create new profile item
        def responsePost = client.post(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/",
            body: [
                dataItemUid: '66056991EE23',
                kgPerMonth: 1000],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.profileItem.uid

        // Get the new profile item
        def responseGet = client.get(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.profileItem.uid == uid
        assert responseGet.data.profileItem.itemValues.size() > 0
        def kgPerMonthItem = responseGet.data.profileItem.itemValues.find{ it.path == 'kgPerMonth' }
        assert kgPerMonthItem.value == '1000'
        assertEquals 2693.5, responseGet.data.profileItem.amountPerMonth, 0.0001

        // Update the profile item
        def responsePut = client.put(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            body: [kgPerMonth: 100],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePut.status

        // Get the updated profile item
        responseGet = client.get(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        def data = responseGet.data
        kgPerMonthItem = responseGet.data.profileItem.itemValues.find{ it.path == 'kgPerMonth' }
        assert kgPerMonthItem.value == '100'
        assertEquals 269.35, responseGet.data.profileItem.amountPerMonth, 0.0001

        // Delete the profile item
        def responseDelete = client.delete(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status

        // Check profile item was deleted
        try{
            responseGet = client.get(
                path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }

    @Test
    void getProfileItemUnauthorisedJson() {
        // Create profile item as standard user
        setStandardUserV1()
        def responsePost = client.post(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/",
            body: [
                dataItemUid: '66056991EE23',
                kgPerMonth: 1000],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.profileItem.uid

        // Try to retrieve as Ecoinvent user
        setEcoinventUser()
        try{
            def responseGet = client.get(
                path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_FORBIDDEN.code == e.response.status
        }
        
        // Delete the profile item
        setStandardUserV1()
        def responseDelete = client.delete(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
    }

    @Test
    void getProfileItemUnauthorisedXML() {
        // Create profile item as standard user
        setStandardUserV1()
        def responsePost = client.post(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/",
            body: [
                dataItemUid: '66056991EE23',
                kgPerMonth: 1000],
            requestContentType: URLENC,
            contentType: XML)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.ProfileCategoryResource.ProfileItem.@uid

        // Try to retrieve as Ecoinvent user
        setEcoinventUser()
        try{
            def responseGet = client.get(
                path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
                contentType: XML)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_FORBIDDEN.code == e.response.status
        }
        
        // Delete the profile item
        setStandardUserV1()
        def responseDelete = client.delete(
            path: "/profiles/P3S1C0SF9T54/home/energy/quantity/" + uid,
            contentType: XML)
        assert SUCCESS_OK.code == responseDelete.status
    }   
}
