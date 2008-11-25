<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteSiteAlias(siteUid, siteAliasUid) {
  resourceUrl = '/environments/${environment.uid}/sites/' + siteUid + '/aliases/' + siteAliasUid + '?method=delete';
  resourceElem = $('Elem_' + siteAliasUid);
  resourceType = 'SiteAlias'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Site Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}'>${site.name}</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/aliases'>Aliases</a></p>

<h2>Site Aliases</h2>
<#if browser.siteAliasActions.allowList>
<p>

<#assign pagerItemsLabel = 'site aliases'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Server Alias</th>
  <th></th>
</tr>
<#list siteAliases as sa>
  <tr id='Elem_${sa.uid}'>
    <td>${sa.name}</td>
    <td>${sa.serverAlias}</td>
    <td>
        <#if browser.siteAliasActions.allowView><a href='/environments/${environment.uid}/sites/${site.uid}/aliases/${sa.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.siteAliasActions.allowDelete><input type="image" onClick="deleteSiteAlias('${site.uid}', '${sa.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>
<#if browser.siteAliasActions.allowCreate>
<h2>Create Site Alias</h2>

<p>
<form action='/environments/${environment.uid}/sites/${site.uid}/aliases' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/>
Server Alias: <input name='serverAlias' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Site Alias"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>