<#assign sectionName = "roles">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function changeButtons(response) {
  var usage = $('Usage_' + this.actionUid);
  var button = $('Button_' + this.actionUid);
  if (this.action == 'create') {
    usage.innerHTML = 'Yes';
    button.innerHTML = '<button type="button" onClick="deleteAction(\'' + this.appUid + '\', \'' + this.actionUid + '\');">Remove</button>';
  } else {
    usage.innerHTML = 'No';
    button.innerHTML = '<button type="button" onClick="addAction(\'' + this.appUid + '\', \'' + this.actionUid + '\');">Add</button>';
  }
}

function deleteAction(appUid, actionUid) {
  resourceUrl = '/environments/${environment.uid}/roles/${role.uid}/actions/' + actionUid + '?method=delete';
  resourceElem = $('Elem_' + actionUid);
  resourceType = 'usage'; 
  var deleteResource = new DeleteResource();
  deleteResource.appUid = appUid;
  deleteResource.actionUid = actionUid;
  deleteResource.successCallback = changeButtons.bind(deleteResource);
  deleteResource.confirm = false;
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}

function addAction(appUid, actionUid) {
  resourceUrl = '/environments/${environment.uid}/roles/${role.uid}/actions';
  resourceElem = $('Elem_' + actionUid);
  resourceType = 'usage'; 
  var createResource = new CreateResource();
  createResource.appUid = appUid;
  createResource.actionUid = actionUid;
  createResource.successCallback = changeButtons.bind(createResource);
  createResource.confirm = false;
  createResource.createResource(resourceUrl, resourceElem, resourceType, 'appUid=' + appUid + '&actionUid=' + actionUid);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/roles'>Roles</a> /
   <a href='/environments/${environment.uid}/roles/${role.uid}'>${role.name}</a> /
   <a href='/environments/${environment.uid}/roles/${role.uid}/actions'>Actions</a></p>

<h2>Role Actions</h2>

<p>
<#if browser.appActions.allowList>
<#assign pagerItemsLabel = 'roles actions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>App</th>
  <th></th>
  <th>Usage</th>
  <th>Actions</th>
</tr>
<#list actions as a>
  <#if actionMap[a.uid]??>
    <#assign usage = actionMap[a.uid]>
  <#else>
    <#assign usage = "">
  </#if>
  <tr id='Elem_${a.uid}'>
    <td>
      <#if browser.appActions.allowView>
        <a href="/apps/${a.app.uid}">${a.app.name}</a>
      <#else>
        ${a.app.name}
      </#if>
    </td>
    <td>
      <#if browser.appActions.allowView>
        <a href="/apps/${a.app.uid}/actions/${a.uid}">${a.name}</a>
      <#else>
        ${a.name}
      </#if>
    </td>
    <td id="Usage_${a.uid}"><#if usage != "">Yes<#else>No</#if></td>
    <td>
      <#if browser.roleActions.allowModify>
        <span id="Button_${a.uid}">
          <#if usage != "">
            <button type="button" onClick="deleteAction('${a.app.uid}', '${a.uid}');">Remove</button>
          <#else>
            <button type="button" onClick="addAction('${a.app.uid}', '${a.uid}');">Add</button>
          </#if>
        </span>
      </#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
</#if>
</p>
<#include '/includes/after_content.ftl'>