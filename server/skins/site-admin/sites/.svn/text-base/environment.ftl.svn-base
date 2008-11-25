<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a></p>
<h2>Environment Details</h2>
<#if browser.environmentActions.allowView>
<p>
Name: ${environment.name}<br/>
Created: ${environment.created?string.short}<br/>
Modified: ${environment.modified?string.short}<br/>
</p>
</#if>
<h2>Manage</h2>
<p>
<a href='/environments/${environment.uid}/sites'>Sites</a><br/>
<a href='/environments/${environment.uid}/users'>Users</a><br/>
<a href='/environments/${environment.uid}/groups'>Groups</a><br/>
<a href='/environments/${environment.uid}/roles'>Roles</a><br/>
<a href='/environments/${environment.uid}/tasks'>Tasks</a><br/>
<#include '/sites/environment_child_options.ftl'>
</p>
<#if browser.environmentActions.allowModify>
  <h2>Update Environment</h2>
  <p>
  <form action='/environments/${environment.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${environment.name}' type='text' size='60'/><br/>
  Path: <input name='path' value='${environment.path}' type='text' size='60'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${environment.description}</textarea><br/>
  Items per page: <input name='itemsPerPage' value='${environment.itemsPerPage}' type='text' size='10'/><br/><br/>
  <input type='submit' value="Update Environment"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>