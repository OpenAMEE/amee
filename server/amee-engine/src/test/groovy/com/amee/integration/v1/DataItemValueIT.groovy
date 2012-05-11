package com.amee.integration.v1

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

import com.amee.integration.BaseApiTestV1

class DataItemValueIT extends BaseApiTestV1 {

    def paths = [
        "country",
        "massCH4PerEnergy",
        "massCO2PerEnergy",
        "massN2OPerEnergy",
        "source"
    ]

    /**
     * Test that the various properties of a Data Item Value are correct in the JSON response
     */
    @Test
    void getDataItemValueJson() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/grid/585E708CB4BE/massCO2PerEnergy",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.itemValue.uid == '289CCD5394AC'
        assert responseGet.data.itemValue.name == 'Mass CO2 per Energy'
        assert responseGet.data.itemValue.path == 'massCO2PerEnergy'
        assert responseGet.data.itemValue.value == '0.81999'
        assert responseGet.data.itemValue.itemValueDefinition.valueDefinition.valueType == 'DECIMAL'   
    }
    
    /**
    * Test that the various properties of a Data Item Value are correct in the XML response
    */
    @Test
    void getDataItemValueXML() {
        def responseGet = client.get(
            path: "/data/business/energy/electricity/grid/585E708CB4BE/massCO2PerEnergy",
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.DataItemValueResource.ItemValue.@uid.text() == '289CCD5394AC'
        assert responseGet.data.DataItemValueResource.ItemValue.Name.text() == 'Mass CO2 per Energy'
        assert responseGet.data.DataItemValueResource.ItemValue.Path.text() == 'massCO2PerEnergy'
        assert responseGet.data.DataItemValueResource.ItemValue.Value.text() == '0.81999'
        assert responseGet.data.DataItemValueResource.ItemValue.ItemValueDefinition.ValueDefinition.ValueType.text() == 'DECIMAL'
    }
    
    @Test
    void invalidUpdateStartDateJson() {
        setAdminUserV1()
        
        def responseGet = client.get(
            path: "/data/business/energy/electricity/grid/585E708CB4BE/massCO2PerEnergy",
            query: [startDate: '1980-11-18T18:32:00Z'],
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        def firstDataItemValueUid = responseGet.data.itemValue.uid
        
        // Check the startDate of the first Data Item Value cannot be updated
        try{
            def responsePut = client.put(
                path: "/data/business/energy/electricity/grid/585E708CB4BE/" + firstDataItemValueUid,
                body: [startDate: '1998-01-01T00:00:00:00Z'],
                requestContentType: URLENC,
                contentType: JSON)
            fail 'Should have thrown an exception'
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_BAD_REQUEST.code == e.response.status
        }
    }
    
    /**
     * Test creating a DataItem and check that DataItemValue defaults and specified values
     * are created correctly, and that history items can be created and deleted correctly.
     */
    @Test
    void createUpdateDataItemValueJson() {
        setAdminUserV1()

        // Create a new DataItem
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.dataItem.uid

        // Check default Data Item Values were created
        def responseGet = client.get(
            path: "/data/business/energy/electricity/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.dataItem.itemValues.collect{it.path}.sort() == paths

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
        assert SUCCESS_OK.code == responsePost.status
        
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
    
    /**
    * Test creating a DataItem and check that DataItemValue defaults and specified values
    * are created correctly, and that history items can be created and deleted correctly.
    */
   @Test
   void createUpdateDataItemValueXML() {
       setAdminUserV1()

       // Create a new DataItem
       def responsePost = client.post(
           path: "/data/business/energy/electricity",
           body: [
               newObjectType: 'DI',
               country: "Test Country"],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       def uid = responsePost.data.DataCategoryResource.DataItem.Name.text()

       // Check default Data Item Values were created
       def responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid,
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert responseGet.data.DataItemResource.DataItem.ItemValues.ItemValue.Path*.text().sort() == paths

       // Check specified Data Item Value was created
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/country",
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert responseGet.data.DataItemValueResource.ItemValue.Value.text() == 'Test Country'
       
       // Create a new Data Item Value History
       responsePost = client.post(
           path: "/data/business/energy/electricity/" + uid,
           body: [
               massCO2PerEnergy: '100',
               startDate: '2000-01-01T00:00:00Z'],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       
       // Check new history item was created
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           query: [
               startDate: '2000-01-01T00:00:00Z',
               endDate: '2001-01-01T00:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert responseGet.data.DataItemValueResource.ItemValue.Value.text() == '100'
       
       // Update the new Data Item Value History
       def responsePut = client.put(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           body: [
               value: "42",
               startDate: '2000-01-01T00:00:00Z'],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePut.status
       
       // Check new history item was created
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           query: [
               startDate: '2000-01-01T00:00:00Z',
               endDate: '2001-01-01T00:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert responseGet.data.DataItemValueResource.ItemValue.Value.text() == '42'
       
       // Delete the history item
       def responseDelete = client.delete(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           contentType: XML)
       assert SUCCESS_OK.code == responseDelete.status
       
       // Check the history item was deleted
       try{
           responseGet = client.get(
               path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
               query: [
                   startDate: '2001-01-01T00:00:00Z',
                   endDate: '2002-01-01T00:00:00Z'],
               contentType: XML)
       }catch(HttpResponseException e) {
           assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
       }
       
       // Delete the Data Item
       responseDelete = client.delete(
           path: "/data/business/energy/electricity/" + uid,
           contentType: XML)
       assert SUCCESS_OK.code == responseDelete.status
       
       // Check the Data Item was deleted
       try{
           responseGet = client.get(
               path: "/data/business/energy/electricity/" + uid,
               contentType: XML)
       }catch(HttpResponseException e) {
           assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
       }
   }
    
    /**
     * Tests that a data series can be created and edited and that it responds correctly.
     */
    @Test
    void dataItemSeriesJson() {
        setAdminUserV1()
        
        // Create a new DataItem
        def responsePost = client.post(
            path: "/data/business/energy/electricity",
            body: [
                newObjectType: 'DI',
                country: "Test Country"],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        def uid = responsePost.data.dataItem.uid
        
        // Create a data series with a new value each day
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '1',
                startDate: '2000-01-01T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '2',
                startDate: '2000-01-02T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        
        responsePost = client.post(
            path: "/data/business/energy/electricity/" + uid,
            body: [
                massCO2PerEnergy: '3',
                startDate: '2000-01-03T00:00:00Z'],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        
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
    
    /**
    * Tests that a data series can be created and edited and that it responds correctly.
    * The "first" data item value is the row in data_item_number_value that corresponds 
    * to this data item; the data series is represented by entries in 
    * data_item_number_value_history, which each have a start_date in the database to 
    * indicate when they are valid from.  The first data item value has no such start date,
    * and as such it is not permitted to update that start date.
    */
   @Test
   void dataItemSeriesXML() {
       setAdminUserV1()
       
       // Create a new DataItem
       def responsePost = client.post(
           path: "/data/business/energy/electricity",
           body: [
               newObjectType: 'DI',
               country: "Test Country"],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       def uid = responsePost.data.DataCategoryResource.DataItem.@uid
       
       // Create a data series with a new value each day
       responsePost = client.post(
           path: "/data/business/energy/electricity/" + uid,
           body: [
               massCO2PerEnergy: '1',
               startDate: '2000-01-01T00:00:00Z'],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       
       responsePost = client.post(
           path: "/data/business/energy/electricity/" + uid,
           body: [
               massCO2PerEnergy: '2',
               startDate: '2000-01-02T00:00:00Z'],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       
       responsePost = client.post(
           path: "/data/business/energy/electricity/" + uid,
           body: [
               massCO2PerEnergy: '3',
               startDate: '2000-01-03T00:00:00Z'],
           requestContentType: URLENC,
           contentType: XML)
       assert SUCCESS_OK.code == responsePost.status
       
       // Check the history items are all available
       def responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           query: [startDate: '1999-01-01T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           query: [startDate: '2000-01-01T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert '1' == responseGet.data.DataItemValueResource.ItemValue.Value.text()
       
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [startDate: '2000-01-02T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert '2' == responseGet.data.DataItemValueResource.ItemValue.Value.text()
       
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [startDate: '2000-01-03T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert '3' == responseGet.data.DataItemValueResource.ItemValue.Value.text()
       
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [
                startDate: '2000-01-01T12:00:00Z',
                valuesPerPage: '10'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert responseGet.data.DataItemValueResource.ItemValues.ItemValue.size() == 4 // 3 we made plus default
       
       // Delete one in the middle
       def responseDelete = client.delete(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
           query: [startDate: '2000-01-02T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseDelete.status
       
       // Check the previous result is now returned for that time
       responseGet = client.get(
           path: "/data/business/energy/electricity/" + uid + "/massCO2PerEnergy",
            query: [startDate: '2000-01-02T12:00:00Z'],
           contentType: XML)
       assert SUCCESS_OK.code == responseGet.status
       assert '1' == responseGet.data.DataItemValueResource.ItemValue.Value.text()
       
       // Delete the Data Item
       responseDelete = client.delete(
           path: "/data/business/energy/electricity/" + uid,
           contentType: XML)
       assert SUCCESS_OK.code == responseDelete.status
       
       // Check the Data Item was deleted
       try{
           responseGet = client.get(
               path: "/data/business/energy/electricity/" + uid,
               contentType: XML)
       }catch(HttpResponseException e) {
           assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
       }
   }
}
