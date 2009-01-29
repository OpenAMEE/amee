<#include 'profileCommon.ftl'>
<#include '/includes/before_content.ftl'>
<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type="text/javascript">
function deleteProfile(profileUid) {
  resourceUrl = '/profiles/' + profileUid + '?method=delete';
  resourceElem = $('Elem_' + profileUid);
  resourceType = 'Profile'; 
  var deleteResource = new DeleteResource();
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Profiles</h1>

<p><a href='/profiles'>Profiles</a></p>

<#if activeUser.apiVersion.versionOne>
    <#if profiles?? && browser.profileActions.allowList>
      <h2>Profiles</h2>
      <p>

        <#assign pagerItemsLabel = 'profiles'>
        <#assign pagerName = 'pagerTop'>
        <#include '/includes/pager.ftl'>

      <table>
      <tr>
        <th>Path</th>
        <th>Group</th>
        <th>User</th>
        <th>Created</th>
        <th>Actions</th>
      </tr>
        <#list profiles as p>
          <tr id='Elem_${p.uid}'>
            <td>${p.displayPath}</td>
            <td>${p.permission.group.name}</td>
            <td>${p.permission.user.username}</td>
            <td>${p.created?datetime}</td>
            <td>
                <#if browser.profileActions.allowView><a href='/profiles/${p.displayPath}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
                <#if browser.profileActions.allowDelete><input type="image" onClick="deleteProfile('${p.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
            </td>
          </tr>
        </#list>
        </table>

        <#assign pagerName = 'pagerBottom'>
        <#include '/includes/pager.ftl'>

      </p>
    </#if>
<#else>
    <h2 id="apiHeading"></h2>
    <div id="apiTopPager"></div>
    <table id="apiContent">
    </table>
    <div id="apiBottomPager"></div>
</#if>

<script type='text/javascript'>
    var Profile = Class.create();
    Profile.prototype = {
      initialize: function() {
      },
      addProfile: function() {
        var myAjax = new Ajax.Request(window.location.href, {
          method: 'post',
          parameters: 'profile=true',
          requestHeaders: ['Accept', 'application/json'],
          onSuccess: this.addProfileSuccess.bind(this)
        });
      },
      addProfileSuccess: function(t) {
        window.location.href = window.location.href;
      }
    };
    var p = new Profile();

    <#if !activeUser.apiVersion.versionOne>
        document.observe('dom:loaded', function() {
            // hide n/a atom option
            $('showAPIATOM').style.visibility = "hidden";

            // api call
            var profilesApiService = new ProfilesApiService(
                {
                    heading : "Profiles",
                    headingElementName : "apiHeading",
                    contentElementName : "apiContent",
                    pagerTopElementName : 'apiTopPager',
                    pagerBtmElementName : 'apiBottomPager',
                    apiVersion : '2.0',
                    drillDown : false
                });
            profilesApiService.apiRequest();
        });

    </#if>
</script>

<#if browser.profileActions.allowCreate>
  <h2>Create Profile</h2>
  <form onSubmit="return false;">
    <input type='button' value='Create Profile' onClick='p.addProfile();'/>
  </form>
</#if>
<#include '/includes/after_content.ftl'>