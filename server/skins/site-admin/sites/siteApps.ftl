<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteSiteApp(siteUid, siteAppUid) {
  resourceUrl = '/environments/${environment.uid}/sites/' + siteUid + '/apps/' + siteAppUid + '?method=delete';
  resourceElem = $('Elem_' + siteAppUid);
  resourceType = 'Site App';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Site Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}'>${site.name}</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/apps'>Apps</a></p>

<h2>Site Apps</h2>
<#if browser.siteAppActions.allowList>
<p>

<#assign pagerItemsLabel = 'site apps'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>App</th>
  <th>URI Pattern</th>
  <th>Skin Path</th>
  <th>Default</th>
  <th>Enabled</th>
  <th></th>
</tr>
<#list siteApps as sa>
  <tr id='Elem_${sa.uid}'>
    <td>${sa.app.name}</td>
    <td>${sa.uriPattern}</td>
    <td>${sa.skinPath}</td>
    <td><#if sa.defaultApp>Yes<#else>No</#if></td>
    <td><#if sa.enabled>Yes<#else>No</#if></td>
    <td>
        <#if browser.siteAppActions.allowView><a href='/environments/${environment.uid}/sites/${site.uid}/apps/${sa.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.siteAppActions.allowDelete><input type="image" onClick="deleteSiteApp('${site.uid}', '${sa.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<h2>Create Site App</h2>
<#if browser.siteAppActions.allowCreate>
<p>
<form action='/environments/${environment.uid}/sites/${site.uid}/apps' method='POST' enctype='application/x-www-form-urlencoded'>
App: <select id="appUid" name="appUid">
    <#list apps as app>
        <option value="${app.uid}">${app.name}</option>
    </#list>
</select><br/>
URI Pattern: <input name='uriPattern' type='text' size='60'/><br/>
Skin Path: <input name='skinPath' type='text' size='60'/><br/>
Default:
  Yes <input type="radio" name="defaultApp" value="true">
  No <input type="radio" name="defaultApp" value="false"><br/>
Enabled:
  Yes <input type="radio" name="enabled" value="true">
  No <input type="radio" name="enabled" value="false"><br/><br/>
<input type='submit' value="Create Site App"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>