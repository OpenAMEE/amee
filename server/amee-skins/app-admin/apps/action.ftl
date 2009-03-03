<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<h1>App Administration</h1>
<p><a href='/apps'>Apps</a> / 
   <a href='/apps/${app.uid}'>${app.name}</a> / 
   <a href='/apps/${app.uid}/actions'>Actions</a> / 
   <a href='/apps/${app.uid}/actions/${action.name}'>${action.name}</a></p>
<h2>Details</h2>
<p>
Name: ${action.name}<br/>
Created: ${action.created?string.short}<br/>
Modified: ${action.modified?string.short}<br/>
</p>
<#if browser.appActions.allowModify>
  <h2>Update Action</h2>
  <p>
  <form action='/apps/${app.uid}/actions/${action.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${action.name}' type='text' size='30'/><br/>
  Key: <input name='key' value='${action.key}' type='text' size='50'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${action.description}</textarea><br/><br/>
  <input type='submit' value="Update Action"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>