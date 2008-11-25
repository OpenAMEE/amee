<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}'>${site.name}</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/aliases'>Aliases</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/aliases/${siteAlias.uid}'>${siteAlias.name}</a></p>
<h2>Details</h2>
<#if browser.siteAliasActions.allowView>
<p>
Name: ${siteAlias.name}<br/>
Server Alias: ${siteAlias.serverAlias}<br/>
Created: ${siteAlias.created?string.short}<br/>
Modified: ${siteAlias.modified?string.short}<br/>
</p>
</#if>
<#if browser.siteAliasActions.allowModify>
  <h2>Update Site Alias</h2>
  <p>
  <form action='/environments/${environment.uid}/sites/${site.uid}/aliases/${siteAlias.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${siteAlias.name}' type='text' size='30'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${siteAlias.description}</textarea><br/>
  Server Alias: <input name='serverAlias' value='${siteAlias.serverAlias}' type='text' size='30'/><br/><br/>
  <input type='submit' value="Update Site Alias"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>