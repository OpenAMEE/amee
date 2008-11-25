<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

    <script type="text/javascript">
function deleteGroup(environmentUid, groupUid) {
  resourceUrl = '/environments/' + environmentUid + '/groups/' + groupUid + '?method=delete';
  resourceElem = $('Elem_' + groupUid);
  resourceType = 'Group'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/groups'>Groups</a></p>

<h2>Groups</h2>
<#if browser.groupActions.allowList>
<p>

<#assign pagerItemsLabel = 'groups'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Actions</th>
</tr>
<#list groups as g>
  <tr id='Elem_${g.uid}'>
    <td>${g.name}</td>
    <td>
        <#if browser.groupActions.allowView><a href='/environments/${environment.uid}/groups/${g.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.groupActions.allowDelete><input type="image" onClick="deleteGroup('${environment.uid}', '${g.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
    
</p>
</#if>
<#if browser.groupActions.allowCreate>
<h2>Create Group</h2>
<p>
<form action='/environments/${environment.uid}/groups' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Group"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>