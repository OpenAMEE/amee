<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteRole(environmentUid, roleUid) {
  resourceUrl = '/environments/' + environmentUid + '/roles/' + roleUid + '?method=delete';
  resourceElem = $('Elem_' + roleUid);
  resourceType = 'Role'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/roles'>Roles</a></p>

<h2>Roles</h2>
<#if browser.roleActions.allowList>
<p>

<#assign pagerItemsLabel = 'roles'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Actions</th>
</tr>
<#list roles as r>
  <tr id='Elem_${r.uid}'>
    <td>${r.name}</td>
    <td>
        <#if browser.roleActions.allowView><a href='/environments/${environment.uid}/roles/${r.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.roleActions.allowDelete><input type="image" onClick="deleteRole('${environment.uid}', '${r.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>
<h2>Create Role</h2>
<#if browser.roleActions.allowCreate>
<p>
<form action='/environments/${environment.uid}/roles' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Role"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>