<#include '/profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type='text/javascript'>

    var AUTHORIZATION_CONTEXT = new AuthorizationContext(${authorizationContext.getJSONObject()});

    // create resource objects
    var profileItemApiService = new ProfileItemApiService();

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.observe('loaded', function() {
        profileItemApiService.start();
    });
    resourceLoader.start();

</script>

<h1>Profile Item</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Item Details</h2>
<p>
    <span id="name"></span>
    <span id="amount"></span>
    <span id="startDate"></span>
    <span id="endDate"></span>
    <span id="fullPath"></span>
    <span id="dataItemLabel"></span>
    <span id="itemDefinition"></span>
    <span id="environment"></span>
    <span id="uid"></span>
    <span id="created"></span>
    <span id="modified"></span>
</p>

<h2>Profile Item Values</h2>
<p>
    <form id="inputForm" action='#' method='POST' enctype='application/x-www-form-urlencoded'>
        <input name="representation" value="full" type="hidden"/>
        <table id="inputTable">
        </table>
        <br/>
        <div id="inputSubmit"></div><div id="updateStatusSubmit"></div>
    </form>
</p>

<#include '/includes/after_content.ftl'>
