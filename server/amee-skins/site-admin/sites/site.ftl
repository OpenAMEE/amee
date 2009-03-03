<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/sites'>Sites</a> /
   <a href='/environments/${environment.uid}/sites/${site.uid}'>${site.name}</a></p>
<h2>Environment Details</h2>
<#if browser.siteActions.allowView>
<p>
Name: ${site.name}<br/>
Created: ${site.created?string.short}<br/>
Modified: ${site.modified?string.short}<br/>
</p>
</#if>
<h2>Manage</h2>
<p>
<a href='/environments/${environment.uid}/sites/${site.uid}/aliases'>Aliases</a><br/>
<a href='/environments/${environment.uid}/sites/${site.uid}/apps'>Apps</a><br/>
</p>
<#if browser.siteActions.allowModify>
  <h2>Update Site</h2>
  <p>
  <form action='/environments/${environment.uid}/sites/${site.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${site.name}' type='text' size='30'/><br/>
  Description: <textarea name='description' rows='5' cols='60'>${site.description}</textarea><br/>
  Secure Available:
    Yes <input type="radio" name="secureAvailable" value="true"<#if site.secureAvailable> checked</#if>/>
    No <input type="radio" name="secureAvailable" value="false"<#if !site.secureAvailable> checked</#if>/><br/>
  Authentication Cookie Domain: <input name='authCookieDomain' value='${site.authCookieDomain}' type='text' size='30'/><br/>
  Check Remote Address:
    Yes <input type="radio" name="checkRemoteAddress" value="true"<#if site.checkRemoteAddress> checked</#if>/>
    No <input type="radio" name="checkRemoteAddress" value="false"<#if !site.checkRemoteAddress> checked</#if>/><br/>
  Max Authentication Duration: <input name='maxAuthDuration' value='${site.maxAuthDuration?c}' type='text' size='30'/> (milliseconds)<br/>
  Max Authentication Idle: <input name='maxAuthIdle' value='${site.maxAuthIdle?c}' type='text' size='30'/> (milliseconds)<br/><br/>
  <input type='submit' value="Update Site"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>