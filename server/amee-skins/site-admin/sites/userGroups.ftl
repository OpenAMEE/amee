<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<script type="text/javascript">

function changeButtons(response) {
  var name = $('Name_' + this.groupUid);
  var member = $('Member_' + this.groupUid);
  var button = $('Button_' + this.groupUid);
  if (this.action == 'create') {
    member.innerHTML = 'Yes';
    button.innerHTML = '<button type="button" onClick="deleteGroupPrincipal(\'' + this.groupUid + '\');">Leave</button>';
  } else {
    member.innerHTML = 'No';
    button.innerHTML = '<button type="button" onClick="addGroupPrincipal(\'' + this.groupUid + '\');">Join</button>';
  }
}

function deleteGroupPrincipal(groupUid) {
  resourceUrl = '/environments/${environment.uid}/users/${user.uid}/groups/' + groupUid + '?method=delete';
  resourceElem = $('Elem_' + groupUid);
  resourceType = 'membership'; 
  var deleteResource = new DeleteResource();
  deleteResource.groupUid = groupUid;
  deleteResource.successCallback = changeButtons.bind(deleteResource);
  deleteResource.confirm = false;
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}

function addGroupPrincipal(groupUid) {
  resourceUrl = '/environments/${environment.uid}/users/${user.uid}/groups';
  resourceElem = $('Elem_' + groupUid);
  resourceType = 'membership'; 
  var createResource = new CreateResource();
  createResource.groupUid = groupUid;
  createResource.successCallback = changeButtons.bind(createResource);
  createResource.confirm = false;
  createResource.createResource(resourceUrl, resourceElem, resourceType, 'groupUid=' + groupUid);
}

</script>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}'>${user.username}</a> /
   <a href='/environments/${environment.uid}/users/${user.uid}/groups'>Groups</a></p>

<#if browser.userActions.allowList>
<h2>User Groups</h2>
<p>

<#assign pagerItemsLabel = 'groups'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Group</th>
  <th>Member</th>
  <th>Actions</th>
</tr>
<#list groups as g>
  <#if groupPrincipalMap[g.uid]??>
    <#assign gu = groupPrincipalMap[g.uid]>
  <#else>
    <#assign gu = "">
  </#if>
  <tr id='Elem_${g.uid}'>
    <td id="Name_${g.uid}">
      <#if browser.userActions.allowModify>
        <#if gu != "">
          <a href='/environments/${environment.uid}/users/${user.uid}/groups/${g.uid}'>${g.name}</a>
        <#else>
          ${g.name}
        </#if>
      <#else>
        ${g.name}
      </#if>
    </td>
    <td id="Member_${g.uid}"><#if gu != "">Yes<#else>No</#if></td>
    <td>
      <#if browser.userActions.allowModify>
        <span id="Button_${g.uid}">
          <#if gu != "">
            <button type="button" onClick="deleteGroupPrincipal('${g.uid}');">Leave</button>
          <#else>
            <button type="button" onClick="addGroupPrincipal('${g.uid}');">Join</button>
          </#if>
        </span>
      </#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#include '/includes/after_content.ftl'>