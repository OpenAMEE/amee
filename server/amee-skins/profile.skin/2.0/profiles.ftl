<#include '/profileCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type="text/javascript">

    var AUTHORIZATION_CONTEXT = new AuthorizationContext(${authorizationContext.getJSONObject()});

    function deleteProfile(profileUid) {
        resourceUrl = '/profiles/' + profileUid + '?method=delete';
        resourceElem = $('Elem_' + profileUid);
        resourceType = 'Profile';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    // create resource objects
    var profile = new Profile();
    var profilesApiService = new ProfilesApiService({
        heading: "Profiles",
        headingElementName: "apiHeading",
        contentElementName: "apiContent",
        pagerTopElementName: 'apiTopPager',
        pagerBtmElementName: 'apiBottomPager',
        apiVersion: '2.0'});

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.observe('loaded', function() {
        profilesApiService.start();
    });
    resourceLoader.start();

</script>

<h1>Profiles</h1>

<p><a href='/profiles'>profiles</a></p>

<h2 id="apiHeading"></h2>
<div id="apiTopPager"></div>
<table id="apiContent"></table>
<div id="apiBottomPager"></div>

<h2>Create Profile</h2>
<form onSubmit="return false;">
    <input type='button' value='Create Profile' onClick='profile.addProfile();'/>
</form>

<#include '/includes/after_content.ftl'>