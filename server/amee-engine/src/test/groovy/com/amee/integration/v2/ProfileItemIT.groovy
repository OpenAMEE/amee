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

import com.amee.integration.BaseApiTestV2

class ProfileItemIT extends BaseApiTestV2 {

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
        assert SUCCESS_OK.code == responseDelete.status

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
        setStandardUserV2()
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
        
        // Delete the profile item
        setStandardUserV2()
        def responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
    }

    @Test
    void getProfileItemUnauthorisedXML() {
        // Create profile item as standard user
        setStandardUserV2()
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
        
        // Delete the profile item
        setStandardUserV2()
        def responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: XML)
        assert SUCCESS_OK.code == responseDelete.status
    }
    
    @Test
    void getProfileItemCustomUnitsJson() {
        // Create new profile item
        def responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                energyPerTimeUnit: 'J',
                energyPerTimePerUnit: 'month',
                responsibleArea: 100,
                totalArea: 100],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[8]
        
        // Get the new profile item
        def responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            query: [
                returnUnit: 'lb',
                returnPerUnit: 'month'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        
        def energyConsumptionItem = responseGet.data.profileItem.itemValues.find{ it.path == 'energyPerTime' }
        assert energyConsumptionItem.unit == 'J'
        assert energyConsumptionItem.perUnit == 'month'
        assert responseGet.data.profileItem.amount.unit == 'lb/month'        
        assert responseGet.data.profileItem.amount.value != 200
        
        // Delete the profile item
        def responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
    }
    
    @Test
    void timeResolutionCreateProfileItemJson() {
        // Create new profile item
        def responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                responsibleArea: 100,
                totalArea: 100,
                startDate: "2012-11-18T18:32:50Z"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid = responsePost.headers['Location'].value.split("/")[8]
        
        // Get the new profile item
        def responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        
        // Times should get rounded down to the minute
        def startDate = responseGet.data.profileItem.startDate
        assert startDate == "2012-11-18T18:32:00Z"
        
        // Create a new profile item that starts 1 minute later
        responsePost = client.post(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
            body: [
                dataItemUid: '963A90C107FA',
                energyPerTime: 100,
                responsibleArea: 100,
                totalArea: 100,
                startDate: "2012-11-18T18:33:00Z"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_CREATED.code == responsePost.status
        def uid2 = responsePost.headers['Location'].value.split("/")[8]
        
        // Get the new profile item
        responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid2,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        
        assert startDate != responseGet.data.profileItem.startDate
        
        // Should not be able to create a profile item within a minute
        try{
            responsePost = client.post(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
                body: [
                    dataItemUid: '963A90C107FA',
                    energyPerTime: 100,
                    responsibleArea: 100,
                    totalArea: 100,
                    startDate: "2012-11-18T18:32:30Z"],
                requestContentType: URLENC,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e) {
            assert CLIENT_ERROR_BAD_REQUEST.code == e.response.status
        }   
        
        // Delete the profile items
        def responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        responseDelete = client.delete(
            path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid2,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
    }
    
    @Test
    void createProfileItemDodgyCharactersJson() {
        def responsePost, responseGet, responseDelete, dodgyName, uid
        for (s in ["£", "'", "%", "&", "<>"]) {
            dodgyName = 'test ${s}'
            
            // Create a new profile item with a dodgy character
            responsePost = client.post(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity",
                body: [
                    dataItemUid: '963A90C107FA',
                    name: dodgyName],
                requestContentType: URLENC,
                contentType: JSON)
            assert SUCCESS_CREATED.code == responsePost.status
            uid = responsePost.headers['Location'].value.split("/")[8]
            
            // Check it can be retrieved ok
            responseGet = client.get(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
                contentType: JSON)
            assert SUCCESS_OK.code == responseGet.status
            assert responseGet.data.profileItem.name == dodgyName
                
            // Delete the profile item
            responseDelete = client.delete(
                path: "/profiles/UCP4SKANF6CS/business/energy/electricity/" + uid,
                contentType: JSON)
            assert SUCCESS_OK.code == responseDelete.status
        }
    }    
}
