package com.amee.integration.v2

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND
import static org.restlet.data.Status.SUCCESS_CREATED
import static org.restlet.data.Status.SUCCESS_OK

import org.junit.Test

class DataCategoryIT extends BaseApiTest {
    
    // NOTE: Keep these lists up to date if you add new categories to import.sql.
    
    static def categoryNames = [
        'Root', 'Home', 'Appliances', 'Computers', 'Generic', 'Cooking', 'Entertainment', 'Generic', 'Kitchen', 'Generic',
        'Business', 'Energy', 'Electricity', 'US', 'Subregion', 'Waste',
        'Benchmark', 'CO2 Benchmark', 'CO2 Benchmark Two', 'CO2 Benchmark Child',
        'Embodied', 'Clm',
        'ICE Building Materials LCA', 'V2', 'Inventory of Carbon & Energy methodology for materials by mass',
        'Integration', 'Api', 'Item history test', 'Item history dimless test',
        'LCA', 'Ecoinvent', 'chemicals', 'inorganics', 'chlorine, gaseous, diaphragm cell, at plant', 'chlorine, gaseous, diaphragm cell, at plant',
        'Grid',
        'Transport', 'Plane', 'Specific', 'Military', 'Ipcc']

    static def categoryNamesExcEcoinvent = [
        'Root', 'Home', 'Appliances', 'Computers', 'Generic', 'Cooking', 'Entertainment', 'Generic', 'Kitchen', 'Generic',
        'Business', 'Energy', 'Electricity', 'US', 'Subregion', 'Waste',
        'Benchmark', 'CO2 Benchmark', 'CO2 Benchmark Two', 'CO2 Benchmark Child',
        'Embodied', 'Clm',
        'ICE Building Materials LCA', 'V2', 'Inventory of Carbon & Energy methodology for materials by mass',
        'Integration', 'Api', 'Item history test', 'Item history dimless test',
        'LCA',
        'Grid',
        'Transport', 'Plane', 'Specific', 'Military', 'Ipcc']

    static def categoryWikiNames = [
        'Root', 'Home', 'Appliances', 'Computers', 'Computers_generic', 'Cooking', 'Entertainment', 'Entertainment_generic', 'Kitchen', 'Kitchen_generic',
        'Business', 'Business_energy', 'Electricity_by_Country', 'Energy_US', 'US_Egrid', 'Waste',
        'Benchmarking', 'CO2_Benchmark', 'CO2_Benchmark_Two', 'CO2_Benchmark_Child',
        'Embodied', 'CLM_food_life_cycle_database',
        'ICE_Building_Materials_LCA', 'ICE_v2', 'ICE_v2_by_mass',
        'Integration', 'Api', 'Item_history_test', 'Item_history_dimless_test',
        'LCA', 'Ecoinvent', 'Ecoinvent_chemicals', 'Ecoinvent_chemicals_inorganics', 'Ecoinvent_chemicals_inorganics_chlorine_gaseous_diaphragm_cell_at_plant', 'Ecoinvent_chemicals_inorganics_chlorine_gaseous_diaphragm_cell_at_plant_UPR_RER_kg',
        'Greenhouse_Gas_Protocol_international_electricity',
        'Transport', 'Plane', 'Specific_plane_transport', 'Specific_military_aircraft', 'IPCC_military_aircraft',
        'Transport_fuel']

    static def categoryWikiNamesExcEcoinvent = [
        'Root', 'Home', 'Appliances', 'Computers', 'Computers_generic', 'Cooking', 'Entertainment', 'Entertainment_generic', 'Kitchen', 'Kitchen_generic',
        'Business', 'Business_energy', 'Electricity_by_Country', 'Energy_US', 'US_Egrid', 'Waste',
        'Benchmarking', 'CO2_Benchmark', 'CO2_Benchmark_Two', 'CO2_Benchmark_Child',
        'Embodied', 'CLM_food_life_cycle_database',
        'ICE_Building_Materials_LCA', 'ICE_v2', 'ICE_v2_by_mass',
        'Integration', 'Api', 'Item_history_test', 'Item_history_dimless_test',
        'LCA',
        'Greenhouse_Gas_Protocol_international_electricity',
        'Transport', 'Plane', 'Specific_plane_transport', 'Specific_military_aircraft', 'IPCC_military_aircraft']

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
        setAdminUser()
        
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
        responseGet = client.get(
            path: "/data/transport/plane/testPath",
            contentType: JSON)
        assert CLIENT_ERROR_NOT_FOUND.code == responseGet.status
    }
    
}
