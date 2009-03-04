<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteUser(environmentUid, userUid) {
  resourceUrl = '/environments/' + environmentUid + '/users/' + userUid + '?method=delete';
  resourceElem = $('Elem_' + userUid);
  resourceType = 'User'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a></p>


<#if browser.userActions.allowList>
<h2>Users</h2>
<p>

<#assign pagerItemsLabel = 'users'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
    <th>Username</th>
    <th>Name</th>
    <th>API Version</th>
    <th>Status</th>
    <th>Actions</th>
</tr>
<#list users as u>
  <tr id='Elem_${u.uid}'>
    <td>${u.username}</td>
    <td>${u.name}</td>
    <td>${u.APIVersion}</td>
    <td>${u.status}</td>
    <td>
        <#if browser.userActions.allowView><a href='/environments/${environment.uid}/users/${u.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.userActions.allowDelete><input type="image" onClick="deleteUser('${environment.uid}', '${u.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#if browser.userActions.allowCreate>
<h2>Create User</h2>

<p>
<form action='/environments/${environment.uid}/users' method='POST' enctype='application/x-www-form-urlencoded'>
Clone from: <select name="cloneUserUid">
    <option value="">(New User)</option>
    <#list users as u>
        <option value="${u.uid}">${u.username}</option>
    </#list>
    </select><br/>
Name: <input name='name' type='text' size='60'/><br/>
Username: <input name='username' type='text' size='30'/><br/>
Password: <input name='password' type='password' size='30'/><br/>
Email: <input name='email' type='text' size='60'/><br/><br/>
<#if !activeUser.apiVersion.versionOne>
    API Version: <select name='apiVersion'> <br/>
      <#list apiVersions as apiVersion>
          <option value='${apiVersion.version}'>${apiVersion.version}</option>
      </#list>
    </select><br/>
<#else>
    <input type="hidden" name="apiVersion" value="${activeUser.apiVersion.version}" />
</#if>
<input type='submit' value="Create User"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>