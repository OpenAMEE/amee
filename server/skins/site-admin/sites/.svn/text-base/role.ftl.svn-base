<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/roles'>Roles</a> /
   <a href='/environments/${environment.uid}/roles/${role.uid}'>${role.name}</a></p>
<h2>Details</h2>
<#if browser.roleActions.allowView>
<p>
Name: ${role.name}<br/>
Created: ${role.created?string.short}<br/>
Modified: ${role.modified?string.short}<br/>
</p>
</#if>
<h2>Manage</h2>
<p>
<a href='/environments/${environment.uid}/roles/${role.uid}/actions'>Actions</a><br/>
</p>
<#if browser.roleActions.allowModify>
  <h2>Update Role</h2>
  <p>
  <form action='/environments/${environment.uid}/roles/${role.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${role.name}' type='text' size='30'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${role.description}</textarea><br/><br/>
  <input type='submit' value="Update Role"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>