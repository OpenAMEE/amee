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

class ProfileIT extends BaseApiTest {
    
    @Test
    void getProfileListJson() {
        def responseGet = client.get(
            path: "/profiles",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.profiles.size() > 0
    }
    
    @Test
    void getProfileListXML() {
        def responseGet = client.get(
            path: "/profiles",
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.ProfilesResource.Profiles.Profile.size() > 0
    }
    
    @Test
    void createDeleteProfileJson() {
        // Create a new profile
        def responsePost = client.post(
            path: "/profiles",
            body: [ profile: "true" ],
            requestContentType: URLENC,
            contentType: JSON)
        assert SUCCESS_OK.code == responsePost.status
        assert config.api.standard.user == responsePost.data.profile.user.username
        def uid = responsePost.data.profile.uid
        
        // Check the new profile can be retrieved
        def responseGet = client.get(
            path: "/profiles/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        
        // Delete the profile
        def responseDelete = client.delete(
            path: "/profiles/" + uid,
            contentType: JSON)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check it cannot be retrieved any more
        try{
            responseGet = client.get(
                path: "/profiles/" + uid,
                contentType: JSON)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
    
    @Test
    void createDeleteProfileXML() {
        // Create a new profile
        def responsePost = client.post(
            path: "/profiles",
            body: [ profile: "true" ],
            requestContentType: URLENC,
            contentType: XML)
        assert SUCCESS_OK.code == responsePost.status
        assert config.api.standard.user == responsePost.data.ProfilesResource.Profile.User.Username.text()
        def uid = responsePost.data.ProfilesResource.Profile.@uid

        // Check the new profile can be retrieved
        def responseGet = client.get(
            path: "/profiles/" + uid,
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        
        // Delete the profile
        def responseDelete = client.delete(
            path: "/profiles/" + uid,
            contentType: XML)
        assert SUCCESS_OK.code == responseDelete.status
        
        // Check it cannot be retrieved any more
        try{
            responseGet = client.get(
                path: "/profiles/" + uid,
                contentType: XML)
            fail "Should have thrown an exception"
        }catch(HttpResponseException e){
            assert CLIENT_ERROR_NOT_FOUND.code == e.response.status
        }
    }
    
    @Test
    void getProfileCategoriesJson() {
        def responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/home",
            contentType: JSON)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.profileCategories.size() > 0
    }
    
    @Test
    void getProfileCategoriesXML() {
        def responseGet = client.get(
            path: "/profiles/UCP4SKANF6CS/home",
            contentType: XML)
        assert SUCCESS_OK.code == responseGet.status
        assert responseGet.data.ProfileCategoryResource.ProfileCategories.DataCategory.size() > 0
    }   
}
