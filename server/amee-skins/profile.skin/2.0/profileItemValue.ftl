<#include '/profileCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type='text/javascript'>
    
    // create resource objects
    var PROFILE_ACTIONS = new ActionsResource({path: '/profiles/actions'});
    var profileItemValueApiService = new ProfileItemValueApiService();

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.addResource(PROFILE_ACTIONS);
    resourceLoader.observe('loaded', function() {
        profileItemValueApiService.start();
    });
    resourceLoader.start();

</script>

<h1>Profile Item Value</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Item Value Details</h2>
<p>
    <span id="value"></span>
    <span id="fullPath"></span>
    <span id="dataItemLabel"></span>
    <span id="itemValueDefinition"></span>
    <span id="valueDefinition"></span>
    <span id="valueType"></span>
    <span id="environment"></span>
    <span id="uid"></span>
    <span id="created"></span>
    <span id="modified"></span>
</p>

<h2>Update Profile Item Value</h2>
<p>
    <form id="inputForm" action='#' method='POST' enctype='application/x-www-form-urlencoded'>
        <input name="representation" value="full" type="hidden"/>
        <span id="inputValues"></span>
        <br/>
        <div id="inputSubmit"></div><div id="updateStatusSubmit"></div>
    </form>
</p>

<#include '/includes/after_content.ftl'>