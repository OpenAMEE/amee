<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}'>${site.name}</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/apps'>Apps</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}/apps/${siteApp.uid}'>${siteApp.app.name}</a></p>
<h2>Details</h2>
<#if browser.siteAppActions.allowView>
<p>
Name: ${siteApp.app.name}<br/>
URI Pattern: ${siteApp.uriPattern}<br/>
Created: ${siteApp.created?string.short}<br/>
Modified: ${siteApp.modified?string.short}<br/>
</p>
</#if>
<#if browser.siteAppActions.allowModify>
  <h2>Update Site App</h2>
  <p>
  <form action='/environments/${environment.uid}/sites/${site.uid}/apps/${siteApp.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  App: <select id="appUid" name="appUid">
      <#list apps as app>
          <option value="${app.uid}"<#if siteApp.app.uid == app.uid> selected="selected"</#if>>${app.name}</option>
      </#list>
  </select><br/>
  URI Pattern: <input name='uriPattern' value='${siteApp.uriPattern}' type='text' size='60'/><br/>
  Skin Path: <input name='skinPath' value='${siteApp.skinPath}' type='text' size='60'/><br/>
  Default:
    Yes <input type="radio" name="defaultApp" value="true"<#if siteApp.defaultApp> checked</#if>/>
    No <input type="radio" name="defaultApp" value="false"<#if !siteApp.defaultApp> checked</#if>/><br/>
  Enabled:
    Yes <input type="radio" name="enabled" value="true"<#if siteApp.enabled> checked</#if>/>
    No <input type="radio" name="enabled" value="false"<#if !siteApp.enabled> checked</#if>/><br/><br/>
  <input type='submit' value="Update Site App"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>