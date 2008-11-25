<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteSite(siteUid) {
  resourceUrl = '/environments/${environment.uid}/sites/' + siteUid + '?method=delete';
  resourceElem = $('Elem_' + siteUid);
  resourceType = 'Site'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a></p>
<#if browser.siteActions.allowList>
<p>

<#assign pagerItemsLabel = 'sites'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Server Name</th>
  <th></th>
</tr>
<#list sites as s>
  <tr id='Elem_${s.uid}'>
    <td>${s.name}</td>
    <td>${s.serverName}</td>
    <td>
        <#if browser.siteActions.allowView><a href='/environments/${environment.uid}/sites/${s.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.siteActions.allowDelete><input type="image" onClick="deleteSite('${s.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
    
</p>
</#if>
<#if browser.siteActions.allowCreate>
  <h2>Create Site</h2>
  <p>
  <form action='/environments/${environment.uid}/sites' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' type='text' size='30'/><br/>
  Server Name: <input name='serverName' type='text' size='30'/><br/><br/>
  <input type='submit' value="Create Site"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>