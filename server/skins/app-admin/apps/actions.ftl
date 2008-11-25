<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteAction(appUid, actionUid) {
  resourceUrl = '/apps/' + appUid + '/actions/' + actionUid + '?method=delete';
  resourceElem = $('Elem_' + actionUid);
  resourceType = 'Action'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>App Administration</h1>

<p><a href='/apps'>Apps</a> /
   <a href='/apps/${app.uid}'>${app.name}</a> / 
   <a href='/apps/${app.uid}/actions'>Actions</a></p>

<#if browser.appActions.allowView>
<h2>Actions</h2>
<p>

<#assign pagerItemsLabel = 'actions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Key</th>
  <th>Actions</th>
</tr>
<#list actions as a>
  <tr id='Elem_${a.uid}'>
    <td>${a.name}</td>
    <td>${a.key}</td>
    <td>
        <#if browser.appActions.allowView><a href='/apps/${app.uid}/actions/${a.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.appActions.allowModify><input type="image" onClick="deleteAction('${app.uid}', '${a.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#if browser.appActions.allowModify>
<h2>Create Action</h2>
<p>
<form action='/apps/${app.uid}/actions' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Action"/><br/>
</form>
</p>
</#if>

<#include '/includes/after_content.ftl'>