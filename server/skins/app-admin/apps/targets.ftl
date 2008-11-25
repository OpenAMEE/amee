<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteTarget(appUid, targetUid) {
  resourceUrl = '/apps/' + appUid + '/targets/' + targetUid + '?method=delete';
  resourceElem = $('Elem_' + targetUid);
  resourceType = 'Target'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>App Administration</h1>

<p><a href='/apps'>Apps</a> / 
   <a href='/apps/${app.uid}'>${app.name}</a> / 
   <a href='/apps/${app.uid}/targets'>Targets</a></p>

<#if browser.appActions.allowView>
<h2>Targets</h2>
<p>

<#assign pagerItemsLabel = 'targets'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>URI Pattern</th>
  <th>Target</th>
  <th>Actions</th>
</tr>
<#list targets as t>
  <tr id='Elem_${t.uid}'>
    <td>${t.name}</td>
    <td>${t.uriPattern}</td>
    <td>${t.target}</td>
    <td>
        <#if browser.appActions.allowView><a href='/apps/${app.uid}/targets/${t.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.appActions.allowModify><input type="image" onClick="deleteTarget('${app.uid}', '${t.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#if browser.appActions.allowModify>
<h2>Create Target</h2>
<p>
<form action='/apps/${app.uid}/targets' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Target"/><br/>
</form>
</p>
</#if>

<#include '/includes/after_content.ftl'>