<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

    <script type="text/javascript">
function deleteScheduledTask(environmentUid, scheduledTaskUid) {
  resourceUrl = '/environments/' + environmentUid + '/tasks/' + scheduledTaskUid + '?method=delete';
  resourceElem = $('Elem_' + scheduledTaskUid);
  resourceType = 'Task';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/tasks'>Tasks</a></p>

<h2>Tasks</h2>
<#if browser.scheduledTaskActions.allowList>
<p>

<#assign pagerItemsLabel = 'tasks'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Cron</th>
  <th>Servers</th>
  <th>Enabled</th>
  <th>Actions</th>
</tr>
<#list scheduledTasks as st>
  <tr id='Elem_${st.uid}'>
    <#-- TODO: this runTask form is a very ugly - should do with AJAX instead -->
    <form action='/environments/${environment.uid}/tasks?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
    <td>${st.name}</td>
    <td>${st.cron}</td>
    <td>${st.servers}</td>
    <td><#if st.enabled>Yes<#else>No</#if></td>
    <td>
        <#if browser.scheduledTaskActions.allowView><a href='/environments/${environment.uid}/tasks/${st.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.scheduledTaskActions.allowDelete><input type="image" onClick="deleteScheduledTask('${environment.uid}', '${st.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
        <input name='runTask' value='${st.uid}' type='hidden'/>
        <input type='image' src="/images/icons/cog_go.png" border="0"/>
    </td>
    </form>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
    
</p>
</#if>

<#if browser.scheduledTaskActions.allowCreate>
<h2>Create Task</h2>
<p>
<form action='/environments/${environment.uid}/tasks' method='POST' enctype='application/x-www-form-urlencoded'>
Name: <input name='name' type='text' size='30'/><br/><br/>
<input type='submit' value="Create Task"/><br/>
</form>
</p>
</#if>

<#if browser.scheduledTaskActions.allowModify>
  <h3>Restart Tasks</h3>
  <p>
  <form action='/environments/${environment.uid}/tasks?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  <input name='restart' value='true' type='hidden'/>
  <input type='submit' value="Restart"/><br/><br/>
  </form>
  </p>
</#if>

<#include '/includes/after_content.ftl'>