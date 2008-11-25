<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function changeButtons(response) {
  var name = $('Name_' + this.roleUid);
  var member = $('Member_' + this.roleUid);
  var button = $('Button_' + this.roleUid);
  if (this.action == 'create') {
    member.innerHTML = 'Yes';
    button.innerHTML = '<button type="button" onClick="deleteGroupUserRole(\'' + this.roleUid + '\');">Leave</button>';
  } else {
    member.innerHTML = 'No';
    button.innerHTML = '<button type="button" onClick="addGroupUserRole(\'' + this.roleUid + '\');">Join</button>';
  }
}

function deleteGroupUserRole(roleUid) {
  resourceUrl = '/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}/roles/' + roleUid + '?method=delete';
  resourceElem = $('Elem_' + roleUid);
  resourceType = 'membership'; 
  var deleteResource = new DeleteResource();
  deleteResource.roleUid = roleUid;
  deleteResource.successCallback = changeButtons.bind(deleteResource);
  deleteResource.confirm = false;
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}

function addGroupUserRole(roleUid) {
  resourceUrl = '/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}/roles';
  resourceElem = $('Elem_' + roleUid);
  resourceType = 'membership'; 
  var createResource = new CreateResource();
  createResource.roleUid = roleUid;
  createResource.successCallback = changeButtons.bind(createResource);
  createResource.confirm = false;
  createResource.createResource(resourceUrl, resourceElem, resourceType, 'roleUid=' + roleUid);
}

</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}'>${user.username}</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups'>Groups</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}'>${group.name}</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}/roles'>Roles</a></p>

<h2>User Group Roles</h2>

<p>

<#assign pagerItemsLabel = 'user group roles'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>
<#if browser.userActions.allowList>
<table>
<tr>
  <th>Role</th>
  <th>Member</th>
  <th>Actions</th>
</tr>
<#list roles as r>
  <#if roleMap[r.uid]??>
    <#assign ro = roleMap[r.uid]>
  <#else>
    <#assign ro = "">
  </#if>
  <tr id="Elem_${r.uid}">
    <td id="Name_${r.uid}">
      <#if browser.userActions.allowModify>
        <#if ro != "">
          <a href="/environments/${environment.uid}/users/${user.uid}/groups/${group.uid}/roles/${r.uid}">${r.name}</a>
        <#else>
          ${r.name}
        </#if>
      <#else>
        ${r.name}
      </#if>
    </td>
    <td id="Member_${r.uid}"><#if ro != "">Yes<#else>No</#if></td>
    <td>
      <#if browser.userActions.allowModify>
        <span id="Button_${r.uid}">
          <#if ro != "">
            <button type="button" onClick="deleteGroupUserRole('${r.uid}');">Leave</button>
          <#else>
            <button type="button" onClick="addGroupUserRole('${r.uid}');">Join</button>
          </#if>
        </span>
      </#if>
    </td>
  </tr>
</#list>
</table>
</#if>
<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
<#include '/includes/after_content.ftl'>