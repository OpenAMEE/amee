<#include '/profileCommon.ftl'>
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

<p><a href='/profiles'>profiles</a></p>

<#if profiles??>
    <h2>Profiles</h2>
    <p>
        <#assign pagerItemsLabel = 'profiles'>
        <#assign pagerName = 'pagerTop'>
        <#include '/includes/pager.ftl'>
        <table>
            <tr>
                <th>Path</th>
                <th>Created</th>
                <th>Actions</th>
            </tr>
            <#list profiles as p>
            <tr id='Elem_${p.uid}'>
                <td>${p.displayPath}</td>
                <td>${p.created?datetime}</td>
                <td>
                <a href='/profiles/${p.displayPath}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                <input type="image" onClick="deleteProfile('${p.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/>
                </td>
            </tr>
            </#list>
        </table>
        <#assign pagerName = 'pagerBottom'>
        <#include '/includes/pager.ftl'>
    </p>
</#if>

<#if canCreateProfile()>

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
    </script>

    <h2>Create Profile</h2>
    <form onSubmit="return false;">
        <input type='button' value='Create Profile' onClick='p.addProfile();'/>
    </form>

</#if>

<#include '/includes/after_content.ftl'>