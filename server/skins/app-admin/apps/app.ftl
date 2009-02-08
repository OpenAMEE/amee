<#assign sectionName = "apps">
<#include '/includes/before_content.ftl'>
<h1>App Administration</h1>
<p><a href='/apps'>Apps</a> / 
   <a href='/apps/${app.uid}'>${app.name}</a></p>
<h2>Details</h2>
<p>
Name: ${app.name}<br/>
Created: ${app.created?string.short}<br/>
Modified: ${app.modified?string.short}<br/>
</p>
<h2>Manage</h2>
<p>
<a href='/apps/${app.uid}/actions'>Actions</a><br/>
</p>
<#if browser.appActions.allowModify>
  <h2>Update App</h2>
  <p>
  <form action='/apps/${app.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${app.name}' type='text' size='30'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${app.description}</textarea><br/>
  Authentication required:
    Yes <input type="radio" name="authenticationRequired" value="true"<#if app.authenticationRequired> checked="checked"</#if>/>
    No <input type="radio" name="authenticationRequired" value="false"<#if !app.authenticationRequired>  checked="checked"</#if>/><br/>
  Filters: <input name='filterNames' value='${app.filterNames}' type='text' size='60'/><br/>
  Allow Client Cache:
    Yes <input type="radio" name="allowClientCache" value="true"<#if app.allowClientCache>  checked="checked"</#if>/>
    No <input type="radio" name="allowClientCache" value="false"<#if !app.allowClientCache>  checked="checked"</#if>/><br/><br/>
  <input type='submit' value="Update App"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>